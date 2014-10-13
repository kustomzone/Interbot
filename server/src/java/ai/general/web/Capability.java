/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents the capability of a user to participate in a particular activity.
 * Capabilities are dependent on the specific clients with which a user is logged in. As the
 * clients become online or offline, the capabilities of a user change.
 *
 * A capability is always associated with a particular activity definition and role. A capability
 * indicates that the user can participate in an activity defined by the activity definition with
 * the specified role.
 */
public class Capability {

  /**
   * Constructs a capability given the name of an activity definition and the name of the role
   * associated with that activity definition.
   *
   * This constructor throws an IllgalArgumentException if the activity definition does not exist
   * or the role is not a role associated with the activity.
   *
   * @param activity_definition The name of the activity definition.
   * @param role The name of the role.
   * @throws IllegalArgumentException If the activity definition or role does no exist.
   */
  public Capability(String activity_definition, String role) {
    this.activity_definition_ =
      ActivityManager.getInstance().getActivityDefinition(activity_definition);
    if (this.activity_definition_ == null) {
      throw new IllegalArgumentException("No such activity definition: " + activity_definition);
    }
    this.role_ = this.activity_definition_.getRole(role);
    if (this.role_ == null) {
      throw new IllegalArgumentException("No such role for activity: " +
                                         activity_definition + "#" + role);
    }
    this.info_ = new CapabilityInfo(activity_definition_.getName(), role_.getName());
  }

  /**
   * Constructs a capability given an activity definition and role.
   *
   * @param activity_definition The definition of the activity that this capability represents.
   * @param role The role with which the user can participate in the activity.
   */
  public Capability(ActivityDefinition activity_definition, Role role) {
    this.activity_definition_ = activity_definition;
    this.role_ = role;
    this.info_ = new CapabilityInfo(activity_definition.getName(), role.getName());
  }

  /**
   * Returns true if the collection of capabilities contains a capability with the specified
   * activity definition and role.
   *
   * @param capabilities Collection of capabilities.
   * @param activity_definition The activity definition to search for.
   * @param role The role associated with the acitivity definition to search for.
   * @return True if capabilities contains a acapability with the activity definition and role.
   */
  public static boolean hasActivityRole(Collection<Capability> capabilities,
                                        ActivityDefinition activity_definition,
                                        Role role) {
    String activity_definition_name = activity_definition.getName();
    String role_name = role.getName();
    for (Capability capability : capabilities) {
      if (capability.activity_definition_.getName().equals(activity_definition_name) &&
          capability.role_.getName().equals(role_name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Given a list of capabilities returns all capabilities with passive roles as
   * {@link CapabilityInfo} objects.
   *
   * The CapabilityInfo objects provide a brief description of the capabilities that can be
   * serialized into JSON.
   *
   * Passive capabilities are capabilities with passive role. A passive role cannot initiate the
   * activity associated with the capability, but a user with a passive role can be invited
   * to join the activity.
   *
   * @param capabilities List of capabilities.
   * @return List of passive capabilities as CapabilityInfo objects.
   */
  public static List<CapabilityInfo> getPassiveCapabilityInfos(
      Collection<Capability> capabilities) {
    ArrayList<CapabilityInfo> capability_infos = new ArrayList<CapabilityInfo>();
    for (Capability capability : capabilities) {
      if (!capability.getRole().isActive()) {
        capability_infos.add(capability.getCapabilityInfo());
      }
    }
    return capability_infos;
  }

  /**
   * Returns the name of this capability.
   * The name of a capability consists of the activity definition name followed by the '#'
   * character followed by the name of the role.
   *
   * @return The name of this capability.
   */
  public String getName() {
    return activity_definition_.getName() + "#" + role_.getName();
  }

  /**
   * Returns the definition of the activity in which the user can participate.
   *
   * @return The definition of the activity in which the user can participate.
   */
  public ActivityDefinition getActivityDefinition() {
    return activity_definition_;
  }

  /**
   * Returns the role with which the user can participate in the activity.
   *
   * @return The role with which the user can participate in the activity.
   */
  public Role getRole() {
    return role_;
  }

  /**
   * Returns a brief description of the capability for serialization into JSON.
   *
   * @return A brief description of the capability.
   */
  public CapabilityInfo getCapabilityInfo() {
    return info_;
  }

  private ActivityDefinition activity_definition_;
  private Role role_;
  private CapabilityInfo info_;
}
