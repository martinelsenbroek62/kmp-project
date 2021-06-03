package com.nseindia.mc.model;

/** This enum lists the KMP actions: Addition, Deletion, Edited */
public enum KMPAction {
  // The Addition action
  ADDITION("Addition"),

  // The Deletion action
  DELETION("Deletion"),

  // The Edited action
  EDITED("Edited");

  /** The string value of the action */
  public final String value;

  /**
   * Sets the String value of the KMP action
   *
   * @param value The string value to set
   */
  private KMPAction(String value) {
    this.value = value;
  }
}
