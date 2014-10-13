/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import java.util.HashMap;

/**
 * The ClientManager manages the set of all {@link ClientType} instances.
 *
 * ClientManager is a singleton class.
 */
public class ClientManager {

  /**
   * ClientManager is singleton. Use {@link #getInstance()} to create an instance.
   */
  public ClientManager() {
    this.client_types_ = new HashMap<String, ClientType>();
    createClientTypes();
  }

  /**
   * Returns the singleton ClientManager instance.
   *
   * @return The singleton ClientManager instance.
   */
  public static ClientManager getInstance() {
    return Singleton.get(ClientManager.class);
  }

  /**
   * Adds a client type to the set of client types.
   * Each a client type must have a unique name.
   *
   * @param client_type The client type to add.
   */
  public void addClientType(ClientType client_type) {
    client_types_.put(client_type.getName(), client_type);
  }

  /**
   * Returns true if there is a client type with the specified name.
   *
   * @param name The name of the client type.
   * @return True if there is a client type with the specified name.
   */
  public boolean hasClientType(String name) {
    return client_types_.containsKey(name);
  }

  /**
   * Returns the client type with the specified name or null if no such client type exists.
   *
   * @param name The name of the client type.
   * @return The client type with the specified name or null.
   */
  public ClientType getClientType(String name) {
    return client_types_.get(name);
  }

  /**
   * Creates and adds built-in client types.
   */
  private void createClientTypes() {
    addClientType(WebClientType.getInstance());
    addClientType(InterbotClientType.getInstance());
  }

  private HashMap<String, ClientType> client_types_;
}
