package com.nseindia.mc.model;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "TBL_MTR_MRG_TRADING_REPORT")
public class MTRMrgTradingReport extends IdentifiableEntity {

  @Column(name = "REPORTING_DATE")
  private LocalDate reportingDate;

  @Column(name = "MTR_COUNTER_TOTAL")
  private Integer mtrCounterTotal;

  @Column(name = "MTR_COUNTER_LATEST")
  private Integer mtrCounterLatest;

  @Column(name = "MTR_COUNTER_DATE")
  private LocalDate mtrCounterDate;

  @Column(name = "MRG_TRADING_PROVISIONAL_STATUS")
  private Boolean mrgTradingProvisionalStatus;

  @Column(name = "MRG_TRADING_FINAL_STATUS")
  private Boolean mrgTradingFinalStatus;

  @Column(name = "PROVISIONAL_REPORT_DMS_DOC_INDEX")
  private String provisionalReportDmsDocIndex;

  @Column(name = "FINAL_REPORT_DMS_DOC_INDEX")
  private String finalReportDmsDocIndex;

  @Column(name = "NOTIFY_DAILY_SIGNOFF_STATUS")
  private Boolean notifyDailySignoffStatus;

  @Column(name = "NOTIFY_MRG_TRADING_STATUS")
  private Boolean notifyMrgTradingStatus;
}
