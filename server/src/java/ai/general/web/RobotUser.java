/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import ai.general.directory.Directory;
import ai.general.directory.Request;
import ai.general.net.Uri;
import ai.general.plugin.annotation.Subscribe;

import java.util.HashMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Represents a robot user. A robot user can accept motion commands.
 * Robot users can log in via the browser and via the Interbot client.
 */
public class RobotUser extends User {

  /**
   * Creates a new robot user with the specified robot name.
   *
   * @param robotname Robot username.
   */
  public RobotUser(String robotname) {
    super(robotname);
  }

  /**
   * Returns Robot as the user type.
   *
   * @return UserType.Robot.
   */
  @Override
  public UserType getUserType() {
    return UserType.Robot;
  }

  /**
   * Handles login through the Interbot client.
   *
   * The session ID must be identical to the WAMP session ID. This allows the client to obtain
   * the session ID from the WAMP connection.
   *
   * The password follows the same guidelines as the password for weblogin. It must be hashed
   * using SHA-1. It must not be a clear text password. More specifically, the password must be
   * SHA1(clear text passoword + username). The password is also referred to as the API key.
   *
   * @param session_id The session ID of the Interbot session. Same as WAMP session ID.
   * @param password The hashed password for the robot user account.
   * @return True if the robot has been successfully authenticated and logged in.
   */
  public boolean robotLogin(String session_id, String password) {
    if (UserDB.getInstance().authenticateUser(getUsername(), password)) {
      beginSession(session_id, InterbotClientType.getInstance());
      log.info("({}/{}) robot login", getUsername(), session_id);
      TaskManager.getInstance().schedule(new RequestSystemInfoTask(this));
      return true;
    } else {
      log.info("({}/{}) robot login failed", getUsername(), session_id);
      return false;
    }
  }

  /**
   * Handles logout through the Interbot client.
   *
   * The session ID is the same as the WAMP session ID and can be obtained from the WAMP
   * connection.
   *
   * @param session_id The session ID of the Interbot session. Same as WAMP session ID.
   */
  public void robotLogout(String session_id) {
    endSession(session_id);
    clearProperties();
    log.info("({}/{}) robot logout", getUsername(), session_id);
  }

  /**
   * Sends the robot a request to execute the package specified by the URL.
   * The package URL must point to an execution package available on the server. The robot
   * will download and unpack the package and start executing it on the robot.
   *
   * This method is used by server to initiate administrative tasks such as software updates
   * on the robot.
   *
   * An execution package is a zipped tar ball with a run.sh file and any other necessary files.
   * The robot invokes the run.sh file which is a shell script.
   *
   * @param package_url A URL on this server that contains the package to execute.
   */
  public void sendExecutePackageRequest(String package_url) {
    Directory.Instance.handle(
        UserUris.userHomePath(getUsername()),
        new Request(new Uri(UserUris.createEventUri(getUsername(),
                                                    UserUris.kAdminExecutePackageTopic)),
                    Request.RequestType.Publish,
                    new ExecutePackageInstruction(package_url)));
  }

  /**
   * Handles a response to a system info request.
   *
   * @param The response to a system info request.
   */
  @Subscribe(UserUris.kRobotSystemInfoResponseTopic)
  public void onSystemInfoResponse(HashMap<String, Object> response) {
    updateProperties(response);
    // Properties can modify capabilities.
    notifyCapabilityUpdate();
  }

  private static Logger log = LogManager.getLogger();
}
