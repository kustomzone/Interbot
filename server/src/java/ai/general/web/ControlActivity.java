/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import java.util.HashMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Represents control activities. Control activites are initiated by a controller and used to
 * control a robot.
 */
public class ControlActivity extends Activity {

  /**
   * Creates a new control activity.
   */
  public ControlActivity() {
    super(ControlActivityDefinition.getInstance());
  }

  /**
   * Adds control activity specific logic to be executed when a new participant joins the
   * control activity.
   *
   * @param session The session that participates in the activity.
   * @param role The role with which the user joins the activity.
   * @return The participant object.
   */
  @Override
  public Participant join(Session session, Role role) {
    Participant participant = super.join(session, role);
    if (countParticipants() > 1) {
      Participant controller =
        Participant.getFirstWithRole(
            getParticipants(),
            getActivityDefinition().getRole(ControlActivityDefinition.kRoleController));
      Participant robot =
        Participant.getFirstWithRole(
            getParticipants(),
            getActivityDefinition().getRole(ControlActivityDefinition.kRoleRobot));
      if (controller != null && robot != null) {
        UserUris.grantRobotControlAccess(robot.getSession().getUser(),
                                         controller.getSession().getUser());
        log.info("({}/{}) granted ({}/{}) control access",
                 robot.getUser().getUsername(), robot.getSession().getSessionId(),
                 controller.getUser().getUsername(), controller.getSession().getSessionId());
      }
    }
    return participant;
  }


  /**
   * Adds control activity specific logic to be executed when a participant exits the control
   * activity.
   *
   * @param participant The participant who exits the activity.
   * @return True if the participant was a member of the activity and was removed.
   */
  @Override
  public boolean exit(Participant participant) {
    if (countParticipants() > 1) {
      Participant controller =
        Participant.getFirstWithRole(
            getParticipants(),
            getActivityDefinition().getRole(ControlActivityDefinition.kRoleController));
      Participant robot =
        Participant.getFirstWithRole(
            getParticipants(),
            getActivityDefinition().getRole(ControlActivityDefinition.kRoleRobot));
      if (controller != null && robot != null) {
        UserUris.denyRobotControlAccess(robot.getSession().getUser(),
                                         controller.getSession().getUser());
        log.info("({}/{}) denied ({}/{}) control access",
                 robot.getUser().getUsername(), robot.getSession().getSessionId(),
                 controller.getUser().getUsername(), controller.getSession().getSessionId());
      }
    }

    if (!super.exit(participant)) return false;

    if (countParticipants() > 0) {
      Participant controller =
        Participant.getFirstWithRole(
            getParticipants(),
            getActivityDefinition().getRole(ControlActivityDefinition.kRoleController));
      if (controller == null) {
        // controller has exited, robot should exit, too
        Participant robot =
          Participant.getFirstWithRole(
              getParticipants(),
              getActivityDefinition().getRole(ControlActivityDefinition.kRoleRobot));
        if (robot != null) {
          exit(robot);
          robot.getUser().userEvent(UserEvent.exitActivity(robot));
          log.info("({}/{}) exited control activity since controller exited",
                   robot.getUser().getUsername(), robot.getSession().getSessionId());
        }
      }
    }
    return true;
  }

  /**
   * Implements the invitation logic for control activities.
   * Only one controller and robot can participate in a control activity. The robot session
   * cannot participate in another control activity.
   *
   * This method picks the first session that is available to accept the invitation.
   *
   * @param inviter The participant that invites the user.
   * @param user The user who is invited to join the activity.
   * @param role The role into which the user is invited.
   * @return The invitation result.
   */
  @Override
  public synchronized InvitationResult invite(Participant inviter, UserView user, Role role) {
    if (countParticipants() > 1) {
      return InvitationResult.reject("activity is full");
    }
    if (inviter.getActivity() != this) {
      return InvitationResult.reject("inviter must particpate in activity");
    }
    if (!inviter.getRole().getName().equals(ControlActivityDefinition.kRoleController)) {
      return InvitationResult.reject("inviter must be a controller");
    }
    if (!role.getName().equals(ControlActivityDefinition.kRoleRobot)) {
      return InvitationResult.reject("user must be invited to robot role");
    }
    if (user.getUserType() != User.UserType.Robot) {
      return InvitationResult.reject("invited user must be a robot");
    }
    if (!Capability.hasActivityRole(user.getCapabilities(), getActivityDefinition(), role)) {
      return InvitationResult.reject("user cannot support requested role at this time");
    }
    Session user_session = null;
    InterbotClientType interbot_client = InterbotClientType.getInstance();
    for (Session session : user.getSessions()) {
      if (session.getClientType() == interbot_client &&
          !Participant.hasRole(session.getActivityParticipations(), role)) {
        user_session = session;
      }
    }
    if (user_session == null) {
      return InvitationResult.reject("user cannot assume requested role at this time");
    }
    // automatically join activity and notify user
    Participant participant = join(user_session, role);
    user.userEvent(UserEvent.joinActivity(participant));
    log.info("({}/{}) accepted control invitation from ({}/{})",
             user.getUsername(), user_session.getSessionId(),
             inviter.getUser().getUsername(), inviter.getSession().getSessionId());
    return InvitationResult.accept();
  }

  private static Logger log = LogManager.getLogger();
}
