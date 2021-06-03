package com.nseindia.mc.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(
  name = "TBL_MTR_DAILY_JOB_STATUS",
  uniqueConstraints= @UniqueConstraint(columnNames={"REPORTING_DATE"}))
public class MTRDailyJobStatus extends IdentifiableEntity {

  @Column(name = "REPORTING_DATE")
  private LocalDate reportingDate;

  @Column(name = "LAST_RUN_START")
  private LocalDateTime lastRunStart;

  @Column(name = "LAST_RUN_END")
  private LocalDateTime lastRunEnd;

  @Column(name = "RE_RUN_COUNTER")
  private Integer reRunCounter;

  @Column(name = "DAILY_FILE_PROCESS_STATUS")
  private String dailyFileProcessStatus;

  @Column(name = "MRG_TRADING_REPORT_STATUS")
  private String mrgTradingReportStatus;

  @Column(name = "NSE_WEBSITE_PUBLISH_STATUS")
  private String nseWebsitePublishStatus;

  @Column(name = "SEBI_FILE_TRANSFER_STATUS")
  private String sebiFileTransferStatus;

  @Column(name = "NSE_COMPLIANCE_NOTIFY_STATUS")
  private String nseComplianceNotifyStatus;

  @Column(name = "LAST_RUN_REMARK")
  private String lastRunRemark;
}
