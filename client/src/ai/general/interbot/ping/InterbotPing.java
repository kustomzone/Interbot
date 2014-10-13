/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot.ping;

import ai.general.interbot.ConnectionResult;
import ai.general.interbot.InterbotConfig;
import ai.general.interbot.WampProcessor;
import ai.general.interbot.WebSocketUri;
import ai.general.plugin.PluginManager;

/**
 * Runs the Interbot Ping program from the command line.
 *
 * The Interbot ping program can be used to send pings to an Interbot server to test connectivity.
 * The Interbot ping program loads the server configuration from the config/interbot-config.json
 * file.
 *
 * Command line arguments:
 *   ping: Send a single WebSocket ping to the Interbot server and report the latency.
 *   wamp_ping: Send a single WAMP ping to the Interbot server and report the latency.
 *   test_channel: Subscribe and publish to a test channel until the program is terminated.
 */
public class InterbotPing {

  private static final int kPingTimeoutMillis = 10000;
  private static final String kWampPingService = "/interbot/ping/ping.wamp";
  private static final String kWampTestChannel = "/interbot/ping/test_channel.wamp";
  private static final String kWebSocketPingService = "/interbot/ping/ping.ws";

  /**
   * Shutdown handler for test channel.
   */
  private static class ShutdownHandler extends Thread {

    /**
     * Constructs a ShutdownHandler that closes the specified processor on shutdown.
     *
     * @param processor Processor to stop on shutdown.
     */
    public ShutdownHandler(WampProcessor processor) {
      this.processor_ = processor;
    }

    /**
     * Main thread method.
     */
    @Override
    public void run() {
      processor_.close();
      PluginManager.Instance.unloadAll();
    }

    private WampProcessor processor_;  // Processor to be stopped on shutdown.
  }

  /**
   * Main method.
   *
   * @param args Commandline arguments.
   */
  public static void main(String[] args) {
    if (args.length > 0) {
      System.out.println("General AI Interbot Ping");
      InterbotConfig config = InterbotConfig.load();
      if (config == null) {
        System.err.println("Error: Failed to load Interbot configuration file.");
        return;
      }
      System.out.println("Connecting to " + config.getServer());
      switch (args[0]) {
        case "ping": {
          PingProcessor ping = new PingProcessor(
              new WebSocketUri(config.getSecure(), config.getServer(), kWebSocketPingService));
          ping.start();
          try {
            ping.join(kPingTimeoutMillis);
          } catch (InterruptedException e) {}
          ping.halt();
          return;
        }
        case "wamp_ping": {
          WampPingProcessor processor = new WampPingProcessor(
              new WebSocketUri(config.getSecure(), config.getServer(), kWampPingService));
          if (processor.open("/") != ConnectionResult.Success) {
            System.out.println("Failed to connect to " + processor.getConnectionUri());
            return;
          }
          processor.ping();
          processor.close();
          return;
        }
        case "test_channel": {
          PluginManager plugin_manager = PluginManager.Instance;
          if (!plugin_manager.load(TestChannelPlugin.class)) {
            System.out.println("Failed to load TestChannel plugin.");
            return;
          }
          plugin_manager.enableAll();
          WampProcessor processor = new WampProcessor(
              new WebSocketUri(config.getSecure(), config.getServer(), kWampTestChannel));
          if (processor.open("/") != ConnectionResult.Success) {
            System.out.println("Failed to connect to " + processor.getConnectionUri());
            return;
          }
          Runtime.getRuntime().addShutdownHook(new ShutdownHandler(processor));
          System.out.println("ready");
          return;
        }
        default: {
          System.out.println("usage:");
          System.out.println("ping\tsend WebSocket ping.");
          System.out.println("wamp_ping\tsend WAMP ping.");
          System.out.println("test_channel\tsubscribe and publish to a test channel.");
          return;
        }
      }
    }
  }
}
