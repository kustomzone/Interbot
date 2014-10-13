/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot.video;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a video instruction. Video instructions are sent by the server to control the video
 * system.
 * They can be used to turn on or off the video stream of the robot.
 *
 * VideoInstructions are deserialized from JSON.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class VideoInstruction {

  /**
   * The instruction.
   */
  public enum Instruction {
    /** Starts the video stream. */
    StartStream,

    /** Stops the video stream. */
    StopStream,

    /** Unsupported instruction. */
    Undefined,
  }

  /**
   * Creates an empty VideoInstruction.
   */
  public VideoInstruction() {
    this.instruction_ = Instruction.Undefined;
    this.service_path_ = "";
    this.channel_ = "";
  }

  /**
   * Returns the name of the video channel.
   *
   * @return The name of the video channel.
   */
  public String getChannel() {
    return channel_;
  }

  /**
   * Returns the instruction.
   *
   * @return The instruction.
   */
  public Instruction getInstruction() {
    return instruction_;
  }

  /**
   * Returns the path of the WebSocket service that accepts the video input.
   *
   * @return The path of the WebSocket service that accepts the video input.
   */
  public String getServicePath() {
    return service_path_;
  }

  /**
   * Sets the name of the video channel.
   *
   * @param channel The name of the video channel.
   */
  public void setChannel(String channel) {
    this.channel_ = channel;
  }

  /**
   * Sets the instruction from the string representation of the instruction.
   * If the instruction is unsupported, it will be set to Undefined.
   *
   * @param instruction_name The string representation of the instruction.
   */
  public void setInstruction(String instruction_name) {
    try {
      this.instruction_ = Enum.valueOf(Instruction.class, instruction_name);
    } catch (IllegalArgumentException e) {
      this.instruction_ = Instruction.Undefined;
    }
  }

  /**
   * Sets the path of the WebSocket service that accepts the video input.
   *
   * @param service_path The path of the WebSocket service that accepts the video input.
   */
  public void setServicePath(String service_path) {
    this.service_path_ = service_path;
  }

  private Instruction instruction_;  // Video stream instruction.
  private String channel_;  // Name of video channel on server.
  private String service_path_;  // Path of video input service on server.
}
