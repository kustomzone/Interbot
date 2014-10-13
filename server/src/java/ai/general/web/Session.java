/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Represents a login session associated with a user. A particular user may be logged in with
 * multiple sessions at the same time.
 *
 * A users may participate in one or more activites via a session.
 */
public class Session {

  /**
   * Creates a session with the specified session ID for the specified user and client type.
   * The client type is the type of client with which the user is logged in for this session.
   *
   * @param user_ The user with which this session is associated with.
   * @param session_id The session ID of the session.
   * @param client_type The type of client.
   */
  public Session(User user, String session_id, ClientType client_type) {
    this.user_ = user;
    this.session_id_ = session_id;
    this.client_type_ = client_type;
    this.activities_ = new HashMap<String, Participant>();
    this.pong_count_ = 0;
  }

  /**
   * Given a collection of sessions returns the first session which has the specified client type.
   * Returns null if none of the sessions have the specified client type.
   *
   * @param session A collection of sessions.
   * @param client_type The client type to search for.
   * @return The first session with the specified client type or null.
   */
  public static Session findSession(Collection<Session> sessions, ClientType client_type) {
    for (Session session : sessions) {
      if (session.client_type_ == client_type) {
        return session;
      }
    }
    return null;
  }

  /**
   * Returns the user with which this session is associated with.
   */
  public User getUser() {
    return user_;
  }

  /**
   * Returns the session ID of the session.
   *
   * @return The session ID.
   */
  public String getSessionId() {
    return session_id_;
  }

  /**
   * Returns the type of client with which the user has logged in.
   *
   * @return The client type.
   */
  public ClientType getClientType() {
    return client_type_;
  }

  /**
   * Registers the participation in an activity with this session.
   *
   * @param participant The participant associated with this session and the activity.
   */
  public synchronized void registerActivity(Participant participant) {
    activities_.put(participant.getParticipantId(), participant);
  }

  /**
   * Unregisters the participation in an activity from this session.
   * For each call to {@link registerActivity(String)} there must be a corresponding call to this
   * method with the same participant instance.
   */
  public synchronized void unregisterActivity(Participant participant) {
    activities_.remove(participant.getParticipantId());
  }

  /**
   * Returns the particpation in an activity in which this session participates with the
   * specified participant ID.
   * If the participant ID is not valid, returns null.
   *
   * @param participant_id The participant ID for the activity.
   * @return The participant object or null.
   */
  public Participant getActivityParticipant(String participant_id) {
    return activities_.get(participant_id);
  }

  /**
   * Returns the list of all activity participations this session participates in.
   *
   * @return All participations of all activities this session participates in.
   */
  public Collection<Participant> getActivityParticipations() {
    return activities_.values();
  }

  /**
   * Causes this session to exit all activities in which it participates.
   */
  public synchronized void exitAllActivities() {
    if (activities_.size() == 0) return;
    log.debug("({}/{}) exit all from {} activities",
              user_.getUsername(), session_id_, activities_.size());
    ArrayList<Participant> activities = new ArrayList<Participant>();
    activities.addAll(activities_.values());
    for (Participant participant : activities) {
      participant.exitActivity();
    }
  }

  /**
   * Returns the current number of session pongs.
   *
   * @return Session pong count.
   */
  public int getPongCount() {
    return pong_count_;
  }

  /**
   * Updates the pong count.
   *
   * @param pong_count The new pong count.
   */
  public void setPongCount(int pong_count) {
    this.pong_count_ = pong_count;
  }

  private static Logger log = LogManager.getLogger();

  private User user_;
  private String session_id_;
  private ClientType client_type_;
  private HashMap<String, Participant> activities_;
  private int pong_count_;
}
