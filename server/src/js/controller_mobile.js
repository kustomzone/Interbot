/* Copyright (C) 2013 Tuna Oezer, General AI.
 * All rights reserved.
 */

/**
 * Mobile version of controller.
 * Allows setting the speed of the robot using the orientation of a handheld phone.
 * The controller updates the speed of the robot.
 * The controller must be started with the begin() method and stopped with the end() method.
 *
 * @param channel The WAMP communicatons channel.
 */
var Controller = function(channel) {
    this.linear_speed_ = 0.0;
    this.angular_speed_ = 0.0;
    this.last_linear_speed_ = 0.0;
    this.last_angular_speed_ = 0.0;
    this.robot_ = new Robot(channel);
    this.onTouchStartHandler_ = this.onTouchStart.bind(this);
    this.onTouchEndHandler_ = this.onTouchEnd.bind(this);
    this.onOrientationHandler_ = this.onOrientation.bind(this);
    this.touching_ = false;
    this.current_vertical_tilt_ = 0.0;
    this.current_horizontal_tilt_ = 0.0;
    this.start_vertical_tilt_ = 0.0;
    this.start_horizontal_tilt_ = 0.0;
};

Controller.prototype = {
    /**
     * Starts the controller. Activates touch and device orientation input and
     * initializes the robot API.
     */
    begin: function() {
        if (!window.DeviceOrientationEvent) {
            alert("Touch control not supported.");
            return;
        }
        var video_panel = document.getElementById("video_panel");
        video_panel.addEventListener("touchend", this.onTouchEndHandler_, false);
        video_panel.addEventListener("touchstart", this.onTouchStartHandler_, false);
        window.addEventListener("deviceorientation", this.onOrientationHandler_, false);
        this.robot_.begin();
    },

    /**
     * Stops the controller.
     */
    end: function() {
        this.robot_.end();
        window.removeEventListener("deviceorientation", this.onOrientationHandler_, false);
        var video_panel = document.getElementById("video_panel");
        video_panel.removeEventListener("touchstart", this.onTouchStartHandler_, false);
        video_panel.removeEventListener("touchend", this.onTouchEndHandler_, false);
    },

    /**
     * Handles touch start events.
     *
     *
     * @param event The touch start event.
     */
    onTouchStart: function(event) {
        this.linear_speed_ = 0.0;
        this.angular_speed_ = 0.0;
        this.last_linear_speed_ = 0.0;
        this.last_angular_speed_ = 0.0;
        this.start_vertical_tilt_ = this.current_vertical_tilt_;
        this.start_horizontal_tilt_ = this.current_horizontal_tilt_;
        this.robot_.move(0.0, 0.0);
        this.touching_ = true;
    },

    /**
     * Handles touch end events.
     *
     *
     * @param event The touch end event.
     */
    onTouchEnd: function(event) {
        this.touching_ = false;
        this.robot_.move(0.0, 0.0);
        this.linear_speed_ = 0.0;
        this.angular_speed_ = 0.0;
        this.last_linear_speed_ = 0.0;
        this.last_angular_speed_ = 0.0;
    },

    /**
     * Handles device orientation events.
     *
     * @param event The device orientation event.
     */
    onOrientation: function(event) {
        var vertical_tilt = event.beta / 180.0;
        var horizontal_tilt = event.gamma / 90.0;
        if (this.touching_) {
            var delta_vertical = vertical_tilt - this.start_vertical_tilt_;
            var delta_horizontal = horizontal_tilt - this.start_horizontal_tilt_;
            if (delta_vertical == 0 && delta_horizontal == 0) return;

            this.linear_speed_ = delta_vertical * -4.0;
            this.angular_speed_ = delta_horizontal * -2.0;

            if (this.linear_speed_ > 1.0) this.linear_speed_ = 1.0;
            else if (this.linear_speed_ < -1.0) this.linear_speed_ = -1.0;

            if (this.angular_speed_ > 1.0) this.angular_speed_ = 1.0;
            else if (this.angular_speed_ < -1.0) this.angular_speed_ = -1.0;

            if (Math.abs(this.last_linear_speed_ - this.linear_speed_) > 0.05 ||
                Math.abs(this.last_angular_speed_ - this.angular_speed_) > 0.05) {
                var v = this.linear_speed_;
                var w = this.angular_speed_;

                if (v > 0.2) v = (v - 0.2) / 0.8;
                else if (v < -0.2) v = (v + 0.2) / 0.8;
                else v = 0.0;
                v = Math.round(v * 20) / 20;
 
                if (w > 0.2) w = (w - 0.2) / 0.8;
                else if (w < -0.2) w = (w + 0.2) / 0.8;
                else w = 0.0;
                w = Math.round(w * 20) / 20;

                this.robot_.move(v, w);
                this.last_linear_speed_ = this.linear_speed_;
                this.last_angular_speed_ = this.angular_speed_;
            }
        }
        this.current_vertical_tilt_ = vertical_tilt;
        this.current_horizontal_tilt_ = horizontal_tilt;
    },
};
