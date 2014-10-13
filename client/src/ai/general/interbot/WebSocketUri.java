/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot;

import ai.general.net.Uri;

import java.net.URI;

/**
 * Represents a URI for WebSocket connection.
 *
 * WebSocketUri supports adding and removing query parameters from the URI.
 */
public class WebSocketUri extends Uri {

  /**
   * Constructs a WebSocketUri using the specified URI string.
   *
   * The URI string must include the protocol, server domain name or IP address, and the
   * WebSocket service path. The URI string may include a port if a non-standard port is used.
   *
   * The protocol must be either the WebSocket protocol or the secure WebSocket procotol.
   *
   * If the URI is invalid, an IllegalArgumentException is thrown.
   *
   * @param uri The URI string.
   * @throws IllegalArgumentException If the URI is invalid.
   */
  public WebSocketUri(String uri) throws IllegalArgumentException {
    super(uri);
    if (!(getProtocol().equals("ws") || getProtocol().equals("wss"))) {
      throw new IllegalArgumentException("Protocol must be a WebSocket protocol.");
    }
  }

  /**
   * Constructs a WebSocketUri using the specified Java URI.
   *
   * The URI must include the protocol, server domain name or IP address, and the
   * WebSocket service path. The URI may include a port if a non-standard port is used.
   *
   * The protocol must be either the WebSocket protocol or the secure WebSocket procotol.
   *
   * If the URI is invalid, an IllegalArgumentException is thrown.
   *
   * @param uri The URI string.
   * @throws IllegalArgumentException If the URI is invalid.
   */
  public WebSocketUri(URI uri) throws IllegalArgumentException {
    super(uri);
    if (!(getProtocol().equals("ws") || getProtocol().equals("wss"))) {
      throw new IllegalArgumentException("Protocol must be a WebSocket protocol.");
    }
  }

  /**
   * Constructs a WebSocketUri using the specified parameters.
   *
   * The server address may be a domain name or an IP address. By default, port 80 or 443 is used.
   * If the server uses another port, it may be specified via {@link #setPort(int)} after
   * construction.
   *
   * The service path must absolute be and start with a slash.
   *
   * Additional connection parameters may be specified using the
   * {@link #addParameter(String, String)} method.
   *
   * @param secure Whether the connection is encrypted using TLS. Must be supported by server.
   * @param server The Internet address of the server.
   * @param service_path Path of the WebSocket service on the server.
   * @throws IllegalArgumentException if one of the arguments is invalid.
   */
  public WebSocketUri(boolean secure, String server, String service_path)
    throws IllegalArgumentException {
    super(secure ? "wss" : "ws", server, service_path);
  }

  /**
   * Returns true if a secure protocol is used.
   *
   * @return True if a secure protocol is used.
   */
  public boolean isSecure() {
    return getProtocol().equals("wss");
  }
}
