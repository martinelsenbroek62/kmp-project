package com.nseindia.mc.controller.dto;

/**
 * Represents the invalidation types of the PAN number
 */
public enum PanInvalidityType {

  // The invalidation type when the Pan number already exists for the current member
  ALREADY_EXISTS_FOR_CURRENT_MEMBER,

  // The invalidation type when the Pan number already exists for another member
  ALREADY_EXISTS_FOR_OTHER_MEMBER,

  // The invalidation type when the pan does not exist
  DOES_NOT_EXIST,

  // The pan does not match required format
  DOES_NOT_MATCH_REQUIRED_FORMAT,

  // Pan Validationf FAILED
  PAN_VALIDATION_FAILED,
  
  // The pan and name failed the name matching verification
  PAN_NAME_DO_NOT_MATCH,

  // The pan holder is already a KMP of the current member
  PAN_HOLDER_IS_ALREADY_MEMBER_KMP
}
