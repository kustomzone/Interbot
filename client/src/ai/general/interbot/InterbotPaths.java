/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot;

/**
 * Manages file paths to various Interbot system files, including configuration file paths and
 * script file paths.
 */
public class InterbotPaths {

  /**
   * Returns the configuration file directory.
   *
   * This method returns the standard configuration directory which is independent of the
   * application mode. If this program is running in web mode, a secondary application specific
   * configuration directory may be located under WEB-INF which contains configuration files
   * relevant to the web app.
   *
   * @return The configuration file directory.
   */
  public static String getConfigDirectory() {
    return getInterbotHome() + "config/";
  }

  /**
   * Returns the Interbot home directory. This method assumes a standard Interbot installation.
   *
   * @return The Interbot home directory.
   */
  public static String getInterbotHome() {
    return System.getProperty("user.home") + "/interbot/";
  }

  /**
   * Returns the script files directory. The script files directory contains executable shell
   * scripts.
   *
   * @return The script files directory.
   */
  public static String getScriptsDirectory() {
    return getInterbotHome() + "scripts/";
  }

  /**
   * Returns the temporary files directory. Temporary files should be short lived and may be
   * deleted by the system from time to time.
   *
   * @return The temporary files directory.
   */
  public static String getTempDirectory() {
    return getInterbotHome() + "tmp/";
  }
}
