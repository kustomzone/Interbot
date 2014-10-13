/* Copyright (C) 2013 Tuna Oezer, General AI.
 * All rights reserved.
 */

/**
 * Returns true if the browser is currently in full screen mode.
 * Unifies API accross browsers.
 *
 * @return True if the browser is in full screen mode.
 */
function inFullScreenMode() {
    return document.fullScreen ||
        document.webkitIsFullScreen ||
        document.mozFullScreen;
}

/**
 * Enters full screen mode. Unifies API accross browsers.
 * This method puts the robot control view into full screen mode.
 */
function enterFullScreen() {
    var panel = $("#robot_control_view");
    if (panel[0].requestFullScreen) {
        panel[0].requestFullScreen();
    } else if (panel[0].webkitRequestFullScreen) {
        panel[0].webkitRequestFullScreen();
    } else if (panel[0].mozRequestFullScreen) {
        panel[0].mozRequestFullScreen();
    }
}

/**
 * Exits full screen mode. Unifies API accross browsers.
 */
function exitFullScreen() {
    if (document.cancelFullScreen) {
        document.cancelFullScreen();
    } else if (document.webkitCancelFullScreen) {
        document.webkitCancelFullScreen();
    } else if (document.mozCancelFullScreen) {
        document.mozCancelFullScreen();
    }
}

/**
 * Sets a status label to the specified status.
 *
 * @param status_label The status label DOM object.
 * @param online True if status is online, false if offline.
 */
function setStatusLabel(status_label, online) {
    if (online) {
        status_label.text("online");
        status_label.removeClass("text_offline");
        status_label.addClass("text_online");
    } else {
        status_label.text("offline");
        status_label.removeClass("text_online");
        status_label.addClass("text_offline");
    }
}

/**
 * Fills in the robot capabilitiy properties in the properties table on the robot detail view.
 * Displays the control button if the robot is connected, unless the robot is the same as
 * the logged in user.
 *
 * @param robot The robot to display.
 */
function fillCapabilityProperties(robot) {
    if (canBeControlled(robot)) {
        setStatusLabel($("#property_value_robot_status"), true);
        if (robot.username !== g_user_info.username) {
            $("#button_robot_detail_start_control").removeClass("button_start_control_hidden");
        }
    } else {
        setStatusLabel($("#property_value_robot_status"), false);
        if (robot.username !== g_user_info.username) {
            $("#button_robot_detail_start_control").addClass("button_start_control_hidden");
        }
    }
    setStatusLabel($("#property_value_webrtc_status"), canBeWebRtcPeer(robot));
}

/**
 * Fills in the robot properties table on the robot detail view.
 *
 * @param robot The robot to display.
 */
function fillRobotProperties(robot) {
    $("#property_value_username").text(robot.username);
    fillCapabilityProperties(robot);
    var local_ip = getRobotLocalIp(robot.properties);
    var local_webpage_link = $("#property_value_local_ip");
    local_webpage_link.text(local_ip);
    local_webpage_link.attr("href", "http://" + local_ip);
}

/**
 * The list of views.
 * The view values are identical to the HTML tag id's of the spans that contain the views.
 * The view values must be prefixed with a '#' character to allow querying the views.
 */
var Views = {
    RobotList: "#robot_list_view",
    RobotDetails: "#robot_detail_view",
    RobotControl: "#robot_control_view",
};

/**
 * The current call status for WebRTC calls.
 */
var CallStatus = {
    Ready: "ready",
    Connecting: "connecting",
    Receiving: "receiving",
    InCall: "incall",
};

/**
 * Manages the state of the UI.
 */
var UIManager = function() {
    this.current_view_ = g_user_info.type === "Robot" ? Views.RobotDetails : Views.RobotList;
    this.call_status_ = CallStatus.Ready;
    this.active_robot_ = null;
    this.webrtc_peer_ = null;
};

