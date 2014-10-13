/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import ai.general.directory.Directory;
import ai.general.directory.Request;
import ai.general.net.Uri;
import ai.general.plugin.annotation.RpcMethod;
import ai.general.plugin.annotation.Subscribe;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base class for all user types.
 * User implements methods common to all Users.
 */
public abstract class User implements UserView, UserEventListener {

  /** Maximum number of pings the client can miss until it is counted as stale. */
  private static final int kMaxMissedPings = 2;

  /**
   * The User types. Each type corresponds to a subclass of User.
   */
  public enum UserType {
    Admin,
    Human,
    Robot,
  }

  /**
   * The User status values.
   */
  public enum Status {
    /** User is not logged in. */
    Offline,

    /** User is logged in and can receive calls. */
    Online,
  }

  /**
   * @param username The unique username of the User.
   */
  public User(String username) {
    this.username_ = username;
    this.user_home_path_ = UserUris.userHomePath(username);
    this.session_ping_uri_ = UserUris.createEventUri(username, UserUris.kSessionPingTopic);
    this.user_event_uri_ = UserUris.createEventUri(username, UserUris.kUserEventTopic);
    this.status_ = Status.Offline;
    this.sessions_ = new ArrayList<Session>();
    this.num_wamp_connections_ = 0;
    this.friends_ = null;
    this.followers_ = new ArrayList<UserEventListener>();
    this.p2p_topic_ = null;
    this.json_generator_ = new ObjectMapper();
    this.pending_invitations_ = new InvitationList();
    this.properties_ = new HashMap<String, Object>();
  }

  /**
   * Returns the username.
   *
   * @return The username.
   */
  @Override
  public String getUsername() {
    return username_;
  }

  /**
   * Returns the user type.
   * Implemeted by specific User subclasses.
   *
   * @return The user type.
   */
  @Override
  public abstract UserType getUserType();

  /**
   * Returns the current status of the user.
   *
   * @return The current user status.
   */
  @Override
  public Status getStatus() {
    return status_;
  }

  /**
   * Returns the current list of sessions associated with this user.
   *
   * @return The list of sessions associated with this user.
   */
  public List<Session> getSessions() {
    return sessions_;
  }

