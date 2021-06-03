package com.nseindia.mc.model;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "TBL_MTR_CONTROL_RECORD")
public class MTRControlRecord extends BaseRecord {

  @Column(name = "FILE_TYPE")
  private String fileType;

  @Column(name = "MEMBER_CODE")
  private String memberCode;

  @Column(name = "BATCH_DATE")
  private LocalDate batchDate;

  @Column(name = "BATCH_NUMBER")
  private Integer batchNumber;

  @Column(name = "TOTAL_SUMMARY_RECORDS")
  private Integer totalSummaryRecords;

  @Column(name = "TOTAL_DETAIL_RECORDS")
  private Integer totalDetailRecords;

  @Column(name = "TOTAL_AMOUNT_FUNDED")
  private Double totalAmountFunded;
}
