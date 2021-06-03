package com.nseindia.mc.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "TBL_PENALTY_DOC_MASTER")
public class PenaltyDocMaster extends IdentifiableEntity {

  @ManyToOne
  @JoinColumn(name = "PENALTY_ID")
  private Penalty penalty;

  @Column(name = "PENALTY_DOC_INDEX")
  private String penaltyDocIndex;

  @Column(name = "PENALTY_DOC_TYPE_NAME")
  private String penaltyDocTypeName;
}
