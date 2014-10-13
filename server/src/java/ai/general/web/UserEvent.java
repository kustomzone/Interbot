/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Represents a user event. A user event signals a change in state of a user. The particular
 * change is specified by the event type and the event data.
 * User events are always associated with a specific user.
 */
public class UserEvent {

  /**
   * User event types.
   */
  public enum Type {
    /** An undefined event type. Used in deserialization. */
    Undefined,

    /**
     * User is logged out by system.
     * Parameters: none.
     */
    SystemLogout,

    /**
     * User status updated.
     *
     * Parameters:
     * [0] string status: Name of new status.
     */
    StatusUpdate,

    /**
     * A capability has become available or unavailable.
     * Parameters:
     * [..] array capabilities: Array of all passive capabilities.
     */
    CapabilityUpdate,

    /**
     * The properties of a user have been updated.
     * Parameter:
     * [0] object properties: The map of user properties.
     */
    PropertyUpdate,

    /**
     * An invitation by another user to join an activity.
     *
     * Parameters:
     * [0] string activity: The name of the activity the user is invited to.
     * [1] string role: The name of the role in the activity the user is invited to.
     * [2] number invitation_id: The invitation ID to be used in replies.
     * [3] object extra: invitation dependent extra data or null
     *
     * ActivityInvitation events require that the user responds with an invitationReply RPC call.
     */
    ActivityInvitation,

    /**
     * A reply to a pending invitation sent to another user.
     *
     * Parameters:
     * [0] number invitation_id: The invitation ID of the invitation.
     * [1] boolean accepted: Whether the invitation was accepted.
     * [2] object extra: invitation dependent extra data or null
     */
    InvitationReply,

    /**
     * Sent to cancel an invitation sent by an ActivityInvitation event.
     *
     * Parameters:
     * [0] number invitation_id: The invitation ID sent with the ActivityInvitation event.
     */
    CancelInvitation,

    /**
     * Another user has joined an activity in which this user is a participant or this user has
     * been joined to an activity by the server.
     *
     * Parameters:
     * [0] number activity_id: The ID of the activity.
     * [1] string role: The name of the role with which the user joins the activity.
     * [2] number participant_id: The participant ID of the joining user session.
     */
    JoinActivity,

    /**
     * Another user has exited an activity in which this user is a participant.
     *
     * Parameters:
     * [0] number activitiy_id: The activity ID of the activity the user is exiting.
     * [1] number participant_id: The participant ID in the activity the user is exiting.
     */
    ExitActivity,
  }

  /**
   * Constructs an unitialized user event. All fields must be explicitly initialzed.
   * Specific UserEvents should be constructed by using of the static methods below.
   */
  public UserEvent() {
    username_ = "";
    type_ = Type.Undefined;
    data_ = new ArrayList<Object>();
  }

  /**
   * Constructs an event with the specified parameters.
   * This is an internal constructor. Specific UserEvents should be constructed by using one of
   * the static methods below.
   *
   * @param username The username of the user associated with this event.
   * @param type The event type.
   * @param data The event data.
   */
  public UserEvent(String username, Type type, Object ... data) {
    this.username_ = username;
    this.type_ = type;
    data_ = new ArrayList<Object>();
    for (Object element : data) {
      data_.add(element);
    }
  }

  /**
   * Returns a system logout event.
   *
   * @param user The user who is logged out by the system.
   * @return A system logout event.
   */
  public static UserEvent systemLogout(UserView user) {
    return new UserEvent(user.getUsername(), Type.SystemLogout);
  }

  /**
   * Returns a status update event.
   *
   * @param user The user whose status is updated.
   * @param status The updated status.
   * @return A status update event.
   */
  public static UserEvent statusUpdate(UserView user, User.Status status) {
    return new UserEvent(user.getUsername(), Type.StatusUpdate, status.name());
  }

