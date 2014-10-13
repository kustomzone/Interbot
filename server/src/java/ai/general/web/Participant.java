/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import java.util.Collection;

/**
 * Represents a participant in an activity. A particular activity can have multiple participants.
 *
 * A parcipant is associated with a particular user session and role. The participant assumes the
 * specified role in the activity.
 */
public class Participant {

  /**
   * Creates a participant with the specified session and role.
   *
   * @param participant_id A unique ID for the participant.
   * @param activity The activity in which the user participates.
   * @param session The session with which the user participates in the activity.
   * @param role The role with which the user participates in the activity.
   */
  public Participant(String participant_id, Activity activity, Session session, Role role) {
    this.participant_id_ = participant_id;
    this.activity_ = activity;
    this.session_ = session;
    this.role_ = role;
    this.pending_invitations_ = new InvitationList();
  }

  /**
   * Returns the first participant with the specified role.
   * Returns null if none of the participants have the specified role.
   *
   * @param participants Collection of participants.
   * @param role Role to search for.
   * @return The first participant who has the specified role or null.
   */
  public static Participant getFirstWithRole(Collection<Participant> participants, Role role) {
    String role_name = role.getName();
    for (Participant participant : participants) {
      if (participant.role_.getName().equals(role_name)) {
        return participant;
      }
    }
    return null;
  }

  /**
   * Returns true if any of the participants has the specified role.
   *
   * @param participants Collection of participants.
   * @param role Role to search for.
   * @return True if any of the participants has the specified role.
   */
  public static boolean hasRole(Collection<Participant> participants, Role role) {
    return getFirstWithRole(participants, role) != null;
  }

  /**
   * Returns true if any of the participants is participating in an activity based on the specified
   * activity definition.
   *
   * @param participants Collection of participants.
   * @param activity_definition The definition of the activity to search for.
   * @return True if any of the participants is participating in the specified activity.
   */
  public static boolean hasActivity(Collection<Participant> participants,
                                    ActivityDefinition activity_definition) {
    for (Participant participant : participants) {
      if (participant.getActivity().getActivityDefinition() == activity_definition) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the ID of this participant. The ID is unique accross all activities.
   */
  public String getParticipantId() {
    return participant_id_;
  }

  /**
   * Returns the activity in which this user participates.
   *
   * @return The participated activity.
   */
  public Activity getActivity() {
    return activity_;
  }

  /**
   * Returns the session with which the user participates in the activity.
   *
   * @return The session with which the user participates in the activity.
   */
  public Session getSession() {
    return session_;
  }

  /**
   * Returns the user that participates in the activity.
   *
   * @return The user that participates in the activity.
   */
  public User getUser() {
    return session_.getUser();
  }

  /**
   * Returns the role with which the user participates in the activity.
   *
   * @return The role with which the user participates in the activity.
   */
  public Role getRole() {
    return role_;
  }

  /**
   * Exits the activity in which this participant participates.
   */
  public void exitActivity() {
    pending_invitations_.cancelAll();
    activity_.exit(this);
  }

  /**
   * Returns the list of pending invitations that have been initated by this participant.
   */
  public InvitationList getPendingInvitations() {
    return pending_invitations_;
  }

  private String participant_id_;
  private Activity activity_;
  private Session session_;
  private Role role_;
  private InvitationList pending_invitations_;
}
