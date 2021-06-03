package com.nseindia.mc.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "TBL_MTR_ERROR_DAILY_FILE")
public class MTRErrorDailyFile extends IdentifiableEntity {
  @Column(name = "MTR_FILE_ID")
  private Long mtrFileId;

  @Column(name = "ERROR_CODE")
  private String errorCode;
}