UIManager.prototype = {
    /**
     * Shows the specified view. The view is on of the views specified in the Views enum.
     *
     * @param view One of the views from the Views enum.
     */
    showView: function(view) {
        if (this.current_view_ === Views.RobotControl && inFullScreenMode()) {
            exitFullScreen();
        }
        var view_element = $(this.current_view_);
        view_element.removeClass("view_selected");
        view_element.addClass("view_unselected");
        this.current_view_ = view;
        view_element = $(this.current_view_);
        view_element.removeClass("view_unselected");
        view_element.addClass("view_selected");
    },

    /**
     * Updates any displayed information about the capabilities of this user.
     * This method should be only called if this user is a robot user.
     *
     * @param user This user.
     */
    updateUserCapability: function(user) {
        fillCapabilityProperties(user.user_info_);
    },

    /**
     * Updates any displayed information about a friend's capability.
     *
     * @param friend Friend object with updadted capabilities.
     */
    updateFriendCapability: function(friend) {
        var user_status_cell = $("[id='user_status_" + friend.username + "']");
        var user_row = $("[id='user_list_row_" + friend.username + "']");
        if (user_status_cell == null || user_row == null) return;
        var control_button = $("[id='button_start_control_" + friend.username + "']");
        if (canBeControlled(friend)) {
            user_status_cell.text("Online");
            user_row.removeClass("user_list_row_Offline");
            user_row.addClass("user_list_row_Online");
            control_button.removeClass("button_start_control_hidden");
        } else {
            user_status_cell.text("Offline");
            user_row.removeClass("user_list_row_Online");
            user_row.addClass("user_list_row_Offline");
            control_button.addClass("button_start_control_hidden");
        }
        if (this.current_view_ === Views.RobotDetails &&
            this.active_robot_ != null &&
            this.active_robot_.username === friend.username) {
            fillCapabilityProperties(friend);
        }
    },

    /**
     * Updates any displayed information about the properties of this user.
     * This method should be only called if this user is a robot user.
     *
     * @param user This user.
     */
    updateUserProperties: function(user) {
        fillRobotProperties(user.user_info_);
    },

    /**
     * Updates any displayed information about a friend's properties.
     */
    updateFriendProperties: function(friend) {
        if (this.current_view_ === Views.RobotDetails &&
            this.active_robot_ != null &&
            this.active_robot_.username === friend.username) {
            fillRobotProperties(friend);
        }
    },

    /**
     * Called when a control activity has started. Displays the control UI.
     */
    startControl: function() {
        this.showView(Views.RobotControl);
    },

    /**
     * Called when a control activity has ended. Closes the control UI.
     *
     * @param is_remotely_terminated True if the control activity was remotely terminated.
     */
    endControl: function(is_remotely_terminated) {
        if (is_remotely_terminated) {
            $("#dialog_control_terminated").dialog("open");
        }
        this.showView(Views.RobotList);
    },

    /**
     * Called when a video stream activity has started. Updates the UI.
     */
    startRobotVideo: function() {
        $("#button_video").attr("title", "Stop Robot Video");
        $("#button_video_image").attr("src", "../img/camera_stop.jpeg");
    },

    /**
     * Called when a video stream activity has ended. Updates the UI.
     */
    endRobotVideo: function() {
        $("#button_video").attr("title", "Start Robot Video");
        $("#button_video_image").attr("src", "../img/camera.jpeg");
    },

    /**
     * Enters WebRTC calling mode. Waits for call to be accepted or aborted.
     *
     * @param callee The user who is being called by this user.
     */
    makeWebRtcCall: function(callee) {
        this.call_status_ = CallStatus.Connecting;
        this.webrtc_peer_ = callee;
        $("#dialog_calling_username").text(callee.username);
        $("#dialog_calling").dialog("open");
    },

    /**
     * Enters receive incoming WebRTC call mode. Displays a dialog box to allow user to accept
     * the call.
     *
     * @param caller Friend object that represents the caller.
     */
    receiveWebRtcCall: function(caller) {
        this.call_status_ = CallStatus.Receiving;
        this.webrtc_peer_ = caller;
        $("#dialog_receiving_username").text(caller.username);
        $("#dialog_receiving").dialog("open");
        $("#sound_ring")[0].play();
    },

    /**
     * Updates the UI context to reflect that a WebRTC call is started. Closes any call related
     * dialog boxes and stop any ring tones.
     */
    startWebRtcCall: function() {
        switch (this.call_status_) {
          case CallStatus.Connecting:
            $("#sound_calling")[0].pause();
            $("#dialog_calling").dialog("close");
            break;
          case CallStatus.Receiving:
            $("#sound_ring")[0].pause();
            break;
        }
        this.call_status_ = CallStatus.InCall;
        $("#button_webrtc").attr("title", "Hangup WebRTC");
        $("#button_webrtc_image").attr("src", "../img/hangup.jpeg");
        if (this.current_view_ !== Views.RobotControl) {
            this.showView(Views.RobotControl);
        }
    },

    /**
     * Exits or aborts a WebRTC call and returns to default mode.
     *
     * @param is_remote_hangup True if the call was hang up by the WebRTC peer.
     */
    hangupWebRtcCall: function(is_remote_hangup) {
        switch (this.call_status_) {
          case CallStatus.Connecting:
            $("#sound_calling")[0].pause();
            if (is_remote_hangup) {
                $("#dialog_calling").dialog("close");
                $("#dialog_call_declined").dialog("open");
            }
            break;
          case CallStatus.Receiving:
            $("#sound_ring")[0].pause();
            $("#dialog_receiving").dialog("close");
            break;
          case CallStatus.InCall:
            $("#button_webrtc").attr("title", "WebRTC Call");
            $("#button_webrtc_image").attr("src", "../img/call.jpeg");
            if (g_user_info.type === "robot") {
                this.showView(Views.RobotDetails);
            }
            break;
        }
        this.call_status_ = CallStatus.Ready;
    },

    /**
     * Displays a dialog box that informs the user that a system logout has occurred.
     * Initiates a local logout in order to clean up resources.
     */
    systemLogout: function() {
        alert("You have been logged out by an administrator.");
        document.logout_form.submit();
    },
};

