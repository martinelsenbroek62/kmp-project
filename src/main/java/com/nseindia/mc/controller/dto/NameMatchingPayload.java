package com.nseindia.mc.controller.dto;

import lombok.Builder;
import lombok.Data;

/**
 * NameMatchingPayload class to hit NameMatching API.
 */
@Data
@Builder
public class NameMatchingPayload {

  // the request payload for namematching api is name1 and name2.
  /**
   * The name 1 to match
   */
  private String name1;

  /**
   * The name 2 to match
   */
  private String name2;

  /**
   * The customer id
   */
  private String customerId;
}