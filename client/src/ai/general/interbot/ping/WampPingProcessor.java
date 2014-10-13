/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot.ping;

import ai.general.interbot.WampProcessor;
import ai.general.interbot.WebSocketUri;
import ai.general.net.RemoteMethod;
import ai.general.net.RemoteMethodCallException;
import ai.general.net.Connection;

/**
 * Sends a ping to a WAMP ping service. Awaits the response from the server and prints the
 * response and latency to the console.
 */
public class WampPingProcessor extends WampProcessor {

  /**
   * The wamp_ping_service_uri must point to a WAMP ping service.
   *
   * @param wamp_ping_service_uri The server URI endpoint that provides the WAMP ping service.
   */
  public WampPingProcessor(WebSocketUri wamp_ping_service_uri) {
    super(wamp_ping_service_uri);
  }

  /**
   * Prints information about the WAMP connection and sends a ping to server. Once the ping is
   * received, prints the response and latency.
   *
   * Can be called after the the WampConnection is opened successfully.
   *
   * This method is synchronous and blocks the caller until the ping response has been received
   * or a timeout occurs.
   */
  public void ping() {
    Connection connection = getConnection();
    System.out.println("Server ID: " + connection.getServerId());
    System.out.println("Session ID: " + connection.getSessionId());
    RemoteMethod<String> ping =
      new RemoteMethod<String>(connection, "ping_service/ping", String.class);
    try {
      long send_time_millis = System.currentTimeMillis();
      String response = ping.call("pong");
      long receive_time_millis = System.currentTimeMillis();
      System.out.println("Server responded with: " + response);
      System.out.println("Latency: " + (receive_time_millis - send_time_millis) + " ms");
    } catch (RemoteMethodCallException e) {
      switch (e.getReason()) {
        case Timeout: System.out.println("Error: timeout"); break;
        case RemoteError:
          System.out.println("Error: " + e.getMethodCall().getErrorDescription());
          break;
      }
    }
  }
}
