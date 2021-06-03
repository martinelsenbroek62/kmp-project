package com.nseindia.mc.model;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.nseindia.mc.constants.CompCertificationType;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import lombok.Data;

/** The Compliance officer entity */
@Entity
@Table(name = "TBL_COMPLIANCE_OFFICER_KMP")
@Data
public class ComplianceOfficer extends AuditableEntity{

  @Id
  @Column(name = "COMP_ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
 
  /** The related member entity */
  @ManyToOne
  @JoinColumn(name = "MEMBER_ID")
  private MemberMaster member;
  
  @Column(name = "COMP_NAME")
  private String name;
  
  @Column(name = "COMP_EMAIL_ID")
  private String email;
  
  @Column(name = "COMP_MOBILE_NUMBER")
  private String mobileNumber;
  
  @Column(name = "START_DT")
  private LocalDate startDate;
  
  @Column(name = "END_DT")
  private LocalDate endDate;
  
  @Column(name = "COMP_STATUS")
  private Integer compStatus;
  
  @Column(name = "COMP_PAN_NO")
  private String pan;
  
  @Column(name = "COMP_SALUTATION")
  private String compSalutation;
  
  @Column(name = "COMP_EMPLOYMENTDT")
  private LocalDate compEmploymentDt;
  
  @Column(name = "COMP_APPOINTMENTDT")
  private LocalDate compAppointmentDt;
  
  @Column(name = "COMP_QUALIFICATION")
  private String compQualification;
  
  @Column(name = "COMP_PHONENUMBER")
  private String phoneNumber;
  
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "COMP_ADDRESSID")
  @NotFound(action = NotFoundAction.IGNORE)
  private Address address;

  @Enumerated(EnumType.STRING)
  @Column(name = "COMP_CERTIFICATIONTYPE")
  private CompCertificationType compCertificationType;
  
  @Column(name = "COMP_CERTIFICATIONNAME")
  private String compCertificationName;
  
  @Column(name = "COMP_CERTIFICATION_PAN")
  private String compCertificationPan;
  
  @Column(name = "COMP_CERTIFICATION_VALIDTILDT")
  private LocalDate compCertificationValidTilDt;
  
  @Column(name = "COMP_CERTIFICATION_EXAMDT")
  private LocalDate compCertificationExamDt;
  
  @Column(name = "COMP_CERTIFICATION_EXAMTYPE")
  private String compCertificationExamType;
  
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "COMP_CERTIFICATIONDOCID")
  @NotFound(action = NotFoundAction.IGNORE)
  private DocumentUpload compCertificationDoc;
  
  @Column(name = "COMP_CERTIFICATION_VALIDATED")
  private String compCertificationValidated;
  
  @Column(name = "COMP_DECLAREDKMP")
  private String compDeclaredKmp;
  
  @Column(name = "COMP_PAN_VERIFIED")
  private String panValidated;

  /** The din identifier */
  @Column(name = "DIN")
  private Long din;

  /** The Compliance officer title */
  @Column(name = "COMPLIANCE_OFFICER_TITLE")
  private String title;

  /** The Compliance officer date of birth */
  @Column(name = "DOB")
  private LocalDate dob;

  /** The pan status */
  @Column(name = "PAN_STATUS")
  private String panStatus;
}
