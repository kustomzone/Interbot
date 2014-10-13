/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Loads, caches and saves configuration files.
 *
 * Depending on whether the application is run from the command line or as a web app, the location
 * of the configuration files will change. This class loads or saves the configuration files from
 * the location that is appropriate for the context in which the application is run.
 *
 * All configuration files have JSON format. The configuration files are parsed into an object
 * using a JSON parser. Updated configuration objects are written as JSON files.
 *
 * ConfigFiles is a singleton class.
 */
public class ConfigFiles {

  /**
   * ConfigFiles is singleton. A ConfigFiles instance can be obtained via {@link #Instance}.
   */
  private ConfigFiles() {
    configs_ = new HashMap<String, Object>();
  }

  /**
   * If the specified configuration file has not been already loaded, loads the configuration file.
   * Once the file is loaded, it is cached by ConfigFiles, such that future requests for loading
   * the file will be served from the cache.
   *
   * The filename should specify only the configuration filename.
   * The file will be loaded from the configuration file directory that is appropriate for the
   * current context.
   *
   * The file must be a JSON file. If the .json extension is not specified in the filename, it
   * will be appended to the filename.
   *
   * The loaded file is parsed into an instance of config_type.
   *
   * If the config file cannot be loaded, this method returns null.
   *
   * @param filename The name of the configuration file, excluding extension and directory.
   * @param config_type The class that defines the configuration options specified in the file.
   * @return The loaded configuration as an instance of the config_type class or null.
   */
  public <T> T load(String filename, Class<T> config_type) {
    if (!filename.endsWith(".json")) {
      filename = filename + ".json";
    }
    if (configs_.containsKey(filename)) {
      @SuppressWarnings("unchecked") T config = (T) configs_.get(filename);
      return config;
    } else {
      ObjectMapper json_parser = new ObjectMapper();
      try {
        T config = json_parser.readValue(new File(InterbotPaths.getConfigDirectory() + filename),
                                         config_type);
        configs_.put(filename, config);
        return config;
      } catch (IOException e) {
        log.catching(Level.DEBUG, e);
        return null;
      }
    }
  }

  /**
   * Saves a configuration file. The filename should specify only the configuration filename.
   * The file will be saved to the configuration file directory that is appropriate for the
   * current context.
   *
   * The configuration object will be saved in JSON format. The configuration object must be a
   * POJO class that is serializable into JSON.
   *
   * If the .json extension is not specified in the filename, it will be appended to the filename.
   *
   * @param filename The name of the configuration file, excluding extension and directory.
   * @param configuration The object representing the configuration to be saved.
   * @return True if the file was successfully saved.
   */
  public boolean save(String filename, Object configuration) {
    if (!filename.endsWith(".json")) {
      filename = filename + ".json";
    }
    ObjectMapper json_generator = new ObjectMapper();
    try {
      json_generator.writeValue(new File(InterbotPaths.getConfigDirectory() + filename),
                                configuration);
      return true;
    } catch (IOException e) {
      log.catching(Level.DEBUG, e);
      return false;
    }
  }

  public final static ConfigFiles Instance = new ConfigFiles();
  private static Logger log = LogManager.getLogger();

  private HashMap<String, Object> configs_;  // Loaded configurations.
}
