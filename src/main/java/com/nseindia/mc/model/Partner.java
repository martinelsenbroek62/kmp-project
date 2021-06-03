package com.nseindia.mc.model;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;

/** The partner entity */
@Entity
@Table(name = "TBL_PARTNER")
@Data
public class Partner extends IdentifiableEntity {

  /** The partner title */
  @Column(name = "PARTNER_TITLE")
  private String title;

  /** The partner name */
  @Column(name = "NAME")
  private String name;

  /** The partner pan number */
  @Column(name = "PAN_NUMBER")
  private String pan;

  /** The pan validated value  */
  @Column(name = "PAN_VALIDATED")
  private String panValidated;

  /** The pan status */
  @Column(name = "PAN_STATUS")
  private String panStatus;

  /** The partner status */
  @Column(name = "PARTNER_STATUS")
  private String status;

  /** The partner mobile number */
  @Column(name = "MOBILE_NUMBER")
  private String mobileNumber;

   /** The din identifier */
   @Column(name = "DIN")
   private Long din;

   /** The partner type */
  @Column(name = "PARTNER_TYPE")
  private String type;

  /** The partner phone number */
  @Column(name = "PHONE_NUMBER")
  private String phoneNumber;

  /** The partner email */
  @Column(name = "EMAIL_ID")
  private String email;

  /** The partner date of birth */
  @Column(name = "DOB")
  private LocalDate dob;

  /** The partner member id */
  @Column(name = "MEM_ID")
  private Long memberId;

  /** The start date */
  @Column(name = "START_DT")
  private LocalDate startDate;

  /** The end date */
  @Column(name = "END_DT")
  private LocalDate endDate;
}
