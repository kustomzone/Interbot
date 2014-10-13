/* Copyright (C) 2013 Tuna Oezer, General AI.
 * All rights reserved.
 */

/**
 * JavaScript Robot API.
 * Sends motion commands to the robot.
 * Periodically sends control pings to the robot.
 *
 * @param channel The WAMP communication channel.
 */
var Robot = function(channel) {
    this.channel_ = channel;
    this.control_ping_topic_ = "event:robot/control/ping";
    this.control_pong_topic_ = "event:robot/control/pong";
    this.base_velocity_topic_ = "event:robot/base/velocity";
    this.video_pan_tilt_topic_ = "event:robot/video/panTilt";
    this.active_ = false;
    this.ping_timer_id_ = 0;
    this.ping_interval_millis_ = 2000;
};

Robot.prototype = {
    /**
     * Starts interaction with the robot. The begin method must be called before any commands
     * can be send to the robot. Any call to begin() must be paired with a call to end().
     */  
    begin: function() {
        if (this.active_) return;
        this.active_ = true;
        this.channel_.subscribe(this.control_pong_topic_, this.onPong.bind(this));
        this.ping_timer_id_ = setInterval(this.ping.bind(this), this.ping_interval_millis_);
        this.ping();
    },

    /**
     * Ends interaction with the robot. The end method must be called to the terminate the
     * interaction with the robot.
     */
    end: function() {
        if (!this.active_) return;
        clearInterval(this.ping_timer_id_);
        this.move(0.0, 0.0);
        this.channel_.unsubscribe(this.control_pong_topic_);
        this.active_ = false;
    },

    /**
     * Updates the velocity of the base of the robot to move at the specified linear angular
     * speeds.
     * A linear speed of -1.0 corresponds to maximum speed backwards.
     * A linear speed of +1.0 corresponds to maximum speed forwards.
     * An angular speed of +1.0 corresponds to maximum speed turn counter clockwise.
     * An angular speed of -1.0 corresponds to maximum speed turn clockwise.
     *
     * @param linear_speed Linear speed between -1.0 and +1.0.
     * @param angular_speed Angular speed between -1.0 and +1.0.
     */
    move: function(linear_speed, angular_speed) {
        if (!this.active_) return;
        var now = new Date();
        this.channel_.publish(this.base_velocity_topic_, {
            timestamp: now.getTime(),
            velocity: {
                linearSpeed: linear_speed,
                angularSpeed: angular_speed
            }
        });
    },

    /**
     * Sends a pan-tilt instruction to the robot camera.
     * The instruction can be one of: Right, Left, Up, Down, or Center.
     *
     * @param pan_tilt_instruction The pan-tilt instruction.
     */
    cameraPanTilt: function(pan_tilt_instruction) {
        if (!this.active_) return;
        var now = new Date();
        this.channel_.publish(this.video_pan_tilt_topic_, {
            timestamp: now.getTime(),
            instruction: pan_tilt_instruction
        });
    },

    /**
     * Periodically sends a control ping to the robot.
     * If the robot does not receive the periodic control ping it will ignore any commands.
     */
    ping: function() {
        if (!this.active_) return;
        var now = new Date();
        this.channel_.publish(this.control_ping_topic_, {
            timestamp: now.getTime()
        });
    },

    /**
     * Handles ping replies by the robot.
     * Computes and displays latency in the ping_result HTML element.
     *
     * @param topic The control pong topic.
     * @param event The pong message.
     */
    onPong: function(topic, event) {
        var now = new Date();
        $("#ping_result").text((now - event.timestamp) / 2);
    },
};
