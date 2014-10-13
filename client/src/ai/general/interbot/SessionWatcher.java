/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot;

import ai.general.event.Watcher;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * The session watcher monitors connectivity to the Interbot server and reconnects if necessary.
 * The session watcher periodically checks the current ping count received from the Interbot
 * server. If no pings have been received since the last count, the session watcher attempts to
 * reconnect to the Interbot server.
 *
 * To activate the SessionWatcher, a {@link ConnectionProcessor} must be specified. Once a
 * processor is specified, the session watcher attempts to reconnect until an
 * InterbotClientService is specified.
 *
 * The states of the SessionWatcher are as follows:
 * <p><ul>
 * <li>If no processor and client service are specified, the session watcher does nothing.</li>
 * <li>If a processor is specified, but no client service is specified, the session watcher
 * periodically uses the processor to reconnect until a client service is specified.</li>
 * <li>If both a processor and client service are specified, the session watcher consults the
 * client to check whether session pings have been recently received. If yes, it uses the latest
 * session ping parameters from the server to adjust the watch interval. If no pings have
 * been received, the session watcher periodically attempts to reconnect until pings are
 * received again.</li>
 * <li>In case a client service is specified, but no processor is specified, the session watcher
 * will not attempt to reconnect.</li>
 * </ul></p>
 *
 * The sesion watcher adjust its watch period based on feedback from the server. The server
 * may change its ping interval based on load, network weather and current activity. If the robot
 * is currently being controlled, the server is likely to increase the ping frequency, while
 * the server will decrease the ping frequency if the robot is dormant.
 *
 * The session watcher is compatible with the quick reconnect pathway used by the
 * {@link ConnectionProcessor} to quickly re-establish lost connections.
 *
 * The SessionWatcher is a singleton class and a daemon thread.
 */
public class SessionWatcher extends Watcher {

  /**
   * SessionWatcher is singleton. A SessionWatcher instance can be obtained via {@link #Instance}.
   */
  private SessionWatcher() {
    super("session-watcher", InterbotClientService.SessionPingParameters.kDefaultPeriodMillis);
    this.client_service_ = null;
    this.processor_ = null;
  }

  /**
   * Returns the client service that handles session pings. If no client service is active, this
   * method returns null.
   *
   * @return The client service that handles session pings from the server.
   */
  public synchronized InterbotClientService getClientService() {
    return client_service_;
  }

  /**
   * Returns the ConnectionProcessor that manages the connection to the server, or null if no
   * connection is active.
   *
   * @return The ConnectionProcessor that is currently managing connections to the server or null.
   */
  public synchronized ConnectionProcessor getProcessor() {
    return processor_;
  }

  /**
   * Sets the client service that handles session pings. A value of null indicates that no client
   * service is currently active.
   *
   * If necessary, this method also resets the watch period to the default value until new
   * parameters are received from the server.
   *
   * @param client_service The client service that handles session pings from the server.
   */
  public synchronized void setClientService(InterbotClientService client_service) {
    this.client_service_ = client_service;
    if (getWatchPeriodMillis() !=
        InterbotClientService.SessionPingParameters.kDefaultPeriodMillis) {
      log.debug("reschedulig session watcher with period {}",
                InterbotClientService.SessionPingParameters.kDefaultPeriodMillis);
      reschedule(InterbotClientService.SessionPingParameters.kDefaultPeriodMillis);
    }
  }

  /**
   * Sets the ConnectionProcessor that manages connections to the server. A value of null
   * indicates that no connection is currently active.
   *
   * @param processor The processor that manages connections to the server.
   */
  public synchronized void setProcessor(ConnectionProcessor processor) {
    this.processor_ = processor;
  }

  /**
   * This method is periodically called by the watcher thread.
   */
  @Override
  protected void watch() {
    InterbotClientService client_service;
    ConnectionProcessor processor;
    synchronized (this) {
      // The scope of this lock must be minimal to avoid deadlocks with the quick reconnect
      // pathway.
      client_service = client_service_;
      processor = processor_;
    }
    if (client_service != null && client_service.getSessionPingCount() > 0) {
      client_service.clearSessionPingCount();
      if (getWatchPeriodMillis() !=
          client_service.getLastSessionPingParameters().getPeriodMillis()) {
        log.debug("reschedulig session watcher with period {}",
                  client_service.getLastSessionPingParameters().getPeriodMillis());
        reschedule(client_service.getLastSessionPingParameters().getPeriodMillis());
      }
    } else if (processor == null) {
      // no active connection to watch
      return;
    } else {
      log.warn("connection lost, reconnecting...");
      if (processor.reconnect() == ConnectionResult.Success) {
        log.info("session reconnected");
      } else {
        log.warn("failed to reconnect");
        // failed to reconnect, will try again next time
      }
    }
  }

  public final static SessionWatcher Instance = new SessionWatcher();
  private static Logger log = LogManager.getLogger();

  private volatile InterbotClientService client_service_;  // The client service being watched.
  private volatile ConnectionProcessor processor_;  // The processor handling server messages.
}
