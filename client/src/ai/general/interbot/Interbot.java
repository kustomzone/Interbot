/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot;

/**
 * Main class of the Interbot command line client.
 *
 * This class can be used to start Interbot from the command line without running inside a web
 * application container.
 *
 * The Interbot client connects and logs into an Interbot server and loads and initializes
 * all plugins.
 *
 * Interbot uses the configuration files defined at config/interbot-config.json to connect to the
 * Interbot server. The login profile is obtain from config/profiles.json. The desired profile name
 * can be specified as a command line argument to Interbot. If no command line argument is
 * specified Interbot uses the default profile.
 */
public class Interbot {

  /**
   * Main method.
   *
   * @param args Commandline arguments.
   */
  public static void main(String[] args) {
    InterbotClient client = new InterbotClient();
    if (!client.load()) {
      System.err.println("Error: Failed to initialize client.");
      return;
    }

    UserProfiles profiles = client.getUserProfiles();
    if (profiles.size() == 0) {
      System.err.println("Error: No profiles defined.");
      return;
    }
    UserProfile profile = null;
    String default_profile_name;
    if (args.length > 0) {
      profile = profiles.findProfile(args[0]);
      if (profile == null) {
        System.err.println("Error: No profile named '" + args[0] + "'.");
        return;
      }
    } else {
      profile = profiles.defaultProfile();
      if (profile == null) {
        System.err.println("Error: No profile named '" + profiles.getDefault() + "'.");
        return;
      }
    }

    if (client.login(profile) != ConnectionResult.Success) {
      return;
    }
    Runtime.getRuntime().addShutdownHook(new ShutdownHandler(client));
  }
}
