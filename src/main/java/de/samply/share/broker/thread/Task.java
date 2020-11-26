package de.samply.share.broker.thread;

import java.util.concurrent.Callable;

/**
 * Auto-Logging task to be queued in the Application class.
 */
public abstract class Task implements Callable<TaskResult> {

  /**
   * Executes this task.
   *
   * @return the task result
   * @throws Exception the exception
   */
  abstract TaskResult doIt() throws Exception;

  /* (non-Javadoc)
   * @see java.util.concurrent.Callable#call()
   */
  @Override
  public TaskResult call() throws Exception {
    return doIt();
  }

}
