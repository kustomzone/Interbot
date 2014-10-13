/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot;

import ai.general.event.Event;
import ai.general.event.Observer;
import ai.general.net.OutputSender;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Implements a WebSocket client.
 *
 * WebSocket can connect to a WebSocket server and send and receive messages from the server.
 * Each WebSocket instance connects to a specific WebSocket URI on the server.
 *
 * WebSocket implements the {@link ai.general.net.OutputSender} interface and can be used
 * to send text or binary data to the remote endpoint.
 *
 * {@link Processor} instances can register themselves with WebSocket to receive incoming text or
 * binary data.
 *
 * WebSocket is thread-safe.
 */
@ClientEndpoint(subprotocols="wamp")
public class WebSocket implements OutputSender {

  /** Timeout to open a WebSocket in milliseconds. */
  private static final int kOpenTimeoutMillis = 60000;

  /**
   * Creates a WebSocket for the specified URL.
   *
   * The socket must be explicitly opened via {@link #open()}.
   *
   * @param uri The URI of the WebSocket service.
   */
  public WebSocket(WebSocketUri uri) {
    this.uri_ = uri;
    this.session_ = null;
    this.event_handler_ = new Event<WebSocketEvent>();
  }

  /**
   * Closes the WebSocket connection.
   */
  public synchronized void close() {
    if (session_ != null && session_.isOpen()) {
      try {
        session_.close();
      } catch (IOException e) {}
      session_ = null;
    }
  }

  /**
   * Returns the server endpoint URI to which this WebSocket is connected.
   *
   * @return The service URI associated with the connection.
   */
  public WebSocketUri getUri() {
    return uri_;
  }

  /**
   * Returns true if the connection is open.
   *
   * @return True if the connection is open.
   */
  public boolean isOpen() {
    return session_ != null && session_.isOpen();
  }

  /**
   * Called by the container implementation when the WebSocket connection is closed.
   *
   * @param reason The reason why the WebSocket was closed.
   */
  @OnClose
  public synchronized void onClose(CloseReason reason) {
    log.debug("websocket closed: {}", reason.toString());
    event_handler_.trigger(WebSocketEvent.close(reason));
  }

  /**
   * Called by the container implementation when an error has occurred.
   *
   * @param error Error details.
   */
  @OnError
  public synchronized void onError(Throwable error) {
    log.catching(Level.DEBUG, error);
    event_handler_.trigger(WebSocketEvent.error(error));
  }

  /**
   * Called by the container implementation when an incomong WebSocket text message is received.
   * This method delivers the incoming message to any processors registered with this WebSocket.
   *
   * @param text The incoming text data.
   */
  @OnMessage
  public synchronized void onMessage(String text) {
    log.debug("=> {}", text);
    event_handler_.trigger(WebSocketEvent.textMessage(text));
  }

  /**
   * Called by the container implementation when an incomong WebSocket binary message is received.
   * This method delivers the incoming message to any processors registered with this WebSocket.
   *
   * @param data The incoming binary data.
   */
  @OnMessage
  public synchronized void onMessage(ByteBuffer data) {
    log.debug("=> (binary {} bytes)", data.limit());
    event_handler_.trigger(WebSocketEvent.binaryMessage(data));
  }

  /**
   * Called by the container implementation when the WebSocket connection has been opened.
   * This method must not be called directly.
   *
   * @param session The WebSocket session associated with the connection.
   */
  @OnOpen
  public synchronized void onOpen(Session session) {
    log.debug("websocket open uri = {}", uri_.toString());
    this.session_ = session;
    event_handler_.trigger(WebSocketEvent.open());
  }

  /**
   * Opens the WebSocket connection. This method blocks until the connection has been succesfully
   * opened or a timeout or error has occurred.
   *
   * @return True if the connection is open.
   */
  public boolean open() {
    boolean success = false;
    Observer<WebSocketEvent> observer = new Observer<WebSocketEvent>();
    subscribe(observer);
    if (openAsync()) {
      WebSocketEvent event = observer.poll(kOpenTimeoutMillis);
      if (event != null) {
        success = event.getEventType() == WebSocketEvent.Type.Open;
      }
    }
    unsubscribe(observer);
    return success;
  }

