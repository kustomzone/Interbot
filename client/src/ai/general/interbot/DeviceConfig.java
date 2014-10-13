/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot;

import java.util.ArrayList;

/**
 * Defines devices attached to the robot and their configuration options.
 * The device configuration is stored in a JSON file and deserialized from that file.
 * The device configuration is also sent to the server in JSON format.
 */
public class DeviceConfig {

  private static final String kConfigFilename = "device_config";

  /**
   * Represents a device in a device config.
   */
  public static class Device {

    /**
     * Constructs a default device with no name.
     */
    public Device() {
      this.name_ = "";
    }

    /**
     * Returns the name of the device.
     *
     * @return The name of the device.
     */
    public String getName() {
      return name_;
    }

    /**
     * Sets the name of the device.
     *
     * @param name The name of the device.
     */
    public void setName(String name) {
      this.name_ = name;
    }

    private String name_;  // The device name.
  }

  /**
   * Constructs an empty device config.
   */
  public DeviceConfig() {
    devices_ = new ArrayList<Device>();
  }

  /**
   * Loads the device configuration for the current context.
   * Returns null if the configuration cannot be loaded.
   *
   * @return The device config or null.
   */
  public static DeviceConfig load() {
    return ConfigFiles.Instance.load(kConfigFilename, DeviceConfig.class);
  }

  /**
   * Returns a list of devices.
   *
   * @return The list of devices.
   */
  public ArrayList<Device> getDevices() {
    return devices_;
  }

  /**
   * Sets the list of devices.
   *
   * @param devices The list of devices.
   */
  public void setDevices(ArrayList<Device> devices) {
    this.devices_ = devices;
  }

  private ArrayList<Device> devices_;  // The list of devices.
}
