package com.nseindia.mc.controller.dto;

import lombok.Builder;
import lombok.Data;

/**
 * The name matching response
 */
@Data
@Builder
public class NameMatchingResponse {

  /**
   * The name matching score percentage
   */
  private double score;
}
