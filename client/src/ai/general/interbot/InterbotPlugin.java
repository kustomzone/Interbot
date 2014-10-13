/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot;

import ai.general.net.Connection;
import ai.general.plugin.Plugin;

import java.net.URI;

/**
 * Main Interbot plugin. Provides the Interbot client service and starts and stops the session
 * watcher.
 */
public class InterbotPlugin extends Plugin {

  /**
   * Constructs the InterbotPlugin.
   */
  public InterbotPlugin() {
    super("InterbotClient",
          1.0,
          "Provides the Interbot client service.",
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
    InterbotClientService client_service = new InterbotClientService(connection);
    SessionWatcher.Instance.setClientService(client_service);
    registerService("InterbotClientService", client_service, "/", true);
    super.onConnect(connection);
    SystemInfoRequest info_request = new SystemInfoRequest();
    info_request.getProperties().add(SystemProperty.NetworkInterfaces);
    info_request.getProperties().add(SystemProperty.Devices);
    client_service.onSystemInfo(info_request);
    return true;
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
    SessionWatcher.Instance.setClientService(null);
    return true;
  }
}
