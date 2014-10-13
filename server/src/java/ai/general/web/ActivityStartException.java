/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import ai.general.net.RpcException;

/**
 * Thrown during an activity start request when an activity cannot be started.
 */
public class ActivityStartException extends RpcException {

  private static final long serialVersionUID = 1;

  /**
   * ActivityStartException reasons
   */
  public enum Reason {
    /** The specified session is not a session of the user. */
    InvalidSession,

    /** The specified activity definition does not exist. */
    NoSuchActivity,

    /** The specified role is not a role of the specified acitivity definition. */
    NoSuchRole,

    /** The specified role is a passive role and cannot be used to initiated the activity. */
    PassiveRole,
  }

  /**
   * Creates an ActivityStartException for the specified reason.
   *
   * @param reason The reason of the exception.
   */
  public ActivityStartException(Reason reason) {
    super(reason.name(), null);
  }
}
