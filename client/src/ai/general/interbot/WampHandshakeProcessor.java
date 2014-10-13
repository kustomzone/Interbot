/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot;

import ai.general.event.Processor;
import ai.general.net.wamp.WampConnection;

/**
 * Processes the WAMP connection handshake.
 *
 * This class is used by {@link WampProcessor} to initiate a WAMP connection. Clients should use
 * WampProcessor rather than this class.
 *
 * The WampHandshakeProcessor can be used by clients to open a WAMP connection to a server and
 * await a welcome message from the server.
 * Once the welcome message has been received and the WampHandshakeProcessor halts, the caller
 * can start using the WAMP connection.
 *
 * Callers can check whether the handshake was succesfully processed via the connection result
 * returned by {@link #getResult()}.
 */
public class WampHandshakeProcessor extends Processor<WebSocketEvent> {

  /**
   * Prepares a WampHandshakeProcessor for the specified socket and WAMP connection.
   * The socket must not have been opened. It wil be opened by this class.
   *
   * @param socket The WebSocket connection.
   * @param connection The WAMP protocol implementation.
   */
  public WampHandshakeProcessor(WebSocket socket, WampConnection connection) {
    super("wampHandshake(" + socket.getUri() + ")");
    socket_ = socket;
    connection_ = connection;
    result_ = ConnectionResult.Incomplete;
  }

  /**
   * Returns the result of the WAMP handshake.
   *
   * @return The result of the WAMP handshake.
   */
  public ConnectionResult getResult() {
    return result_;
  }

  /**
   * Halts the WampHandshakeProcessor.
   *
   * Typically, the WampHandshakeProcessor halts itself, but it may be halted by another thread
   * when a time-out occurs.
   */
  @Override
  public void halt() {
    socket_.unsubscribe(getObserver());
    super.halt();
  }

  /**
   * Opens the socket and awaits a welcome message from the server.
   */
  @Override
  public void start() {
    socket_.subscribe(getObserver());
    if (!socket_.openAsync()) {
      socket_.unsubscribe(getObserver());
      result_ = ConnectionResult.ConnectionError;
      return;
    }
    super.start();
  }

  /**
   * Processes WebSocket events.
   * Once the WebSocket is open, awaits a welcome message and halts. The reception of the welcome
   * message is verified by checking that the server and session ID's have been received.
   */
  @Override
  protected void process(WebSocketEvent event) {
    switch (event.getEventType()) {
      case Close:
        if (result_ == ConnectionResult.Incomplete) {
          result_ = ConnectionResult.LoginError;
        }
        halt();
        break;
      case Open: break;
      case TextMessage:
        connection_.process(event.getTextMessage());
        if (connection_.isReady()) {
          result_ = ConnectionResult.Success;
        }
        halt();
        break;
    default:
      // Error events and binary messages are interpreted as connection errors.
      if (result_ == ConnectionResult.Incomplete) {
        result_ = ConnectionResult.ConnectionError;
      }
      halt();
      break;
    }
  }

  private WampConnection connection_;  // The WAMP message handler.
  private ConnectionResult result_;  // The result of the WAMP handshake.
  private WebSocket socket_;  // The WebSocket used to connect to the server.
}
