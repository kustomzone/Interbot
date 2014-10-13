/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Manages users.
 * The UserManager keeps track of all loaded users. A user does not need to be logged in to be
 * loaded.
 * The UserManager can be used to obtain an instance of a user. If a User instance does not
 * already exist, the UserManager creates a new instance for the user.
 *
 * UserManager is a singleton and thread-safe.
 */
public class UserManager {

  public static final long kSessionPingIntervalMillis = 20000;

  // User types are stored as integer constants in the database.
  private static final int kUserTypeAdmin = 1;
  private static final int kUserTypeHuman = 2;
  private static final int kUserTypeRobot = 3;

  /**
   * Implements the session ping timer task.
   */
  private class SessionPingTimerTask extends TimerTask {
    private static final int kMaxUsersPerBatch = 20;
    private static final int kDealyMillisBetweenBatches = 50;

    /**
     * Instructs users to run session pings in batches.
     * Each batch consists of no more than kMaxUsersPerBatch. The timer waits
     * kDealyMillisBetweenBatches milliseconds before starting the next batch. This spreads
     * pong responses over time avoiding a pong of death.
     */
    @Override
    public void run() {
      int count = 0;
      for (User user : users_.values()) {
        user.executeSessionPing();
        count++;
        if (count == kMaxUsersPerBatch) {
          try {
            Thread.sleep(kDealyMillisBetweenBatches);
          } catch (InterruptedException e) {}
          count = 0;
        }
      }
    }
  }

  /**
   * UserManager is singleton. Use {@link #getInstance()} to create an instance.
   */
  public UserManager() {
    users_ = new HashMap<String, User>();
    session_ping_timer_ = new Timer("session ping timer", true);
    session_ping_timer_.schedule(new SessionPingTimerTask(),
                                 kSessionPingIntervalMillis,
                                 kSessionPingIntervalMillis);
  }

  /**
   * Returns the singleton UserManager instance.
   *
   * @return The singleton UserManager instance.
   */
  public static UserManager getInstance() {
    return Singleton.get(UserManager.class);
  }

  /**
   * Executes shutdown code.
   * This method should be called during shutdown for a clean shutdown.
   */
  public static void shutdown() {
    UserManager instance = getInstance();
    instance.session_ping_timer_.cancel();
  }

  /**
   * Returns a User instance of the user with the specified username. If no instance is currently
   * loaded, loads the user from the database.
   * There is only one User instance for each user. A user may be loaded even if the user is not
   * logged.
   * If no user with the specified username exists, or the user cannot be loaded returns null.
   *
   * @param username The username of the user.
   * @return The User instance that represents the user.
   */
  public synchronized User getUser(String username) {
    if (!UserDB.checkString(username)) return null;
    if (users_.containsKey(username)) {
      return users_.get(username);
    } else {
      User user;
      switch (UserDB.getInstance().getUserType(username)) {
        case kUserTypeHuman: user = new HumanUser(username); break;
        case kUserTypeRobot: user = new RobotUser(username); break;
        default: return null;
      }
      users_.put(username, user);
      return user;
    }
  }

  /**
   * Returns a UserView for the specified user. This methodis equivalent to
   * {@link #getUser(String)}, except that it returns a UserView.
   *
   * @param username The username of the user.
   * @return The UserView instance that represents the user.
   */
  public UserView getUserView(String username) {
    return getUser(username);
  }

  /**
   * This method can be called to unload a particular user. This will cause the user to be
   * reloaded the next time it is obtained via {@link #getUser(String)}.
   *
   * It is important that the User instance is not referenced anymore by any class before it
   * is unloaded. After unloading, the user instance will become invalid. This method should be
   * only called by the User class or administrative code.
   *
   * @param user User instance to unload.
   */
  public synchronized void unloadUser(User user) {
    users_.remove(user);
  }

  /**
   * This method can be called by administrative code to unload all users. This will cause that
   * all users will be reloaded the next time a user is accessed via the {@link #getUser(String)}
   * method.
   */
  public synchronized void unloadAllUsers() {
    users_.clear();
  }

  /**
   * Returns a list of all currently loaded users. This method should be called only be
   * administrative code.
   *
   * @return Array of all loaded users.
   */
  public User[] listAllLoadedUsers() {
    return users_.values().toArray(new User[0]);
  }

  private HashMap<String, User> users_;
  private Timer session_ping_timer_;
}
