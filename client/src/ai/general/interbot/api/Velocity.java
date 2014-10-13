/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot.api;

/**
 * Represents a velocity.
 *
 * Velocities are represented as a combination of linear and angular speeds.
 * The linear speed specifies the linear motion of a point with respect to the reference frame.
 * The angular speed specifies the rotation of a point with respect to the refence frame.
 * Positive angular speeds indicate counter-clockwise rotation.
 */
public class Velocity {

  /**
   * Sets all speeds to 0.
   */
  public Velocity() {
    this.linear_speed_ = 0.0;
    this.angular_speed_ = 0.0;
  }

  /**
   * Initializes a new velocity object with the specified speeds.
   *
   * @param linear_speed The linear speed.
   * @param angular_speed The angular speed.
   */
  public Velocity(double linear_speed, double angular_speed) {
    this.linear_speed_ = linear_speed;
    this.angular_speed_ = angular_speed;
  }

  /**
   * Returns the angular speed.
   *
   * @return The angular speed.
   */
  public double getAngularSpeed() {
    return angular_speed_;
  }

  /**
   * Returns the linear speed.
   *
   * @return The linear speed.
   */
  public double getLinearSpeed() {
    return linear_speed_;
  }

  /**
   * Updates the angular speed.
   *
   * @param angular_speed The updated angular speed.
   */
  public void setAngularSpeed(double angular_speed) {
    this.angular_speed_ = angular_speed;
  }

  /**
   * Updates the linear speed.
   *
   * @param linear_speed The updated linear speed.
   */
  public void setLinearSpeed(double linear_speed) {
    this.linear_speed_ = linear_speed;
  }

  private double angular_speed_;  // Angular speed.
  private double linear_speed_;  // Linear speed.
}
