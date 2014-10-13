/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot.video;

import ai.general.interbot.ConfigFiles;

import java.util.ArrayList;

/**
 * IP camera configuration options.
 * The IP camera configuration is stored in a JSON file and deserialzed from that file.
 */
public class IpCameraConfig {

  private static final String kConfigFilename = "ip_camera_config";

  /**
   * Constructs an empty IpCameraConfig.
   */
  public IpCameraConfig() {
    ip_address_ = "";
    port_ = 80;
    videostream_url_ = "";
    username_ = "";
    password_ = "";
    frame_rate_ = 0.0;
    init_instructions_ = new ArrayList<String>();
    pan_tilt_ = new PanTiltConfig();
  }

  /**
   * Loads the IP camera configuration for the current context.
   * Returns null if the IP camera configuration cannot be loaded.
   *
   * @return The IP Camera configuration or null.
   */
  public static IpCameraConfig load() {
    return ConfigFiles.Instance.load(kConfigFilename, IpCameraConfig.class);
  }

  /**
   * Returns the frame rate at which frames from the camera should be streamed to the server.
   * The IP Camera driver reads all frames produced by the camera, but frames will be streamed
   * to the server at this rate. This frame rate should not exceed the maximum frame rate of
   * the camera.
   *
   * @return The frame rate in Hz.
   */
  public double getFrameRate() {
    return frame_rate_;
  }

  /**
   * Returns a sequence of instructions to be executed during initialization of the camera.
   * The IP camera driver applies these instructions to the camera before any frames are read.
   * The instructions are HTTP get requests.
   *
   * @return The sequence of initialization instructions.
   */
  public ArrayList<String> getInitInstructions() {
    return init_instructions_;
  }

  /**
   * Returns the IP address of the IP camera.
   *
   * @return The string form of the IP address of the IP camera.
   */
  public String getIpAddress() {
    return ip_address_;
  }

  /**
   * Returns the configuration for the pan-tilt unit.
   *
   * @return The configuration for the pan-tilt unit.
   */
  public PanTiltConfig getPanTilt() {
    return pan_tilt_;
  }

  /**
   * Returns the password required to login into the IP camera.
   *
   * @return The password required to login into the IP camera.
   */
  public String getPassword() {
    return password_;
  }

  /**
   * Returns the TCP port of the IP camera.
   *
   * @return The TCP port of the IP camera.
   */
  public int getPort() {
    return port_;
  }

  /**
   * Returns the username required to login into the IP camera.
   *
   * @return The username required to login into the IP camera.
   */
  public String getUsername() {
    return username_;
  }

  /**
   * Returns the URL path of the video stream on the IP camera.
   *
   * @return The URL path of the video stream on the IP camera.
   */
  public String getVideostreamUrl() {
    return videostream_url_;
  }

  /**
   * Sets the frame rate at which frames from the camera should be streamed to the server.
   * The IP Camera driver reads all frames produced by the camera, but frames will be streamed
   * to the server at this rate. This frame rate should not exceed the maximum frame rate of
   * the camera.
   *
   * @param frame_rate The frame rate in Hz.
   */
  public void setFrameRate(double frame_rate) {
    this.frame_rate_ = frame_rate;
  }

  /**
   * Sets the sequence of instructions to be executed during initialization of the camera.
   * The IP camera driver applies these instructions to the camera before any frames are read.
   * The instructions are HTTP get requests.
   *
   * @param init_instructions The sequence of initialization instructions.
   */
  public void setInitInstructions(ArrayList<String> init_instructions) {
    this.init_instructions_ = init_instructions;
  }

  /**
   * Sets the IP address of the IP camera.
   *
   * @param ip_address The string form of the IP address of the IP camera.
   */
  public void setIpAddress(String ip_address) {
    this.ip_address_ = ip_address;
  }

  /**
   * Sets the configuration for the pan-tilt unit.
   *
   * @param pan_tilt The configuration for the pan-tilt unit.
   */
  public void setPanTilt(PanTiltConfig pan_tilt) {
    this.pan_tilt_ = pan_tilt;
  }

  /**
   * Sets the password required to login into the IP camera.
   *
   * @param password The password required to login into the IP camera.
   */
  public void setPassword(String password) {
    this.password_ = password;
  }

  /**
   * Sets the TCP port of the IP camera.
   *
   * @param port The TCP port of the IP camera.
   */
  public void setPort(int port) {
    this.port_ = port;
  }

  /**
   * Sets the username required to login into the IP camera.
   *
   * @param username The username required to login into the IP camera.
   */
  public void setUsername(String username) {
    this.username_ = username;
  }

  /**
   * Sets the URL path of the video stream on the IP camera.
   *
   * @param videostream_url The URL path of the video stream on the IP camera.
   */
  public void setVideostreamUrl(String videostream_url) {
    this.videostream_url_ = videostream_url;
  }

  private double frame_rate_;  // Frame rate at which to stream to the server.
  private ArrayList<String> init_instructions_;  // IP camera initialization commands.
  private String ip_address_;  // IP address of IP camera.
  private PanTiltConfig pan_tilt_;  // Pan-tilt configuration.
  private String password_;  // Password to log into IP camera.
  private int port_;  // TCP port of IP camera.
  private String username_;  // Username to log into IP camera.
  private String videostream_url_;  // URL Path of the video stream on the IP camera.
}
