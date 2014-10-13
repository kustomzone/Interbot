/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot.api;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a command with velocity data.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class VelocityCommand extends Command {

  /**
   * Constructs a velocity command with 0 velocity at timestamp 0.
   */
  public VelocityCommand() {
    this.velocity_ = new Velocity();
  }

  /**
   * Constructs a velocity command with the specified speeds and timestamp.
   *
   * @param timestamp The time when this command was issued.
   * @param linear_speed The linear speed.
   * @param angular_speed The angular speed.
   */
  public VelocityCommand(Date timestamp, double linear_speed, double angular_speed) {
    super(timestamp);
    this.velocity_ = new Velocity(linear_speed, angular_speed);
  }

  /**
   * Returns the velocity value.
   *
   * @return The velocity.
   */
  public Velocity getVelocity() {
    return velocity_;
  }

  /**
   * Sets the velocity.
   *
   * @param velocity The new velocity.
   */
  public void setVelocity(Velocity velocity) {
    this.velocity_ = velocity;
  }

  private Velocity velocity_;  // The velocity.
}