/**
 * Main method.
 */
$(document).ready(function() {
    var channel = new Channel(g_user_info.username, g_session_id);
    var ui_manager = new UIManager();
    var user = null;
    channel.open(function() {
        user = new User(g_user_info, g_session_id, channel, ui_manager, g_friends);
        if (user.user_info_.type === "robot") {
            fillRobotProperties(user.user_info_);
        }
    });

    // Dialog displayed when a call is initiated.
    $("#dialog_calling").dialog({
        autoOpen: false,
        width: 400,
        resizable: false,
        modal: true,
        buttons: {
            "Abort Call": function() {
                $(this).dialog("close");
                user.endWebRtcVideo();
            },
        },
    });

    // Dialog displayed when a call is received.
    $("#dialog_receiving").dialog({
        autoOpen: false,
        width: 400,
        resizable: false,
        modal: true,
        buttons: {
            "Accept Call": function() {
                $(this).dialog("close");
                user.acceptWebRtcCall();
            },
            "Decline Call": function() {
                $(this).dialog("close");
                user.rejectWebRtcCall();
            },
        },
    });

    // Dialog displayed when a call is declined by the remote user.
    $("#dialog_call_declined").dialog({
        autoOpen: false,
        width: 400,
        resizable: false,
        modal: true,
        buttons: {
            "OK": function() {
                $(this).dialog("close");
            },
        },
    });

    // Dialog displayed when a control activity was remotely terminated.
    $("#dialog_control_terminated").dialog({
        autoOpen: false,
        width: 400,
        resizable: false,
        modal: true,
        buttons: {
            "OK": function() {
                $(this).dialog("close");
            },
        },
    });

    // Dialog displayed to edit account settings.
    $("#dialog_account_settings").dialog({
        autoOpen: false,
        width: 400,
        resizable: true,
        modal: true,
        buttons: {
            "Close": function() {
                $(this).dialog("close");
            },
        },
    });

    // Dialog displayed to change password.
    $("#dialog_change_password").dialog({
        autoOpen: false,
        width: 320,
        height: 250,
        resizable: true,
        modal: true,
        buttons: {
            "Submit": function() {
                $(this).dialog("close");
                if (user != null) {
                    user.setPassword($("#current_password").val(), $("#new_password").val());
                } else {
                    alert("Error: Failed to change password.");
                }
            },
            "Cancel": function() {
                $(this).dialog("close");
            },
        },
    });

    // Opens up a dialog that allows editing account settings.
    $("#username").click(function() {
        $("#dialog_account_settings").dialog("open");
    });

    // Opens up a dialog that allows changing the user password.
    $("#dialog_account_settings_change_password").click(function() {
        $("#dialog_account_settings").dialog("close");
        $("#dialog_change_password").dialog("open");
    });

    // Logout button handler.
    $("#button_logout").click(function() {
        if (user != null) user.close();
        if (channel != null) channel.close();
        document.logout_form.submit();
    });

    // Handles clicks on a user table row. Displays details about a user.
    $(".user_list_row").click(function() {
        if (user == null) return;
        var username = $(this).attr("username");
        var friend = user.friends_[username];
        if (friend == null) return;
        ui_manager.active_robot_ = friend;
        fillRobotProperties(user.friends_[username]);
        $("#button_robot_detail_start_control").attr("username", username);
        ui_manager.showView(Views.RobotDetails);
    });

    // Control button handler for control buttons located in robot list table.
    $(".button_start_control").click(function(event) {
        event.stopPropagation();
        if (user == null) return;
        var friend = user.friends_[$(this).attr("username")];
        if (friend == null) return;
        ui_manager.active_robot_ = friend;
        user.beginControl(friend.username);
    });

    // Control button handler for control button on robot details view page.
    $("#button_robot_detail_start_control").click(function() {
        if (user == null) return;
        var friend = user.friends_[$(this).attr("username")];
        if (friend == null) return;
        ui_manager.active_robot_ = friend;
        user.beginControl(friend.username);
    });

    // Returns to the robot list view from the robot details view.
    $("#link_return_to_robot_list_view").click(function() {
        ui_manager.showView(Views.RobotList);
        ui_manager.active_robot_ = null;
    });

    // Disconnect button handler.
    $("#button_disconnect").click(function() {
        if (user != null) {
            user.endAllActivities();
        }
        if (user.user_info_.type === "robot") {
            ui_manager.showView(Views.RobotDetails);
        } else {
            ui_manager.showView(Views.RobotList);
        }
        ui_manager.active_robot_ = null;
    });

    // Robot video button handler.
    $("#button_video").click(function() {
        if (user == null) return;
        var friend = ui_manager.active_robot_;
        if (friend == null) return;
        if (user.robot_video_activity_ != null) {
            user.endRobotVideo(friend.username);
        } else {
            user.beginRobotVideo(friend.username);
        }
    });

    // WebRTC button handler.
    $("#button_webrtc").click(function() {
        if (user == null) return;
        switch (ui_manager.call_status_) {
          case CallStatus.Ready: {
            var friend = ui_manager.active_robot_;
            if (friend == null) return;
            user.beginWebRtcVideo(friend.username);
            $("#sound_calling")[0].play();
            break;
          }
          case CallStatus.InCall:
            user.endWebRtcVideo();
            break;
        }
    });

    // Fullscreen button handler.
    $("#button_fullscreen").click(function() {
        if (inFullScreenMode()) {
            exitFullScreen();
        } else {
            enterFullScreen();
        }
    });

    // Handles switch into or exit from full screen mode.
    $(document).on("fullscreenchange webkitfullscreenchange mozfullscreenchange", function(event) {
        setTimeout(function() {
            if (inFullScreenMode()) {
                $("#button_fullscreen").attr("title", "Exit Fullscreen");
                $("#button_fullscreen_image").attr("src", "../img/exit_fullscreen.jpeg");
            } else {
                $("#button_fullscreen").attr("title", "Enter Fullscreen");
                $("#button_fullscreen_image").attr("src", "../img/enter_fullscreen.jpeg");
            }
        }, 100);
    });

    // Loops the sounds.
    $("#sound_ring").bind("ended", function() {
        this.currentTime = 0;
        this.play();
    });

    // Loops the sounds.
    $("#sound_calling").bind("ended", function() {
        this.currentTime = 0;
        this.play();
    });

    // Android workaround. Sounds can only be loaded when the user actively interacts with the UI.
    var android_initialized = false;
    $("#root_pane").click(function() {
        if (navigator.userAgent.match(/Android/i) &&
            !android_initialized) {
            $("#sound_ring")[0].play();
            $("#sound_ring")[0].pause();
            android_initialized = true;
        }
    });

    window.onbeforeunload = function() {
        if (user != null) {
            user.close();
            user = null;
        }
        if (channel != null) {
            channel.close();
            channel = null;
        }
    };

    $(window).unload(function() {
        if (user != null) user.close();
        if (channel != null) channel.close();
    });
});
