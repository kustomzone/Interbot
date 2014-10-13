/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web.video;

/**
 * Represents a video instruction. Video instructions are sent by the server to control the video
 * system.
 * They can be used to turn on or off the video stream of the robot.
 *
 * VideoInstructions are serialized into JSON.
 */
public class VideoInstruction {

  public static final String kVideoInputServicePath = "/interbot/video/in";

  /**
   * The instruction.
   */
  public enum Instruction {
    /** Unsupported instruction. */
    Undefined,
    /** Starts the video stream. */
    StartStream,
    /** Stops the video stream. */
    StopStream
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
   * Creates a VideoInstruction with the specified parameters.
   *
   * @param instruction The instruction type.
   * @param service_path The path of the server endpoint associated with the instruction.
   * @param video_channel The video channel ID.
   */
  public VideoInstruction(Instruction instruction, String service_path, String video_channel) {
    this.instruction_ = instruction;
    this.service_path_ = service_path;
    this.channel_ = video_channel;
  }

  /**
   * Creates a start stream instruction for the specified video channel.
   *
   * @param video_channel The video channel ID.
   * @return A video instruction to start streaming video on the specified video channel.
   */
  public static VideoInstruction startStream(String video_channel) {
    return new VideoInstruction(Instruction.StartStream, kVideoInputServicePath, video_channel);
  }

  /**
   * Creates a stop stream instruction for the specified video channel.
   *
   * @param video_channel The video channel ID.
   * @return A video instruction to stop streaming video on the specified video channel.
   */
  public static VideoInstruction stopStream(String video_channel) {
    return new VideoInstruction(Instruction.StopStream, kVideoInputServicePath, video_channel);
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
   * Returns the path of the WebSocket service that accepts the video input.
   *
   * @return The path of the WebSocket service that accepts the video input.
   */
  public String getServicePath() {
    return service_path_;
  }

  /**
   * Sets the path of the WebSocket service that accepts the video input.
   *
   * @param service_path The path of the WebSocket service that accepts the video input.
   */
  public void setServicePath(String service_path) {
    this.service_path_ = service_path;
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
   * Sets the name of the video channel.
   *
   * @param channel The name of the video channel.
   */
  public void setChannel(String channel) {
    this.channel_ = channel;
  }

  private Instruction instruction_;
  private String service_path_;
  private String channel_;
}
