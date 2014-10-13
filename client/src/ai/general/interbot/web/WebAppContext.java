/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Executes startup and shutdown code.
 *
 * WebAppContext automates the execution of startup and shutdown code when the web application is
 * loaded and unloaded.
 */
@WebListener
public class WebAppContext implements ServletContextListener {

  /**
   * Runs the initializer on a separate thread.
   *
   * Running the initializer on a separate thread ensures that initialization does not interfere
   * with web app deployment or server startup.
   */
  private static class Initializer extends Thread {

    /**
     * Constructs an Initializer.
     *
     * @param webapp_directory The real path of the web application root directory.
     */
    public Initializer(String webapp_directory) {
      super("InterbotWeb-Initializer");
      this.webapp_directory_ = webapp_directory;
    }

    /**
     * Main method of the initializer thread.
     */
    @Override
    public void run() {
      try {
        InterbotWeb.Instance.deploy(webapp_directory_);
      } catch (Exception e) {
        log.catching(Level.ERROR, e);
      }
    }

    private String webapp_directory_;  // The real absolute path to the webapp directory.
  }

  /**
   * Called when the web application is unloaded. This methods executes shutdown code.
   *
   * @param context_event The servlet context event.
   */
  @Override
  public void contextDestroyed(ServletContextEvent context_event) {
    InterbotWeb.Instance.undeploy();
  }

  /**
   * Called when the web application is loaded. This method executes startup code.
   *
   * @param context_event The servlet context event.
   */
  @Override
  public void contextInitialized(ServletContextEvent context_event) {
    Initializer initializer = new Initializer(context_event.getServletContext().getRealPath(""));
    initializer.start();
  }

  private static Logger log = LogManager.getLogger();
}
