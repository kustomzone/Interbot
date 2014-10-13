/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot;

import java.nio.ByteBuffer;
import javax.websocket.CloseReason;

/**
 * Represents WebSocket events. WebSocketEvent allows notifying {@link Observer} instances of
 * a WebSocket event.
 */
public class WebSocketEvent {

  /**
   * Type of WebSocket event.
   */
  public enum Type {
    /** A binary message was received. */
    BinaryMessage,

    /** The WebSocket was closed. */
    Close,

    /** An error has occurred. */
    Error,

    /** The WebSocket was opened. */
    Open,

    /** A text message was received. */
    TextMessage,
  }

  /**
   * Creates a WebSocketEvent of the specified type.
   * To create a WebSocketEvent, one of static methods below that return a WebSocket instance
   * can be used.
   */
  private WebSocketEvent(Type event_type) {
    this.event_type_ = event_type;
    text_message_ = null;
    binary_message_ = null;
    close_reason_ = null;
    error_ = null;
  }

  /**
   * Creates a WebSocketEvent of type BinaryMessage.
   *
   * @param binary_message The binary message received from the remote endpoint.
   * @return New WebSocketEvent.
   */
  public static WebSocketEvent binaryMessage(ByteBuffer binary_message) {
    WebSocketEvent event = new WebSocketEvent(Type.BinaryMessage);
    event.binary_message_ = binary_message;
    return event;
  }

  /**
   * Creates a WebSocketEvent of type Close with the specified close reason.
   *
   * @param close_reason Reason why the socket was closed.
   * @return New WebSocketEvent.
   */
  public static WebSocketEvent close(CloseReason close_reason) {
    WebSocketEvent event = new WebSocketEvent(Type.Close);
    event.close_reason_ = close_reason;
    return event;
  }

  /**
   * Creates a WebSocketEvent of type Error.
   *
   * @param error The error description.
   * @return New WebSocketEvent.
   */
  public static WebSocketEvent error(Throwable error) {
    WebSocketEvent event = new WebSocketEvent(Type.Error);
    event.error_ = error;
    return event;
  }

  /**
   * Creates a WebSocketEvent of type Open.
   *
   * @return New WebSocketEvent.
   */
  public static WebSocketEvent open() {
    return new WebSocketEvent(Type.Open);
  }

  /**
   * Creates a WebSocketEvent of type TextMessage.
   *
   * @param text_message The text message received from the remote endpoint.
   * @return New WebSocketEvent.
   */
  public static WebSocketEvent textMessage(String text_message) {
    WebSocketEvent event = new WebSocketEvent(Type.TextMessage);
    event.text_message_ = text_message;
    return event;
  }

  /**
   * If this is an event of type BinaryMessage, returns the binary message.
   *
   * @return The binary message or null.
   */
  public ByteBuffer getBinaryMessage() {
    return binary_message_;
  }

  /**
   * If this is an event of type Close, returns the close reason.
   *
   * @return The close reason or null.
   */
  public CloseReason getCloseReason() {
    return close_reason_;
  }

  /**
   * If this is an event of type Error, returns the error details.
   *
   * @return The error details or null.
   */
  public Throwable getError() {
    return error_;
  }

  /**
   * Returns the event type.
   *
   * @return The event type.
   */
  public Type getEventType() {
    return event_type_;
  }

  /**
   * If this is an event of type TextMessage, returns the text message.
   *
   * @return The text message or null.
   */
  public String getTextMessage() {
    return text_message_;
  }

  private ByteBuffer binary_message_;  // set if event_type_ == Type.BinaryMessage
  private CloseReason close_reason_;  // set if event_type_ == Type.Close
  private Throwable error_;  // set if event_type_ == Type.Error
  private Type event_type_;  // The type of WebSocket event.
  private String text_message_;  // set if event_type_ == Type.TextMessage
}
