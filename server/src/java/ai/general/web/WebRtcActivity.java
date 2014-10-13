/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import java.util.HashMap;

/**
 * Represents WebRTC activities. WebRTC activites are initiated by a caller and can be joined
 * by a callee.
 */
public class WebRtcActivity extends Activity {

  /**
   * Creates a new WebRTC activity.
   */
  public WebRtcActivity() {
    super(WebRtcActivityDefinition.getInstance());
    this.p2p_topic_ = null;
    this.caller_ = null;
    this.callee_ = null;
  }

  /**
   * Adds WebRTC specific logic to be executed when a participant exits the activity.
   *
   * @param participant The participant who exits the activity.
   * @return True if the participant was a member of the activity and was removed.
   */
  @Override
  public boolean exit(Participant participant) {
    if (!super.exit(participant)) return false;
    if (countParticipants() == 0) {
      // All participants exited, destroy the P2P topic.
      if (p2p_topic_ != null) {
        UserUris.destroyWebRtcP2PTopic(p2p_topic_, caller_, callee_);
      }
    }
    return true;
  }

  /**
   * Implements the invitation logic for WebRTC activities.
   * A WebRTC activity consists of one caller and one callee. Neiter the caller session nor the
   * callee session can participate in any other WebRTC activity.
   *
   * If the preconditions for the invitation are met, this method notifes the callee end-user
   * about the inivtation and lets the end-user pick the session with which to accept the call.
   *
   * The returned invitation result will be either rejected or pending. In case of a pending
   * result, the final result will communicated via an InvitationReply event.
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
    if (!inviter.getRole().getName().equals(WebRtcActivityDefinition.kRoleCaller)) {
      return InvitationResult.reject("inviter must be caller");
    }
    if (!role.getName().equals(WebRtcActivityDefinition.kRoleCallee)) {
      return InvitationResult.reject("user must be invited to callee role");
    }
    if (!Capability.hasActivityRole(user.getCapabilities(), getActivityDefinition(), role)) {
      return InvitationResult.reject("user cannot support requested role at this time");
    }
    if (Participant.hasActivity(user.getAllActivityParticipations(), getActivityDefinition())) {
      return InvitationResult.reject("user is busy");
    }
    caller_ = inviter.getUser();
    callee_ = user;
    p2p_topic_ = UserUris.createWebRtcP2PTopic(caller_, callee_);
    Invitation invitation = new Invitation(inviter, user, role, p2p_topic_);
    user.userEvent(UserEvent.activityInvitation(invitation));
    return InvitationResult.pending(invitation);
  }

  private String p2p_topic_;
  private UserView caller_;
  private UserView callee_;
}
