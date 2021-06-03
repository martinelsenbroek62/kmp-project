package com.nseindia.mc.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** This DTO is used for transfering the data of a KMP based on role */
@Data
@AllArgsConstructor
public class RoleBasedKMPDataDto {

  /** The KMP id */
  private Long kmpId;

  /** The KMP name */
  private String kmpName;

  /** The KMP pan number */
  private String pan;

  /** The KMP director identifier */
  private Long din;

  /** The KMP mmobile number */
  private String mobileNumber;

  /** The KMP phone number */
  private String phoneNumber;

  /** The KMP email */
  private String email;
}
