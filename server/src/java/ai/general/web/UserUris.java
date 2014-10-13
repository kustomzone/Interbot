/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import ai.general.common.RandomString;
import ai.general.directory.Directory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Computes standard URI's and paths for user services.
 */
public class UserUris {

  /** The host used in URI's. */
  public static final String kHost = "general.ai";

  /** Base path for general user events. */
  public static final String kEventsBasePath = "/events";

  /** The session ping topic URI. */
  public static final String kSessionPingTopic = kEventsBasePath + "/session/ping";

  /** The session pong topic URI. */
  public static final String kSessionPongTopic = kEventsBasePath + "/session/pong";

  /** The user event topic URI. */
  public static final String kUserEventTopic = kEventsBasePath + "/user_event";

  /** Base path for WebRTC events. */
  public static final String kWebRtcEventBasePath = kEventsBasePath + "/webrtc";

  /** Base path for robot services. */
  public static final String kRobotBasePath = "/robot";

  /** Robot control ping topic URI */
  public static final String kRobotControlPingTopic = kRobotBasePath + "/control/ping";

  /** Robot control pong topic URI */
  public static final String kRobotControlPongTopic = kRobotBasePath + "/control/pong";

  /** Robot base velocity topic URI */
  public static final String kRobotBaseVelocityTopic = kRobotBasePath + "/base/velocity";

  /** Robot video camera topic URI */
  public static final String kRobotVideoTopic = kRobotBasePath + "/video";

  /** Robot video camera pan-tilt topic URI */
  public static final String kRobotVideoPanTiltTopic = kRobotBasePath + "/video/panTilt";

  /** Robot system info request topic URI */
  public static final String kRobotSystemInfoRequestTopic =
    kRobotBasePath + "/system/info/request";

  /** Robot system info response topic URI */
  public static final String kRobotSystemInfoResponseTopic =
    kRobotBasePath + "/system/info/response";

  /** Request execution of administrative code (server to robot only). */
  public static final String kAdminExecutePackageTopic = "/admin/execute";

  /**
   * Returns the home path for the specified username.
   *
   * @param username Username of user.
   * @return Home path for user.
   */
  public static String userHomePath(String username) {
    return "/user/" + username + "/home";
  }

  /**
   * Returns the full directory path of an event for the specified username and topic.
   *
   * @param username The username.
   * @param topic The topic.
   * @return The full directory path for the event.
   */
  public static String fullEventPath(String username, String topic) {
    return userHomePath(username) + topic;
  }

  /**
   * Creates all event paths for the specified username in the Intercom Directory.
   *
   * @param user The user for whom to create the event paths.
   */
  public static void createEventPathsForUser(UserView user) {
    String username = user.getUsername();
    Directory directory = Directory.Instance;
    directory.createPath(fullEventPath(username, kSessionPingTopic));
    directory.createPath(fullEventPath(username, kSessionPongTopic));
    directory.createPath(fullEventPath(username, kUserEventTopic));
    directory.createPath(fullEventPath(username, kWebRtcEventBasePath));
    if (user.getUserType() == User.UserType.Robot) {
      directory.createPath(fullEventPath(username, kRobotControlPingTopic));
      directory.createPath(fullEventPath(username, kRobotControlPongTopic));
      directory.createPath(fullEventPath(username, kRobotBaseVelocityTopic));
      directory.createPath(fullEventPath(username, kRobotVideoTopic));
      directory.createPath(fullEventPath(username, kRobotVideoPanTiltTopic));
      directory.createPath(fullEventPath(username, kRobotSystemInfoRequestTopic));
      directory.createPath(fullEventPath(username, kRobotSystemInfoResponseTopic));
      directory.createPath(fullEventPath(username, kAdminExecutePackageTopic));
    }
  }

  /**
   * Returns the event topic URI for the specified username and topic.
   *
   * @param username Username of user.
   * @param topic The event topic for which to create the URI.
   * @return User event topic URI of user.
   */
  public static URI createEventUri(String username, String topic) {
    try {
      return new URI("wamp", username, kHost, -1, topic, null, null);
    } catch(URISyntaxException e) {
      // Not thrown due to construction.
      return null;
    }
  }

  /**
   * Creates and returns a random WebRTC P2P topic. The topic can be used to send control
   * messages in a new WebRTC call.
   *
   * The topic is formulated with the event CURIE and can be directly used by the frontend
   * assuming the CURIE prefix has been registered with the WAMP server.
   *
   * This method also sets up the path in the Intercom Directory and grants necessary permissions
   * to caller and callee.
   *
   * The returned topic should be destroyed by calling
   * {@link #destroyWebRtcP2PTopic(String, UserView, UserView)} once the WebRTC call is terminated.
   *
   * @param caller The calling user.
   * @param callee The user being called.
   * @return A new WebRTC P2P topic.
   */
  public static String createWebRtcP2PTopic(UserView caller, UserView callee) {
    String topic = "/p2p" + RandomString.nextString(16);
    Directory directory = Directory.Instance;
    String full_path = fullEventPath(caller.getUsername(), kWebRtcEventBasePath) + topic;
    directory.createPath(full_path);
    directory.link(fullEventPath(callee.getUsername(), kWebRtcEventBasePath), full_path);
    return "event:webrtc" + topic;
  }

  /**
   * Destroys a WebRTC P2P topic created by {@link createWebRtcP2PTopic(UserView, UserView)}.
   * The p2p_topic must be the topic returned by createWebRtcP2PTopic and the same caller and
   * callee must be used.
   * This method cleans up any paths created in the Intercom Directory and removes permissions.
   *
   * @param p2p_topic The P2P topic.
   * @param caller The calling user.
   * @param callee The user being called.
   */
  public static void destroyWebRtcP2PTopic(String p2p_topic, UserView caller, UserView callee) {
    int topic_index = p2p_topic.lastIndexOf('/');
    if (topic_index < 0) return;
    p2p_topic = p2p_topic.substring(topic_index);
    String full_path = fullEventPath(caller.getUsername(), kWebRtcEventBasePath) + p2p_topic;
    Directory directory = Directory.Instance;
    directory.unlink(fullEventPath(callee.getUsername(), kWebRtcEventBasePath), full_path);
    directory.removePath(full_path);
  }

  /**
   * Grants the controller user control access to the robot connected via the robot account.
   * Once access is granted, the controller can send control commands to the robot using the
   * general events path. Robot events start with the 'event:robot' CURIE.
   *
   * The access can be removed via the {@link #denyRobotControlAccess(UserView, UserView)} method.
   *
   * @param robot The robot to which to grant control access.
   * @param controller The user to whom to grant control access.
   */
  public static void grantRobotControlAccess(UserView robot, UserView controller) {
    Directory.Instance.link(fullEventPath(controller.getUsername(), kEventsBasePath),
                            fullEventPath(robot.getUsername(), kRobotBasePath));
  }

  /**
   * Removes control access that was granted to a user via the
   * {@link #grantRobotControlAcess(UserView, UserView)} method. The parameters given to this
   * method must match the parameters used for granting access.
   *
   * @param robot The robot to which the user was granted control access.
   * @param controller The user to whom control access was granted.
   */
  public static void denyRobotControlAccess(UserView robot, UserView controller) {
    Directory.Instance.unlink(fullEventPath(controller.getUsername(), kEventsBasePath),
                              fullEventPath(robot.getUsername(), kRobotBasePath));
  }
}
