/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import ai.general.directory.Directory;
import ai.general.directory.Request;
import ai.general.net.Uri;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * The RequestSystemInfoTask is used to request system information from a robot when it is ready
 * to supply such information. The request has to be scheduled as a task since a robot is
 * typically not immediatly ready to provide the specified information when it logs in.
 */
public class RequestSystemInfoTask extends Task {

  public static final long kDelayMillis = 30000;

  /**
   * Constucts a RequestSystemInfoTask to be run at a fixed time in the future. This task
   * should be typically constructed when the robot logs in.
   *
   * @param robot The robot from which the system information is to be requested.
   */
  public RequestSystemInfoTask(RobotUser robot) {
    super(kDelayMillis);
    this.robot_ = robot;
  }

  /**
   * Sends the request to the robot.
   */
  @Override
  public void run() {
    if (robot_.getStatus() != User.Status.Online) return;
    if (robot_.getProperties().size() > 0) return;
    log.debug("({}) Requesting system info.", robot_.getUsername());
    SystemInfoRequest request = new SystemInfoRequest();
    request.addProperty(SystemProperty.NetworkInterfaces);
    Directory.Instance.handle(
        UserUris.userHomePath(robot_.getUsername()),
        new Request(new Uri(UserUris.createEventUri(
                        robot_.getUsername(),
                        UserUris.kRobotSystemInfoRequestTopic)),
                    Request.RequestType.Publish,
                    request));
    request = new SystemInfoRequest();
  }

  private static Logger log = LogManager.getLogger();

  private RobotUser robot_;
}
