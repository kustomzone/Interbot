/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

/**
 * Represents the result of an activity invitation.
 * An invitation result is returned to a user that invites another user to an activity.
 * The invitation result specifies whether ht other user has accepted the invitation or wheter
 * any acceptance is pending end-user action.
 *
 * InvitationResult is serializable to JSON.
 */
public class InvitationResult {

  /**
   * Represents the invitation response.
   */
  public enum Response {
    /** The invited user has rejected the invitation. */
    Reject,

    /** The invited user has accepted the invitation and will join the activity. */
    Accept,

    /**
     * The invitation is pending end-user action. An InvitationReply event will be sent once
     * the invitation is accepted or rejected by the end-user.
     */
    Pending,
  }

  /**
   * Creates an invitation result with the specified response and invitation ID.
   * This is an internal constructor. To create an invitation result one of the static methods
   * of the class must be used.
   *
   * @param response The invitation response.
   * @param invitation_id The invitation ID.
   * @param reason If the response is Reject, the rejection reason.
   * @param extra Any activity specific extra information or null.
   */
  private InvitationResult(Response response, String invitation_id, String reason, Object extra) {
    this.response_ = response;
    this.invitation_id_ = invitation_id;
    this.reason_ = reason;
    this.extra_ = extra;
  }

  /**
   * Returns an accepted invitation result.
   *
   * @param participant The participant object associated with the accepted activity.
   * @return An accepted InvitationResult.
   */
  public static InvitationResult accept() {
    return new InvitationResult(Response.Accept, "", "", null);
  }


  /**
   * Returns an accepted invitation result with activity specific extra information.
   *
   * @param participant The participant object associated with the accepted activity.
   * @param extra Any extra information to be returned with the invitation result.
   * @return An accepted InvitationResult.
   */
  public static InvitationResult accept(Object extra) {
    return new InvitationResult(Response.Accept, "", "", extra);
  }

  /**
   * Returns a rejected invitation result.
   *
   * @param reason The reason for rejection.
   * @return A rejected InvitationResult.
   */
  public static InvitationResult reject(String reason) {
    return new InvitationResult(Response.Reject, "", reason, null);
  }

  /**
   * Returns a pending invitation result. A pending invitation result has an invitation ID
   * associated with it which will be used to notify the user of the final inviation result
   * via an InvitationResponse event.
   *
   * @param invitation The invitation object that represents the pending invitation.
   * @return A pending InvitationResult.
   */
  public static InvitationResult pending(Invitation invitation) {
    return new InvitationResult(Response.Pending, invitation.getInvitationId(), "", null);
  }

  /**
   * Returns the invitation response.
   *
   * @return The invitation response.
   */
  public Response getResponse() {
    return response_;
  }

  /**
   * Returns the invitation ID of the invitation. The invitation ID is only set if the invitation
   * is pending. If the invitation has been accepted or rejected, the invitation ID is set to
   * the empty string.
   *
   * @return The participant ID.
   */
  public String getInvitationId() {
    return invitation_id_;
  }

  /**
   * If the invitation has been rejected, returns the rejection reason.
   * Otherwise, returns the empty string.
   *
   * @return The rejection reason.
   */
  public String getReason() {
    return reason_;
  }

  /**
   * Returns any activity specific extra information or null if there is no extra information.
   *
   * @return Activity specific extra information or null.
   */
  public Object getExtra() {
    return extra_;
  }

  private Response response_;
  private String invitation_id_;
  private String reason_;
  private Object extra_;
}
