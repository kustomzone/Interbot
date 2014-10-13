/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.scriptduino;

import ai.general.net.Connection;
import ai.general.plugin.Plugin;

import java.net.URI;

/**
 * The Scriptduino plugin provides the Scriptduino service that enables communications between
 * an Arduino running Scriptduino and an Interbot server.
 */
public class ScriptduinoPlugin extends Plugin {

  public ScriptduinoPlugin() {
    super("Scriptduino",
          1.0,
          "Connects an Arduino running Scriptduino to an Interbot server.",
          "General AI");
    scriptduino_ = null;
    scriptduino_service_ = null;
  }

  /**
   * Returns the Scriptduino API if this plugin is enabled. Returns null if this plugin is
   * disabled.
   *
   * @return The Scriptduino API.
   */
  public Scriptduino getScriptduino() {
    return scriptduino_;
  }

  /**
   * Called when a new connection is established.
   *
   * @param connection The new connection.
   * @return True if the plugin has been successfully connected.
   */
  @Override
  public boolean onConnect(Connection connection) {
    scriptduino_service_ = new ScriptduinoService(scriptduino_, connection);
    registerService("ScriptduinoService", scriptduino_service_, "/", true);
    super.onConnect(connection);
    scriptduino_service_.begin();
    return true;
  }

  /**
   * Called when this plug is disabled.
   */
  @Override
  public void onDisable() {
    scriptduino_.stop();
    scriptduino_.close();
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
    if (scriptduino_service_ != null) {
      scriptduino_service_.end();
      scriptduino_service_ = null;
    }
    unregisterAllServices();
    return true;
  }

  /**
   * Called when this plug is enabled.
   *
   * @return True if the plugin can be enabled.
   */
  @Override
  public boolean onEnable() {
    scriptduino_ = new Scriptduino();
    scriptduino_.open();
    return true;
  }

  private Scriptduino scriptduino_;  // Manages communication with the board.
  private ScriptduinoService scriptduino_service_;  // Manages communication with the server.
}