  /**
   * Returns a capability update event.
   *
   * @param user The user whose capabilities are updated.
   * @param capabilities The updated collection of all passive capabilities of the user.
   * @return A capability update event.
   */
  public static UserEvent capabilityUpdate(UserView user,
                                           Collection<CapabilityInfo> capabilities) {
    UserEvent event = new UserEvent(user.getUsername(), Type.CapabilityUpdate);
    event.data_.addAll(capabilities);
    return event;
  }

  /**
   * Returns a property update event.
   *
   * @param user The user whose properties have been updated.
   * @param properties The updated properties.
   * @return A property update event.
   */
  public static UserEvent propertyUpdate(UserView user, Map<String, Object> properties) {
    return new UserEvent(user.getUsername(), Type.PropertyUpdate, properties);
  }

  /**
   * Returns an activity invitation event.
   *
   * @param invitation The invitation.
   * @return An activity invitation event.
   */
  public static UserEvent activityInvitation(Invitation invitation) {
    return new UserEvent(invitation.getInviter().getUser().getUsername(),
                         Type.ActivityInvitation,
                         invitation.getActivity().getActivityDefinition().getName(),
                         invitation.getRole().getName(),
                         invitation.getInvitationId(),
                         invitation.getExtra());
  }

  /**
   * Returns an invitation reply event.
   *
   * @param invitation The invitation.
   * @param accepted Whether the invitation was accepted.
   * @return An invitation reply event.
   */
  public static UserEvent invitationReply(Invitation invitation, boolean accepted) {
    return new UserEvent(invitation.getUser().getUsername(),
                         Type.InvitationReply,
                         invitation.getInvitationId(),
                         accepted,
                         invitation.getExtra());
  }

  /**
   * Returns a cancel invitation event.
   *
   * @param invitation The invitation that was cancelled.
   * @return A cancel invitation event.
   */
  public static UserEvent cancelInvitation(Invitation invitation) {
    return new UserEvent(invitation.getInviter().getUser().getUsername(),
                         Type.CancelInvitation,
                         invitation.getInvitationId());
  }

  /**
   * Returns a join activity event.
   *
   * @param participant The participant who is joining the activity.
   * @return A join activity event.
   */
  public static UserEvent joinActivity(Participant participant) {
    return new UserEvent(participant.getUser().getUsername(),
                         Type.JoinActivity,
                         participant.getActivity().getActivityId(),
                         participant.getRole().getName(),
                         participant.getParticipantId());
  }

  /**
   * Returns an exit activity event.
   *
   * @param participant The participant who is exiting the activity.
   * @return An exit activity event.
   */
  public static UserEvent exitActivity(Participant participant) {
    return new UserEvent(participant.getUser().getUsername(),
                         Type.ExitActivity,
                         participant.getActivity().getActivityId(),
                         participant.getParticipantId());
  }

  /**
   * Returns the username of the user associated with this event.
   *
   * @return The username.
   */
  public String getUsername() {
    return username_;
  }

  /**
   * Updates the username.
   *
   * @param username The username of the user associated with this event.
   */
  public void setUsername(String username) {
    this.username_ = username;
  }

  /**
   * Returns the event type.
   *
   * @return The event type.
   */
  public Type getType() {
    return type_;
  }

  /**
   * Sets the event type from the specified type name. The type name must be the string value
   * of one the enum values in Type. If an invalid name is specified, the event type will be
   * set to Undefined.
   *
   * This method allows safe deserialization from JSON.
   *
   * @param type The event type.
   */
  public void setType(String type_name) {
    try {
      this.type_ = Enum.valueOf(Type.class, type_name);
    } catch (IllegalArgumentException e) {
      this.type_ = Type.Undefined;
    }
  }

  /**
   * The event data array.
   *
   * @return The event data array.
   */
  public ArrayList<Object> getData() {
    return data_;
  }

  /**
   * Adds the specified data to the event.
   *
   * @param The data to be added to the event.
   */
  public void addData(Object data) {
    data_.add(data);
  }

  private String username_;
  private Type type_;
  private ArrayList<Object> data_;
}
