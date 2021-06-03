package com.nseindia.mc.controller.dto;

import lombok.Builder;
import lombok.Data;

/**
 * This DTO is the member information item
 */
@Data
@Builder
public class MemberInfoItem {

  /**
   * The member id
   */
  private long id;

  /**
   * The member name
   */
  private String name;

  /**
   * The member code
   */
  private String code;

  /**
   * The member constitute type
   */
  private String type;
}
