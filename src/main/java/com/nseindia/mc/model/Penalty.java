package com.nseindia.mc.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "TBL_PENALTY")
public class Penalty extends IdentifiableEntity {

  @Column(name = "PENALTY_YEAR")
  private Integer penaltyYear;

  @Column(name = "PENALTY_MONTH")
  private Integer penaltyMonth;

  @Column(name = "APPROVAL_FILE_NAME")
  private String approvalFileName;

  @Column(name = "ANNEXURE_FILE_NAME")
  private String annexureFileName;

  @Column(name = "INSPECTION_FILE_NAME")
  private String inspectionFileName;

  @Column(name = "APPROVAL_NOTE_DMS_INDEX")
  private String approvalNoteDmsIndex;

  @Column(name = "ANNEXURE_FILE_DMS_INDEX")
  private String annexureFileDmsIndex;

  @Column(name = "INSPECTION_FILE_DMS_INDEX")
  private String inspectionFileDmsIndex;

  @ManyToOne
  @JoinColumn(name = "HO_MAKER_ID")
  private UserMemCom maker;

  @JoinColumn(name = "NOTIFY_HO_MAKER_STATUS")
  private Boolean notifyMakerStatus;

  @ManyToOne
  @JoinColumn(name = "CHECKER_ID")
  private UserMemCom checker;

  @Column(name = "PENALTY_STATUS")
  private PenaltyStatus penaltyStatus;

  @ManyToOne
  @JoinColumn(name = "APP_ID")
  private MemComApplication memComApplication;

  /** The penalty type */
  @Column(name = "PENALTY_TYPE")
  @Enumerated(EnumType.STRING)
  private PenaltyType penaltyType;
}
