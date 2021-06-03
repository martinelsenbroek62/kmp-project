package com.nseindia.mc.controller.dto;

import lombok.Data;

/**
 * Response class for NameMatching Api
 */

@Data
public class VerificationNameMatchingResponse {

  /**
   * The verification transaction id
   */
  private String verifyTranId;

  /**
   * The response status code
   */
  private String statusCode;

  /**
   * The response status message
   */
  private String statusMessage;

  /**
   * The name matching response
   */
  private NameMatchingApiResponse nameMatchingResponse;
}