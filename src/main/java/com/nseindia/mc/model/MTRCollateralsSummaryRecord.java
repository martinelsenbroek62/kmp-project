package com.nseindia.mc.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "TBL_MTR_COLLATERALS_SUMMARY_RECORD")
public class MTRCollateralsSummaryRecord extends BaseRecord {

  @Column(name = "COLLATERAL_CASH")
  private Double collateralCash;

  @Column(name = "COLLATERAL_CASH_EQUIVALENT")
  private Double collateralCashEquivalent;

  @Column(name = "COLLATERAL_SCRIPS")
  private Double collateralScrips;
}
