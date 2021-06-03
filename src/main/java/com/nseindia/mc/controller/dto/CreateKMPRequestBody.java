package com.nseindia.mc.controller.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

/** The request body for creating KMP. It holds necessary fields for creating a single KMP */
@Data
public class CreateKMPRequestBody {
  /** The KMP Salutation */
  private String salutation;

  /** The KMP name */
  @NotBlank private String kmpName;

  /** The KMP date of declaration */
  @NotBlank private String dateOfDeclaration;

  /** The KMP Pan number */
  @NotBlank private String pan;

  /** The KMP role name */
  @NotBlank private String role;

  /** The KMP mobile number */
  @NotBlank private String mobileNumber;

  /** The KMP phone number */
  @NotBlank private String phoneNumber;

  /** The KMP email */
  @NotBlank private String emailId;

  /**
   * The flag indicating wether the KMP data exists in NSE
   */
  private Boolean fromNse;
}
