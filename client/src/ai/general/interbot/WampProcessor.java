/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot;

import ai.general.net.Connection;
import ai.general.net.Uri;
import ai.general.net.wamp.WampConnection;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Implements a ConnectionProcessor for WAMP messages.
 */
public class WampProcessor extends ConnectionProcessor {

  private static final int kHandshakeTimeoutMillis = 60000;

  /**
   * The service URI must point to a WAMP service.
   *
   * The constructor does not open the connection. It must be opened via the {@link #open()}
   * method.
   *
   * @param wamp_service_uri The server URI endpoint that provides the WAMP service.
   */
  public WampProcessor(WebSocketUri wamp_service_uri) {
    super(wamp_service_uri);
  }

  /**
   * This constructor allows specification of a username and API key which are used to log into
   * the server. The API key is equivalent to a hashed password.
   *
   * The service URI must point to a WAMP service.
   * The constructor does not open the connection. It must be opened via the {@link #open()}
   * method.
   *
   * @param wamp_service_uri The server URL endpoint that provides the WAMP service.
   * @param username The username with which to log in.
   * @param api_key The API key to use to log in.
   */
  public WampProcessor(WebSocketUri wamp_service_uri, String username, String api_key) {
    super(wamp_service_uri, username, api_key);
  }

  /**
   * Creates and returns the protocol specific connection instance.
   * The return connection instance implements the WAMP protocol.
   *
   * This method does not open the connection.
   *
   * @param home_path The local home directory path to be used for the connection.
   * @return The unopened connection instance or null.
   */
  @Override
  protected Connection createConnection(String home_path) {
    Uri uri = getWebSocket().getUri();
    if (uri.toUri() == null) {
      return null;
    }
    return new WampConnection(uri, uri.getUser(), home_path, getWebSocket());
  }

  /**
   * Establishes the connection to the server.
   *
   * @return The result of the connection attempt.
   */
  @Override
  protected ConnectionResult connect() {
    WampHandshakeProcessor handshake =
      new WampHandshakeProcessor(getWebSocket(), (WampConnection) getConnection());
    handshake.start();
    try {
      handshake.join(kHandshakeTimeoutMillis);
    } catch (InterruptedException e) {}
    if (handshake.isAlive()) {
      handshake.halt();
      log.error("WAMP handshake unsuccessful (timeout).");
      return ConnectionResult.Timeout;
    }
    return handshake.getResult();
  }

  private static Logger log = LogManager.getLogger();
}
