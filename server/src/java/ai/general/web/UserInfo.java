/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import java.util.HashMap;
import java.util.List;

/**
 * A brief description of a User used for deserialization into JSON.
 */
public class UserInfo {

  /**
   * Constructs a UserInfo for the specified user.
   */
  public UserInfo(UserView user) {
    this.user_ = user;
  }

  /**
   * Returns the username of the user.
   *
   * @return The username.
   */
  public String getUsername() {
    return user_.getUsername();
  }

  /**
   * Returns the user type of the user as a string.
   *
   * @return The user type as a string.
   */
  public String getType() {
    return user_.getUserType().name().toLowerCase();
  }

  /**
   * Returns the status of the user as a string.
   *
   * @return The status of the user as a string.
   */
  public String getStatus() {
    return user_.getStatus().name();
  }

  /**
   * Returns a brief description of the set of capabilities of the user.
   *
   * @return Brief description of the set of capabilities of the user.
   */
  public List<CapabilityInfo> getCapabilities() {
    return user_.getPassiveCapabilities();
  }

  /**
   * Returns a list of user properties.
   *
   * @return List of user properties.
   */
  public HashMap<String, Object> getProperties() {
    return user_.getProperties();
  }

  private UserView user_;
}
