package com.nseindia.mc.model;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;

/** The proprietor entity */
@Entity
@Table(name = "TBL_PROPRIETOR")
@Data
public class Proprietor extends IdentifiableEntity {

  /** The proprietor title */
  @Column(name = "PROPRIETOR_TITLE")
  private String title;

  /** The proprietor name */
  @Column(name = "NAME")
  private String name;

  /** The proprietor pan number */
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

  /** The proprietor mobile number */
  @Column(name = "MOBILE_NUMBER")
  private String mobileNumber;

   /** The din identifier */
   @Column(name = "DIN")
   private Long din;

   /** The proprietor type */
  @Column(name = "PROPRIETOR_TYPE")
  private String type;

  /** The proprietor phone number */
  @Column(name = "PHONE_NUMBER")
  private String phoneNumber;

  /** The proprietor email */
  @Column(name = "EMAIL_ID")
  private String email;

  /** The proprietor date of birth */
  @Column(name = "DOB")
  private LocalDate dob;

  /** The proprietor member id */
  @Column(name = "MEM_ID")
  private Long memberId;

  /** The start date */
  @Column(name = "START_DT")
  private LocalDate startDate;

  /** The end date */
  @Column(name = "END_DT")
  private LocalDate endDate;
}
