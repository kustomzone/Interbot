/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents an incoming system info request.
 * The request specifies what system information is requested.
 *
 * SystemInfoRequests are deserialized from JSON.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class SystemInfoRequest {

  /**
   * Creates an empty SystemInfoRequest.
   */
  public SystemInfoRequest() {
    this.properties_ = new ArrayList<SystemProperty>();
  }

  /**
   * Returns a list of properties queried in the SystemInfoRequest.
   *
   * @return The list of properties queried by the SystemInfoRequest.
   */
  public ArrayList<SystemProperty> getProperties() {
    return properties_;
  }

  /**
   * Sets the list of properties queried by the SystemInfoRequest.
   *
   * @param properties The list of properties queried by the SystemInfoRequest.
   */
  public void setProperties(ArrayList<SystemProperty> properties) {
    this.properties_ = properties;
  }

  private ArrayList<SystemProperty> properties_;  // List of queried system properties.
}
