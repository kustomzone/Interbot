/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import ai.general.net.Uri;
import ai.general.net.wamp.WampConnection;

import javax.websocket.Session;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Base class for all WAMP servlet.
 * Manages a WAMP connection and processes WAMP requests.
 *
 * Subclasses must implement annotated onOpen, onClose and onMessage methods and call the
 * corresponding methods in this class.
 * In addition subclasses must define a ServerEndpoint annotation with subprotocol "wamp".
 */
public abstract class WampServlet {

  public WampServlet() {
    wamp_ = null;
    username_ = "[null]";
  }

  /**
   * Returns the WAMP connection.
   *
   * @return The WAMP connection or null.
   */
  protected WampConnection getConnection() {
    return wamp_;
  }

  /**
   * Opens the WAMP connection and completes the opening handshake.
   * user_account may be null for anonymous access.
   * All requets paths will be interpreted with respect to the home_path.
   *
   * If the session ID is null a new random session ID is generated for the WAMP connection.
   *
   * @param session The WebSocket session.
   * @param session_id The session ID to use for the WAMP connection or null.
   * @param user_account The user account associated with this connection or null.
   * @param home_path The local home path associated with this connection.
   */
  protected void open(Session session, String session_id, String user_account, String home_path) {
    if (user_account != null) {
      this.username_ = user_account;
    }
    WebSocketSender sender = new WebSocketSender(session.getBasicRemote());
    sender.setUserInfo(user_account, session_id);
    wamp_ = new WampConnection(new Uri(session.getRequestURI()),
                               user_account,
                               home_path,
                               sender);
    if (session_id == null) {
      wamp_.welcome();
      // update session ID
      sender.setUserInfo(user_account, wamp_.getSessionId());
    } else {
      wamp_.welcome(session_id);
    }
  }

  /**
   * Closes the WAMP connection.
   */
  protected void close() {
    if (wamp_ == null) return;
    wamp_.close();
    wamp_ = null;
  }

  /**
   * Processes an incoming text message.
   * This method must be called from the onMessage method to process WAMP requests.
   *
   * @param String Incoming text message.
   */
  protected void process(String message) {
    log.debug("({}/{}) >> {}", username_, wamp_.getSessionId(), message);
    if (wamp_ == null) return;
    wamp_.process(message);
  }

  private static Logger log = LogManager.getLogger();
  private WampConnection wamp_;
  private String username_;
}
