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
@Table(name = "TBL_MTR_SEBI_DAILY_STATUS")
public class MTRSebiDailyStatus extends IdentifiableEntity {

  @ManyToOne
  @JoinColumn(name = "MEMBER_ID")
  private MemberMaster member;

  @Column(name = "REPORTING_DATE")
  private LocalDateTime reportingDate;

  @Column(name = "DAILY_FILE_STATUS")
  private Boolean dailyFileStatus;

  @Column(name = "SEBI_FILE_STATUS")
  private Boolean sebiFileStatus;

  @Column(name = "NIL_SUBMISSION_STATUS")
  private Boolean nilSubmissionStatus;

  @Column(name = "NOTIFY_DAILY_FILE_STATUS")
  private Boolean notifyDailyFileStatus;

  @Column(name = "NOTIFY_NIL_STATUS")
  private Boolean notifyNilStatus;
}
