/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

/**
 * Represents a human user. A human user is a standard user who logs in through a browser.
 */
public class HumanUser extends User {

  /**
   * Constructs a human user with the specified username.
   *
   * @param username Username of user.
   */
  public HumanUser(String username) {
    super(username);
  }

  /**
   * Returns Human as the user type.
   *
   * @return UserType.Human.
   */
  @Override
  public UserType getUserType() {
    return UserType.Human;
  }
}
