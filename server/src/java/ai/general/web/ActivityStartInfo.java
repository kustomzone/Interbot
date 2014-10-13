/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

/**
 * Serializable information sent to a user when an activity is started or joined.
 * The activity start information includes the activity id and participant id.
 */
public class ActivityStartInfo {

  /**
   * Creates an ActivityStartInfo with the specified parameters.
   *
   * @param activity_id The activity ID.
   * @param participant_id The participant ID.
   */
  public ActivityStartInfo(String activity_id, String participant_id) {
    this.activity_id_ = activity_id;
    this.participant_id_ = participant_id;
  }

  /**
   * Returns the activity ID.
   *
   * @return The activity ID.
   */
  public String getActivityId() {
    return activity_id_;
  }

  /**
   * Returns the participant ID.
   *
   * @return The participant ID.
   */
  public String getParticipantId() {
    return participant_id_;
  }

  private String activity_id_;
  private String participant_id_;
}
