package com.nseindia.mc.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Builder;
import lombok.Data;

/**
 * The response from the pan validation API
 */
@Data
@Builder
@JsonInclude(Include.NON_NULL)
public class PanValidationResponse {
  /**
   * The pan validation status
   */
  private PanValidationStatus panStatus;

  /**
   * The pan invalidation type, it is only relevant when status = INVALID
   */
  private PanInvalidityType invalidityType;

  /**
   * The name of the pan holder
   */
  private String panHolderName;

  /**
   * The pan validation response message
   */
  private String message;

  /**
   * The pan number of the user
   */
  private String pan;

  /**
   * The user's role
   */
  private String role;
}
