/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

/**
 * Definition for control activites.
 *
 * A control activity consists of a controller and a robot. The controller controls the robot by
 * sending control command to the robot.
 *
 * ControlActivityDefinition is a singleton.
 */
public class ControlActivityDefinition extends ActivityDefinition {

  public static final String kName = "control";
  public static final String kRoleController = "controller";
  public static final String kRoleRobot = "robot";

  /**
   * Defines control activities.
   * This class is a singleton. Use {@link #getInstance()} to create an instance.
   */
  public ControlActivityDefinition() {
    super(kName);
    addRole(new Role(kRoleController, true));
    addRole(new Role(kRoleRobot, false));
  }

  /**
   * Returns the singleton ControlActivityDefinition instance.
   *
   * @rreturn The singleton ControlActivityDefinition instance.
   */
  public static ControlActivityDefinition getInstance() {
    return Singleton.get(ControlActivityDefinition.class);
  }

  /**
   * Creates a new {@link ControlActivity}.
   *
   * @return A new ControlActivity.
   */
  @Override
  public Activity createActivity() {
    return new ControlActivity();
  }
}
