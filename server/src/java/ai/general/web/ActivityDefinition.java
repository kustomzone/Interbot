/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import java.util.HashMap;

/**
 * Defines an Activity. An Activity represents an action taken by one or more users that lasts
 * for a period of time. Users can take on a role in an activity and participate via one of the
 * sessions with which they are logged in. A user can participate in multiple activities at the
 * same time.
 *
 * ActivityDefinition defines the parameters of an activity. Specific activities are created
 * based on an activity definition.
 */
public class ActivityDefinition {

  /**
   * Constructs an activity definition with a the specified name, but no roles.
   * Roles must be added via the {@link #addRole(Role)} method.
   *
   * @param name The name of the activity definition.
   */
  public ActivityDefinition(String name) {
    this.name_ = name;
    this.roles_ = new HashMap<String, Role>();
  }

  /**
   * Returns the name of the activity definition.
   *
   * @return The name of the activity definition.
   */
  public String getName() {
    return name_;
  }

  /**
   * Adds a role to the activity definition.
   * Each role must have a unique name.
   *
   * @param role The role to add to the activity definition.
   */
  public void addRole(Role role) {
    roles_.put(role.getName(), role);
  }

  /**
   * Returns true if the activity definition contains has a role with the specified name.
   *
   * @param name The name of the role.
   * @return True if the activity definition contains a role with the specified name.
   */
  public boolean hasRole(String name) {
    return roles_.containsKey(name);
  }

  /**
   * Returns the role with the specified name or null if there is no such role.
   *
   * @param name The name of the role.
   * @return The role with the specified name or null if no such role exists.
   */
  public Role getRole(String name) {
    return roles_.get(name);
  }

  /**
   * Creates an activity based on this activity definition. This method may be overriden
   * by subclasses to create specific Activities.
   *
   * @return An activity based on this activity definition.
   */
  public Activity createActivity() {
    return new Activity(this);
  }

  private String name_;
  private HashMap<String, Role> roles_;
}
