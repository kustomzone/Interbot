/* General AI - Interbot
 * Copyright (C) 2014 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot;

/**
 * Represents the result of a connection attempt to a server.
 *
 * If a connection to the server could be established, but the connection was closed by the
 * server, it is interpreted as a login error.
 */
public enum ConnectionResult {
  /** The connection was unsuccessful due to a network error. */
  ConnectionError,

  /** The connection handshake has not started or is in progress. */
  Incomplete,

  /** The connection was unsuccessful due to a login error. */
  LoginError,

  /** The connection was successful and the client is logged into the server. */
  Success,

  /** The connection was unsuccessful due to a timeout. */
  Timeout,
}