  /**
   * Initiates the opening of the WebSocket connection and immediately returns.
   *
   * The WebSocket connection will be opened asynchronously. When the connection is open,
   * the {@link #isOpen()} method will return true and a {@link WebSocketEvent} of type Open will
   * be triggered.
   *
   * @return True if the opening of the WebSocket connection was succesfully initiated.
   */
  public synchronized boolean openAsync() {
    try {
      WebSocketContainer socket_factory = ContainerProvider.getWebSocketContainer();
      if (socket_factory != null) {
        socket_factory.connectToServer(this, uri_.toUri());
        return true;
      }
    } catch (DeploymentException e) {
      log.catching(Level.ERROR, e);
    } catch (IOException e) {
      log.catching(Level.ERROR, e);
    } catch (Exception e) {
      // This catches unresolved address exceptions and other undocumented exceptions.
      log.catching(Level.ERROR, e);
    }
    return false;
  }

  /**
   * Sends a binary message to the remote endpoint.
   *
   * The socket must be open. Triggers a {@link WebSocketEvent} of type Error if the socket is
   * open and the message could not be sent.
   *
   * @param data Binary data to send.
   * @return True if the data was successfully sent.
   */
  @Override
  public boolean sendBinary(ByteBuffer data) {
    if (!isOpen()) {
      return false;
    }
    try {
      session_.getBasicRemote().sendBinary(data);
      log.debug("<= (binary {} bytes)", data.limit());
      return true;
    } catch (Exception e) {
      log.catching(Level.DEBUG, e);
      event_handler_.trigger(WebSocketEvent.error(e));
      return false;
    }
  }

  /**
   * Sends a chunked binary message to the remote endpoint.
   *
   * A chunked message is split into multiple parts. The parts are sent in order until the last
   * chunk completes the message.
   * Large messages should be sent as a series of chunks.
   *
   * No other message can be sent until a chunked message is complete.
   *
   * The socket must be open. Triggers a {@link WebSocketEvent} of type Error if the socket is
   * open and the message could not be sent.
   *
   * @param data Chunk of binary data to send.
   * @param last_chunk True if this is the last chunk of the message.
   * @return True if the data was successfully sent.
   */
  public boolean sendBinaryChunk(ByteBuffer data, boolean last_chunk) {
    if (!isOpen()) {
      return false;
    }
    try {
      session_.getBasicRemote().sendBinary(data, last_chunk);
      log.debug("<= (binary chunk {} bytes, last = {})", data.limit(), last_chunk);
      return true;
    } catch (Exception e) {
      log.catching(Level.TRACE, e);
      event_handler_.trigger(WebSocketEvent.error(e));
      return false;
    }
  }

  /**
   * Sends a text message to the remote endpoint.
   *
   * The socket must be open. Triggers a {@link WebSocketEvent} of type Error if the socket is
   * open and the message could not be sent.
   *
   * @param text Text message to send.
   * @return True if the message was successfully sent.
   */
  @Override
  public boolean sendText(String text) {
    if (!isOpen()) {
      return false;
    }
    try {
      session_.getBasicRemote().sendText(text);
      log.debug("<= {}", text);
      return true;
    } catch (Exception e) {
      log.catching(Level.TRACE, e);
      event_handler_.trigger(WebSocketEvent.error(e));
      return false;
    }
  }

  /**
   * Subscribes the observer to events triggered by this WebSocket instance.
   *
   * @param observer Observer to subscribe.
   */
  public synchronized void subscribe(Observer<WebSocketEvent> observer) {
    event_handler_.subscribe(observer);
  }

  /**
   * Unsubscribes a subscribed observer from WebSocket events.
   *
   * @param observer Observer to unsubscribe.
   */
  public synchronized void unsubscribe(Observer<WebSocketEvent> observer) {
    event_handler_.unsubscribe(observer);
  }

  private static Logger log = LogManager.getLogger();

  private Event<WebSocketEvent> event_handler_;  // Event handler for WebSocket events.
  private Session session_;  // WebSocket session.
  private WebSocketUri uri_;  // URI of remote endpoint.
}
