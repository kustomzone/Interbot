/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import ai.general.common.RandomString;

import java.util.Collection;
import java.util.HashMap;

/**
 * Represents an activity. An activity is an action or interaction that one or more users can
 * engage in. Each user participates with a specific role in the activity. Users can join and
 * exit an activity during its life time. A user may be involved in multiple activities at
 * the same time.
 *
 * Activites are based on an {@link ActivityDefinition}.
 */
public class Activity {

  /**
   * Constructs an activity based on the specified activity definition.
   *
   * @param activity_definition The definition of the activity.
   */
  public Activity(ActivityDefinition activity_definition) {
    this.activity_id_ = createActivityId();
    this.activity_definition_ = activity_definition;
    this.participants_ = new HashMap<String, Participant>();
    this.participant_id_count_ = 0;
  }

  /**
   * Creates a unique activity ID.
   *
   * @return A unique activity ID.
   */
  private synchronized static String createActivityId() {
    activity_count++;
    return RandomString.nextString(12) + activity_count;
  }

  /**
   * Returns the unique ID of this activity.
   *
   * @return The unique activity ID.
   */
  public String getActivityId() {
    return activity_id_;
  }

  /**
   * Returns the definition of this activity.
   *
   * @return The definition of the activity.
   */
  public ActivityDefinition getActivityDefinition() {
    return activity_definition_;
  }

  /**
   * Returns the number of participants in the activity.
   *
   * @return The number of participants.
   */
  public int countParticipants() {
    return participants_.size();
  }

  /**
   * Called by a user to join an activity via a session associated with the user.
   * The user joins the activity with the specified role.
   *
   * This method returns a participant object with which the participant is identified.
   * Participant ID's are unique accross all activities.
   * Each join results in a different participant object, such that in principal a session can join
   * an activity with the same role multiple times.
   *
   * After joining an activity, the user must call {@link #exit(String)} with the participant
   * object returned by this method in order to exit the activity.
   *
   * This method also adds the participation of this activity to the session.
   *
   * @param session The session that participates in the activity.
   * @param role The role with which the user joins the activity.
   * @return The participant object.
   */
  public synchronized Participant join(Session session, Role role) {
    participant_id_count_++;
    String participant_id = activity_id_ + "#" + participant_id_count_;
    Participant participant = new Participant(participant_id, this, session, role);
    String username = participant.getUser().getUsername();
    for (Participant current_participant : participants_.values()) {
      current_participant.getUser().userEvent(UserEvent.joinActivity(participant));
    }
    participants_.put(participant_id, participant);
    session.registerActivity(participant);
    return participant;
  }

  /**
   * Called by a user to stop participating in an activity.
   * The participant is returned by a previous call to {@link #join(Session, Role)}.
   *
   * This method removes the specified participant from this activity and unregisters
   * the participation from from the session of the participant.
   *
   * @param participant The participant who exits the activity.
   * @return True if the participant was a member of the activity and was removed.
   */
  public synchronized boolean exit(Participant participant) {
    if (participants_.remove(participant.getParticipantId()) == null) {
      return false;
    }
    participant.getSession().unregisterActivity(participant);
    String username = participant.getUser().getUsername();
    for (Participant remaining_participant : participants_.values()) {
      remaining_participant.getUser().userEvent(UserEvent.exitActivity(participant));
    }
    return true;
  }

  /**
   * Invites a user to join this activity.
   * By default, this method rejects the invitation. Specific subclasses must imlement
   * invitation acceptance logic that is appropriate for the specific activity.
   *
   * The inviter must be already a participant in this activity.
   *
   * @param inviter The participant that invites the user.
   * @param user The user who is invited to join the activity.
   * @param role The role into which the user is invited.
   * @return The invitation result.
   */
  public InvitationResult invite(Participant inviter, UserView user, Role role) {
    return InvitationResult.reject("activity not supported");
  }

  /**
   * Returns the current collection of participants.
   *
   * @return The current participants.
   */
  public Collection<Participant> getParticipants() {
    return participants_.values();
  }

  private static int activity_count = 0;

  private String activity_id_;
  private ActivityDefinition activity_definition_;
  private HashMap<String, Participant> participants_;
  private int participant_id_count_;
}
