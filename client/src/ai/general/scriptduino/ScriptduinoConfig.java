/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.scriptduino;

import ai.general.interbot.ConfigFiles;

/**
 * Represents Scriptduino configuration.
 * The configuration is loaded and deserialized from a JSON file.
 */
public class ScriptduinoConfig {

  private static final String kConfigFilename = "scriptduino-config";

  /**
   * Initializes an empty Scriptduino config.
   */
  public ScriptduinoConfig() {
    this.serial_port_ = "";
  }

  /**
   * Loads the Scriptduino config for the current context.
   * Returns null if the configuration cannot be loaded.
   *
   * @return The Scriptduino config or null.
   */
  public static ScriptduinoConfig load() {
    return ConfigFiles.Instance.load(kConfigFilename, ScriptduinoConfig.class);
  }

  /**
   * Returns the name of the serial port.
   *
   * @return The serial port.
   */
  public String getSerialPort() {
    return serial_port_;
  }

  /**
   * Saves the Scriptduino config by updating the file from which the config was loaded.
   *
   * @return True if the configuration was successfully saved.
   */
  public boolean save() {
    return ConfigFiles.Instance.save(kConfigFilename, this);
  }

  /**
   * Sets the serial port.
   *
   * @param serial_port The serial port.
   */
  public void setSerialPort(String serial_port) {
    this.serial_port_ = serial_port;
  }

  private String serial_port_;  // Device name of the serial port.
}
