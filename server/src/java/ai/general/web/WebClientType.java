/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import ai.general.web.video.VideoStreamActivityDefinition;

/**
 * Defines web clients. A web client is typically a browser or any other program that logs in
 * via the login.jsp web page.
 *
 * WebClientType is a singleton.
 */
public class WebClientType extends ClientType {

  private static final String kName = "web";

  /**
   * Defines web clients.
   * This class is a singleton. Use {@link #getInstance()} to create an instance.
   */
  public WebClientType() {
    super(kName);
    addCapability(new Capability(ControlActivityDefinition.kName,
                                 ControlActivityDefinition.kRoleController));
    addCapability(new Capability(WebRtcActivityDefinition.kName,
                                 WebRtcActivityDefinition.kRoleCaller));
    addCapability(new Capability(WebRtcActivityDefinition.kName,
                                 WebRtcActivityDefinition.kRoleCallee));
    addCapability(new Capability(VideoStreamActivityDefinition.kName,
                                 VideoStreamActivityDefinition.kRoleReceiver));
  }

  /**
   * Returns the singleton WebClientType instance.
   *
   * @rreturn The singleton WebClientType instance.
   */
  public static WebClientType getInstance() {
    return Singleton.get(WebClientType.class);
  }
}
