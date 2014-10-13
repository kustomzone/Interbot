/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot.video;

import ai.general.interbot.WebSocket;

import java.nio.ByteBuffer;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Streams video to the server.
 * The video streamer starts a thread that reads video from the camera and streams the video to
 * the server.
 *
 * The video streamer thread is started with the {@link #start()} method.
 */
public class VideoStreamer extends Thread {

  /**
   * Constructs a new video streamer.
   * The supplied WebSocket must have been already opened by the client.
   *
   * @param camera The camera from which the video stream is read.
   * @param socket A WebSocket connection to which the video stream is sent.
   */
  public VideoStreamer(IpCamera camera, WebSocket socket) {
    setName("video-streamer");
    this.camera_ = camera;
    this.socket_ = socket;
    this.run_ = false;
  }

  /**
   * Halts the video streamer. Halting the video streamer closes the connection to the server.
   * The halt method does not wait for the video streamer to exit. It immediately returns while
   * the video streamer asynchronuously exits.
   */
  public void halt() {
    run_ = false;
  }

  /**
   * Main method of the video streamer.
   */
  @Override
  public void run() {
    run_ = true;
    if (camera_.getIpCameraConfig() == null) {
      return;
    }
    int sleep_time_millis = (int) (1000.0 / camera_.getIpCameraConfig().getFrameRate());
    log.debug("starting video streamer");
    while (run_) {
      try {
        Thread.sleep(sleep_time_millis);
      } catch (InterruptedException e) {}
      socket_.sendBinary(camera_.getFrame());
    }
    log.debug("stopping video streamer");
  }

  private static Logger log = LogManager.getLogger();

  private IpCamera camera_;  // The IP camera used to capture video.
  private volatile boolean run_;  // True if the video streamer is running.
  private WebSocket socket_;  // The connection used to upload video to the server.
}
