package com.nseindia.mc.controller.dto;

import lombok.Builder;
import lombok.Data;

/**
 * The pan payload
 */
@Data
@Builder
public class PanPayload {

  /**
   * The pan number
   */
  private String panNumber;

  /**
   * The customer id
   */
  private String customerId;
  
}
