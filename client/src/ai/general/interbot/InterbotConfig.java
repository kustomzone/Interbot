/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot;

/**
 * Defines Interbot configuration options.
 * The Interbot configuration is stored in a JSON file and deserialized from that file.
 */
public class InterbotConfig {

  private static final String kConfigFilename = "interbot-config";

  /**
   * Constructs an empty Interbot config.
   */
  public InterbotConfig() {
    server_ = "";
    service_ = "";
    secure_ = false;
    autostart_ = false;
  }

  /**
   * Loads the Interbot configuration for the current context.
   * Returns null if the configuration cannot be loaded.
   *
   * @return The Interbot config or null.
   */
  public static InterbotConfig load() {
    if (config == null) {
      config = ConfigFiles.Instance.load(kConfigFilename, InterbotConfig.class);
    }
    return config;
  }

  /**
   * Whether to automatically start Interbot and log into the server on startup.
   * This setting is used by the Interbot web app to automatically start the web app when the
   * robot is powered up.
   *
   * @return Whether to automatically start the Interbot web app on robot startup.
   */
  public boolean getAutostart() {
    return autostart_;
  }

  /**
   * Whether to use a secure encrypted connection to the server.
   *
   * @return Whether to use a secure connection to the server.
   */
  public boolean getSecure() {
    return secure_;
  }

  /**
   * Returns the Interbot server domain address or IP address. The server address may include a
   * port number separated with colons from the server address.
   *
   * @return The Interbot server address.
   */
  public String getServer() {
    return server_;
  }

  /**
   * Returns the Interbot service address at the Interbot server. The service address is the
   * connection endpoint at the server.
   *
   * @return The Interbot server address.
   */
  public String getService() {
    return service_;
  }

  /**
   * Sets whether to automatically start Interbot and log into the server on startup.
   * This setting is used by the Interbot web app to automatically start the web app when the
   * robot is powered up.
   *
   * @param autostart Whether to automatically start the Interbot web app on robot startup.
   */
  public void setAutostart(boolean autostart) {
    this.autostart_ = autostart;
  }

  /**
   * Sets whether to use a secure encrypted connection to the server.
   *
   * @param secure Whether to use a secure connection to the server.
   */
  public void setSecure(boolean secure) {
    this.secure_ = secure;
  }

  /**
   * Sets the Interbot server domain address or IP address. The server address may include a
   * port number separated with colons from the server address.
   *
   * @param The Interbot server address.
   */
  public void setServer(String server) {
    this.server_ = server;
  }

  /**
   * Sets the Interbot service address at the Interbot server. The service address is the
   * connection endpoint at the server.
   *
   * @param The Interbot service address.
   */
  public void setService(String service) {
    this.service_ = service;
  }

  private static InterbotConfig config = null;

  private boolean autostart_;  // Whether to log into the server on startup.
  private boolean secure_;  // Whether to use a secure connection.
  private String server_;  // Server host address.
  private String service_;  // Absolute endpoint path on server.
}
