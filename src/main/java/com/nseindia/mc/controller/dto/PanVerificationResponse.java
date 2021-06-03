package com.nseindia.mc.controller.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Response class for Pan Verification API.
 */

@Data
@Builder
public class PanVerificationResponse {
  /**
   * The pan verification transaction id
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
   * The pan varification response data
   */
  private PanResponse panResponse;
}