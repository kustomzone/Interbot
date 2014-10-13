/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import ai.general.common.RandomString;

/**
 * Manages client sessions. A user may be associated with multiple active sessions if the user
 * is logged in from different devices or browsers.
 *
 * A session may be associated with multiple users, if multiple users are logged in from the
 * same device and browser.
 */
public class SessionManager {

  /**
   * Generates and returns a random session ID.
   *
   * @return A random session ID.
   */
  public static String createSessionId() {
    return RandomString.nextString(16);
  }

  /**
   * Returns true if the user agent is a mobile browser running on a phone form factor.
   *
   * @param user_agent The user agent reported by the browser.
   * @return True if the user agent is associated with a phone form factor. 
   */
  public static boolean isPhone(String user_agent) {
    if (user_agent == null) return false;
    return user_agent.indexOf("Mobile") >= 0 || user_agent.indexOf("iPhone") >= 0;
  }
}
