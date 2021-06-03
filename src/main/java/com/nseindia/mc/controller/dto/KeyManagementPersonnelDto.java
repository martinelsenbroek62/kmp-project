package com.nseindia.mc.controller.dto;

import com.nseindia.mc.model.KeyManagementPersonnel;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** This is a DTO for Key Management Personnel data */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyManagementPersonnelDto {

  /**
   * The KMP id
   */
  private Long kmpId;

  /** The latest action performed on the KMP. */
  private String action;

  /** The KMP action date */
  private LocalDateTime actionDate;

  /** The KMP application id */
  private Long appId;

  /** The KMP role */
  private String role;

  /** The KMP additional designation */
  private String addlDesignation;

  /** The KMP salutation */
  private String salutation;

  /** The KMP name */
  private String name;

  /** The KMP pan number */
  private String pan;

  /** The KMP pan status */
  private String panStatus;

  /** The KMP director identifier */
  private Long din;

  /** The KMP mobile number */
  private String mobileNumber;

  /** The KMP phone number */
  private String phoneNumber;

  /** The KMP email */
  private String email;

  /**
   * The flag indicating whether the KMP data exists in NSE
   */
  private Boolean fromNse;

  /** The KMP declaration date */
  private LocalDateTime declarationDate;

  /** The KMP submission date */
  private LocalDateTime submissionDate;

  /** The KMP last update date */
  private LocalDateTime lastUpdateDate;

  /**
   * The pan doc file name
   */
  private String panDocFileName;

  /**
   * This constructor creates a new KeyManagementPersonnelDto instance from KeyManagementPersonnel
   * model instance
   *
   * @param kmp The KeyManagementPersonnel instance from which to create KeyManagementPersonnelDto
   *     data
   */
  public KeyManagementPersonnelDto(KeyManagementPersonnel kmp) {
    kmpId = kmp.getId();
    action = kmp.getAction().value;
    actionDate = kmp.getActionDate();
    appId = kmp.getAppId();
    role = kmp.getRole();
    addlDesignation = kmp.getAdditionalDesignation();
    salutation = kmp.getSalutation();
    name = kmp.getName();
    pan = kmp.getPan();
    panStatus = kmp.getPanStatus();
    din = kmp.getDin();
    mobileNumber = kmp.getMobileNumber();
    phoneNumber = kmp.getPhoneNumber();
    email = kmp.getEmail();
    fromNse = kmp.getFromNse();
    declarationDate = kmp.getDeclarationDate();
    submissionDate = kmp.getSubmissionDate();
    lastUpdateDate = kmp.getUpdatedDate();
    panDocFileName = kmp.getPanDocFileName();
  }
}
