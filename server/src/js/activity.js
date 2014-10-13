/* Copyright (C) 2013 Tuna Oezer, General AI.
 * All rights reserved.
 */

/**
 * Represents the activity API.
 *
 * @params channel The communication channel.
 * @param session_id The session ID.
 */
var Activity = function(channel, session_id) {
    this.channel_ = channel;
    this.session_id_ = session_id;
    this.activity_name_ = null;
    this.role_name_ = null;
    this.activity_id_ = null;
    this.participant_id_ = null;
    this.invitations_ = [];
};

Activity.prototype = {
    /**
     * Starts an activity with another user.
     *
     * @param activity_name The name of the activity.
     * @param role_name The name of the role of this user.
     * @param username The username of the other user to invite to the activity.
     * @param friend_role_name The role of the invited user.
     * @param on_success Callback to be executed when the joint activity has succesfully started.
     * @param on_pending Callback to be executed when the invitation to the user is pending.
     * @param on_error Callback to be executed when the joint activity cannot be started.
     */
    startActivityWithUser: function(activity_name,
                                    role_name,
                                    username,
                                    friend_role_name,
                                    on_success,
                                    on_pending,
                                    on_error) {
        var self = this;
        this.startActivity(activity_name,
                           role_name,
                           this.invite.bind(this,
                                            username,
                                            friend_role_name,
                                            on_success,
                                            on_pending,
                                            function() {
                                                self.exitActivity();
                                                on_error();
                                            }),
                           on_error);
    },

    /**
     * Starts an activity with the activity name assuming the specified role.
     *
     * @param activity_name The name of the activity.
     * @param role_name The name of the role.
     * @param on_success Callback to be executed when the activity has succesfully started.
     * @param on_error Callback to be executed when the activity cannot be started.
     */
    startActivity: function(activity_name, role_name, on_success, on_error) {
        this.activity_name_ = activity_name;
        this.role_name_ = role_name;
        this.channel_.call("rpc:user_service/startActivity",
                           this.session_id_,
                           activity_name,
                           role_name).then(
                               this.onActivityStart.bind(this, on_success),
                               this.onActivityStartError.bind(this, on_error)
                           );
    },

    /**
     * Exits a started activity.
     */
    exitActivity: function() {
        if (this.activity_id_ == null || this.participant_id_ == null) return;
        this.channel_.call("rpc:user_service/exitActivity",
                          this.session_id_,
                          this.participant_id_);
        this.activity_id_ = null;
        this.participant_id_ = null;
    },

    /**
     * Invites another user to this activity.
     *
     * @param username Username of user to invite.
     * @param role_name Name of role of user to invite.
     * @param on_accept Callback to be executed when the invitation was accepted.
     * @param on_pending Callback to be executed when the invitation is pending.
     * @param on_reject Callback to be executed when the invitation was rejected.
     */
    invite: function(username, role_name, on_accept, on_pending, on_reject) {
        if (this.activity_id_ == null || this.participant_id_ == null) return;
        this.channel_.call(
            "rpc:user_service/invite",
            this.session_id_,
            this.participant_id_,
            username,
            role_name).then(
                this.onInvitationResult.bind(this, on_accept, on_pending, on_reject));
    },

    /**
     * Handles the reception of an invitation response.
     *
     * @return True if the invitation ID is valid.
     */
    receiveInvitationResponse: function(invitation_id) {
        for (var i = 0; i < this.invitations_.length; i++) {
            if (this.invitations_[i] === invitation_id) {
                this.invitations_.splice(i, 1);
                return true;
            }
        }
        return false;
    },

    /**
     * Handles activity start results.
     *
     * @param continuation The method to call to continue execution.
     * @param activity_start_info ActivityStartInfo returned by server.
     */
    onActivityStart: function(continuation, activity_start_info) {
        this.activity_id_ = activity_start_info.activityId;
        this.participant_id_ = activity_start_info.participantId;
        continuation();
    },

    /**
     * Handles activity start errors.
     *
     * @param continuation The method to call to continue execution.
     * @param error Error returned by server.
     * @param description Description returned by server.
     */
    onActivityStartError: function(continuation, error, description) {
        alert(description);
        continuation();
    },

    /**
     * Handles invitation results.
     *
     * @param on_accept The method to call to continue execution when the invitation was accepted.
     * @param on_pending The method to call to continue execution when the invitation is pending.
     * @param on_reject The method to call to continue execution when the invitation was rejected.
     * @param result The invitation result returned by the server.
     */
    onInvitationResult: function(on_accept, on_pending, on_reject, result) {
        switch (result.response) {
          case "Accept":
            if (on_accept != null) {
                on_accept(result.extra);
            }
            break;
          case "Pending":
            this.invitations_.push(result.invitationId);
            if (on_pending != null) {
                on_pending();
            }
            break;
          case "Reject":
            alert(result.reason);
            if (on_reject != null) {
                on_reject();
            }
            break;
        }
    },
};

/**
 * Represents a pending invitation that this user must reply to.
 *
 * @params channel The communication channel.
 * @param session_id The session ID.
 * @param invitation_id The invitation ID.
 */
var Invitation = function(channel, session_id, invitation_id) {
    this.channel_ = channel;
    this.session_id_ = session_id;
    this.invitation_id_ = invitation_id;
};

Invitation.prototype = {
    /**
     * Accepts the invitation.
     * The properties of the returned activity are asynchronously populated as the server
     * returns more information.
     *
     * @return The activity that has been joined.
     */
    accept: function() {
        var activity = new Activity(this.channel_, this.session_id_);
        this.channel_.call(
            "rpc:user_service/invitationReply",
            this.session_id_,
            this.invitation_id_,
            true).then(
                function (activity_start_info) {
                    activity.activity_id_ = activity_start_info.activityId;
                    activity.participant_id_ = activity_start_info.participantId;
                }
            );
        return activity;
    },

    /**
     * Rejects the invitation.
     */
    reject: function() {
        this.channel_.call(
            "rpc:user_service/invitationReply",
            this.session_id_,
            this.invitation_id_,
            false);
    },
};
