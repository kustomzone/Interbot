/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

/**
 * Enumeration of system properties that can be queried via a {@link SystemInfoRequest}.
 */
public enum SystemProperty {
  /** The list of network interfaces and their associated IP addresses. */
  NetworkInterfaces,

  /** Devices supported by the robot. */
  Devices
}
