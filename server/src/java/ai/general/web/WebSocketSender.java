/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import ai.general.net.OutputSender;

import java.nio.ByteBuffer;
import javax.websocket.RemoteEndpoint;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * OutputSender implementation that sends output over a WebSocket connection.
 */
public class WebSocketSender implements OutputSender {

  private javax.websocket.RemoteEndpoint.Basic socket_;

  /**
   * @param socket The remote endpoint of the WebSocket connection.
   */
  public WebSocketSender(RemoteEndpoint.Basic socket) {
    this.socket_ = socket;
    this.username_ = "[null]";
    this.session_id_ = "[null]";
  }

  /**
   * Sends a text message to the remote endpoint.
   *
   * @param text Text message to send.
   * @return True if the message was successfully sent.
   */
  @Override
  public synchronized boolean sendText(String text) {
    try {
      socket_.sendText(text);
      log.debug("({}/{}) << {}", username_, session_id_, text);
      return true;
    } catch (Exception e) {
      log.catching(Level.DEBUG, e);
      return false;
    }
  }

  /**
   * Sends a binary message to the remote endpoint.
   *
   * @param data Binary data to send.
   * @return True if the data was successfully sent.
   */
  @Override
  public synchronized boolean sendBinary(ByteBuffer data) {
    try {
      socket_.sendBinary(data);
      return true;
    } catch (Exception e) {
      log.catching(Level.DEBUG, e);
      return false;
    }
  }

  /**
   * Sets information about the user account that is used for logging.
   *
   * @param username The username of the user at the remote end.
   * @param session_id The session with which this WebSocketSender is associated.
   */
  public void setUserInfo(String username, String session_id) {
    this.username_ = username != null ? username : "[null]";
    this.session_id_ = session_id != null ? session_id : "[null]";
  }

  private static Logger log = LogManager.getLogger();
  private String username_;
  private String session_id_;
}
