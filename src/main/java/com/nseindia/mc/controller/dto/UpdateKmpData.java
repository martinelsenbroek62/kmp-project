package com.nseindia.mc.controller.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class UpdateKmpData {

  /**
   * The KMP id to update
   */
  @NotNull private Long kmpId;

  /** The KMP role */
  private String role;

  /** The KMP additional designation */
  private String addlDesignation;

  /** The KMP mobile number */
  private String mobileNumber;

  /**
   * The KMP Phone number
   */
  private String phoneNumber;

  /** The KMP email */
  private String emailId;
}
