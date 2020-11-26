package de.samply.share.broker.thread;

/**
 * An instance of this class needs to be returned by any Task to be executed by
 * ApplicationController. Extend this class to include custom return values.
 */
public class TaskResult {

  /**
   * The error code.
   */
  private int errorCode;

  /**
   * The message to be logged.
   */
  private String messageToBeLogged;

  /**
   * Instantiates a new task result.
   *
   * @param errorCode         the error code
   * @param messageToBeLogged the message to be logged
   */
  public TaskResult(int errorCode, String messageToBeLogged) {
    this.errorCode = errorCode;
    this.messageToBeLogged = messageToBeLogged;
  }

  /**
   * Gets the error code.
   *
   * @return the error code
   */
  public int getErrorCode() {
    return errorCode;
  }

  /**
   * Gets the message to be logged.
   *
   * @return the message to be logged
   */
  public String getMessageToBeLogged() {
    return messageToBeLogged;
  }
}

