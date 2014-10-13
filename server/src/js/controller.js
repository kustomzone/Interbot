/* Copyright (C) 2013 Tuna Oezer, General AI.
 * All rights reserved.
 */

/**
 * Allows setting the speed of the robot using the keyboard.
 * The controller updates the speed of the robot.
 * The controller must be started with the begin() method and stopped with the end() method.
 *
 * The robot moves if a key is held pressed. When movement keys are released, the robot stops.
 *
 * Controller keys: arrow keys, numlock keys or WASD.
 * Shift key turns on fast mode.
 * Ctrl key turns on slow mode.
 *
 * @param channel The WAMP communicatons channel.
 */
var Controller = function(channel) {
    this.linear_slow_speed_ = 0.2;
    this.linear_normal_speed_ = 0.6;
    this.linear_fast_speed_ = 1.0;

    this.angular_slow_speed_ = 0.2;
    this.angular_normal_speed_ = 0.5;
    this.angular_fast_speed_ = 1.0;

    this.linear_speed_ = 0.0;
    this.angular_speed_ = 0.0;
    this.last_linear_speed_ = 0.0;
    this.last_angular_speed_ = 0.0;

    this.linear_speed_factor_ = 0.0;
    this.angular_speed_factor_ = 0.0;

    this.robot_ = new Robot(channel);
    $(window).keydown(this.onKeyDown.bind(this));
    $(window).keyup(this.onKeyUp.bind(this));
};

Controller.prototype = {
    /**
     * Starts the controller. Activates keyboard input and initializes the robot API.
     */
    begin: function() {
        this.robot_.begin();
    },

    /**
     * Stops the controller.
     */
    end: function() {
        this.robot_.end();
    },

    /**
     * Handles keydown events.
     *
     * @param event The keydown event.
     */
    onKeyDown: function(event) {
        event.preventDefault();
        switch (event.keyCode) {
          case 38:  // up
          case 104: // numpad 8 (numlock)
            // forward
            this.linear_speed_factor_ = 1.0;
            break;
          case 40:  // down
          case 98:  // numpad 2 (numlock)
            // backward
            this.linear_speed_factor_ = -1.0;
            break;
          case 39:  // right
          case 102:  // numpad 6 (numlock)
            // right
            this.angular_speed_factor_ = -1.0;
            break;
          case 37:  // left
          case 100: // numpad 4 (numlock)
            this.angular_speed_factor_ = 1.0;
            break;
          case 16:  // shift
          case 17:  // control
            break;
          case 87:  // w
            this.robot_.cameraPanTilt("Up");
            return;
          case 65:  // a
            this.robot_.cameraPanTilt("Left");
            return;
          case 83:  // s
            this.robot_.cameraPanTilt("Down");
            return;
          case 68:  // d
            this.robot_.cameraPanTilt("Right");
            return;
          case 90:  // z
            this.robot_.cameraPanTilt("Center");
            return;
          default:
            this.linear_speed_factor_ = 0.0;
            this.angular_speed_factor_ = 0.0;
            break;
        }
        if (event.shiftKey) {
            this.linear_speed_ = this.linear_speed_factor_ * this.linear_fast_speed_;
            this.angular_speed_ = this.angular_speed_factor_ * this.angular_fast_speed_;
        } else if (event.ctrlKey) {
            this.linear_speed_ = this.linear_speed_factor_ * this.linear_slow_speed_;
            this.angular_speed_ = this.angular_speed_factor_ * this.angular_slow_speed_;
        } else {
            this.linear_speed_ = this.linear_speed_factor_ * this.linear_normal_speed_;
            this.angular_speed_ = this.angular_speed_factor_ * this.angular_normal_speed_;
        }
        if (this.linear_speed_ !== this.last_linear_speed_ ||
            this.angular_speed_ !== this.last_angular_speed_) {
            this.robot_.move(this.linear_speed_, this.angular_speed_);
            this.last_linear_speed_ = this.linear_speed_;
            this.last_angular_speed_ = this.angular_speed_;
        }
    },

    /**
     * Handles keyup events.
     *
     * @param event The keyup event.
     */
    onKeyUp: function(event) {
        event.preventDefault();
        switch (event.keyCode) {
          case 38:  // up
          case 104: // numpad 8 (numlock)
          case 40:  // down
          case 98:  // numpad 2 (numlock)
            this.linear_speed_factor_ = 0.0;
            this.linear_speed_ = 0.0;
            break;
          case 39:  // right
          case 102:  // numpad 6 (numlock)
          case 37:  // left
          case 100: // numpad 4 (numlock)
            this.angular_speed_factor_ = 0.0;
            this.angular_speed_ = 0.0;
            break;
          case 16:
          case 17:
             // ignore shift or control key up
            this.linear_speed_ = this.linear_speed_factor_ * this.linear_normal_speed_;
            this.angular_speed_ = this.angular_speed_factor_ * this.angular_normal_speed_;
            break;
          case 65:  // a
          case 68:  // d
          case 83:  // s
          case 87:  // w
             // ignore pan-tilt keys up
            return;
          default:
            this.linear_speed_factor_ = 0.0;
            this.angular_speed_factor_ = 0.0;
            this.linear_speed_ = 0.0;
            this.angular_speed_ = 0.0;
            break;
        }
        this.robot_.move(this.linear_speed_, this.angular_speed_);
        this.last_linear_speed_ = this.linear_speed_;
        this.last_angular_speed_ = this.angular_speed_;
    },
};
