package com.nseindia.mc.model;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;

/** The shareholder entity */
@Entity
@Table(name = "TBL_SHAREHOLDER")
@Data
public class Shareholder extends IdentifiableEntity {

  /** The shareholder title */
  @Column(name = "SHAREHOLDER_TITLE")
  private String title;

  /** The shareholder name */
  @Column(name = "NAME")
  private String name;

  /** The shareholder pan number */
  @Column(name = "PAN_NUMBER")
  private String pan;

  /** The pan validated value  */
  @Column(name = "PAN_VALIDATED")
  private String panValidated;

  /** The pan status */
  @Column(name = "PAN_STATUS")
  private String panStatus;

  /** The proprietor status */
  @Column(name = "PROPRIETOR_STATUS")
  private String status;

  /** The shareholder mobile number */
  @Column(name = "MOBILE_NUMBER")
  private String mobileNumber;

   /** The din identifier */
   @Column(name = "DIN")
   private Long din;

   /** The shareholder type */
  @Column(name = "SHAREHOLDER_TYPE")
  private String type;

  /** The shareholder phone number */
  @Column(name = "PHONE_NUMBER")
  private String phoneNumber;

  /** The shareholder email */
  @Column(name = "EMAIL_ID")
  private String email;

  /** The shareholder date of birth */
  @Column(name = "DOB")
  private LocalDate dob;

  /** The shareholder member id */
  @Column(name = "MEM_ID")
  private Long memberId;

  /** The start date */
  @Column(name = "START_DT")
  private LocalDate startDate;

  /** The end date */
  @Column(name = "END_DT")
  private LocalDate endDate;
}
