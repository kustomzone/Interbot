/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot;

import ai.general.event.Processor;
import ai.general.net.Connection;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Processes incoming messages sent over a WebSocket connection.
 *
 * The ConnectionProcessor implements a general WebSocket processor. A concrete subclass must
 * provide the specific server protocol used to process message.
 * The specific server protocol is provided via implementing the {@link #createConnection(String)}
 * and {@link #connect()} methods.
 *
 * The connection to the server can be opened via the {@link #open()} method. Once opened, clients
 * can make requests via the {@link Connection} object, which can be obtained via
 * {@link #getConnection()}.
 * Any incoming messages will be automatically processed on a separate thread. In order to respond
 * to incoming messages, clients must implement handlers using the the ai.general.plugin
 * and ai.general.directory packages.
 */
public abstract class ConnectionProcessor extends Processor<WebSocketEvent> {

  private static final String kKeyParameter = "key";
  private static final String kReconnectParameter = "reconnect";
  private static final int kReconnectionWaitMillis = 5000;
  private static final String kUsernameParameter = "username";
  private static final String kVersionParameter = "v";

  /** Processor states during a quick reconnection attempt. */
  private enum QuickReconnectState {
    None,  // not reconnecting
    Reconnecting,  // quick reconnection attempt is executing
    Scheduled,  // quick reconnection attempt has been scheduled
  }

  /**
   * The service URI must point to a protocol specific service on the server.
   *
   * The constructor does not open the connection. It must be opened via the {@link #open()}
   * method.
   *
   * @param service_uri The server URI endpoint that provides the protocol specific service.
   */
  public ConnectionProcessor(WebSocketUri service_uri) {
    super("ConnectionProcessor(" + service_uri + ")");
    service_uri.setParameter(kVersionParameter, Version.kVersion);
    socket_ = new WebSocket(service_uri);
    connection_ = null;
    quick_reconnect_state_ = QuickReconnectState.None;
  }

  /**
   * This constructor allows specification of a username and API key which are used to log into
   * the server. The API key is equivalent to a hashed password.
   *
   * The service URI must point to a protocol specific service on the server.
   * The constructor does not open the connection. It must be opened via the {@link #open()}
   * method.
   *
   * @param service_uri The server URL endpoint that provides the protocol specific service.
   * @param username The username with which to log in.
   * @param api_key The API key to use to log in.
   */
  public ConnectionProcessor(WebSocketUri service_uri, String username, String api_key) {
    this(service_uri);
    service_uri.setParameter(kUsernameParameter, username);
    service_uri.setParameter(kKeyParameter, api_key);
  }

  /**
   * Closes the connection to the server and halts.
   */
  public void close() {
    halt();
  }

  /**
   * Returns the server URI to which this processor is connected.
   *
   * @return Server URI to which the processor is connected.
   */
  public WebSocketUri getConnectionUri() {
    return socket_.getUri();
  }

  /**
   * Returns the connection instance which implements the protocol to send and receive messages
   * to the server. The return value is only valid if the connection has been successfully opened.
   *
   * @return The connection instance if the connection has been opened or null.
   */
  public Connection getConnection() {
    return connection_;
  }

  /**
   * Closes the socket and halts.
   */
  @Override
  public void halt() {
    socket_.unsubscribe(getObserver());
    super.halt();
    if (socket_.isOpen()) {
      try {
        join(2 * kPollTimeoutMillis);
      } catch (InterruptedException e) {}
      socket_.close();
    }
  }

  /**
   * Opens the connection to server.
   *
   * @param home_path The local home directory path to be used for the connection.
   * @return The result of the connection attempt.
   */
  public ConnectionResult open(String home_path) {
    connection_ = createConnection(home_path);
    if (connection_ == null) {
      return ConnectionResult.ConnectionError;
    }
    ConnectionResult result = connect();
    if (result == ConnectionResult.Success) {
      log.debug("Connected to server {}", connection_.getServerId());
      socket_.subscribe(getObserver());
      start();
    } else {
      log.error("Could not connect to server: {}", result.name());
    }
    return result;
  }

  /**
   * Attempts to reconnect to the server.
   *
   * This method reconnects without halting the processor. During reconnection, no messages may
   * be send via the connection.
   *
   * @return The result of the reconnection attempt.
   */
  public synchronized ConnectionResult reconnect() {
    if (quick_reconnect_state_ == QuickReconnectState.Scheduled) {
      // quick reconnect in progress
      // a return value of ConnectionError ensures that the session manager continues reconnection
      // attempts if the quick reconnect fails
      return ConnectionResult.ConnectionError;
    }
    socket_.unsubscribe(getObserver());
    connection_.close();
    if (socket_.isOpen()) {
      socket_.close();
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {}
    }
    socket_.getUri().setParameter(kReconnectParameter, connection_.getSessionId());
    ConnectionResult result = connect();
    socket_.getUri().removeParameter("reconnect");
    if (result == ConnectionResult.Success) {
      socket_.subscribe(getObserver());
    } else {
      log.error("Could not connect to server: {}", result.name());
    }
    return result;
  }

  /**
   * Creates and returns the protocol specific connection instance.
   * The connection instance implements the specific protocol used to communicate with the server.
   *
   * This method does not open the connection.
   * This method may return null if the connection object cannot be created, in which case the
   * connection attempt will fail.
   *
   * The WebSocket used to connect to the server can be obtained via the {@link #getWebSocket()}
   * method.
   *
   * @param home_path The local home directory path to be used for the connection.
   * @return The unopened connection instance or null.
   */
  protected abstract Connection createConnection(String home_path);

  /**
   * Establishes the connection to the server.
   * This method is called after the connection has been created and may be called repeatedly
   * with the same connection instance to reconnect to the server if the connection is interrupted.
   *
   * The WebSocket used to connect to the server can be obtained via the {@link #getWebSocket()}
   * method.
   *
   * The Connection instance used to connect to the server can be obtained via the
   * {@link #getConnection()} method.
   *
   * @return The result of the connection attempt.
   */
  protected abstract ConnectionResult connect();

  /**
   * Returns the WebSocket instance used to connect to the server.
   *
   * @return The WebSocket instance used to connect to the server.
   */
  protected WebSocket getWebSocket() {
    return socket_;
  }

  /**
   * Processes WebSocket events.
   * Relays messages to the WampConnection instance.
   * This method assumes that the connection has already been opened and initialized.
   *
   * If the connection is closed or an error is encountered, this method tries to quickly
   * reconnect once. The reconnection is attempted on a separate Thread after a short wait period.
   * If immediate reconnection fails, further reconnection attempts are left to
   * the {@link SessionManager}.
   */
  @Override
  protected void process(WebSocketEvent event) {
    switch (event.getEventType()) {
      case TextMessage: connection_.process(event.getTextMessage()); break;
      case Close:
      case Error: {
        synchronized (this) {
          if (quick_reconnect_state_ == QuickReconnectState.None) {
            quick_reconnect_state_ = QuickReconnectState.Scheduled;
            socket_.unsubscribe(getObserver());
            Thread reconnection_thread = new Thread() {
                @Override
                public void run() {
                  try {
                    Thread.sleep(kReconnectionWaitMillis);
                  } catch (InterruptedException e) {}
                  reconnectQuick();
                }
              };
            reconnection_thread.start();
            log.warn("connection error or unexpectedly closed, reconnecting quick...");
          }
        }
        break;
      }
      default: break;
    }
  }

  /**
   * This method is called to attempt a quick reconnection without waiting for the
   * {@link SessionManager} if a connection error is encountered or the connection is
   * unexpectedly closed.
   */
  private synchronized void reconnectQuick() {
    quick_reconnect_state_ = QuickReconnectState.Reconnecting;
    if (reconnect() == ConnectionResult.Success) {
      log.info("session reconnected (quick)");
    } else {
      log.warn("failed to reconnect (quick)");
      // Any further attempts must be executed by the SessionManager.
    }
    quick_reconnect_state_ = QuickReconnectState.None;
  }

  private static Logger log = LogManager.getLogger();

  private Connection connection_;  // Implementation of server protocol.
  private QuickReconnectState quick_reconnect_state_;  // Quick reconnection state.
  private WebSocket socket_;  // WebSocket connected to remote endpoint.
}
