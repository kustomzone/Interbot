/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot.video;

/**
 * IP camera pan-tilt configuration options.
 */
public class PanTiltConfig {

  /**
   * Pan-tilt instruction configuration options.
   * An instruction is initiated via a begin command and completed via an end command.
   * The duration of the instruction specifies the time between the begin and end commands.
   */
  public static class InstructionConfig {

    /**
     * Constructs a default InstructionConfig.
     */
    public InstructionConfig() {
      begin_ = "";
      end_ = "";
      duration_millis_ = 0;
    }

    /**
     * Retuns the command to begin the instruction.
     *
     * @return The command to begin the instruction.
     */
    public String getBegin() {
      return begin_;
    }

    /**
     * Returns the duration of the instruction, i.e. the time between the begin and end commands.
     *
     * @return The duration of the instruction in milliseconds.
     */
    public int getDurationMillis() {
      return duration_millis_;
    }

    /**
     * Returns the command to end the instruction.
     *
     * @return The command to end the instruction.
     */
    public String getEnd() {
      return end_;
    }

    /**
     * Sets the command to begin the instruction.
     *
     * @param begin The command to begin the instruction.
     */
    public void setBegin(String begin) {
      this.begin_ = begin;
    }

    /**
     * Sets the the duration of the instruction, i.e. the time between the begin and end commands.
     *
     * @param duration_millis The duration of the instruction in milliseconds.
     */
    public void setDurationMillis(int duration_millis) {
      this.duration_millis_ = duration_millis;
    }

    /**
     * Sets the command to end the instruction.
     *
     * @param end The command to end the instruction.
     */
    public void setEnd(String end) {
      this.end_ = end;
    }

    private String begin_;  // instruction begin command
    private int duration_millis_;  // instruction duration in milliseconds
    private String end_;  // instruction end command
  }

  /**
   * Constructs a default pan-tilt configuration.
   */
  public PanTiltConfig() {
    url_ = "";
    right_ = new InstructionConfig();
    left_ = new InstructionConfig();
    up_ = new InstructionConfig();
    down_ = new InstructionConfig();
    center_ = new InstructionConfig();
  }

  /**
   * Retuns the URL to begin the specified pan-tilt instruction.
   * Returns the empty string if there is no appropriate instruction.
   *
   * @return The URL to begin the specified pan-tilt instruction.
   */
  public String beginUrl(PanTiltInstruction instruction) {
    InstructionConfig instruction_config = findInstructionConfig(instruction);
    if (instruction_config == null) {
      return "";
    }
    if (instruction_config.getBegin().length() == 0) {
      return "";
    }
    return url_ + instruction_config.getBegin();
  }

  /**
   * Returns the duration of the specified pan-tilt instruction in milliseconds.
   *
   * @return The duration of the specified pan-tilt instruction in milliseconds.
   */
  public int durationMillis(PanTiltInstruction instruction) {
    InstructionConfig instruction_config = findInstructionConfig(instruction);
    if (instruction_config == null) {
      return 0;
    }
    return instruction_config.getDurationMillis();
  }

  /**
   * Retuns the URL to end the specified pan-tilt instruction.
   * Returns the empty string if there is no end URL for the instruction.
   *
   * @return The URL to end the specified pan-tilt instruction.
   */
  public String endUrl(PanTiltInstruction instruction) {
    InstructionConfig instruction_config = findInstructionConfig(instruction);
    if (instruction_config == null) {
      return "";
    }
    if (instruction_config.getEnd().length() == 0) {
      return "";
    }
    return url_ + instruction_config.getEnd();
  }

  /**
   * Returns the configuration for the instruction to center the pan-tilt unit.
   *
   * @return The configuration for the center instruction.
   */
  public InstructionConfig getCenter() {
    return center_;
  }

  /**
   * Returns the configuration for the instruction to move the pan-tilt unit down.
   *
   * @return The configuration for the move down instruction.
   */
  public InstructionConfig getDown() {
    return down_;
  }

  /**
   * Returns the configuration for the instruction to move the pan-tilt unit towards the left.
   *
   * @return The configuration for the move left instruction.
   */
  public InstructionConfig getLeft() {
    return left_;
  }

  /**
   * Returns the configuration for the instruction to move the pan-tilt unit towards the right.
   *
   * @return The configuration for the move right instruction.
   */
  public InstructionConfig getRight() {
    return right_;
  }

  /**
   * Returns the configuration for the instruction to move the pan-tilt unit up.
   *
   * @return The configuration for the move up instruction.
   */
  public InstructionConfig getUp() {
    return up_;
  }

  /**
   * Returns the URL path of the pan-tilt unit controller on the IP camera.
   *
   * @return The URL path of the pan-tilt unit controller on the IP camera.
   */
  public String getUrl() {
    return url_;
  }

  /**
   * Returns the instruction configuration for the specified pan-tilt instruction.
   * Returns null if there is no configuration for the specified instruction.
   *
   * @return The instruction configuration for the specified pan-tilt instruction or null.
   */
  public InstructionConfig findInstructionConfig(PanTiltInstruction instruction) {
    switch (instruction) {
    case Right: return right_;
    case Left: return left_;
    case Up: return up_;
    case Down: return down_;
    case Center: return center_;
    default: return null;
    }
  }

  /**
   * Sets the configuration for the instruction to center the pan-tilt unit.
   *
   * @param center The configuration for the center instruction.
   */
  public void setCenter(InstructionConfig center) {
    this.center_ = center;
  }

  /**
   * Sets the configuration for the instruction to move the pan-tilt unit down.
   *
   * @param down The configuration for the move down instruction.
   */
  public void setDown(InstructionConfig down) {
    this.down_ = down;
  }

  /**
   * Sets the configuration for the instruction to move the pan-tilt unit towards the left.
   *
   * @param left The configuration for the move left instruction.
   */
  public void setLeft(InstructionConfig left) {
    this.left_ = left;
  }

  /**
   * Sets the configuration for the instruction to move the pan-tilt unit towards the right.
   *
   * @param right The configuration for the move right instruction.
   */
  public void setRight(InstructionConfig right) {
    this.right_ = right;
  }

  /**
   * Sets the configuration for the instruction to move the pan-tilt unit up.
   *
   * @param up The configuration for the move up instruction.
   */
  public void setUp(InstructionConfig up) {
    this.up_ = up;
  }

  /**
   * Sets the URL path of the pan-tilt unit controller on the IP camera.
   *
   * @param url The URL path of the pan-tilt unit controller on the IP camera.
   */
  public void setUrl(String url) {
    this.url_ = url;
  }

  private InstructionConfig center_;  // instruction to center the camera
  private InstructionConfig down_;  // instruction to move down
  private InstructionConfig left_;  // instruction to move up
  private InstructionConfig right_;  // instruction to move right
  private InstructionConfig up_;  // instructionto move up
  private String url_;  // URL prefix of the pan-tilt controller
}
