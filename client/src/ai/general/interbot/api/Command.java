/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot.api;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Base class for all commands sent by the user to a robot.
 * Subclasses of this class implement specific commands.
 *
 * All commands have a timestamp associated with them that indicates when the command was first
 * issued by the user.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Command {

  /**
   * Constructs a command object with timestamp set to 0.
   */
  public Command() {
    this.timestamp_ = 0;
    this.reception_timestamp_millis_ = 0;
  }

  /**
   * Constructs a command with the specified timestamp.
   *
   * @param timestamp The time when this command was issued.
   */
  public Command(Date timestamp) {
    this.timestamp_ = timestamp.getTime();
    this.reception_timestamp_millis_ = 0;
  }

  /**
   * Returns when this command was received.
   *
   * The reception timestamp is only set on the reciever end. It is not serialized into or
   * deserialized from JSON.
   *
   * The receiver must set the reception timestamp by calling
   * {@link #setReceptionTimestampMillis(long)} or {@link #setReceptionTimestampToNow()}.
   *
   * @return The time when this command was received in milliseconds since the Epoch.
   */
  @JsonIgnore
  public long getReceptionTimestampMillis() {
    return reception_timestamp_millis_;
  }

  /**
   * Returns when this command was first issued. The timestamp is represented as the number of
   * milliseconds since the Unix Epoch (midnight January 1, 1970) in UTC time.
   *
   * @return The time when this command was first issued in milliseconds since the Epoch.
   */
  public long getTimestamp() {
    return timestamp_;
  }

  /**
   * Sets the reception timestamp to the specified time in milliseconds since the Unix Epoch.
   * The reception timestamp is not serialized into or deserialized from JSON.
   *
   * @param reception_timestamp_millis The time when this command was received.
   */
  @JsonIgnore
  public void setReceptionTimestampMillis(long reception_timestamp_millis) {
    this.reception_timestamp_millis_ = reception_timestamp_millis;
  }

  /**
   * Sets the reception timestamp to the current system time. This method can be called by
   * a receiver when a command has just been received.
   */
  @JsonIgnore
  public void setReceptionTimestampToNow() {
    setReceptionTimestampMillis(System.currentTimeMillis());
  }

  /**
   * Sets the time when this command was first issued. The timestamp is represented as the number
   * of milliseconds since the Unix Epoch (January 1, 1970) in UTC time.
   *
   * @param timestamp The time when this command was first issued in milliseconds since the Epoch.
   */
  public void setTimestamp(long timestamp) {
    this.timestamp_ = timestamp;
  }

  private long timestamp_;  // Command issue time in milliseconds since Epoch.
  private long reception_timestamp_millis_;  // Command reception time in milliseconds since Epoch.
}
