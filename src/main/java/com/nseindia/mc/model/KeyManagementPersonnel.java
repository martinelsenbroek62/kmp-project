package com.nseindia.mc.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import lombok.Data;

/** The KMP entity */
@Entity
@Table(name = "TBL_KMP")
@Data
public class KeyManagementPersonnel extends IdentifiableEntity {

  /** The KMP member id */
  @Column(name = "MEM_ID")
  private Long memId;

  /** The KMP application id */
  @Column(name = "APP_ID")
  private Long appId;

  /** The KMP role name */
  @Column(name = "ROLE")
  private String role;

  /** The KMP additional designation */
  @Column(name = "ADDL_DESIGNATION")
  private String additionalDesignation;

  /** The KMP salutation */
  @Column(name = "SALUTATION")
  private String salutation;

  /** The KMP name */
  @Column(name = "NAME")
  private String name;

  /** The KMP Pan number */
  @Column(name = "PAN")
  private String pan;

  /** The KMP pan status */
  @Column(name = "PAN_STATUS")
  private String panStatus;

  /** The KMP director identifier */
  @Column(name = "DIN")
  private Long din;

  /** The KMP email address */
  @Column(name = "EMAIL_ID")
  private String email;

  /** The KMP mobile number */
  @Column(name = "MOBILE_NO")
  private String mobileNumber;

  /** The KMP phone number */
  @Column(name = "PHONE_NUMBER")
  private String phoneNumber;

  /** The KMP declaration date */
  @Column(name = "DECLARATION_DT")
  private LocalDateTime declarationDate;

  /** The KMP submission date */
  @Column(name = "SUBMISSION_DT")
  private LocalDateTime submissionDate;

  /** The KMP constitute nm */
  @Column(name = "CONSTITUTE_NM")
  private String constituteNM;

  /** The action (Addition, Edited or Deletion) */
  @Column(name = "ACTION")
  @Enumerated(EnumType.STRING)
  private KMPAction action;

  /** The action date */
  @Column(name = "ACTION_DT")
  private LocalDateTime actionDate;

  /**
   * The flag indicating whether the data exists in NSE
   */
  @Column(name = "FROM_NSE")
  private Boolean fromNse;

  /** The identifier of the user who deleted the KMP */
  @Column(name = "DELETED_BY")
  private String deletedBy;

  /** The KMP deletion date */
  @Column(name = "DELETED_DT")
  private LocalDateTime deletedDate;

  /**
   * The pan document file name
   */
  @Column(name = "PAN_DOC_FILE_NAME")
  private String panDocFileName;

  /**
   * The pan document dms index
   */
  @Column(name = "PAN_DOC_DMS_INDEX")
  private String panDocDmsIndex;
}
