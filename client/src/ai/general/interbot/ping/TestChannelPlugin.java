/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot.ping;

import ai.general.net.Connection;
import ai.general.plugin.Plugin;

import java.net.URI;

/**
 * Plugin for the {@link TestChannelSerivce}.
 */
public class TestChannelPlugin extends Plugin {

  /**
   * Constructs the TestChannel plugin.
   */
  public TestChannelPlugin() {
    super("TestChannel",
          1.0,
          "Provides a service that subscribes to a test channel.",
          "General AI");
  }

  /**
   * Called when a connection is opened.
   *
   * @param connection The new connection.
   * @return True if the plugin has been successfully connected.
   */
  @Override
  public boolean onConnect(Connection connection) {
    if (!connection.getUri().getPath().endsWith("/test_channel.wamp")) {
      return false;
    }
    registerService("test_channel",
                    new TestChannelService(connection),
                    connection.getHomePath(),
                    true);
    return super.onConnect(connection);
  }

  /**
   * Called when a connection is closed.
   *
   * @param connection The connection being closed.
   * @return True if the plugin has been successfully disconnected.
   */
  @Override
  public boolean onDisconnect(Connection connection) {
    if (!connection.getUri().getPath().endsWith("/test_channel.wamp")) {
      return true;
    }
    super.onDisconnect(connection);
    unregisterService("test_channel");
    return true;
  }
}