  /**
   * Returns true if the user has the capability to assume the specified role in activities
   * with the specified name.
   *
   * @param activity_name The name of the activity definition.
   * @param role_name The name of the role.
   */
  @Override
  public boolean hasCapability(String activity_name, String role_name) {
    for (Capability capability : getCapabilities()) {
      if (capability.getActivityDefinition().getName().equals(activity_name) &&
          capability.getRole().getName().equals(role_name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the set of all capabilities of the user. The set of capabilities depend on the
   * clients with which the user is logged in and may change over the lifetime of the user.
   *
   * @return The set of all capabilities of the user.
   */
  @Override
  public List<Capability> getCapabilities() {
    List<Capability> capabilities = new ArrayList<Capability>();
    for (Session session : sessions_) {
      capabilities.addAll(session.getClientType().getCapabilities());
    }

    // Tentative code to add capabilities from properties.
    if (properties_.containsKey(SystemProperty.Devices.name())) {
      @SuppressWarnings("unchecked") ArrayList<HashMap<String, Object>> devices =
        (ArrayList<HashMap<String, Object>>) properties_.get(SystemProperty.Devices.name());
      for (HashMap<String, Object> device : devices) {
        String device_name = (String) device.get("name");
        if (device_name != null && device_name.equals("ipcamera")) {
          capabilities.add(new Capability(
              ai.general.web.video.VideoStreamActivityDefinition.kName,
              ai.general.web.video.VideoStreamActivityDefinition.kRoleSender));
        }
      }
    }
    return capabilities;
  }

  /**
   * Returns the set of passive capabilities of the user. The set of capabilities depend on the
   * clients with which the user is logged in and may change over the lifetime of the user.
   *
   * The passive capabilities are returned in brief form as {@link CapabilityInfo} objects that
   * can be serialized into JSON.
   *
   * @return Brief description of the set of passive capabilities of the user.
   */
  @Override
  public List<CapabilityInfo> getPassiveCapabilities() {
    return Capability.getPassiveCapabilityInfos(getCapabilities());
  }

  /**
   * Returns a list of all activities that this user currently participates in.
   * The list specifies the participation object in each user. If the user participates in an
   * activity multiple times, returns a participation object for each participation in the
   * activity.
   *
   * @return The list of all activities this user currently participates in.
   */
  @Override
  public List<Participant> getAllActivityParticipations() {
    List<Participant> participations = new ArrayList<Participant>();
    for (Session session : sessions_) {
      participations.addAll(session.getActivityParticipations());
    }
    return participations;
  }

  /**
   * Returns the friends of this user.
   *
   * @return The list of friends of this user.
   */
  public List<UserView> getFriends() {
    return friends_;
  }

  /**
   * Returns a JSON representation of this User.
   *
   * The returned JSON is a JSON object that describes the user. The description contains the
   * username, the user type, current user status, and current user capabilities.
   *
   * @return JSON representation describing the current state of the user.
   */
  @Override
  public String toJson() {
    try {
      return json_generator_.writeValueAsString(new UserInfo(this));
    } catch (JsonProcessingException e) {
      return "{}";
    }
  }

  /**
   * Returns the friends of this user in a JSON object.
   *
   * The JSON object contains one property for each friend. The name of the property is the
   * username of the friend. The value of the property is an object describing the current state
   * of the friend.
   * In JavaScript, the returned JSON object can be indexed by the username of the friend.
   *
   * @return JSON object with all friends of the user as properties.
   */
  public String getFriendsAsJson() {
    HashMap<String, Object> friends_description = new HashMap<String, Object>();
    for (UserView friend : friends_) {
      friends_description.put(friend.getUsername(), new UserInfo(friend));
    }
    try {
      return json_generator_.writeValueAsString(friends_description);
    } catch (JsonProcessingException e) {
      return "{}";
    }
  }

  /**
   * Returns the list of pending activity invitations this user has received.
   * The end-user associated with this user account is expecte to reply to these invitations.
   *
   * @return The list of pending activity invitations received by this user.
   */
  @Override
  public InvitationList getPendingInvitations() {
    return pending_invitations_;
  }

  /**
   * Returns user specific properties. The returned property map is keyed by property name and
   * a property specific value. The value may be null.
   *
   * @return The user properties.
   */
  @Override
  public HashMap<String, Object> getProperties() {
    return properties_;
  }

  /**
   * Returns true if the user is currently logged in with the specified session.
   * A session corresponds to an active client program, such as a browser.
   * A user may be logged in through multiple sessions at the same time and multiple users may
   * be logged in through the same session at the same time (e.g., using the same browser).
   *
   * @param session_id The session ID of the session.
   * @return True if the user is logged in with the session_id.
   */
  public boolean isLoggedInWithSessionId(String session_id) {
    return getSession(session_id) != null;
  }

  /**
   * Returns the number of active WAMP connections associated with this user.
   *
   * @return The current WAMP connection count.
   */
  public int getWampConnectionCount() {
    return num_wamp_connections_;
  }

  /**
   * Increments the WAMP connection count.
   */
  public void incrementWampConnectionCount() {
    num_wamp_connections_++;
  }

  /**
   * Decrements the WAMP connection count.
   */
  public void decrementWampConnectionCount() {
    num_wamp_connections_--;
  }

  /**
   * Handles a web login. This method is usually called by a JSP page when the user is logging
   * in through the website using a browser. The login page automatically set the session ID
   * to the user, which must be used as the session ID parameter to this method. The session ID
   * is initally obtained from the {@link SessionManager}.
   *
   * The password must be hashed using SHA-1. It must not be a clear text password.
   * More specifically, the password must be SHA1(clear text passoword + username).
   *
   * @param session_id The session ID of the browser session.
   * @param password The hashed password.
   * @return True if the user has been successfully authenticated and logged in.
   */
  public boolean webLogin(String session_id, String password) {
    if (UserDB.getInstance().authenticateUser(username_, password)) {
      log.info("({}/{}) web login", username_, session_id);
      beginSession(session_id, WebClientType.getInstance());
      return true;
    } else {
      log.info("({}/{}) web login failed", username_, session_id);
      return false;
    }
  }

  /**
   * Handles a web logout. This method is usually called by a JSP page when the user logs out
   * through the website using a browser. The session id must be same id used with the
   * corresponding {@link #webLogin(String, String)} call.
   *
   * @param session_id The session ID of the browser session.
   */
  public void webLogout(String session_id) {
    endSession(session_id);
    log.info("({}/{}) web logout", username_, session_id);
  }

  /**
   * Executes a logout initiated by the system. A system logout may be initiated by an
   * administrator or administrative code.
   * A system logout logs out the user from all active sessions. The user is notified that
   * a system logout has occurred.
   */
  public void systemLogout() {
    userEvent(UserEvent.systemLogout(this));
    Session[] sessions = sessions_.toArray(new Session[0]);
    for (int i = 0; i < sessions.length; i++) {
      endSession(sessions[i].getSessionId());
    }
  }

  /**
   * Registers a follower of this user. The follower will received events related to this user.
   *
   * @param follower The UserEventListener to register.
   */
  @Override
  public synchronized void follow(UserEventListener follower) {
    followers_.add(follower);
  }

  /**
   * Unregisters a previously registered follower.
   *
   * @param follower The UserEventListener to unregister.
   */
  @Override
  public synchronized void unfollow(UserEventListener follower) {
    followers_.remove(follower);
    if (status_ == Status.Offline && followers_.size() == 0) {
      UserManager.getInstance().unloadUser(this);
    }
  }

  /**
   * Starts and joins a new activity.
   * The specified activity definition must exist and the specified role must be an active role
   * of the activity.
   *
   * If the activity can be started, this method returns the new activity ID and participant
   * id to the user.
   *
   * A started activity must be exited by the user via {@link #exitActivity(String, String)} when
   * the activity is complete.
   *
   * If the activity cannot be started, this method throws an ActivityStartException with the
   * reason as the description.
   *
   * @param session_id ID of session that is used to start the activity.
   * @param activity_name The name of the definition of the activity to start.
   * @param role_name The name of the role with which the activity is started.
   * @return ActivityStartInfo with the ID of the activity and the participant ID.
   * @throws ActivityStartException If the activity cannot be started.
   */
  @RpcMethod("bin/user_service/startActivity")
  public ActivityStartInfo startActivity(String session_id,
                                         String activity_name,
                                         String role_name)
    throws ActivityStartException {
    Session session = getSession(session_id);
    if (session == null)
      throw new ActivityStartException(ActivityStartException.Reason.InvalidSession);
    ActivityDefinition activity_definition =
      ActivityManager.getInstance().getActivityDefinition(activity_name);
    if (activity_definition == null)
      throw new ActivityStartException(ActivityStartException.Reason.NoSuchActivity);
    Role role = activity_definition.getRole(role_name);
    if (role == null)
      throw new ActivityStartException(ActivityStartException.Reason.NoSuchRole);
    if (!role.isActive())
      throw new ActivityStartException(ActivityStartException.Reason.PassiveRole);
    Activity activity = activity_definition.createActivity();
    Participant participant = activity.join(session, role);
    log.info("({}/{}) started activity {} with role {}",
             username_, session_id, activity_name, role_name);
    return new ActivityStartInfo(activity.getActivityId(), participant.getParticipantId());
  }

  /**
   * Exits an activity started by {@link #startActivity(String, String, String)} or
   * joined by {@link #invitationReply(String, String, boolean)}.
   *
   * This method notifies any remaining particpants that this user has exited the activity with
   * the role associated with the participant_id.
   *
   * A user may participate in an activity multiple times. The user must exit with each
   * participant id it has obtained for the activity.
   *
   * If no particpants are left, the activity can be ended.
   *
   * @param session_id The ID of the session that joined the activity and is now exiting.
   * @param participant_id The ID of the participant who is exiting the activity.
   */
  @RpcMethod("bin/user_service/exitActivity")
  public void exitActivity(String session_id, String participant_id) {
    Session session = getSession(session_id);
    if (session == null) return;
    Participant participant = session.getActivityParticipant(participant_id);
    if (participant == null) return;
    participant.exitActivity();
    log.info("({}/{}) exited activity {} with role {}", username_, session_id,
             participant.getActivity().getActivityDefinition().getName(),
             participant.getRole().getName());
  }

  /**
   * Invites another user to an activity in which this user participates. This user must be a
   * participant in the activity. A user must be invited into a specific role suggested by this
   * user.
   *
   * The user is invited into the activity in which this user participates with the specified
   * participant ID.
   *
   * The invited user object may immediatly accept or reject the invitation or may prompt the
   * end-user to accept or reject the invition.
   *
   * If the invitation can be immediatly accepted by the user object on the server, this method
   * returns an invitation result with an accept or reject response.
   *
   * If the end-user needs to be consulted, this method returns an invitation result with a
   * a pending response and an invitation ID. Once the end-user replies to the invitation
   * id an invitation reply event will be sent to this user indicating whether the end-user
   * has accepted the invitation. The invitation ID will be included in the invitation reply
   * event to reference this invitation.
   *
   * @param session_id The ID of the session from which the invitation is initiated.
   * @param participant_id The participant ID in the activity into which to invite the user.
   * @param username The name of the user to invite.
   * @param role_name The role into which to invite the user.
   * @return The invitation result.
   */
  @RpcMethod("bin/user_service/invite")
  public InvitationResult invite(String session_id,
                                 String participant_id,
                                 String username,
                                 String role_name) {
    Session session = getSession(session_id);
    if (session == null) return InvitationResult.reject("invalid session");
    Participant participant = session.getActivityParticipant(participant_id);
    if (participant == null) return InvitationResult.reject("not a participant");
    Activity activity = participant.getActivity();
    Role role = activity.getActivityDefinition().getRole(role_name);
    if (role == null) return InvitationResult.reject("invalid role");
    UserView friend = findFriend(username);
    if (friend == null) return InvitationResult.reject("cannot invite user");
    log.info("({}/{}) invites {} to {} as {}", username_,  session_id, username,
             activity.getActivityDefinition().getName(), role_name);
    return activity.invite(participant, friend, role);
  }

  /**
   * Used to respond to pending a invitation event. This method is used to indicate the final
   * acceptance or rejection of the pending invitation.
   *
   * The invitation ID is communicated with the invitation event.
   *
   * If the inviation is accepted, this user will join the activity. Upon acceptance, this method
   * returns the activity ID and the participant ID in its response.
   * A joined activity must be exited via {@link #exitActivity(String, String)}.
   * Other participants will be notified that this user has joined the activity.
   *
   * If the invitation is rejected, this method notifies the inviter of the rejection and this
   * user will not participate in the activity. In case of rejection, the returned ID's will
   * be empty.
   *
   * @param session_id The ID of the session that is invited to join the activity.
   * @param invitation_id The invitation ID communicated via the invitation event.
   * @param accept Whether the user accepts the invitation and wants to join the activity.
   * @return The ID of the activity and the participant ID.
   */
  @RpcMethod("bin/user_service/invitationReply")
  public ActivityStartInfo invitationReply(String session_id,
                                           String invitation_id,
                                           boolean accept) {
    Invitation invitation = pending_invitations_.get(invitation_id);
    if (invitation == null) return new ActivityStartInfo("", "");
    Session session = getSession(session_id);
    if (session == null || !accept) {
      invitation.reject();
      return new ActivityStartInfo("", "");
    }
    Activity activity = invitation.getActivity();
    Participant participant = activity.join(session, invitation.getRole());
    invitation.accept();
    log.info("({}/{}) accepts invitation from {}", username_, session_id,
             invitation.getInviter().getUser().getUsername());
    return new ActivityStartInfo(activity.getActivityId(), participant.getParticipantId());
  }

  /**
   * Sets the password of this user. The password is only set if the old_password matches the
   * current password.
   *
   * The passwords must be hashed using SHA-1. Passwords must not be a clear text.
   * More specifically, passwords must be equal to SHA1(clear text passoword + username).
   *
   * @param session_id The ID of the session that is requesting the password change.
   * @param old_password Hash of the current password.
   * @param new_password Hash of the new password.
   * @return True if the password has been successfully changed.
   */
  @RpcMethod("bin/user_service/setPassword")
  public boolean setPassword(String session_id, String old_password, String new_password) {
    if (UserDB.getInstance().authenticateUser(username_, old_password)) {
      log.info("({}/{}) changed password", username_, session_id);
      UserDB.getInstance().setPassword(username_, new_password);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Responds to user status update events of friends. Implements {@link UserEventListener} method.
   * Forwards the event to subscribers of this user's user event topic.
   *
   * @param user Friend whose status is updated.
   */
  @Override
  public void userStatusUpdated(User user) {
    userEvent(UserEvent.statusUpdate(user, user.getStatus()));
  }

  /**
   * Responds to user user capability update events of friends. Implements
   * {@link UserEventListener} method.
   *
   * Forwards the event to subscribers of this user's user event topic.
   *
   * @param user User whose capabilities are updated.
   */
  @Override
  public void userCapabilityUpdated(User user) {
    userEvent(UserEvent.capabilityUpdate(user, user.getPassiveCapabilities()));
  }

  /**
   * Called when the properties of a user have been updated.
   * Implements {@link UserEventListener} method.
   *
   * Forwards the event to subscribers of this user's user event topic.
   *
   * @param user The user whose properties have been updated.
   */
  @Override
  public void userPropertiesUpdated(User user) {
    userEvent(UserEvent.propertyUpdate(user, user.getProperties()));
  }

  /**
   * Executes a session ping round to all active sessions.
   * Checks whether a pong has been received from the last round. If a no pong has been received
   * logs out the session.
   */
  public synchronized void executeSessionPing() {
    if (sessions_.size() == 0) return;
    ArrayList<Session> stale_sessions = new ArrayList<Session>();
    for (Session session : sessions_) {
      if (session.getPongCount() <= 0) {
        stale_sessions.add(session);
      } else {
        session.setPongCount(session.getPongCount() - 1);
      }
    }
    for (Session session : stale_sessions) {
      log.info("({}/{}) stale session", username_, session.getSessionId());
      endSession(session.getSessionId());
    }
    if (sessions_.size() == 0) return;
    Directory.Instance.handle(
        user_home_path_,
        new Request(new Uri(session_ping_uri_),
                    Request.RequestType.Publish,
                    new SessionPingParameters(2 * UserManager.kSessionPingIntervalMillis)));
  }

  /**
   * Handles an incoming session pong. Session pongs are send by active sessions in response to
   * a session ping sent by {@link #runSessionPing()}.
   *
   * @param session_id The session ID of the session that sent the session pong.
   */
  @Subscribe(UserUris.kSessionPongTopic)
  public void onSessionPong(String session_id) {
    Session session = getSession(session_id);
    if (session != null) {
      session.setPongCount(kMaxMissedPings);
    }
  }

  /**
   * Triggers a user event. All subscribers of the user event of this user will be notified about
   * the event. Typically, the subscribers will be remote sessions subscribed via WAMP pub/sub.
   *
   * @param event The event to trigger.
   */
  @Override
  public void userEvent(UserEvent event) {
    Directory.Instance.handle(user_home_path_,
                              new Request(new Uri(user_event_uri_),
                                          Request.RequestType.Publish, event));
  }

  /**
   * Updates the user properties and notifies this user and all followers of this user about
   * the property update.
   *
   * @param new_properties The set of new properties to be merged with any existing properties.
   */
  public synchronized void updateProperties(HashMap<String, Object> new_properties) {
    log.debug("({}) Properties updated.", username_);
    properties_.putAll(new_properties);
    userPropertiesUpdated(this);
    for (UserEventListener follower : followers_) {
      follower.userPropertiesUpdated(this);
    }
  }

  /**
   * Clears all user properties and notifies this user and all followers about the property
   * update.
   */
  public synchronized void clearProperties() {
    log.debug("({}) Properties cleared.", username_);
    properties_.clear();
    userPropertiesUpdated(this);
    for (UserEventListener follower : followers_) {
      follower.userPropertiesUpdated(this);
    }
  }

  /**
   * Returns the followers of this user.
   *
   * @return The list of followers.
   */
  protected List<UserEventListener> getFollowers() {
    return followers_;
  }

  /**
   * Updates the user status and notifies any followers about the new status if it has changed.
   *
   * @param status The updated user status.
   */
  protected void setStatus(Status status) {
    if (this.status_ == status) return;
    this.status_ = status;
    for (UserEventListener follower : followers_) {
      follower.userStatusUpdated(this);
    }
  }

  /**
   * Notifies the clients of this user and all the followers of this user of a capability update
   * by transmitting the updated set of passive capabilities to the clients.
   */
  protected void notifyCapabilityUpdate() {
    userCapabilityUpdated(this);
    for (UserEventListener follower : followers_) {
      follower.userCapabilityUpdated(this);
    }
  }

  /**
   * Implements common initialization logic that is executed when a user logs in regardless of log
   * in source. All source specific log in methods should call this method to execute common
   * session initialization code.
   *
   * This method begins the specified session. A user may log in through multiple sessions
   * using different login sources (e.g., browser or Interbot client). This method must be called
   * for each session through which the user logs in. The session ID can be obtained from the
   * {@link SessionManager}.
   *
   * This method assumes that the user has already been authenticated via a source specific login
   * mechanism.
   *
   * Every session must be ended by calling {@link #endSession(String)} method with the same
   * session id used with this method.
   *
   * @param session_id The id of the session to begin.
   * @param client_type The type of client with which the user has logged in.
   */
  protected synchronized void beginSession(String session_id, ClientType client_type) {
    if (getSession(session_id) != null) return;
    Session session = new Session(this, session_id, client_type);
    session.setPongCount(kMaxMissedPings);
    sessions_.add(session);
    if (status_ == Status.Offline) {
      setStatus(Status.Online);
      followFriends();
    }
    notifyCapabilityUpdate();
  }

  /**
   * Implements common clean up logic that is executed when a user logs out regardless of source.
   * All source specific logout methods should call this method to execute common session clean up
   * code.
   *
   * This method ends the specified session. If a user is logged in through other sessions, the
   * user will remain logged in through the other sessions. This method must be called for each
   * session for which {@link #beginSession(String, ClientType)} has been called.
   *
   * @param session_id The session id of the session to end.
   */
  protected synchronized void endSession(String session_id) {
    Session session = getSession(session_id);
    if (session == null) return;
    session.exitAllActivities();
    sessions_.remove(session);
    notifyCapabilityUpdate();
    if (sessions_.size() == 0) {
      setStatus(Status.Offline);
      pending_invitations_.rejectAll();
      unfollowFriends();
      if (followers_.size() == 0) {
        UserManager.getInstance().unloadUser(this);
      }
    }
  }

  /**
   * Returns the friend with the specified username or null if this user does not have any such
   * friend.
   *
   * @param friend_username The username of the friend.
   * @return The friend user or null.
   */
  protected UserView findFriend(String friend_username) {
    for (UserView friend : friends_) {
      if (friend.getUsername().equals(friend_username)) {
        return friend;
      }
    }
    return null;
  }

  /**
   * Starts following all friends of this user. This method ensures that this user receives
   * updates about all of its friends.
   * This method is called when the user instance is loaded and the user has successfully logged
   * in into the first active session.
   */
  protected void followFriends() {
    if (friends_ == null) {
      friends_ = new ArrayList<UserView>();
      List<String> friend_names = UserDB.getInstance().listFriends(username_);
      Collections.sort(friend_names);
      UserManager user_manager = UserManager.getInstance();
      for (String friend_name : friend_names) {
        UserView friend = user_manager.getUserView(friend_name);
        if (friend != null) {
          friends_.add(friend);
        }
      }
    }
    for (UserView friend : friends_) {
      friend.follow(this);
    }
  }

  /**
   * Stops following all friends of this user.
   * This method is called when the user has logged out of sessions.
   */
  protected void unfollowFriends() {
    for (UserView friend : friends_) {
      friend.unfollow(this);
    }
  }

  /**
   * Helper method that returns a JSON name-value property. The value is appended as-is, i.e.
   * not converted into a JSON string. This allows constructing properties which are not string
   * properties.
   *
   * @param name The name of the property.
   * @param value The raw value of the property.
   * @return A JSON property.
   */
  private static String jsonProperty(String name, String value) {
    return "\"" + name + "\": "  + value.toString();
  }

  /**
   * Helper method that returns a JSON name-value string property.
   * The value is interpreted as a JSON string.
   *
   * @param name The name of the string property.
   * @param value The string value of the property.
   * @return A JSON string property.
   */
  private static String jsonStringProperty(String name, String value) {
    return "\"" + name + "\": \""  + value + "\"";
  }

  /**
   * Returns the session with the specified ID or null if the user is not logged in via a session
   * with the specified session ID.
   *
   * @param session_id The ID of the session.
   * @return The session or null if the user is not logged in via a session with the specified id.
   */
  private Session getSession(String session_id) {
    for (Session session : sessions_) {
      if (session.getSessionId().equals(session_id)) {
        return session;
      }
    }
    return null;
  }

  private static Logger log = LogManager.getLogger();

  private String username_;
  private String user_home_path_;
  private URI session_ping_uri_;
  private URI user_event_uri_;
  private Status status_;
  private List<Session> sessions_;  // All active sessions of this user.
  private int num_wamp_connections_;
  private List<UserView> friends_;
  private List<UserEventListener> followers_;
  private String p2p_topic_;
  private ObjectMapper json_generator_;
  private InvitationList pending_invitations_;
  private HashMap<String, Object> properties_;
}
