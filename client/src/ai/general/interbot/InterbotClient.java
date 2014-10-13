/* General AI - Interbot
 * Copyright (C) 2014 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot;

import ai.general.interbot.video.VideoPlugin;
import ai.general.plugin.PluginManager;
import ai.general.scriptduino.ScriptduinoPlugin;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Implements the InterbotClient.
 *
 * The InterbotClient loads and initializes the Interbot application and logs into an
 * InterbotServer.
 *
 * The InterbotClient uses the WAMP protocol.
 */
public class InterbotClient {

  /**
   * Constructs an InterbotClient.
   */
  public InterbotClient() {
    interbot_config_ = null;
    user_profiles_ = null;
    processor_ = null;
    current_user_profile_ = null;
    is_loaded_ = false;
    is_connected_ = false;
  }

  /**
   * Returns the user profile with which the client is currently logged into the Interbot server
   * or null if the client is not logged in.
   *
   * @return The UserProfile with which the client is logged in or null.
   */
  public UserProfile getCurrentUserProfile() {
    return current_user_profile_;
  }

  /**
   * Returns the Interbot configuration.
   *
   * @return The Interbot config.
   */
  public InterbotConfig getInterbotConfig() {
    return interbot_config_;
  }

  /**
   * Returns the processor that handles server messages if the client is connected to a server.
   * Retruns null if the client is not connected.
   *
   * @return ConnectionProcessor that handles server messages or null.
   */
  public ConnectionProcessor getProcessor() {
    return processor_;
  }

  /**
   * Returns the set of all user profiles.
   *
   * @return The set of all user profiles.
   */
  public UserProfiles getUserProfiles() {
    return user_profiles_;
  }

  /**
   * Returns true if the client is connected and logged into an Interbot server.
   *
   * @return True if the client is connected and logged into an Interbot server.
   */
  public boolean isConnected() {
    return is_connected_;
  }

  /**
   * Returns true if configuration files have been loaded and the client is initialized.
   *
   * @return True if configuration files have been loaded and the client is initialized.
   */
  public boolean isLoaded() {
    return is_loaded_;
  }

  /**
   * Loads configuration data and initializes the InterbotClient.
   *
   * @return True if all data was successfully loaded and the client is initialized.
   */
  public boolean load() {
    interbot_config_ = InterbotConfig.load();
    user_profiles_ = UserProfiles.load();
    if (interbot_config_ == null || user_profiles_ == null) {
      log.error("Failed to load configuration files.");
      interbot_config_ = new InterbotConfig();
      user_profiles_ = new UserProfiles();
      return false;
    }

    PluginManager plugin_manager = PluginManager.Instance;
    if (!plugin_manager.load(InterbotPlugin.class) ||
        !plugin_manager.load(VideoPlugin.class) ||
        !plugin_manager.load(ScriptduinoPlugin.class)) {
      log.error("Failed to load plugins.");
      return false;
    }
    plugin_manager.enableAll();
    is_loaded_ = true;
    return true;
  }

  /**
   * Logs into the Interbot server with the specified user profile.
   * If the client is already logged in with another user profile, this method first logs out
   * and then logs back in with the new profile.
   *
   * @param user_profile The user profile to use for login.
   * @return The result of the login attempt.
   */
  public ConnectionResult login(UserProfile user_profile) {
    logout();
    WebSocketUri service_uri = new WebSocketUri(interbot_config_.getSecure(),
                                                interbot_config_.getServer(),
                                                interbot_config_.getService());
    processor_ = new WampProcessor(service_uri, user_profile.getUsername(), user_profile.getKey());
    ConnectionResult result = processor_.open("/");
    if (result != ConnectionResult.Success) {
      log.error("Error: Failed to connect to {} as {}: {}",
                service_uri, user_profile.getUsername(), result.name());
      processor_ = null;
      return result;
    }
    log.info("connected to {} as {}", service_uri, user_profile.getUsername());
    current_user_profile_ = user_profile;
    SessionWatcher watcher = SessionWatcher.Instance;
    watcher.setProcessor(processor_);
    watcher.start();
    is_connected_ = true;
    return ConnectionResult.Success;
  }

  /**
   * Logs out from the Interbot server with the current user profile.
   * Results in a no-op if the user is not logged in.
   */
  public void logout() {
    if (!is_connected_) {
      return;
    }
    SessionWatcher watcher = SessionWatcher.Instance;
    watcher.stop();
    watcher.setProcessor(null);
    if (processor_ != null) {
      processor_.close();
      processor_ = null;
    }
    log.info("logged out as {}", current_user_profile_.getUsername());
    current_user_profile_ = null;
    is_connected_ = false;
  }

  /**
   * Prepares the client for shutdown.
   * If the client is logged in, logs out of the server.
   */
  public void unload() {
    logout();
    PluginManager.Instance.unloadAll();
  }

  private static Logger log = LogManager.getLogger();

  private UserProfile current_user_profile_;  // Currently logged in user profile or null.
  private InterbotConfig interbot_config_;  // Interbot configuration.
  private boolean is_connected_;  // True if the client is connected to a server.
  private boolean is_loaded_;  // True if configurations were loaded successfully.
  private ConnectionProcessor processor_;  // Processor used to process server messages.
  private UserProfiles user_profiles_;  // All user profiles.
}
