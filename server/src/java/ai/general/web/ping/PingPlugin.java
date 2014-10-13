/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web.ping;

import ai.general.plugin.Plugin;
import ai.general.plugin.ServiceManager;

/**
 * The Ping plugin provides a service that implements an RPC method that replies to WAMP pings.
 */
public class PingPlugin extends Plugin {

  public PingPlugin() {
    super("PingPlugin",
          1.0,
          "Provides a WAMP ping service.",
          "General AI");
  }

  /**
   * Called when the Plugin is enabled.
   */
  @Override
  public boolean onEnable() {
    registerService("ping", new PingService(), "/share/bin/", false);
    return true;
  }

  /**
   * Called when the Plugin is disabled.
   */
  @Override
  public void onDisable() {
    unregisterAllServices();
  }
}
