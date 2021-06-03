package com.nseindia.mc.controller.dto;

import java.util.List;

import lombok.Data;

/**
 * Represents the response from the external name matching API
 */
@Data
public class NameMatchingApiResponse {

  /**
   * The response hash
   */
  private String responseHash;

  /**
   * The response salt value
   */
  private String salt;

  /**
   * The response source
   */
  private String source;

  /**
   * The name matching response target
   */
  private String target;

  /**
   * The name matching scores
   */
  private List<ScoreResponse> scores;

  /**
   * The customer id
   */
  private String customerId;
}