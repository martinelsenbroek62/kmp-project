package com.nseindia.mc.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;

/** The director entity */
@Entity
@Table(name = "TBL_DIRECTOR_KMP")
@Data
public class Director extends AuditableEntity {

  @Id
  @Column(name = "ID")
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private long id;
  
  @Column(name = "DIRECTORID")
  private long directorId;

  /** The director title */
  @Column(name = "DIRECTOR_TITLE")
  private String title;

  /** The director name */
  @Column(name = "NAME")
  private String name;

  /** The director pan number */
  @Column(name = "PAN_NUMBER")
  private String pan;

  /** The director mobile number */
  @Column(name = "MOBILE_NUMBER")
  private String mobileNumber;

  /** The director phone number */
  @Column(name = "PHONE_NUMBER")
  private String phoneNumber;

  /** The director email */
  @Column(name = "EMAIL_ID")
  private String email;

  /** The director date of birth */
  @Column(name = "DOB")
  private LocalDate dob;

  /** The total years of experience of the director */
  @Column(name = "TOTAL_EXPERIENCE")
  private Long totalExperience;

  /** The related member entity */
  @ManyToOne
  @JoinColumn(name = "MEM_ID")
  private MemberMaster member;

  /** The director identifier */
  @Column(name = "DIN")
  private Long din;

  /** The director type */
  @Column(name = "DIRECTOR_TYPE")
  private String type;

  /** The director e-signature */
  @Column(name = "E_SIGNATURE")
  private String eSignature;

  /** The director status */
  @Column(name = "DIR_STATUS")
  private String status;

  /** The director post facto */
  @Column(name = "POST_FACTO")
  private String postFacto;

  /** The director nationality */
  @Column(name = "NATIONALITY")
  private String nationality;

  /** The director education qualification */
  @Column(name = "EDUCATION_QUALIFICATION")
  private String educationQualification;

  /** The director start date */
  @Column(name = "START_DT")
  private LocalDate startDate;

  /** The director end date */
  @Column(name = "END_DT")
  private LocalDate endDate;

  /** The pan validated of the director */
  @Column(name = "PAN_VALIDATED")
  private String panValidated;

  /** The director pan status */
  @Column(name = "PAN_STATUS")
  private String panStatus;

  /** The director joining date */
  @Column(name = "JOINING_DT")
  private LocalDate joiningDate;

  /** The director resigning date */
  @Column(name = "RESIGNING_DT")
  private LocalDate resigningDate;

  /** The director address */
  @Column(name = "ADDRESS_ID")
  private String address;

  /** The director prior approval date */
  @Column(name = "PRIOR_APPROVAL_DT")
  private LocalDateTime priorApprovalDate;

  /** The director extension date. */
  @Column(name = "EXTENSION_DT")
  private LocalDateTime extensionDate;

  /** The director dir12 value */
  @Column(name = "DIR_12")
  private String dir12;

  /** The director rejection date */
  @Column(name = "REJECTED_DT")
  private LocalDate rejectedDate;
}
