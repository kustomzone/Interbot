/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web.video;

import ai.general.directory.Directory;
import ai.general.directory.Request;
import ai.general.net.Uri;
import ai.general.web.Activity;
import ai.general.web.Capability;
import ai.general.web.InterbotClientType;
import ai.general.web.InvitationResult;
import ai.general.web.Participant;
import ai.general.web.Role;
import ai.general.web.Session;
import ai.general.web.User;
import ai.general.web.UserEvent;
import ai.general.web.UserUris;
import ai.general.web.UserView;

import java.util.NoSuchElementException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Represents video stream activities.
 * A video stream activity is initiated by a receiver and used to stream video from a sender to
 * the receiver.
 */
public class VideoStreamActivity extends Activity {

  /**
   * Creates a new video stream activity.
   */
  public VideoStreamActivity() {
    super(VideoStreamActivityDefinition.getInstance());
    this.channel_ = null;
  }

  /**
   * Adds video stream activity specific logic to be executed when a participant exits the control
   * activity.
   *
   * @param participant The participant who exits the activity.
   * @return True if the participant was a member of the activity and was removed.
   */
  @Override
  public boolean exit(Participant participant) {
    if (!super.exit(participant)) return false;
    switch (participant.getRole().getName()) {
      case VideoStreamActivityDefinition.kRoleSender:
        log.debug("({}/{}) requesting stop video stream for channel {}",
                  participant.getUser().getUsername(), participant.getSession().getSessionId(),
                  channel_.getChannelId());
        Directory.Instance.handle(
            UserUris.userHomePath(participant.getUser().getUsername()),
            new Request(new Uri(UserUris.createEventUri(participant.getUser().getUsername(),
                                                        UserUris.kRobotVideoTopic)),
                        Request.RequestType.Publish,
                        VideoInstruction.stopStream(channel_.getChannelId())));
        break;
      case VideoStreamActivityDefinition.kRoleReceiver:
        if (channel_ != null) {
          channel_.removeReceiver();
        }
        break;
    }
    log.info("({}/{}) exited video stream activity with channel {}",
             participant.getUser().getUsername(), participant.getSession().getSessionId(),
             channel_ != null ? channel_.getChannelId() : "0");

    if (countParticipants() == 0 && channel_ != null) {
      channel_.close();
      channel_ = null;
    } else {
      Participant sender = Participant.getFirstWithRole(
          getParticipants(),
          getActivityDefinition().getRole(VideoStreamActivityDefinition.kRoleSender));
      if (sender != null && countParticipants() == 1) {
        // All receivers exited, exit sender.
        exit(sender);
        sender.getUser().userEvent(UserEvent.exitActivity(sender));
      }
    }
    return true;
  }

  /**
   * Implements the invitation logic for video stream activities.
   *
   * A video stream activity consists of one sender and one receiver.
   * The sender or receiver must not alreay participate in a video stream activity.
   *
   * Video is streamed over a video channel. The sender endpoint for video streams is video/in
   * and the receiver endpoint is video/out. Each channel has a unique channel ID, which must
   * be specified when connecting to the endpoints.
   *
   * The channel ID of the video channel is communicated as extra information in the invitation
   * result.
   *
   * If the preconditions for the invitation are met, this method sends a request to the sender
   * to start streaming the video. The receiver may start listening on the video channel once
   * this method has returned.
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
    if (!inviter.getRole().getName().equals(VideoStreamActivityDefinition.kRoleReceiver)) {
      return InvitationResult.reject("inviter must be a receiver");
    }
    if (!role.getName().equals(VideoStreamActivityDefinition.kRoleSender)) {
      return InvitationResult.reject("user must be invited to sender role");
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
    channel_ = VideoChannelManager.getInstance().createChannel(this);
    // automatically join activity and notify user
    Participant participant = join(user_session, role);
    user.userEvent(UserEvent.joinActivity(participant));
    log.debug("({}/{}) requesting start video stream for channel {}",
              user.getUsername(), user_session.getSessionId(), channel_.getChannelId());
    Directory.Instance.handle(
        UserUris.userHomePath(user.getUsername()),
        new Request(new Uri(UserUris.createEventUri(user.getUsername(), UserUris.kRobotVideoTopic)),
                    Request.RequestType.Publish,
                    VideoInstruction.startStream(channel_.getChannelId())));
    log.info("({}/{}) accepted video stream invitation from ({}/{})",
             user.getUsername(), user_session.getSessionId(),
             inviter.getUser().getUsername(), inviter.getSession().getSessionId());
    return InvitationResult.accept(channel_.getChannelId());
  }

  private static Logger log = LogManager.getLogger();

  private VideoChannel channel_;
}
