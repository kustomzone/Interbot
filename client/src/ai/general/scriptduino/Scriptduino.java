/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.scriptduino;

import ai.general.event.Watcher;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Defines the Scriptduino API. Scriptduino forwards any API calls to a board connected on the
 * {@link Serial} port. If no connection exist, API calls are only logged.
 *
 * Scriptduino loads configuration information from the scripduino-config.json config file, which
 * must be located in the config directory.
 * If the serialPort property is empty, no connection will be opened.
 *
 * Logging is done at the debug level.
 */
public class Scriptduino {

  private static final String kInstructionPing = "ping();";
  private static final String kInstructionStop = "stop();";

  /**
   * The BoardWatcher periodically sends a ping to the board. Scriptduino boards require a
   * periodic ping with a period that is configured with the board configuration.
   * If the Scriptduino board does not receive the periodic ping, the robot will be stopped
   * by the board and instructions will be ignored until a ping is received.
   * This minimizes the possibility of damage due to broken connectivity between the Interbot
   * client and the Scriptduino board.
   */
  private class BoardWatcher extends Watcher {
    /** Period with which a ping is sent to the board. This must match the board configuration. */
    private static final int kWatchPeriodMillis = 2000;

    public BoardWatcher() {
      super("scriptduino-board-watcher", kWatchPeriodMillis);
    }

    /**
     * This method is periodically called by the watcher thread.
     */
    @Override
    protected void watch() {
      ping();
    }
  }

  /**
   * Initializes the Scriptduino instance.
   */
  public Scriptduino() {
    this.config_ = null;
    this.serial_ports_ = null;
    this.board_ = null;
    this.board_watcher_ = null;
  }

  /**
   * Closes the connection to the board and releases resources.
   */
  public synchronized void close() {
    if (board_watcher_ != null) {
      board_watcher_.stop();
    }
    if (board_ != null) {
      board_.close();
      board_ = null;
      log.info("Closed connection to board '{}'", config_.getSerialPort());
    }
  }

  /**
   * Returns the ScriptduinoConfig. Returns null, if the Scriptduino library has not been
   * initialized or if the config file could not be loaded.
   *
   * @return The ScriptduinoConfig or null.
   */
  public ScriptduinoConfig getScriptduinoConfig() {
    return config_;
  }

  /**
   * Returns a list of serial ports on the system. Returns null if the Scriptduino library has not
   * been initialized.
   *
   * The serial ports are queried when the {@link #open()} method is called. Serial ports cannot
   * be queried while they are used. In order to refresh the list, open() must be called again
   * after {@link #close()} has been called.
   *
   * @return The list of serial ports or null.
   */
  public List<String> listSerialPorts() {
    return serial_ports_;
  }

  /**
   * Sends a moveTurn instruction to the robot.
   * Sets the linear and angular speeds to the indicated values. All speeds must be between -1.0
   * and +1.0.
   *
   * @param linear_speed Linear speed of robot base.
   * @param angular_speed Angular speed of robot base.
   */
  public void moveTurn(double linear_speed, double angular_speed) {
    send("moveTurn(" + linear_speed + "," + angular_speed + ");");
  }

  /**
   * Loads the configuration and attempts to connect to the board. If no connection can be
   * established, commands are only logged.
   *
   * Any call to open() must be followed by a call to {@link #close()}.
   *
   * This method returns false if the connection to the board is already open or an error is
   * encountered.
   * This method also updates the list of serial ports.
   *
   * @return True if the board was successfully opened.
   */
  public synchronized boolean open() {
    if (board_ != null) {
      return false;
    }
    config_ = ScriptduinoConfig.load();
    if (config_ == null) {
      log.error("Error: Failed to load Scriptduino configuration file.");
      return false;
    }
    serial_ports_ = Serial.listPorts();
    if (config_.getSerialPort().length() == 0) {
      log.warn("No serial port specified.");
      return true;
    }
    board_ = new Serial(config_.getSerialPort());
    if (!board_.open()) {
      log.error("Error while opening connection to board '{}'", config_.getSerialPort());
      board_.close();
      board_ = null;
      return false;
    }
    board_watcher_ = new BoardWatcher();
    board_watcher_.start();
    log.info("Connected to board '{}'", config_.getSerialPort());
    return true;
  }

  /**
   * Sends a ping instruction to the robot.
   */
  public void ping() {
    send(kInstructionPing);
  }

  /**
   * Sends an instruction to the board if the board is opened.
   * Always logs the instruction.
   *
   * @param The instruction to send.
   */
  public synchronized void send(String instruction) {
    log.debug(instruction);
    if (board_ != null) {
      board_.write(instruction);
    }
  }

  /**
   * Sends a stop instruction to the robot.
   */
  public void stop() {
    send(kInstructionStop);
  }

  private static Logger log = LogManager.getLogger();

  private Serial board_;  // USB or serial port connection to board.
  private BoardWatcher board_watcher_;  // Send periodic pings to board.
  private ScriptduinoConfig config_;  // Scriptduino configuration.
  private List<String> serial_ports_;  // List of serial ports on machine.
}
