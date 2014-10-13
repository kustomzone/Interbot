/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import ai.general.directory.Directory;
import ai.general.net.Connection;
import ai.general.plugin.Plugin;

/**
 * Plugin for user service. Initializes paths and services for user service WAMP connections.
 */
public class UserPlugin extends Plugin {

  /** Plugin version. */
  public static final double kVersion = 1.0;

  public UserPlugin() {
    super("ai.general.web.UserPlugin",
          kVersion,
          "Provides user service.",
          "General AI");
  }

  /**
   * Handles new connections.
   * This method assumes that the connection has already been authenticated.
   *
   * @param connection The new connection.
   * @return True if the plugin has been successfully connected.
   */
  @Override
  public boolean onConnect(Connection connection) {
    if (!connection.getUri().getPath().endsWith("/user/user_service.wamp") &&
        !connection.getUri().getPath().endsWith("/robot/robot_service.wamp")) {
      return false;
    }
    User user = UserManager.getInstance().getUser(connection.getUserAccount());
    if (user == null) return false;
    String service_name = "user:" + user.getUsername();
    synchronized (user) {
      if (user.getWampConnectionCount() == 0) {
        UserUris.createEventPathsForUser(user);
        registerService(service_name, user, connection.getHomePath(), false);
      }
      user.incrementWampConnectionCount();
    }
    getServiceDefinition(service_name).connect(connection);
    return true;
  }

  /**
   * Handles closed connections.
   *
   * @param connection The connection being closed.
   * @return True if the plugin has been successfully disconnected.
   */
  @Override
  public boolean onDisconnect(Connection connection) {
    if (!connection.getUri().getPath().endsWith("/user/user_service.wamp") &&
        !connection.getUri().getPath().endsWith("/robot/robot_service.wamp")) {
      return true;
    }
    User user = UserManager.getInstance().getUser(connection.getUserAccount());
    if (user == null) return true;
    String service_name = "user:" + user.getUsername();
    getServiceDefinition(service_name).disconnect(connection);
    synchronized (user) {
      user.decrementWampConnectionCount();
      if (user.getWampConnectionCount() > 0) return true;
      unregisterService(service_name);
      Directory.Instance.removePath(UserUris.userHomePath(user.getUsername()));
    }
    return true;
  }
}
