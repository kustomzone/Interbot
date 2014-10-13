/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Enumeration of system properties that can be queried via a {@link SystemInfoRequest}.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public enum SystemProperty {
  /** Devices supported by the robot. */
  Devices,

  /** The list of network interfaces and their associated IP addresses. */
  NetworkInterfaces,
}
