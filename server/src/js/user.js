/* Copyright (C) 2013 Tuna Oezer, General AI.
 * All rights reserved.
 */

/**
 * Returns true if the specified user can assume the specified role in the specified activity.
 *
 * @param friend The user JSON object.
 * @param activity The name of the activity.
 * @param role The name of the role.
 */
function hasCapability(friend, activity, role) {
    for (var i = 0; i < friend.capabilities.length; i++) {
        if (friend.capabilities[i].activity === activity &&
            friend.capabilities[i].role === role) {
            return true;
        }
    }
    return false;
}

/**
 * Returns true if the friend has the capability to be a robot in a control activity.
 *
 * @param friend Friend object.
 */
function canBeControlled(friend) {
    if (friend.type !== "robot") return false;
    return hasCapability(friend, "control", "robot");
}

/**
 * Returns true if the friend has the WebRTC callee capability.
 *
 * @param friend Friend object.
 */
function canBeWebRtcPeer(friend) {
    return hasCapability(friend, "webrtc", "callee");
}

/**
 * Returns true if the friend can stream video.
 *
 * @param friend Friend object.
 */
function canStreamVideo(friend) {
    return hasCapability(friend, "videostream", "sender");
}

/**
 * Returns the local IP address of the robot or an empty string.
 *
 * @param properties The properties of the robot.
 */
function getRobotLocalIp(properties) {
    if (properties.NetworkInterfaces) {
        for (var i = 0; i < properties.NetworkInterfaces.length; i++) {
            var iface = properties.NetworkInterfaces[i];
            for (var j = 0; j < iface.addresses.length; j++) {
                if (iface.addresses[j].version === "IPv4") {
                    return iface.addresses[j].address;
                }
            }
        }
    }
    return "";
}

/**
 * User API.
 *
 * @param user_info JSON object describing this user.
 * @param session_id The ID of the session under which the user is logged in.
 * @param channel The WAMP communication channel.
 * @param ui_manager The UI status manager.
 * @param friends JSON representation of all friends of this user.
 */
var User = function(user_info, session_id, channel, ui_manager, friends) {
    this.user_info_ = user_info;
    this.session_id_ = session_id;
    this.channel_ = channel;
    this.ui_manager_ = ui_manager;
    this.friends_ = friends;
    this.session_ping_topic_ = "event:session/ping";
    this.session_pong_topic_ = "event:session/pong";
    this.user_event_topic_ = "event:user_event";
    this.p2p_topic_ = null;
    this.web_rtc_ = null;
    this.controller_ = null;
    this.control_activity_ = null;
    this.robot_video_activity_ = null;
    this.webrtc_activity_ = null;
    this.webrtc_invitation_ = null;
    channel.subscribe(this.session_ping_topic_, this.onSessionPing.bind(this));
    channel.subscribe(this.user_event_topic_, this.onUserEvent.bind(this));
    this.open_ = true;
};

