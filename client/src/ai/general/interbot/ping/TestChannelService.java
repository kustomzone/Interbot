/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot.ping;

import ai.general.net.Connection;
import ai.general.plugin.annotation.Subscribe;

/**
 * Subscribes to a test channel. Prints messages received from the test channel to the console.
 * Echoes messages back to the channel.
 */
public class TestChannelService {

  /**
   * Constructs the TestChannel service.
   *
   * @param connection The connection associated with the service instance.
   */
  public TestChannelService(Connection connection) {
    this.connection_ = connection;
  }

  /**
   * Handles messages received from the channel.
   *
   * @param message Incoming text message.
   */
  @Subscribe("text")
  public void receive(String message) {
    System.out.println(message);
    connection_.publish("text", message, true);
  }

  private Connection connection_;  // The connection to the server.
}
