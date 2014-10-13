/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

/**
 * Receiver of User events.
 */
public interface UserEventListener {

  /**
   * Called when the user status has been updated.
   *
   * @param user The user whose status has been updated.
   */
  void userStatusUpdated(User user);

  /**
   * Called when the capabilities of a user have changed.
   * The capabilities of a user can change at any time.
   *
   * @param user The user whose capabilities have changed.
   */
  void userCapabilityUpdated(User user);

  /**
   * Called when the properties of a user have been updated.
   *
   * @param user The user whose properties have been updated.
   */
  void userPropertiesUpdated(User user);
}
