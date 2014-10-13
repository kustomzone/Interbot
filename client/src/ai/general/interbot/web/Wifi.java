/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot.web;

import ai.general.interbot.InterbotPaths;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Manages Wifi connections.
 *
 * Wifi provides a bridge between the web application and the system network manager.
 * Wifi provides methods to list, add and delete Wifi connections.
 * Wifi can also scan for nearby access points.
 *
 * The system can be configured with multipe Wifi connections at the same time. The
 * network manager will pick the best connection.
 *
 * Wifi supports only WPA security.
 *
 * Wifi needs the ability to execute system commands as sudo.
 */
public class Wifi {

  // Wifi scripts.
  private static final String kAddWifi = "add_wifi.sh";
  private static final String kDeleteWifi = "delete_wifi.sh";
  private static final String kListConnections = "list_connections.sh";
  private static final String kScanWifi = "scan_wifi.sh";

  // The name of the network connections file produced by the list_connections.sh script.
  private static final String kConnectionsFile = "network_connections";

  // The name of the wifi scan file produced by the scan_wifi.sh script.
  private static final String kWifiScanFile = "wifi_scan";

  /**
   * Wifi has only static methods.
   */
  private Wifi() {}

  /**
   * Adds a Wifi connection associated with the specified SSID. The password is the clear text
   * WPA password of the Wifi network.
   *
   * If possible, this method may also attempt to connect to the new Wifi network.
   *
   * After adding a new Wifi connection, {@link #listConnections()} should be used to obtain
   * the updated list of connections.
   *
   * @param ssid The SSID of the Wifi network to add.
   * @param password The clear text WPA password of the Wifi network to add.
   * @return True if the Wifi connection was added successfully.
   */
  public static boolean addConnection(String ssid, String password) {
    return execute(kAddWifi, ssid, password);
  }

  /**
   * Deletes the Wifi connection associated with the specified SSID.
   *
   * After deleting a Wifi connection, {@link #listConnections()} should be used to obtain
   * the updated list of connections.
   *
   * @param The SSID of the Wifi connection to delete.
   * @return True if the Wifi connection was deleted successfully.
   */
  public static boolean deleteConnection(String ssid) {
    return execute(kDeleteWifi, ssid);
  }

  /**
   * Scans for nearby access points and returns a list of the access points.
   * The scan is executed when this method is called and may take a short amount of time.
   *
   * @return The current set of nearby access points.
   */
  public static Set<String> listAccessPoints() {
    Set<String> access_points = new TreeSet<String>();
    if (execute(kScanWifi)) {
      try {
        FileReader file = new FileReader(InterbotPaths.getTempDirectory() + kWifiScanFile);
        BufferedReader reader = new BufferedReader(file);
        String line = reader.readLine();
        // skip header line
        line = reader.readLine();
        while (line != null) {
          int index = line.indexOf('\'', 1);
          if (index != -1) {
            access_points.add(line.substring(1, index));
          }
          line = reader.readLine();
        }
        file.close();
      } catch (IOException e) {
        log.catching(Level.ERROR, e);
      }
    }
    return access_points;
  }

  /**
   * Returns a list of all configured connections.
   * A connection can be added via this class or another operating system program.
   * The list is updated each time this method is called, such that connections added via the
   * operating system will be picked up.
   *
   * @return The current list of managed connections.
   */
  public static Set<String> listConnections() {
    Set<String> connections = new TreeSet<String>();
    if (execute(kListConnections)) {
      try {
        FileReader file = new FileReader(InterbotPaths.getTempDirectory() + kConnectionsFile);
        BufferedReader reader = new BufferedReader(file);
        String line = reader.readLine();
        while (line != null) {
          if (!line.startsWith("Wired")) {
            connections.add(line);
          }
          line = reader.readLine();
        }
        file.close();
      } catch (IOException e) {
        log.catching(Level.ERROR, e);
      }
    }
    return connections;
  }

  /**
   * Executes the specified script with the given arguments.
   * This method blocks and waits until execution of the script has completed.
   *
   * @param script_name The name of the script to execute.
   * @param args Script arguments.
   * @return True if the script was successfully executed.
   */
  private static boolean execute(String script_name, String ... args) {
    log.debug("executing {}", script_name);
    String[] exec_args = new String[args.length + 1];
    exec_args[0] = InterbotPaths.getScriptsDirectory() + script_name;
    for (int i = 0; i < args.length; i++) {
      exec_args[i + 1] = args[i];
    }
    try {
      Process process = Runtime.getRuntime().exec(exec_args);
      process.waitFor();
      return true;
    } catch (IOException e) {
      log.catching(Level.ERROR, e);
    } catch (InterruptedException e) {}
    return false;
  }

  private static Logger log = LogManager.getLogger();
}
