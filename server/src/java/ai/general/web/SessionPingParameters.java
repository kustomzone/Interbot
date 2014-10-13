/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

/**
 * Represents parameters transmitted to the client during a session ping.
 * The parameters are serialzed and transmitted as JSON.
 */
public class SessionPingParameters {

  /**
   * Constructs a default session ping parameters object.
   */
  public SessionPingParameters() {
    this.period_millis_ = 0;
  }

  /**
   * Constructs a session ping parameters object with the specified ping period.
   * The server commits to sending a ping at least once within this period.
   * The actual ping rate may be higher.
   *
   * @param period_millis The session ping period in milliseconds.
   */
  public SessionPingParameters(long period_millis) {
    this.period_millis_ = period_millis;
  }

  /**
   * The session ping period advertised by the server in milliseconds.
   * The server commits to sending at least one session ping within this period.
   * The actual ping rate may be higher.
   *
   * @return The session ping period in milliseconds.
   */
  public long getPeriodMillis() {
    return period_millis_;
  }

  /**
   * Sets the the session ping period.
   *
   * @param period_millis The session ping period in milliseconds.
   */
  public void setPeriod(long period_millis) {
    this.period_millis_ = period_millis;
  }
  
  private long period_millis_;
}
