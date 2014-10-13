/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot.video;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Represents an HTTP 1.1 response.
 * An HTTP response is constructed by a {@link HttpRequest} when the request is sent.
 *
 * The HTTP response can be used to accept streaming output from an HTTP server.
 * The HTTP response remains open until it is closed via {@link #close()} or the connection is
 * closed by the server. 
 */
public class HttpResponse {

  /** Size of read buffer. */
  public static final int kInputBufferSize = 32768;

  /**
   * Constructs an HttpResponse for an HTTP request sent on the provided socket.
   * The socket must have been created and opened by {@link HttpRequest}. This constructor is
   * directly called by {@link HttpRequest} after the request has been sent.
   *
   * The socket ownership is handed over to this class, which closes the socket when all data has
   * been read.
   *
   * This constructor automatically reads the response headers from the provided socket.
   *
   * @param request The request associated with this response.
   * @param resource_path The absolute path of the request target on the server.
   * @param socket The open connection to the server.
   */
  protected HttpResponse(HttpRequest request, String resource_path, Socket socket) {
    this.request_ = request;
    this.resource_path_ = resource_path;
    this.socket_ = socket;
    headers_ = new HashMap<String, String>();
    response_code_ = -1;
    boundary_sequence_ = null;
    if (socket != null) {
      try {
        input_stream_ = new BufferedInputStream(socket_.getInputStream(), kInputBufferSize);
        readResponseCode();
        if (isHttpOk()) {
          readResponseHeaders();
        }
      } catch (IOException e) {
        close();
      }
    }
  }

  /**
   * Closes the HTTP connection if it is open.
   */
  public void close() {
    if (socket_ != null) {
      try {
        socket_.close();
      } catch (IOException e) {}
      socket_ = null;
    }
  }

  /**
   * If the HTTP response specifies a content boundary, returns the content boundary string.
   * If no content boundary is specified, returns the empty string.
   *
   * The content boundary is used in a streaming response to separate response frames.
   *
   * @return The content boundary string or empty string.
   */
  public String getContentBoundary() {
    if (headers_.containsKey("content-type")) {
      String value = headers_.get("content-type");
      int index = value.indexOf("boundary=");
      if (index > 0) {
        return value.substring(index + 9);
      }
    }
    return "";
  }

  /**
   * If the HTTP response specified a content length, returns the content length.
   * If no content length is specified returns -1.
   *
   * @return The content length or -1.
   */
  public int getContentLength() {
    if (headers_.containsKey("content-length")) {
      try {
        return Integer.valueOf(headers_.get("content-length").trim());
      } catch (NumberFormatException e) {
        log.catching(Level.DEBUG, e);
      }
    }
    return -1;
  }

  /**
   * Returns the {@link HttpRequest} associated with this response.
   *
   * @return The HttpRequest associated with this response.
   */
  public HttpRequest getRequest() {
    return request_;
  }

  /**
   * Returns the absolute path of the resource on the server targeted by this request.
   *
   * @return The absolute path of the resource on the server targeted by this request.
   */
  public String getResourcePath() {
    return resource_path_;
  }

  /**
   * Returns the HTTP response code received from the server.
   * A value of -1 indicates that no valid response code has been received.
   *
   * @return The HTTP response code.
   */
  public int getResponseCode() {
    return response_code_;
  }

  /**
   * Returns the value for the specified HTTP response header or null if there is no such header.
   * HttpResponse lower cases all HTTP response header names.
   *
   * @param name The lower case name of the HTTP response header.
   * @return The value of the HTTP response header or null.
   */
  public String getResponseHeader(String name) {
    return headers_.get(name);
  }

  /**
   * Returns true if the response has an HTTP OK response code.
   *
   * @return True if the response has an HTTP OK response code.
   */
  public boolean isHttpOk() {
    return response_code_ == 200;
  }

  /**
   * Returns true if the connection to the server is open.
   *
   * @return True if the connection to the server is open.
   */
  public boolean isOpen() {
    return socket_ != null && socket_.isConnected();
  }

  /**
   * Reads binary data from the response into the provided buffer.
   * This method reads at most num_bytes many bytes from the input and returns the actual number
   * of bytes read.
   *
   * This method may block until all bytes have been read.
   *
   * @param buffer Buffer into which to read the data.
   * @param num_bytes The maximum number of bytes to read.
   * @return The actual number of bytes read.
   */
  public int readBinary(byte[] buffer, int num_bytes) {
    int bytes_read = 0;
    if (isOpen()) {
      try {
        while (bytes_read < num_bytes) {
          bytes_read += input_stream_.read(buffer, bytes_read, num_bytes - bytes_read);
        }
      } catch (IOException e) {
        log.catching(Level.DEBUG, e);
      }
    }
    return bytes_read;
  }

  /**
   * Reads and returns the next byte in the response.
   * Returns -1 if the read fails.
   *
   * @return The next byte in the response or -1.
   */
  public int readByte() {
    try {
      return input_stream_.read();
    } catch (IOException e) {
      return -1;
    }
  }

  /**
   * Reads an entire streamed frame from the HTTP response.
   * This method can only be used for servers that stream content, such as IP cameras.
   * The server is expected to separate the content using a content boundary specified in the
   * main HTTP headers.
   * Each frame is expected to a start a short list of headers with at least the length of the
   * frame.
   *
   * If the frame sent by the server exceeds the size of the frame buffer, this method skips the
   * entire frame.
   *
   * This method automatically synchronizes the input stream with the frame boundaries sent by
   * the server and can be called at any time. In order to avoid dropping partial frames, this
   * method should not be called in conjunction with other read methods. This method may skip
   * the first frame if the input stream is not synchronized.
   *
   * @param frame The input buffer to hold the frame data.
   * @return The actual size of the frame.
   */
  public int readFrame(byte[] frame) {
    final byte[] boundary_sequence = getContentBoundarySequence();
    int boundary_sequence_index = 0;
    while (isOpen()) {
      int input = readByte();
      if (input == -1) {
        return 0;
      }
      if (input == boundary_sequence[boundary_sequence_index]) {
        boundary_sequence_index++;
        if (boundary_sequence_index == boundary_sequence.length) {
          boundary_sequence_index = 0;
          readLine();
          readResponseHeaders();
          int frame_size = getContentLength();
          if (frame_size > 0 && frame_size < frame.length) {
            frame_size = readBinary(frame, frame_size);
            log.debug("Read frame with {} bytes.", frame_size);
            return frame_size;
          } else {
            log.debug("Skipping frame with {} bytes.", frame_size);
            return 0;
          }
        }
      } else {
        boundary_sequence_index = 0;
      }
    }
    return 0;
  }

  /**
   * Reads a line from the input stream and returns the read line.
   *
   * The length of the line is limited to {@link #kInputBufferSize} in order to avoid buffer
   * overflows.
   *
   * If the conenction is closed, returns the empty string.
   *
   * @return The next line in the input stream.
   */
  public String readLine() {
    if (!isOpen()) {
      return "";
    }
    StringBuilder line = new StringBuilder();
    do {
      int input;
      try {
        input = input_stream_.read();
      } catch (IOException e) {
        return line.toString();
      }
      switch (input) {
        case -1:
        case '\n':
          return line.toString();
        case '\r': break;
        default:
          line.append((char) input);
          if (line.length() > kInputBufferSize) {
            return line.toString();
          }
          break;
      }
    } while (true);
  }

  /**
   * If the HTTP response specifies a content boundary, returns the content boundary as a sequence
   * of characters.
   *
   * This method only computes the boundary sequence, the first time it is called. On subsequent
   * calls, a cached version of the boundary sequence is returned.
   *
   * @return The content boundary as a sequence of characters.
   */
  private byte[] getContentBoundarySequence() {
    if (boundary_sequence_ == null) {
      String boundary_string = getContentBoundary();
      if (!boundary_string.startsWith("--")) {
        boundary_string = "--" + boundary_string;
      }
      boundary_sequence_ = boundary_string.getBytes();
    }
    return boundary_sequence_;
  }

  /**
   * Reads the response code from the input.
   *
   * This method is automatically called during construction of the HttpResponse object to read
   * the response code sent by the server. The response code is the first line sent by the server.
   */
  private void readResponseCode() {
    String line = readLine();
    String[] tokens = line.split(" ");
    if (tokens.length > 2) {
      try {
        response_code_ = Integer.valueOf(tokens[1]);
      } catch (NumberFormatException e) {}
    }
    if (response_code_ != 200) {
      log.warn("Received error code from camera: {}", line);
    }
  }

  /**
   * Reads the response headers from the input.
   * All response header names are converted to lower case.
   *
   * This method is automatically called during construction of the HttpResponse object to read
   * the main response headers. However, for streaming responses this method can be called
   * repeatedly to read response headers associated with frames in the stream. This method must
   * only be called when the next input from the server is expected to be a header line. In
   * streaming requests this will typically be the case after a content boundary.
   */
  private void readResponseHeaders() {
    String line;
    do {
      line = readLine();
      int split_index = line.indexOf(':');
      if (split_index > 0) {
        headers_.put(line.substring(0, split_index).toLowerCase(),
                     line.substring(split_index + 1));
      }
    } while (line.length() > 0);
  }

  private static Logger log = LogManager.getLogger();

  private byte[] boundary_sequence_;  // Characters of frame start marker string or null.
  private HashMap<String, String> headers_;  // Response headers received from server.
  private InputStream input_stream_;  // The input stream from the server.
  private HttpRequest request_;  // The associated HTTP request.
  private String resource_path_;  // The request resource path.
  private int response_code_;  // The response code received from the server.
  private Socket socket_;  // The connection to the server.
}
