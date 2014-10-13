/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import java.util.Collection;
import java.util.HashMap;

/**
 * Represents a type of client endpoint which a user can log in. Users can log in via different
 * kinds of clients, including browsers or command line tools. Each type of client provides the
 * user with a unique set of capabilities. The capabilities of a user depend on the clients
 * with which a user is logged in.
 */
public class ClientType {

  /**
   * Constructs a client type with the specified name.
   *
   * @param name The name of the client type.
   */
  public ClientType(String name) {
    this.name_ = name;
    this.capabilities_ = new HashMap<String, Capability>();
  }

  /**
   * Returns the name of the client type.
   *
   * @return The name of the client type.
   */
  public String getName() {
    return name_;
  }

  /**
   * Adds a capability to this client type. A capability added to a client type effects all client
   * sessions logged in via this client type.
   *
   * @param capability The capability to add.
   */
  public void addCapability(Capability capability) {
    capabilities_.put(capability.getName(), capability);
  }

  /**
   * Returns true if this client type grants a user the specified capability.
   *
   * @param name The name of the capability.
   * @return True if this client type grants a user the specified capability.
   */
  public boolean hasCapability(String name) {
    return capabilities_.containsKey(name);
  }

  /**
   * Returns the capability with the specified name associated with this client type or null if
   * this client type does not have the specified capability.
   *
   * @param name The name of the capability.
   * @return The capability or null.
   */
  public Capability getCapability(String name) {
    return capabilities_.get(name);
  }

  /**
   * Returns a collection of all capabilities associated with this client type.
   *
   * @return All capabilities associated with this client type.
   */
  public Collection<Capability> getCapabilities() {
    return capabilities_.values();
  }

  private String name_;
  private HashMap<String, Capability> capabilities_;
}
