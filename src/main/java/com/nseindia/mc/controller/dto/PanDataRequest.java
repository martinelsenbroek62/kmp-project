package com.nseindia.mc.controller.dto;

import lombok.Builder;
import lombok.Data;

/**
 * The pan verification payload
 */
@Data
@Builder
public class PanDataRequest {

  /**
   * The pan payload
   */
  private PanPayload panPayload;
}
