/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot.video;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Represents an HTTP 1.1 request.
 *
 * An HttpRequest object is created for an HTTP server and can be reused to issue multiple
 * requests to the server to different URL's on the server.
 *
 * HttpRequest supports basic HTTP authentication which can be enabled by calling
 * {@link #setAuthorization(String, String)}.
 */
public class HttpRequest {

  /**
   * Creates an HTTP request for the specified server.
   *
   * @param hostname Hostname or IP address of HTTP server.
   */
  public HttpRequest(String hostname) {
    this(hostname, 80);
  }

  /**
   * Creates an HTTP request for the specified server.
   *
   * @param hostname Hostname or IP address of HTTP server.
   * @param port Port of HTTP server.
   */
  public HttpRequest(String hostname, int port) {
    this.hostname_ = hostname;
    this.port_ = port;
    authorization_ = null;
  }

  /**
   * Sends an HTTP GET request to the server for the resource specified by the resource path.
   * This method can be called repeatedly to issue requets to the same server.
   *
   * The returned {@link HttpResonse} object can be used to check whether the request was
   * successful and to obtain the returned data.
   *
   * If the connection could not be established, the returned HttpResponse object will return
   * a response code of -1.
   *
   * The caller should close the returned HTTP response.
   *
   * @param resource_path The absolute path of the request target on the server.
   * @return The HTTP response.
   */
  public HttpResponse get(String resource_path) {
    log.debug("HTTP GET {}:{}/{}", hostname_, port_, resource_path);
    try {
      Socket socket = new Socket(hostname_, port_);
      PrintWriter out = new PrintWriter(socket.getOutputStream());
      out.print("GET /" + resource_path + " HTTP/1.1\r\n");
      out.print("Host: " + hostname_ + "\r\n");
      if (authorization_ != null) {
        out.print(authorization_);
      }
      out.print("\r\n");
      out.flush();
      return new HttpResponse(this, resource_path, socket);
    } catch (IOException e) {
      log.catching(Level.DEBUG, e);
      return new HttpResponse(this, resource_path, null);
    }
  }

  /**
   * Specifies the HTTP basic authorization to be used in HTTP requests.
   * To disable authorization in future requests, the username can be set to null or the empty
   * string.
   *
   * @param username The username to use in HTTP requests.
   * @param password The password to use in HTTP requests.
   */
  public void setAuthorization(String username, String password) {
    if (username != null && username.length() > 0 && password != null) {
      String user_id = username + ":" + password;
      authorization_ = "Authorization: Basic " +
        DatatypeConverter.printBase64Binary(user_id.getBytes()) + "\r\n";
    } else {
      authorization_ = null;
    }
  }

  private static Logger log = LogManager.getLogger();

  private String authorization_;  // HTTP basic authentication string.
  private String hostname_;  // Hostname or IP address of TCP server.
  private int port_;  // TCP port of HTTP server.
}
