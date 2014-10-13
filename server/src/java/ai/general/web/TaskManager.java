/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import java.util.ArrayList;
import java.util.concurrent.DelayQueue;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Manages tasks that need to be executed by the server at a future time.
 * {@link Task} objects can be scheduled with the TaskManager to be run in the future.
 *
 * TaskManager is a singleton object and thread-safe. The TaskManager runs as daemon thread.
 * It is must be explicitly started when the application loads and should be halted when the
 * application unloads.
 */
public class TaskManager extends Thread {

  private static final int kSleepTimeMillis = 10000;

  /**
   * TaskManager is singleton. Use {@link #getInstance()} to create an instance.
   */
  public TaskManager() {
    setName("task-manager");
    setDaemon(true);
    this.tasks_ = new DelayQueue<Task>();
    this.run_ = false;
  }

  /**
   * Returns the singleton TaskManager instance.
   *
   * @return The singleton TaskManager instance.
   */
  public static TaskManager getInstance() {
    return Singleton.get(TaskManager.class);
  }

  /**
   * Schedules the specified task to be executed. The task will be executed anytime when it
   * becomes due, which may be a minute after its due time.
   */
  public void schedule(Task task) {
    tasks_.put(task);
    log.trace("Scheduled task.");
  }

  /**
   * Causes the TaskManager to exit. Once halted, the TaskManager cannot run anymore and will
   * not execute any remaining task.
   *
   * This method immediately returns without waiting for the TaskManager to exit.
   */
  public void halt() {
    run_ = false;
  }

  /**
   * Main method of the TaskManager thread.
   */
  @Override
  public void run() {
    run_ = true;
    ArrayList<Task> current_tasks = new ArrayList<Task>();
    while (run_) {
      tasks_.drainTo(current_tasks);
      log.trace("Running {} tasks.", current_tasks.size());
      for (Task task : current_tasks) {
        task.run();
      }
      current_tasks.clear();
      try {
        sleep(kSleepTimeMillis);
      } catch (InterruptedException e) {}
    }
  }

  private static Logger log = LogManager.getLogger();

  private DelayQueue<Task> tasks_;
  private boolean run_;
}
