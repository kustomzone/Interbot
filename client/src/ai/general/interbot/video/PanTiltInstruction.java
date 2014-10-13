/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot.video;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Specifies pan-tilt unit instructions for an IP camera.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public enum PanTiltInstruction {
  /** Center position. */
  Center,

  /** Move down.*/
  Down,

  /** Move left. */
  Left,

  /** Move right. */
  Right,

  /** Move up.*/
  Up,
}
