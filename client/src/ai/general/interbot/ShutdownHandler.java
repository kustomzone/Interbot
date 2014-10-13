/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Responds to a shutdown signal received by the operating system. Closes all connections and
 * halts all processors. Cleans up and shuts down.
 */
public class ShutdownHandler extends Thread {

  /**
   * Constructs the ShutdownHandler.
   *
   * @param client The client used to connect to the server.
   */
  public ShutdownHandler(InterbotClient client) {
    this.client_ = client;
  }

  /**
   * The main thread method.
   */
  @Override
  public void run() {
    try {
      client_.unload();
      log.info("Exiting.");
      // allow time for flush
      Thread.sleep(100);
    } catch (Exception e) {}
  }

  private static Logger log = LogManager.getLogger();

  private InterbotClient client_;  // The client used to connect to the server.
}
