/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web.ping;

import ai.general.plugin.annotation.RpcMethod;

/**
 * Implements a ping service.
 */
public class PingService {

  /**
   * Responds to incoming ping requests by sending back a string that starts with "ping:"
   * followed by the client message. If the client message is more than 256 characters it
   * is truncated.
   *
   * @param message Ping message from client.
   * @return Ping reply, which includes the ping message from the client.
   */
  @RpcMethod("ping_service/ping")
  public String ping(String message) {
    if (message.length() > 256) message = message.substring(0, 256);
    return "ping:" + message;
  }
}
