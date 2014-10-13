/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Read-only user interface that allows querying public user information.
 */
public interface UserView {

  /**
   * Returns the username of the user.
   *
   * @return The username.
   */
  String getUsername();

  /**
   * Returns the user type of the user.
   *
   * @return The user type.
   */
  User.UserType getUserType();

  /**
   * Returns the current status of the user.
   *
   * @return The current user status.
   */
  User.Status getStatus();

  /**
   * Returns the current list of sessions associated with this user.
   *
   * @return The list of sessions associated with this user.
   */
  List<Session> getSessions();

  /**
   * Returns true if the user has the capability to assume the specified role in activities
   * with the specified name.
   *
   * @param activity_name The name of the activity definition.
   * @param role_name The name of the role.
   */
  boolean hasCapability(String activity_name, String role_name);

  /**
   * Returns the set of all capabilities of the user. The set of capabilities depend on the
   * clients with which the user is logged in and may change over the lifetime of the user.
   *
   * @return The set of all capabilities of the user.
   */
  List<Capability> getCapabilities();

  /**
   * Returns the set of passive capabilities of the user. The set of capabilities depend on the
   * clients with which the user is logged in and may change over the lifetime of the user.
   *
   * The passive capabilities are returned in brief form as {@link CapabilityInfo} objects that
   * can be serialized into JSON.
   *
   * @return Brief description of the set of passive capabilities of the user.
   */
  List<CapabilityInfo> getPassiveCapabilities();

  /**
   * Returns a list of all activities that this user currently participates in.
   * The list specifies the participation object in each user. If the user participates in an
   * activity multiple times, returns a participation object for each participation in the
   * activity.
   *
   * @return The list of all activities this user currently participates in.
   */
  List<Participant> getAllActivityParticipations();

  /**
   * Returns the list of pending activity invitations this user has received.
   * The end-user associated with this user account is expecte to reply to these invitations.
   *
   * @return The list of pending activity invitations received by this user.
   */
  InvitationList getPendingInvitations();

  /**
   * Returns user specific properties. The returned property map is keyed by property name and
   * a property specific value. The value may be null.
   *
   * @return The user properties.
   */
  HashMap<String, Object> getProperties();

  /**
   * Registers a follower of this user. The follower will received events related to this user.
   *
   * @param follower The UserEventListener to register.
   */
  void follow(UserEventListener follower);

  /**
   * Unregisters a previously registered follower.
   *
   * @param follower The UserEventListener to unregister.
   */
  void unfollow(UserEventListener follower);

  /**
   * Triggers a user event. All subscribers of the user event of this user will be notified about
   * the event. Typically, the subscribers will be remote sessions subscribed via WAMP pub/sub.
   *
   * @param event The event to trigger.
   */
  void userEvent(UserEvent event);

  /**
   * Returns a JSON representation of this user.
   *
   * @return JSON representation of user.
   */
  String toJson();
}
