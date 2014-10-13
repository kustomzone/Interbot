/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import ai.general.common.RandomString;

/**
 * Represents an invitation to a user to join an activity.
 * An Invitation object is created to keep track of pending invitation until the end-user issues
 * a final acceptance or rejection of the invitation.
 * Invitations may be cancelled by the inviter.
 */
public class Invitation {

  /**
   * Constructs an Invitation with the specified parameters.
   * Registers the invitation with both the inviter and user.
   *
   * @param inviter The participant who has invited the user.
   * @param user The user who has been invited.
   * @param role The role into which the user has been invited.
   * @param extra Any extra invitation data or null.
   */
  public Invitation(Participant inviter, UserView user, Role role, Object extra) {
    this.invitation_id_ = RandomString.nextString(10);
    this.inviter_ = inviter;
    this.user_ = user;
    this.role_ = role;
    this.extra_ = extra;
    inviter.getPendingInvitations().put(invitation_id_, this);
    user.getPendingInvitations().put(invitation_id_, this);
  }

  /**
   * Returns the participant that has invited the user.
   *
   * @return The participant that has invited the user.
   */
  public Participant getInviter() {
    return inviter_;
  }

  /**
   * Returns the invited user.
   *
   * @return The invited user.
   */
  public UserView getUser() {
    return user_;
  }

  /**
   * Returns the role into which the user has been invited.
   *
   * @return The role into which the user has been invited.
   */
  public Role getRole() {
    return role_;
  }

  /**
   * Returns the invitation ID of the invitation.
   *
   * @return The invitation ID.
   */
  public String getInvitationId() {
    return invitation_id_;
  }

  /**
   * Returns the activity associated with this invitation.
   *
   * @return The activity associated with this invitation.
   */
  public Activity getActivity() {
    return inviter_.getActivity();
  }

  /**
   * Returns any extra invitation data or null if there is no extra invitation data.
   *
   * @return Extra invitation data or null.
   */
  public Object getExtra() {
    return extra_;
  }

  /**
   * Accepts the invitation. Notifies the inviter of the rejection via an InvitationReply event.
   * Unregisters the invitation from both the inviter and user.
   */
  public void accept() {
    reply(true);
  }

  /**
   * Rejects the invitation. Notifies the inviter of the rejection via an InvitationReply event.
   * Unregisters the invitation from both the inviter and user.
   */
  public void reject() {
    reply(false);
  }

  /**
   * Cancels the invitation by sending a CancelInvitation event to the user.
   * Unregisters the invitation from both the inviter and user.
   */
  public void cancel() {
    user_.getPendingInvitations().remove(invitation_id_);
    user_.userEvent(UserEvent.cancelInvitation(this));
    inviter_.getPendingInvitations().remove(invitation_id_);
  }

  /**
   * Sends an invitation reply to the inviter.
   *
   * @param accepted True if the invitation was accepted.
   */
  private void reply(boolean accepted) {
    user_.getPendingInvitations().remove(invitation_id_);
    inviter_.getUser().userEvent(UserEvent.invitationReply(this, accepted));
    inviter_.getPendingInvitations().remove(invitation_id_);
  }

  private Participant inviter_;
  private UserView user_;
  private Role role_;
  private String invitation_id_;
  private Object extra_;
}
