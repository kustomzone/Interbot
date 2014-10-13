/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web.video;

import ai.general.web.Activity;
import ai.general.web.ActivityDefinition;
import ai.general.web.Role;
import ai.general.web.Singleton;

/**
 * Definition for video stream activities.
 *
 * A video stream activity consists of a sender and a receiver. The sender sends a video stream
 * that is received by the receiver.
 *
 * VideoStreamActivityDefinition is a singleton.
 */
public class VideoStreamActivityDefinition extends ActivityDefinition {

  public static final String kName = "videostream";
  public static final String kRoleSender = "sender";
  public static final String kRoleReceiver = "receiver";

  /**
   * Defines video stream activities.
   * This class is a singleton. Use {@link #getInstance()} to create an instance.
   */
  public VideoStreamActivityDefinition() {
    super(kName);
    addRole(new Role(kRoleSender, false));
    addRole(new Role(kRoleReceiver, true));
  }

  /**
   * Returns the singleton VideoStreamActivityDefinition instance.
   *
   * @rreturn The singleton VideoStreamActivityDefinition instance.
   */
  public static VideoStreamActivityDefinition getInstance() {
    return Singleton.get(VideoStreamActivityDefinition.class);
  }

  /**
   * Creates a new {@link VideoStreamActivity}.
   *
   * @return A new VideoStreamActivity.
   */
  @Override
  public Activity createActivity() {
    return new VideoStreamActivity();
  }
}
