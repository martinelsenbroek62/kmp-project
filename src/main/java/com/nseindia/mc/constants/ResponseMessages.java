package com.nseindia.mc.constants;

/**
 * The responses messages constants
 */
public final class ResponseMessages {

  /**
   * The error message to send when the entered name and pan failed name matching
   */
  public static final String PAN_NAME_DO_NOT_MATCH = "The provided pan and name do not match";

  /**
   * The error message to send to the user if an error ocurs when validating the PAN using external service
   */
  public static final String PAN_VALIDATION_FAILED = "Please enter valid PAN number";

  /**
   * The error message sent when some external service is unavailable
   */
  public static final String SERVICE_UNAVAILABLE = "Service unavailable, please try again later";


  /** The message to return when the PAN is valid */
  public static final String PAN_IS_VALID = "The provided pan exists and is valid";

  /**
   * The message to return when a member tries to assign their existing KMP as a KMP
   */
  public static final String PERSON_IS_ALREADY_OWN_KMP = "The person you are trying to assign as KMP is already your KMP";

  public static final String IN_PROGRESS_COMPLIANCE_OFFICER_ALREADY_EXISTS = "You seem to have an application in-progress for changing Compliance officer. Please close that application first in order to assign him as KMP or contact the helpdesk for further assistance.";

  /**
   * private empty constructor
   */
  private ResponseMessages() {
    // empty
  }
}
