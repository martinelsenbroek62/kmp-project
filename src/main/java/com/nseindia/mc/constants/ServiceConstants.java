package com.nseindia.mc.constants;

/**
 * This class contains all constants used by this service
 */
public final class ServiceConstants {
  /**
   * Private empty constructor to prevent instanciation
   */
  private ServiceConstants() {
    // private empty constructor
  }

  /**
   * The minimum confidence score for name matching
   */
  public static final double NAME_MATCHING_MINIMUM_SCORE = 80;

  /**
   * The PAN name constant
   */
  public static final String PAN = "PAN";
  public static final String OTP = "OTP";

  // Workflow content constants
  public static final String SUCCESS = "SUCCESS";

  /**
   * The constant customer id to use for validating pan with external verification service
   */
  public static final String PAN_VALIDATION_CUSTOMER_ID = "1";

  /**
   * The constant customer id to use when calling the external name matching API
   */
  public static final String NAME_MATCHING_CUSTOMER_ID = "Today";

  // The constitution types constants
  public static final String CORPORATE_CONSTITUTION = "Corporate";
  public static final String BANK_CONSTITUTION = "Bank";
  public static final String LLP_CONSTITUTION = "LLP";
  public static final String PARTNERSHIP_CONSTITUTION = "PartnershipFirm";
  public static final String INDIVIDUAL_CONSTITUTION = "Individual";

  /**
   * The Compliance Officer spaced name to use when formatting the message sent to the user
   */
  public static final String COMPLIANCE_OFFICER_SPACED_NAME = "Compliance Officer";

  /**
   * The error message to retrun when the external service for pan validation is unavailable
   */
  public static final String PAN_EXTERNAL_VALIDATION_ERROR_MESSAGE = "Please enter valid PAN number";

  /**
   * The error message to return when the external name matching service is unavailable
   */
  public static final String EXTERNAL_NAME_MATCHING_VERIFICATION_ERROR_MESSAGE = "Please try again";

  public static final int IN_PROGRESS_COMPLIANCE_OFFICER = 2;

  public static final String LIST_AUDIT_KMP_ACTION = "viewAuditTrail";
}
