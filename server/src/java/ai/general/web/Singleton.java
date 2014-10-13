/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import java.util.HashMap;

/**
 * Implements the Singleton pattern.
 *
 * Singleton manages all singleton classes. Singleton ensures that each singleton class is
 * instantiated only once. Singleton is thread-safe.
 *
 * An instance of a singleton class can be obtained via the {@link #get(Class)} method.
 * If an instance of type class does not already exist, Singleton creates an instance of that
 * object.
 *
 * All singleton classes must define a public constructor that does not take any arguments.
 */
public class Singleton {

  /**
   * Singleton cannot be directly instantiated.
   */
  private Singleton() {
    singletons_ = new HashMap<Object, Object>();
  }

  /**
   * Returns the Singleton instance that manages all singletons.
   *
   * @return The Singleton instance.
   */
  private static Singleton getInstance() {
    if (singleton_instance == null) {
      synchronized (Singleton.class) {
        if (singleton_instance == null) {
          Singleton new_singleton = new Singleton();
          singleton_instance = new_singleton;
        }
      }
    }
    return singleton_instance;
  }

  /**
   * Returns a singleton instance of the specified class.
   * If an instance of the class does not already exist, creates a new instance of the class.
   * The class type must not be abstract and must define a public constructor that takes no
   * arguments.
   *
   * If the class cannot be instantiated, throws an IllegalArgumentException with the exception
   * encountered while instantiating the class as a nested exception.
   *
   * @return The singleton instance of the specified class.
   * @throws IllegalArgumentException If the class cannot be instantiated with nested reason.
   */
  @SuppressWarnings("unchecked")
  public static <T> T get(Class<T> type) {
    Singleton singleton = Singleton.getInstance();
    Object instance = singleton.singletons_.get(type);
    if (instance == null) {
      synchronized (type) {
        instance = singleton.singletons_.get(type);
        if (instance == null) {
          try {
            instance = type.newInstance();
          } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
          }
          singleton.singletons_.put(type, instance);
        }
      }
    }
    return (T) instance;
  }

  private static Singleton singleton_instance = null;

  private HashMap<Object, Object> singletons_;
}
