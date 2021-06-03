package com.nseindia.mc.controller.dto;

import lombok.Data;

/**
 * scores data class for NameMatchingResponse
 */

@Data
public class ScoreResponse {

  /**
   * The score
   */
  private String score;

  /**
   * The scoring algorithm
   */
  private String algorithm;
}
