/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * A Task represents some work that can be scheduled with the {@link TaskManager} to be run
 * at a future time.
 *
 * Specific Tasks must be implemented by subclassing this class.
 */
public abstract class Task implements Delayed {

  /**
   * Creates a task to be executed after the specified time. The task will not be executed
   * before the delay, but may be executed anytime after the delay.
   *
   * When a task is due, it will be executed at a suitable time by the TaskManager. The task may
   * not immediately execute when it is due. It is possible that it may take a minute or more
   * after the task is due when it is actually executed.
   *
   * @param delay_millis The minimum time to wait before this task can be executed.
   */
  protected Task(long delay_millis) {
    this.due_time_millis_ = System.currentTimeMillis() + delay_millis;
  }

  /**
   * Compares this task with the specified delayed object.
   * Returns the difference between the remaining delay of the other object and this task.
   * The returned value is positive if this task is due after the other object and negative if it
   * is due before the other object.
   *
   * @param other The other delayed object to compare with this task.
   * @return The difference in remaining delays in milliseconds.
   */
  @Override
  public int compareTo(Delayed other) {
    if (other instanceof Task) {
      return (int) (due_time_millis_ - ((Task) other).due_time_millis_);
    } else {
      return (int) (getDelay(TimeUnit.MILLISECONDS) - other.getDelay(TimeUnit.MILLISECONDS));
    }
  }

  /**
   * Returns the remaining delay of the task. A negative value indicates that the task is due.
   *
   * @param time_unit The time unit of the return value.
   * @return The remaining delay time in the specified time unit.
   */
  @Override
  public long getDelay(TimeUnit time_unit) {
    return time_unit.convert(due_time_millis_ - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
  }

  /**
   * Runs the task. This is method is called by the {@link TaskManager} when the task is due
   * to run. After the task has been run it is discarded.
   */
  public abstract void run();

  private long due_time_millis_;
}
