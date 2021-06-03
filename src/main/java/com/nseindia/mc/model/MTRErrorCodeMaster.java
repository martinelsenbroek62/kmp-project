package com.nseindia.mc.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "TBL_MTR_ERROR_CODE_MASTER")
public class MTRErrorCodeMaster extends AuditableEntity {

  @Id
  @Column(name = "ERROR_CODE")
  private String errorCode;

  @Column(name = "ERROR_DESC")
  private String errorDesc;
}
