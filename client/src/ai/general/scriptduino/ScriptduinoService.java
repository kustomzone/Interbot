/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.scriptduino;

import ai.general.event.Event;
import ai.general.event.Processor;
import ai.general.event.Watcher;
import ai.general.interbot.api.Command;
import ai.general.interbot.api.VelocityCommand;
import ai.general.net.Connection;
import ai.general.plugin.annotation.Subscribe;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * The ScriptduinoService allows an Arduino running Scriptduino to communicate with an Interbot
 * server.
 *
 * The ScriptduinoService forwards commands received from the server to the Arduino.
 */
public class ScriptduinoService extends Processor<VelocityCommand> {

  public static final String kControlPingTopic = "robot/control/ping";
  public static final String kControlPongTopic = "robot/control/pong";
  public static final String kVelocityTopic = "robot/base/velocity";

  // Latency measurements in the past are discounted by this factor. This ensures that the current
  // latency value is biased towards more recent measurements.
  private static final double kLatencyDecay = 0.8;

  // Maximum permissible difference between last control ping timestamp and command timestamp.
  private static final int kMaxCommandDelayMillis = 5000;

  // Maximum permissible delay between successive control pings.
  // If no control pings are received within this time period, the robot is stopped.
  private static final int kMaxControlPingDelay = 5000;

  // Minimum number of samples to collect before stale pings are rejected.
  private static final int kMinLatencySamples = 5;

  /**
   * The ControlPingWatcher watches the connection to the Interbot server while the robot is
   * controlled.
   * The ControlPingWatcher expects that the server peridocially sends a control ping. Control
   * pings are only send while the robot is actively controlled. They are sent with much higher
   * frequency than session pings in order to reduce the possibility of damage due to connectivity
   * issues while the robot is moving.
   * If a control ping is not received by the server on time, the ControlPingWatcher stops the
   * robot.
   */
  private class ControlPingWatcher extends Watcher {
    /** Period with which the control ping watcher checks reception of control pings. */
    private static final int kWatchPeriodMillis = kMaxControlPingDelay;

    public ControlPingWatcher() {
      super("control-ping-watcher", kWatchPeriodMillis);
    }

    /**
     * This method is periodically called by the watcher thread.
     */
    @Override
    protected void watch() {
      long now = System.currentTimeMillis();
      if (now - last_control_ping_timestamp_millis_ > kMaxControlPingDelay) {
        log.trace("missing control ping");
        scriptduino_.stop();
      }
    }
  }

  /**
   * Constructs a Scriptduino service for the specified server connection.
   *
   * @param scriptduino The Scriptduino API.
   * @param connection Connection to server.
   */
  public ScriptduinoService(Scriptduino scriptduino, Connection connection) {
    super("ScriptduinoService");
    this.scriptduino_ = scriptduino;
    this.connection_ = connection;
    last_control_ping_timestamp_millis_ = 0;
    control_ping_watcher_ = new ControlPingWatcher();
    event_ = new Event<VelocityCommand>();
    average_latency_ = 0.0;
    int ping_count_ = 0;
    latency_decay_integral_ = 0.0;
  }

  /**
   * Begins the Scriptduino service. This method starts the control ping watcher.
   */
  public void begin() {
    control_ping_watcher_.start();
    event_.subscribe(getObserver());
    start();
  }

  /**
   * Stops the Scriptduino service. This method stops the control ping watcher.
   */
  public void end() {
    event_.unsubscribe(getObserver());
    halt();
    control_ping_watcher_.stop();
  }

  /**
   * Responds to control pings from the controller. Echoes the message back to the controller.
   *
   * @param ping The ping command. Echoed back to the controller
   */
  @Subscribe(kControlPingTopic)
  public void onControlPing(Command ping) {
    log.trace("control ping received");
    connection_.publish(kControlPongTopic, ping);
    ping.setReceptionTimestampToNow();
    long latency = ping.getReceptionTimestampMillis() - ping.getTimestamp();
    if (ping_count_ < kMinLatencySamples ||
        latency - average_latency_ < kMaxCommandDelayMillis) {
      last_control_ping_timestamp_millis_ = ping.getReceptionTimestampMillis();
    } else {
      log.warn("stale ping: pings={} latency={} average={}",
               ping_count_, latency, average_latency_);
      // control ping watcher will stop the robot
    }
    // The latency is a weighted average over all latency measurements, where more recent
    // measurements are weighted higher than measurements further in the past. The kLatencyDecay
    // factor defines the discount factor used to reduce the weight of measurements in the past.
    ping_count_++;
    latency_decay_integral_ *= kLatencyDecay;
    average_latency_ = average_latency_ * latency_decay_integral_ + latency;
    latency_decay_integral_ += 1.0;
    average_latency_ /= latency_decay_integral_;
    log.debug("average latency = {}", average_latency_);
  }

  /**
   * Responds to velocity commands from the controller. Sends updated velocity settings to Arduino.
   * If stale commands are encountered, the robot is stopped and the commands are ignored.
   *
   * @param command The velocity command.
   */
  @Subscribe(kVelocityTopic)
  public void onVelocity(VelocityCommand command) {
    command.setReceptionTimestampToNow();
    event_.trigger(command);
  }

  /**
   * Processes velocity commands.
   */
  @Override
  protected void process(VelocityCommand command) {
    if (command.getVelocity().getLinearSpeed() == 0 &&
        command.getVelocity().getAngularSpeed() == 0) {
      // always obey stop
      scriptduino_.stop();
    } else if (command.getReceptionTimestampMillis() - last_control_ping_timestamp_millis_ >
               kMaxCommandDelayMillis) {
      log.warn("stale command");
      scriptduino_.stop();
    } else {
      scriptduino_.moveTurn(command.getVelocity().getLinearSpeed(),
                            command.getVelocity().getAngularSpeed());
    }
  }

  private static Logger log = LogManager.getLogger();

  private double average_latency_;  // Weighted average of latency.
  private Connection connection_;  // Connection to server.
  private ControlPingWatcher control_ping_watcher_;  // Monitors controls pings from server.
  private Event<VelocityCommand> event_;  // Velocity command queue.
  private long last_control_ping_timestamp_millis_;  // Time of last control ping.
  private double latency_decay_integral_;  // Lossy integral of latency weights.
  private long ping_count_;  // Totla number of control pings.
  private Scriptduino scriptduino_;  // Connection to board.
}
