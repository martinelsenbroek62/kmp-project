package com.nseindia.mc.model;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
public abstract class BaseRecord extends IdentifiableEntity {

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
}
