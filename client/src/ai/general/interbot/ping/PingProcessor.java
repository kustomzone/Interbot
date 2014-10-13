/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot.ping;

import ai.general.event.Processor;
import ai.general.interbot.WebSocket;
import ai.general.interbot.WebSocketEvent;
import ai.general.interbot.WebSocketUri;

/**
 * Sends a ping to a WebSocket server. Prints the response and the time it took to receive the
 * response to the console.
 */
public class PingProcessor extends Processor<WebSocketEvent> {

  /**
   * The ping URI must point to a WebSocket ping service.
   * The ping will be sent when {@link #start()} is called.
   *
   * @param ping_uri The server URI endpoint that provides the ping service.
   */
  public PingProcessor(WebSocketUri ping_uri) {
    super("ping");
    socket_ = new WebSocket(ping_uri);
    socket_.subscribe(getObserver());
  }

  /**
   * Closes the socket and halts.
   */
  @Override
  public void halt() {
    super.halt();
    if (socket_.isOpen()) {
      try {
        join(2 * kPollTimeoutMillis);
      } catch (InterruptedException e) {}
      socket_.close();
    }
  }

  /**
   * Opens the socket, sends the ping to the server and awaits the response.
   * Prints results to the console.
   */
  @Override
  public void start() {
    if (!socket_.open()) {
      System.out.println("Failed to connect to server: " + socket_.getUri());
      return;
    }
    super.start();
  }

  /**
   * Processes WebSocket events.
   * Sends a ping when the socket was opened.
   * Halts once a reply has been received or an unexpected event has occurred.
   */
  @Override
  protected void process(WebSocketEvent event) {
    switch (event.getEventType()) {
      case Open:
        System.out.println("Sending ping to " + socket_.getUri() + "...");
        send_time_millis_ = System.currentTimeMillis();
        socket_.sendText("pong");
        break;
      case TextMessage:
        long receive_time_millis = System.currentTimeMillis();
        System.out.println("Server responded with: " + event.getTextMessage());
        System.out.println("Latency: " + (receive_time_millis - send_time_millis_) + " ms");
        halt();
        break;
      case Error:
        System.out.println(event.getError().toString());
        halt();
        break;
      default:
        System.out.println("Unexpected WebSocket event: " + event.getEventType().name());
        halt();
        break;
    }
  }

  private long send_time_millis_;  // Time ping was sent in milliseconds since Epoch.
  private WebSocket socket_;  // Connection to server.
}
