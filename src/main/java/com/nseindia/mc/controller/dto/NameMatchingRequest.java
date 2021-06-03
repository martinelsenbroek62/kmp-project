package com.nseindia.mc.controller.dto;

import lombok.Builder;
import lombok.Data;

/**
 * NameMatchingRequest class to hit NameMatching API.
 */

@Data
@Builder
public class NameMatchingRequest {

  /**
   * The name matching request payload
   */
  private NameMatchingPayload nameMatchingPayload;
}