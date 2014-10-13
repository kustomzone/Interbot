/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import ai.general.web.video.VideoStreamActivityDefinition;

import java.util.HashMap;

/**
 * The ActivityManager contains the set of all {@link ActivityDefinition} instances.
 *
 * ActivityManager is a singleton class.
 */
public class ActivityManager {

  /**
   * ActivityManager is singleton. Use {@link #getInstance()} to create an instance.
   */
  public ActivityManager() {
    this.activity_definitions_ = new HashMap<String, ActivityDefinition>();
    createActivityDefinitions();
  }

  /**
   * Returns the singleton ActivityManager instance.
   *
   * @return The singleton ActivityManager instance.
   */
  public static ActivityManager getInstance() {
    return Singleton.get(ActivityManager.class);
  }

  /**
   * Adds an activity definition to the set of activity definitions.
   * Each activity definition must have a unique name.
   *
   * @param activity_definition The activity definition to add.
   */
  public void addActivityDefinition(ActivityDefinition activity_definition) {
    activity_definitions_.put(activity_definition.getName(), activity_definition);
  }

  /**
   * Returns true if there is an activity definiton with the specified name.
   *
   * @param name The name of the activity definition.
   * @return True if there is an activity definition with the specified name.
   */
  public boolean hasActivityDefinition(String name) {
    return activity_definitions_.containsKey(name);
  }

  /**
   * Returns the activity definition with the specified name or null if no such activity
   * definiton exists.
   *
   * @param name The name of the activity definition.
   * @return The activity definition with the specified name or null.
   */
  public ActivityDefinition getActivityDefinition(String name) {
    return activity_definitions_.get(name);
  }

  /**
   * Creates and adds built-in activity definitions.
   */
  private void createActivityDefinitions() {
    addActivityDefinition(ControlActivityDefinition.getInstance());
    addActivityDefinition(WebRtcActivityDefinition.getInstance());
    addActivityDefinition(VideoStreamActivityDefinition.getInstance());
  }

  private HashMap<String, ActivityDefinition> activity_definitions_;
}
