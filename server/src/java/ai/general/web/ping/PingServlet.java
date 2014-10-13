/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web.ping;

import javax.websocket.OnMessage;
import javax.websocket.server.ServerEndpoint;

/**
 * Responds to WebSocket ping requests.
 */
@ServerEndpoint("/ping/ping.ws")
public class PingServlet {

  /**
   * Sends a ping reply by echoing the received message.
   *
   * @param message Incoming WebSocket message.
   * @return The incoming message.
   */
  @OnMessage
  public String onMessage(String message) {
    if (message.length() > 256) message = message.substring(0, 256);
    return "ping:" + message;
  }
}
