/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot.video;

import ai.general.interbot.api.Command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a command to change the orientation of the camera.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class PanTiltCommand extends Command {

  /**
   * Constructs a default pan-tilt command.
   */
  public PanTiltCommand() {
    instruction_ = PanTiltInstruction.Center;
  }

  /**
   * Returns the pan-tilt instruction.
   *
   * @return The pan-tilt instruction.
   */
  public PanTiltInstruction getInstruction() {
    return instruction_;
  }

  /**
   * Sets the pan-tilt instruction.
   *
   * @param instruction The pan-tilt instruction.
   */
  public void setInstruction(PanTiltInstruction instruction) {
    this.instruction_ = instruction;
  }

  private PanTiltInstruction instruction_;  // Pan-tilt instruction.
}