User.prototype = {
    /**
     * Closes any open resources.
     */
    close: function() {
        if (!this.open_) return;
        this.endAllActivities();
        this.channel_.unsubscribe(this.user_event_topic_);
        this.channel_.unsubscribe(this.session_ping_topic_);
        this.open_ = false;
    },

    /**
     * Responds to session ping events.
     *
     * @param topic The session ping topic.
     * @param event The session ping event.
     */
    onSessionPing: function(topic, event) {
        this.channel_.publish(this.session_pong_topic_, this.session_id_);
    },

    /**
     * Responds to subscribed user events.
     *
     * @param topic The event topic URI. This is always the user event topic.
     * @param event The event data.
     */
    onUserEvent: function(topic, event) {
        if (!this.open_) return;
        switch (event.type) {
            case "StatusUpdate": this.onStatusUpdate(event); break;
            case "CapabilityUpdate": this.onCapabilityUpdate(event); break;
            case "PropertyUpdate": this.onPropertyUpdate(event); break;
            case "ActivityInvitation": this.onActivityInvitation(event); break;
            case "InvitationReply": this.onInvitationReply(event); break;
            case "CancelInvitation": this.onCancelInvitation(event); break;
            case "JoinActivity": break;
            case "ExitActivity": this.onExitActivity(event); break;
            case "SystemLogout": {
              this.close();
              this.ui_manager_.systemLogout();
              break;
            }
        };
    },

    /**
     * Processes status update events.
     *
     * @param event The status update event.
     */
    onStatusUpdate: function(event) {
        var friend = this.friends_[event.username];
        if (friend == null) return;
        if (event.data.length == 0) return;
        friend.status = event.data[0];
    },

    /**
     * Processes capability update events.
     *
     * @param event The capability update event.
     */
    onCapabilityUpdate: function(event) {
        if (event.username === this.user_info_.username) {
            this.user_info_.capabilities = event.data;
            this.ui_manager_.updateUserCapability(this);
        } else {
            var friend = this.friends_[event.username];
            if (friend == null) return;
            friend.capabilities = event.data;
            this.ui_manager_.updateFriendCapability(friend);
        }
    },

    /**
     * Processes property update events.
     *
     * @param event The property update event.
     */
    onPropertyUpdate: function(event) {
        if (event.data.length < 1) return;
        if (event.username === this.user_info_.username) {
            this.user_info_.properties = event.data[0];
            this.ui_manager_.updateUserProperties(this);
        } else {        
            var friend = this.friends_[event.username];
            if (friend == null) return;
            friend.properties = event.data[0];
            this.ui_manager_.updateFriendProperties(friend);
        }
    },

    /**
     * Processes an incoming invitation to join an activity.
     *
     * @param event The invitation event.
     */
    onActivityInvitation: function(event) {
        if (event.data.length < 3) return;
        var invitation = new Invitation(this.channel_, this.session_id_, event.data[2]);
        var friend = this.friends_[event.username];
        // Currently, all invitations are WebRTC invitations.
        if (this.webrtc_invitation_ != null ||
            friend == null ||
            event.data.length !== 4 ||
            event.data[0] !== "webrtc" ||
            event.data[1] !== "callee") {
            invitation.reject();
            return;
        }
        this.p2p_topic_ = event.data[3];
        this.webrtc_invitation_ = invitation;
        this.ui_manager_.receiveWebRtcCall(friend);
    },

    /**
     * Processes an incoming invitation reply event.
     *
     * @param event The invitation reply event.
     */
    onInvitationReply: function(event) {
        // Currently, all invitations are WebRTC invitations.
        if (this.webrtc_activity_ == null) return;
        if (event.data.length !== 3) return;
        if (this.webrtc_activity_.receiveInvitationResponse(event.data[0])) {
            if (event.data[1]) {
                this.p2p_topic_ = event.data[2];
                this.ui_manager_.startWebRtcCall();
                if (g_use_webrtc) {
                    this.newWebRTC();
                    this.web_rtc_.call();
                }
            } else {
                this.webrtc_activity_.exitActivity();
                this.ui_manager_.hangupWebRtcCall(true);
            }
        }
    },

    /**
     * Processes an incoming cancel invitation event.
     *
     * @param event The invitation to cancel.
     */
    onCancelInvitation: function(event) {
        if (event.data.length < 1) return;
        // Currently, all invitations are WebRTC invitations.
        if (this.webrtc_invitation_ == null ||
            this.webrtc_invitation_.invitation_id_ !== event.data[0]) return;
        this.ui_manager_.hangupWebRtcCall(true);
        this.webrtc_invitation_ = null;
    },

    /**
     * Processes an incoming exit activity event.
     *
     * @param event The incoming exit activity event.
     */
    onExitActivity: function(event) {
        if (event.data.length !== 2) return;
        if (this.webrtc_activity_ != null &&
            this.webrtc_activity_.activity_id_ === event.data[0]) {
            if (g_use_webrtc) {
                if (this.web_rtc_ != null) {
                    this.web_rtc_.hangup();
                    this.web_rtc_ = null;
                }
            }
            this.ui_manager_.hangupWebRtcCall(true);
            this.webrtc_activity_.exitActivity();
            this.webrtc_activity_ = null;
        } else if (this.robot_video_activity_ != null &&
                   this.robot_video_activity_.activity_id_ === event.data[0]) {
            this.robot_video_activity_.exitActivity();
            this.robot_video_activity_ = null;
            this.ui_manager_.endRobotVideo();
        } else if (this.control_activity_ != null &&
                   this.control_activity_.activity_id_ === event.data[0]) {
            if (this.controller_ != null) {
                this.controller_.end();
                this.controller_ = null;
            }
            this.control_activity_.exitActivity();
            this.control_activity_ = null;
            this.ui_manager_.endControl(true);
        }
    },

    /**
     * Begins a control activity with a robot.
     * The started activity must be stopped with endControl().
     *
     * @param username Username of robot to control.
     */
    beginControl: function(username) {
        if (!this.open_) return;
        var friend = this.friends_[username];
        if (friend == null) return;
        if (!canBeControlled(friend)) {
            alert("Cannot start control activity with " + username + ".");
            return;
        }
        this.control_activity_ = new Activity(this.channel_, this.session_id_);
        var self = this;
        this.control_activity_.startActivityWithUser(
            "control",
            "controller",
            friend.username,
            "robot",
            function() {
                self.controller_ = new Controller(self.channel_);
                self.controller_.begin();
                self.ui_manager_.startControl();
            },
            null,
            function() {
                self.control_activity_ = null;
            });
    },

    /**
     * Ends the control activity started with beginControl().
     */
    endControl: function() {
        if (this.control_activity_ != null) {
            if (this.controller_ != null) {
                this.controller_.end();
                this.controller_ = null;
            }
            this.control_activity_.exitActivity();
            this.control_activity_ = null;
            this.ui_manager_.endControl(false);
        }
    },

    /**
     * Begins a video stream activity with a robot.
     *
     * @param username The username of the robot.
     */
    beginRobotVideo: function(username) {
        if (!this.open_) return;
        var friend = this.friends_[username];
        if (friend == null) return;
        if (!canStreamVideo(friend)) {
            alert("Robot does not have this capability.");
            return;
        }
        this.robot_video_activity_ = new Activity(this.channel_, this.session_id_);
        var self = this;
        this.robot_video_activity_.startActivityWithUser(
            "videostream",
            "receiver",
            friend.username,
            "sender",
            function(video_channel) {
                self.ui_manager_.startRobotVideo();
                $("#robot_video").attr("src",
                                       "/interbot/video/out?session_id=" + self.session_id_ +
                                       "&channel=" + video_channel);
            },
            null,
            function() {
                self.robot_video_activity_ = null;
            });
    },

    /**
     * Ends the video stream activity started with beginRobotVideo().
     */
    endRobotVideo: function() {
        if (this.robot_video_activity_ != null) {
            this.robot_video_activity_.exitActivity();
            this.robot_video_activity_ = null;
            this.ui_manager_.endRobotVideo();
        }
    },

    /**
     * Begins a WebRTC video activity with another user.
     * The started activity must be stopped with endWebRtcVideo().
     *
     * @param username Username of user with whom to start a WebRTC video activity.
     */
    beginWebRtcVideo: function(username) {
        if (!this.open_) return;
        var friend = this.friends_[username];
        if (friend == null) return;
        if (!canBeWebRtcPeer(friend)) {
            alert(username + " cannot receive a WebRTC call at this time.");
            return;
        }
        this.webrtc_activity_ = new Activity(this.channel_, this.session_id_);
        var self = this;
        this.webrtc_activity_.startActivityWithUser(
            "webrtc",
            "caller",
            friend.username,
            "callee",
            null,
            function() {
                self.ui_manager_.makeWebRtcCall(friend);
            },
            function() {
                self.webrtc_activity_ = null;
            });
    },

    /**
     * Ends the WebRTC video activity started with beginWebRtcVideo().
     */
    endWebRtcVideo: function() {
        if (this.webrtc_activity_ != null) {
            if (this.web_rtc_ != null) {
                this.web_rtc_.hangup();
                this.web_rtc_ = null;
            }
            this.webrtc_activity_.exitActivity();
            this.ui_manager_.hangupWebRtcCall(false);
            this.webrtc_activity_ = null;
        }
    },

    /**
     * Ends all current activities.
     */
    endAllActivities: function() {
        this.endWebRtcVideo();
        this.endRobotVideo();
        this.endControl();
    },

    /**
     * Signals the server that an incoming WebRTC call is accepted by the user.
     */
    acceptWebRtcCall: function() {
        if (!this.open_) return;
        if (this.ui_manager_.call_status_ !== CallStatus.Receiving) return;
        if (this.webrtc_invitation_ == null) return;
        this.ui_manager_.startWebRtcCall();
        if (g_use_webrtc) {
            var self = this;
            this.newWebRTC(function() {
                self.webrtc_activity_ = self.webrtc_invitation_.accept();
                self.webrtc_invitation_ = null;
            });
            this.web_rtc_.listen();
        } else {
            this.webrtc_activity_ = this.webrtc_invitation_.accept();
            this.webrtc_invitation_ = null;
        }
    },

    /**
     * Rejects an incoming WebRTC call.
     */
    rejectWebRtcCall: function() {
        if (this.webrtc_invitation_ != null) {
            this.webrtc_invitation_.reject();
            this.webrtc_invitation_ = null;
        }
    },

    /**
     * Creates and initializes a new WebRTC instance.
     *
     * @param onReady Callback to be called when the WebRTC instance is ready to start.
     */
    newWebRTC: function(onReady) {
        if (!this.open_) return;
        var webrtc_config = webRtcDefaultConfig();
        webrtc_config.ice_servers = g_ice_servers;
        webrtc_config.p2p_channel = this.channel_;
        webrtc_config.p2p_topic = this.p2p_topic_;
        webrtc_config.remote_video_display = "remote_video";
        webrtc_config.local_video_display = "local_video";
        webrtc_config.onReady = typeof onReady === "undefined" ? null : onReady;
        this.web_rtc_ = new WebRTC(webrtc_config);
    },

    /**
     * Updates the passowrd of this user.
     * Displays a confirmation that informs the user whether the password has been updated.
     *
     * @param old_password The current password of the user.
     * @param new_password The new password of the user.
     */
    setPassword: function(old_password, new_password) {
        if (!checkString(old_password) || !checkString(new_password)) {
            alert("Error: Invalid password.");
            return;
        }
        this.channel_.call("rpc:user_service/setPassword",
                           this.session_id_,
                           SHA1(old_password + this.user_info_.username),
                           SHA1(new_password + this.user_info_.username)).then(
                               function(result) {
                                   if (result) {
                                       alert("Password has been updated.");
                                   } else {
                                       alert("Error: Failed to change password.");
                                   }
                               },
                               function(error, description) {
                                   alert("Error: Failed to change password.");
                               });
    },
};
