/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot.video;

import java.nio.ByteBuffer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Implements an IP Camera driver.
 * IpCamera provides access to the video stream of an IP camera.
 * IpCamera also manages the basic configuration of the IP camera.
 *
 * IPCamera supports cameras that provide an MJPEG video stream.
 */
public class IpCamera {

  // Note: increasing the buffer below may require configuration changes at the server.
  private static final int kFrameBufferSize = 65536;  // output buffer size

  /**
   * Implements a thread that reads frames from an IP Camera.
   */
  private class FrameReader extends Thread {

    /**
     * Constructs a frame reader.
     */
    public FrameReader() {
      setName("ip-camera-frame-reader");
      run_ = false;
    }

    /**
     * Stops the FrameReader thread. This method initiates the end of the frame reader thread
     * but does not wait for the thread to exit.
     */
    public void halt() {
      run_ = false;
    }

    /**
     * Main method of FrameReader.
     */
    @Override
    public void run() {
      log.debug("Starting IP camera frame reader thread.");
      run_ = true;
      while (run_) {
        int frame_size = video_stream_.readFrame(current_frame_);
        if (frame_size > 0) {
          swapInputFrames(frame_size);
        }
      }
      log.debug("IP camera frame reader thread is exiting.");
    }

    private volatile boolean run_;  // True if the thread is running.
  }

  /**
   * Initializes the IP camera.
   */
  public IpCamera() {
    config_ = IpCameraConfig.load();
    camera_ = null;
    video_stream_ = null;
    frame_reader_ = null;
    last_frame_ = new byte[kFrameBufferSize];
    current_frame_ = new byte[kFrameBufferSize];
    output_frame_ = new byte[kFrameBufferSize];
    last_frame_size_ = 0;
    output_frame_size_ = 0;
  }

  /**
   * Closes the connection opened by {@link #open()}. Stops reading frames from the camera and
   * halts the reader thread.
   */
  public void close() {
    if (frame_reader_ != null) {
      frame_reader_.halt();
      try {
        frame_reader_.join();
      } catch (InterruptedException e) {}
      frame_reader_ = null;
    }
    if (video_stream_ != null) {
      video_stream_.close();
      video_stream_ = null;
      log.debug("closed connection to IP camera at {}", config_.getIpAddress());
    }
  }

  /**
   * Returns the last frame read from the IP camera.
   * The returned array is not modified until this method is called again.
   *
   * Calling this method invalidates the array returned in a previous call to this method.
   * To avoid race conditions, frames should be read only by one reader thread or the readers must
   * be carefully synchronized.
   *
   * @return The last frame read from the IP camera.
   */
  public synchronized ByteBuffer getFrame() {
    swapOutputFrames();
    return ByteBuffer.wrap(output_frame_, 0, output_frame_size_);
  }

  /**
   * Returns the IP camera configuration.
   * This method may return null if the configuration could not be loaded.
   *
   * @return The IP camera configuration.
   */
  public IpCameraConfig getIpCameraConfig() {
    return config_;
  }

  /**
   * Executes the specified pan-tilt instruction.
   * This method blocks until the pan-tilt instruction has completed.
   *
   * @param instruction The pan-tilt instruction.
   */
  public void panTilt(PanTiltInstruction instruction) {
    PanTiltConfig pan_tilt_config = config_.getPanTilt();
    String command = pan_tilt_config.beginUrl(instruction);
    if (command.length() == 0) {
      return;
    }
    camera_.get(command).close();
    command = pan_tilt_config.endUrl(instruction);
    if (command.length() == 0) {
      return;
    }
    try {
      Thread.sleep(pan_tilt_config.durationMillis(instruction));
    } catch (InterruptedException e) {}
    camera_.get(command).close();
  }

  /**
   * Opens a connection to the IP camera and starts reading frames from the camera on a separate
   * thread.
   * If the camera cannot be opened, returns false.
   *
   * @return True if the camera has been successfully opened.
   */
  public boolean open() {
    if (config_ == null || video_stream_ != null) {
      return false;
    }
    if (!connect()) {
      log.error("Failed to connect to IP camera.");
      return false;
    }
    frame_reader_ = new FrameReader();
    frame_reader_.start();
    log.debug("opened connection to IP camera at {}", config_.getIpAddress());
    return true;
  }

  /**
   * Establishes a connection to the IP camera and executes the handshake to start the video
   * stream.
   *
   * When this method returns with true, the socket_ and input_stream_ variables will be set to
   * valid values.
   *
   * @return True if the connection to the IP camera has been established.
   */
  private boolean connect() {
    camera_ = new HttpRequest(config_.getIpAddress(), config_.getPort());
    camera_.setAuthorization(config_.getUsername(), config_.getPassword());
    initializeCamera();
    video_stream_ = camera_.get(config_.getVideostreamUrl());
    if (video_stream_.isHttpOk()) {
      if (video_stream_.getContentBoundary().length() > 0) {
        return true;
      } else {
        log.warn("Did not receive a content boundary from IP camera.");
      }
    } else {
      log.warn("Received error code from camera: {}", video_stream_.getResponseCode());
    }
    video_stream_.close();
    video_stream_ = null;
    return false;
  }

  /**
   * Executes the init instructions specified in the IP camera configuration file.
   */
  private void initializeCamera() {
    for (String init_instruction : config_.getInitInstructions()) {
      log.debug("Initializing IP camera with {}", init_instruction);
      camera_.get(init_instruction).close();
    }
  }

  /**
   * Swaps the current frame buffer with the last frame buffer.
   * Input is written to the current frame.
   * The last frame is complete and ready for output.
   *
   * @param frame_size The size of the current frame in bytes.
   */
  private synchronized void swapInputFrames(int frame_size) {
    byte[] temp = last_frame_;
    last_frame_ = current_frame_;
    current_frame_ = temp;
    this.last_frame_size_ = frame_size;
  }

  /**
   * Swaps the last frame buffer with the output frame buffer.
   * IpCamera returns the output frame to callers. The output frame must not be modified.
   * The last frame can become the current frame. The extra output frame ensures that callers
   * get a frame that is not modified when the current and last frames are swapped.
   */
  private synchronized void swapOutputFrames() {
    byte[] temp = last_frame_;
    last_frame_ = output_frame_;
    output_frame_ = temp;
    output_frame_size_ = last_frame_size_;
  }

  private static Logger log = LogManager.getLogger();

  private HttpRequest camera_;  // Represents requests to the IP camera.
  private IpCameraConfig config_;  // IP camera configuration.
  private byte[] current_frame_;  // Frame currently being written by FrameReader.
  private FrameReader frame_reader_;  // IP camera output reader thread.
  private byte[] last_frame_;  // Last fully read frame ready for output.
  private int last_frame_size_;  // Size of last_frame_ in bytes.
  private byte[] output_frame_;  // Frame locked by external thread.
  private int output_frame_size_;  // Size of output_frame_ in bytes.
  private HttpResponse video_stream_;  // The video stream from the IP camera.
}
