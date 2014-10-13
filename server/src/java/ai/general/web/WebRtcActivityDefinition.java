/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

/**
 * Definition of WebRTC activities.
 *
 * A WebRTC activity consists of a caller and callee. The caller initiates a WebRTC call which the
 * callee can join.
 *
 * WebRtcDefinition is a singleton.
 */
public class WebRtcActivityDefinition extends ActivityDefinition {

  public static final String kName = "webrtc";
  public static final String kRoleCaller = "caller";
  public static final String kRoleCallee = "callee";

  /**
   * Defines WebRTC activities.
   * This class is a singleton. Use {@link #getInstance()} to create an instance.
   */
  public WebRtcActivityDefinition() {
    super(kName);
    addRole(new Role(kRoleCaller, true));
    addRole(new Role(kRoleCallee, false));
  }

  /**
   * Returns the singleton WebRtcActivityDefinition instance.
   *
   * @rreturn The singleton WebRtcActivityDefinition instance.
   */
  public static WebRtcActivityDefinition getInstance() {
    return Singleton.get(WebRtcActivityDefinition.class);
  }

  /**
   * Creates a new {@link WebRtcActivity}.
   *
   * @return A new WebRtcActivity.
   */
  @Override
  public Activity createActivity() {
    return new WebRtcActivity();
  }
}
