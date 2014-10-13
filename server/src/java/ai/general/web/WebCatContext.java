/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import ai.general.directory.Directory;
import ai.general.plugin.PluginManager;
import ai.general.web.ping.PingPlugin;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Executes startup and shutdown code.
 *
 * WebCatContext automates the execution of startup and shutdown code when the web application is
 * loaded and unloaded.
 */
@WebListener
public class WebCatContext implements ServletContextListener {

  /**
   * Called when the web application is loaded. This method executes startup code.
   *
   * @param context_event The servlet context event.
   */
  @Override
  public void contextInitialized(ServletContextEvent context_event) {
    Directory directory = Directory.Instance;
    directory.createPath("/channel/0/text");
    ActivityManager.getInstance();
    ClientManager.getInstance();
    UserManager.getInstance();
    TaskManager.getInstance().start();
    PluginManager plugin_manager = PluginManager.Instance;
    plugin_manager.load(PingPlugin.class);
    plugin_manager.load(UserPlugin.class);
    plugin_manager.enableAll();
  }

  /**
   * Called when the web application is unloaded. This methods executes shutdown code.
   *
   * @param context_event The servlet context event.
   */
  @Override
  public void contextDestroyed(ServletContextEvent context_event) {
    TaskManager.getInstance().halt();
    PluginManager.Instance.unloadAll();
    UserManager.shutdown();
  }
}
