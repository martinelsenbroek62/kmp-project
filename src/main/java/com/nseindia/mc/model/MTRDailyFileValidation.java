package com.nseindia.mc.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "TBL_MTR_DAILY_FILE_VALIDATION")
public class MTRDailyFileValidation extends IdentifiableEntity {
  
  @ManyToOne
  @JoinColumn(name = "MEMBER_ID", referencedColumnName = "MEM_ID")
  private MemberMaster member;

  @Column(name = "REPORTING_DATE")
  private LocalDateTime reportingDate;

  @Column(name = "BATCH_NO")
  private Integer batchNo;

  @Column(name = "DAILY_FILE_NAME")
  private String dailyFileName;

  @Column(name = "RESPONSE_FILE_NAME")
  private String responseFileName;

  @Column(name = "DMS_DOC_INDEX")
  private String dmsDocIndex;

  @Column(name = "DMS_RES_INDEX")
  private String dmsResIndex;

  @Column(name = "DAILY_FILE_STATUS")
  private Boolean dailyFileStatus;

  @Column(name = "DAILY_FILE_SUBMISSION_DATE")
  private LocalDateTime dailyFileSubmissionDate;

  @Column(name = "ERROR_CODE_FILE_NAME")
  private String errorCodeFileName;

  @Column(name = "ERROR_CODE_FILE_DMS_INDEX")
  private String errorCodeFileDmsIndex;
}
