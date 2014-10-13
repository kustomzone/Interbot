/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot.video;

import ai.general.net.Connection;
import ai.general.plugin.Plugin;

import java.net.URI;

/**
 * The video plugin defines video related services.
 * The VideoPlugin provides the video stream service that handles streaming of video to the server.
 */
public class VideoPlugin extends Plugin {

  public VideoPlugin() {
    super("VideoPlugin",
          1.0,
          "Provides video related services.",
          "General AI");
  }

  /**
   * Called when a new connection is established.
   *
   * @param connection The new connection.
   * @return True if the plugin has been successfully connected.
   */
  @Override
  public boolean onConnect(Connection connection) {
    registerService("VideoStreamService", new VideoStreamService(connection), "/", true);
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
    super.onDisconnect(connection);
    unregisterAllServices();
    return true;
  }
}
