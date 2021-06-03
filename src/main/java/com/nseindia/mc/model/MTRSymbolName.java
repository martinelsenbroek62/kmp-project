package com.nseindia.mc.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "TBL_MTR_SYMBOL_NAME")
public class MTRSymbolName extends AuditableEntity {

  @Id
  @Column(name = "SYMBOL_CODE")
  private String symbolCode;

  @Column(name = "SYMBOL_NAME")
  private String symbolName;
}
