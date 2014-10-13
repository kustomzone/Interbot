/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot;

import ai.general.net.Connection;
import ai.general.plugin.annotation.Subscribe;

import java.io.IOException;
import java.util.HashMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Implements the Interbot Client Service. The client service responds to session pings and
 * reconnects when the connection is interrupted.
 *
 * Upon request the client service provides the server with system information.
 *
 * The client service also responds to execute package requests which are used for software
 * updates.
 */
public class InterbotClientService {

  public static final String kAdminExecutePackageTopic = "/admin/execute";
  public static final String kSessionPingTopic = "events/session/ping";
  public static final String kSessionPongTopic = "events/session/pong";
  public static final String kSystemInfoRequestTopic = "/robot/system/info/request";
  public static final String kSystemInfoResponseTopic = "/robot/system/info/response";

  private static final String kExecutePackageScript = "start_execute_package.sh";

  /**
   * Represents parameters associated with a Session ping sent by the server.
   */
  @JsonIgnoreProperties(ignoreUnknown=true)
  public static class SessionPingParameters {
    /** Default period until a new period is communicated by the server. */
    public static final long kDefaultPeriodMillis = 200000;

    /**
     * Creates default SessionPingParameters.
     */
    public SessionPingParameters() {
      this.period_millis_ = kDefaultPeriodMillis;
    }

    /**
     * The session ping period advertised by the server in milliseconds.
     * The server commits to sending at least one session ping within this period.
     * The actual ping rate may be higher.
     *
     * @return The session ping period in milliseconds.
     */
    public long getPeriodMillis() {
      return period_millis_;
    }

    /**
     * Sets the the session ping period.
     *
     * @param period_millis The session ping period in milliseconds.
     */
    public void setPeriodMillis(long period_millis) {
      this.period_millis_ = period_millis;
    }

    /**
     * Returns a string representation of the session parameters.
     *
     * @return A string representation of the parameters.
     */
    @Override
    public String toString() {
      return "{\"periodMillis\": " + period_millis_ + "}";
    }

    private long period_millis_;  // Maximum interval of session pings.
  }

  /**
   * Constructs a client service for the specified connection.
   *
   * @param connection Connection to server.
   */
  public InterbotClientService(Connection connection) {
    this.connection_ = connection;
    this.session_ping_count_ = 1;
    this.last_session_ping_parameters_ = new SessionPingParameters();
    this.device_config_ = DeviceConfig.load();
  }

  /**
   * Resets the session ping count to zero.
   */
  public void clearSessionPingCount() {
    session_ping_count_ = 0;
  }

  /**
   * Returns the session ping parameters received from the server during the last session
   * ping.
   *
   * @return The last session ping parameters.
   */
  public SessionPingParameters getLastSessionPingParameters() {
    return last_session_ping_parameters_;
  }

  /**
   * Returns the current session ping count.
   *
   * @return The session ping count.
   */
  public int getSessionPingCount() {
    return session_ping_count_;
  }

  /**
   * Handles a request to execute the package as specified in the execute package instruction.
   * This method is called by the server to run administrative code such as software updates.
   *
   * For security reasons, the package URL must be located on the localhost or the general.ai
   * server and must use the HTTPS protocol if it is not local.
   *
   * @param instruction The execute package instruction with request details.
   */
  @Subscribe(kAdminExecutePackageTopic)
  public void onExecutePackage(ExecutePackageInstruction instruction) {
    String url = instruction.getUrl();
    if (!url.startsWith("https://general.ai") &&
        !url.startsWith("http://localhost")) {
      // domain not authorized
      log.warn("Unauthorized execute request: " + url);
      return;
    }
    String package_name = url.substring(url.lastIndexOf('/') + 1);
    // remove .tar.gz ending
    package_name = package_name.substring(0, package_name.length() - 7);
    try {
      log.info("Executing: " + url);
      Runtime.getRuntime().exec(new String[] {
          InterbotPaths.getScriptsDirectory() + kExecutePackageScript,
          url,
          package_name
        });
    } catch (IOException e) {
      log.catching(Level.ERROR, e);
    }
  }

  /**
   * Handles session pings from the server by responding with a pong.
   *
   * @param parameters Session ping parameters.
   */
  @Subscribe(kSessionPingTopic)
  public void onSessionPing(SessionPingParameters parameters) {
    log.trace("session ping received: " + parameters);
    session_ping_count_++;
    this.last_session_ping_parameters_ = parameters;
    connection_.publish(kSessionPongTopic, connection_.getSessionId(), true);
  }

  /**
   * Handles system info requests.
   *
   * System info requests query one or more system properties. This method tries to obtain the
   * requested information and sends a response to the server.
   *
   * The returned response is a map with the property name as the key and an appropriate value.
   * The value depends on the property.
   *
   * If a particular property is not supported, returns null as the value.
   *
   * @param request The system information that is requested.
   */
  @Subscribe(kSystemInfoRequestTopic)
  public void onSystemInfo(SystemInfoRequest request) {
    HashMap<String, Object> response = new HashMap<String, Object>();
    for (SystemProperty property : request.getProperties()) {
      Object result = null;
      switch (property) {
        case NetworkInterfaces:
          result = NetworkInterfaceInfo.queryAll();
          break;
        case Devices:
          result = device_config_.getDevices();
          break;
        default:
          // not supported
          break;
      }
      response.put(property.name(), result);
    }
    connection_.publish(kSystemInfoResponseTopic, response);
  }

  private static Logger log = LogManager.getLogger();

  private Connection connection_;  // Connection to server.
  private DeviceConfig device_config_;  // Device configuration.
  private SessionPingParameters last_session_ping_parameters_;  // Last ping parameters.
  private int session_ping_count_;  // The number of session pings since the last reset.
}
