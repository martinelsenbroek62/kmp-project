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

@Data
@Entity
@Table(name = "TBL_MTR_SUMMARY_RECORD")
public class MTRSummaryRecord {

  /** The id. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "MTR_FILE_ID")
  private MTRDailyFile mtrFile;

  @Column(name = "RECORD_TYPE")
  private Integer recordType;

  @Column(name = "ROW_STATUS")
  private Boolean rowStatus;

  @Column(name = "ERROR_CODES")
  private String errorCodes;

  @Column(name = "SUBMISSION_DATE")
  private LocalDate submissionDate;

  @Column(name = "CREATED_BY")
  private String createdBy;

  @Column(name = "CREATED_DT")
  private LocalDateTime createdDate;

  @Column(name = "UPDATED_BY")
  private String updatedBy;

  @Column(name = "UPDATED_DT")
  private LocalDateTime updatedDate;

  @Column(name = "LENDER_NAME")
  private String lenderName;

  @Column(name = "LENDER_CATEGORY")
  private Integer lenderCategory;

  @Column(name = "AMOUNT_FUNDED")
  private Double amountFunded;
}
