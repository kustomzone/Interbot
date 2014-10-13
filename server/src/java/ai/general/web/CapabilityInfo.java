/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

/**
 * Brief description of the capability that include the activity definition name and the name
 * the role.
 *
 * This description is used for serialziation into and deserialization from JSON.
 */
public class CapabilityInfo {

  /**
   * Creates a default CapabilityInfo.
   */
  public CapabilityInfo() {
    this.activity_definition_name_ = "";
    this.role_name_ = "";
  }

  /**
   * Creates a CapabilityInfo with the specified parameters.
   *
   * @param activity_definition_name The name of the activity definition.
   * @param role_name The name of the role.
   */
  public CapabilityInfo(String activity_definition_name, String role_name) {
    this.activity_definition_name_ = activity_definition_name;
    this.role_name_ = role_name;
  }

  /**
   * Returns the name of the activity definition associated with the capability.
   *
   * @return The name of the activity definition.
   */
  public String getActivity() {
    return activity_definition_name_;
  }

  /**
   * Sets the name of the activity definition associated with the capability.
   *
   * @param activity_definition_name The name of the activity definition.
   */
  public void setActivity(String activity_definition_name) {
    this.activity_definition_name_ = activity_definition_name;
  }

  /**
   * Returns the name of the role associated with the capability.
   *
   * @return The name of the role.
   */
  public String getRole() {
    return role_name_;
  }

  /**
   * Sets the name of the role associated with the capability.
   *
   * @param role_name The name of the role.
   */
  public void setRole(String role_name) {
    this.role_name_ = role_name;
  }

  private String activity_definition_name_;
  private String role_name_;
}
