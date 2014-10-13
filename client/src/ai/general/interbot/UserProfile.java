/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot;

/**
 * Defines a user profile that is used to log into an Interbot server.
 */
public class UserProfile {

  /**
   * Creates an empty user profile.
   */
  public UserProfile() {
    name_ = "";
    username_ = "";
    key_ = "";
  }

  /**
   * Creates a user profile with the specified parameters.
   * The key is a hashed version of the user password.
   *
   * @param profile_name The name of the user profile.
   * @param username The username associated with the profile.
   * @param key The API key associated with the username.
   */
  public UserProfile(String profile_name, String username, String key) {
    this.name_ = profile_name;
    this.username_ = username;
    this.key_ = key;
  }

  /**
   * Returns the API key for the username.
   *
   * @return The API key.
   */
  public String getKey() {
    return key_;
  }

  /**
   * The unique profile name. This name can be displayed to the user or used in command
   * line arguments.
   *
   * @return The profile name.
   */
  public String getName() {
    return name_;
  }

  /**
   * Returns the username used to log into the Interbot server.
   *
   * @return The username.
   */
  public String getUsername() {
    return username_;
  }

  /**
   * Sets the API key for the username..
   *
   * @param key The API key.
   */
  public void setKey(String key) {
    this.key_ = key;
  }

  /**
   * Sets the profile name. Each profile must have a unique name.
   *
   * @param name The profile name.
   */
  public void setName(String name) {
    this.name_ = name;
  }

  /**
   * Sets the username used to log into the Interbot server.
   *
   * @param username The username.
   */
  public void setUsername(String username) {
    this.username_ = username;
  }

  private String key_;  // Hashed user password.
  private String name_;  // The unique profile name.
  private String username_;  // The username used to log into the server.
}
