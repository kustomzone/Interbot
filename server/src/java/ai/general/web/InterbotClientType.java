/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import ai.general.web.video.VideoStreamActivityDefinition;

/**
 * Defines Interbot clients. An Interbot client is typically a robot logged in via the Interbot
 * client application.
 *
 * InterbotClientType is a singleton.
 */
public class InterbotClientType extends ClientType {

  private static final String kName = "interbot";

  /**
   * Defines Interbot clients.
   * This class is a singleton. Use {@link #getInstance()} to create an instance.
   */
  public InterbotClientType() {
    super(kName);
    addCapability(new Capability(ControlActivityDefinition.kName,
                                 ControlActivityDefinition.kRoleRobot));
  }

  /**
   * Returns the singleton InterbotClientType instance.
   *
   * @rreturn The singleton InterbotClientType instance.
   */
  public static InterbotClientType getInstance() {
    return Singleton.get(InterbotClientType.class);
  }
}
