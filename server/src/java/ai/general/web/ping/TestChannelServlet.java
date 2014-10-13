/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web.ping;

import ai.general.web.WampServlet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * Allows subscription and publishing to a test channel.
 */
@ServerEndpoint(value="/ping/test_channel.wamp",
                subprotocols="wamp")
public class TestChannelServlet extends WampServlet {

  private static final String kChannelPath = "/channel/0";

  /**
   * Called when the connection is opened.
   *
   * @param session The WebSocket session associated with the connection.
   */
  @OnOpen
  public synchronized void onOpen(Session session) {
    open(session, null, null, kChannelPath);
  }

  /**
   * Called when the connection is closed.
   */
  @OnClose
  public synchronized void onClose() {
    close();
  }

  /**
   * Called when a text message is received.
   *
   * @param data The incoming text message.
   */
  @OnMessage
  public synchronized void onMessage(String message) {
    process(message);
  }

  /**
   * Called when an error has occurred.
   *
   * @param error Error details.
   */
  @OnError
  public synchronized void onError(Throwable error) {
  }
}
