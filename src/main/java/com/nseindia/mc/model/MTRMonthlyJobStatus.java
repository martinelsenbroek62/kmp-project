package com.nseindia.mc.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Data
@Entity
@Table(
  name = "TBL_MTR_MONTHLY_JOB_STATUS",
  uniqueConstraints= @UniqueConstraint(columnNames={"JOB_YEAR", "JOB_MONTH"}))
public class MTRMonthlyJobStatus extends IdentifiableEntity {

  @Column(name = "JOB_YEAR")
  private Integer jobYear;

  @Column(name = "JOB_MONTH")
  private Integer jobMonth;

  @Column(name = "LAST_RUN_START")
  private LocalDateTime lastRunStart;

  @Column(name = "LAST_RUN_END")
  private LocalDateTime lastRunEnd;

  @Column(name = "RE_RUN_COUNTER")
  private Integer reRunCounter;

  @Column(name = "JOB_TYPE")
  private String jobType;

  @Column(name = "LAST_RUN_STATUS")
  private String lastRunStatus;

  @Column(name = "LAST_RUN_REMARK")
  private String lastRunRemark;
}
