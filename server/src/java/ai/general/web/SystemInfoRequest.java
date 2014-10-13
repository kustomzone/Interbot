/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import java.util.ArrayList;

/**
 * Represents an incoming system info request.
 * The request specifies what system information is requested.
 *
 * SystemInfoRequests are serialized into JSON.
 */
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

  /**
   * Add the specified property to the request.
   *
   * @param property The property to add to the request.
   */
  public void addProperty(SystemProperty property) {
    properties_.add(property);
  }

  private ArrayList<SystemProperty> properties_;
}
