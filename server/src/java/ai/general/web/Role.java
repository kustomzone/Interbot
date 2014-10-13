/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

/**
 * Represents a role of a user in an activity.
 *
 * An activity can have multiple participants and each participants has one or more specific roles
 * in that activity.
 *
 * A role can be either active or passive. An active role can initiates an activity, while a
 * passive role can only participate in an activity when it is invited.
 */
public class Role {

  /**
   * Constructs a role with a the specified name.
   * An active role can initiate an activity, while a passive role must be invited.
   *
   * @param name The name of the role.
   * @param active Whether this role can initiate the activity.
   */
  public Role(String name, boolean active) {
    this.name_ = name;
    this.active_ = active;
  }

  /**
   * Returns the name of the role.
   *
   * @return The name of the role.
   */
  public String getName() {
    return name_;
  }

  /**
   * Returns true if this role can initiate the activity.
   *
   * @return True if this role can initiate the activity.
   */
  public boolean isActive() {
    return active_;
  }

  private String name_;
  private boolean active_;
}
