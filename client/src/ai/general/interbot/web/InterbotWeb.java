/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot.web;

import ai.general.interbot.ConnectionResult;
import ai.general.interbot.InterbotClient;
import ai.general.interbot.UserProfiles;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Main class of the Interbot web app.
 *
 * The Interbot web app runs inside a web server and provides a web UI for Interbot.
 * The web app exposes all Interbot functionality through a web browser.
 *
 * InterbotWeb is a singleton class.
 *
 * InterbotWeb is thread-safe.
 */
public class InterbotWeb {

  // If a login fails due to an automatic login at startup, the client tries to log in again
  // after a period of time.
  // This constant specifies the minimum delay between login attempts.
  // As logins continue to fail, the client increases the delay between successive attempts.
  private static final int kMinLogingRetryPeriodMillis = 30000;

  /**
   * InterbotWeb is singleton. An InterbotWeb instance can be obtained via {@link #Instance}.
   */
  private InterbotWeb() {
    client_ = new InterbotClient();
    webapp_directory_ = null;
  }

  /**
   * Returns true if the IP address is a local address.
   *
   * @param address An IP address.
   * @return True if the IP is a local address.
   */
  public static boolean isLocalAddress(String ip_address) {
    try {
      InetAddress ip = InetAddress.getByName(ip_address);
      return ip.isLoopbackAddress() ||
        ip.isSiteLocalAddress() ||
        ip.isLinkLocalAddress();
    } catch (UnknownHostException e) {
      return false;
    }
  }

  /**
   * Called when the web application is deployed by the web server.
   * Initializes the Interbot application.
   *
   * If autostart is enabled, this method automatically starts the Interbot program and logs
   * into the Interbot server.
   *
   * This method must be called on a dedicated thread. If the are connection issues this method
   * may sleep for a while before attempting to connect again.
   *
   * @param webapp_directory The real path to the Interbot web app on the server.
   */
  public void deploy(String webapp_directory) {
    if (webapp_directory == null) {
      log.error("Cannot access file system.");
      return;
    }
    this.webapp_directory_ = webapp_directory;
    if (!client_.load()) {
      log.error("Failed to initialize client.");
      return;
    }

    UserProfiles profiles = client_.getUserProfiles();
    if (profiles.size() > 0) {
      if (client_.getInterbotConfig().getAutostart()) {
        int login_delay_millis = kMinLogingRetryPeriodMillis;
        ConnectionResult result = client_.login(profiles.defaultProfile());
        while (result != ConnectionResult.Success) {
          if (result == ConnectionResult.LoginError) {
            // Do not keep trying to log in with incorrect credentials.
            break;
          }
          log.debug("Will reattempt login in {} ms...", login_delay_millis);
          try {
            Thread.sleep(login_delay_millis);
          } catch (InterruptedException e) {}
          if (!client_.isConnected()) {
            // User logged in manually through web page.
            break;
          }
          // This slows login attempt but also ensures that the client keeps trying withi a
          // reasonable period of time.
          login_delay_millis += Math.sqrt(login_delay_millis);
          result = client_.login(profiles.defaultProfile());
        }
      }
    } else {
      log.error("No user profiles have been defined.");
    }
  }

  /**
   * Returns the Interbot client.
   *
   * @return The Interbot client.
   */
  public InterbotClient getClient() {
    return client_;
  }

  /**
   * Returns the root directory of the web app if the web app has been deployed.
   * Returns null if the web app is not running.
   *
   * @return The root directory of the web app or null.
   */
  public String getWebappDirectory() {
    return webapp_directory_;
  }

  /**
   * Returns true if the application has successfully completed initialization.
   *
   * @return True if initialization has successfully completed.
   */
  public boolean isInitialized() {
    return client_.isLoaded();
  }

  /**
   * Powers off the robot. This method logs out and initiates the shutdown of the computer.
   * The robot will need to be turned back on manually after this method has executed.
   * No further actions can be initiated after this method has been called until the robot is
   * turned off and turned back on again.
   *
   * This method requires that the web app has administrative privileges on the computer.
   */
  public void poweroff() {
    client_.unload();
    log.info("Powering off.");
    try {
      Runtime.getRuntime().exec("sudo poweroff");
    } catch (IOException e) {
      log.catching(Level.ERROR, e);
    }
  }

  /**
   * Reboots the robot. This method logs out and initiates the reboot of the computer.
   * No further actions can be initiated after this method has been called until the robot has
   * completed rebooting.
   *
   * This method requires that the web app has administrative privileges on the computer.
   */
  public void reboot() {
    client_.unload();
    log.info("Rebooting.");
    try {
      Runtime.getRuntime().exec("sudo reboot");
    } catch (IOException e) {
      log.catching(Level.ERROR, e);
    }
  }

  /**
   * Called when the web application is undeployed by the web server.
   * This method logs out of the Interbot server and shuts down the Interbot program.
   */
  public void undeploy() {
    client_.unload();
  }

  public final static InterbotWeb Instance = new InterbotWeb();
  private static Logger log = LogManager.getLogger();

  private InterbotClient client_;  // The client used to connect to the server.
  private String webapp_directory_;  // The real absolute path to the webapp directory.
}
