package com.nseindia.mc.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "TBL_MTR_HO_MAKER")
public class MTRHoMaker extends IdentifiableEntity {

  @ManyToOne
  @JoinColumn(name = "HO_MAKER_ID")
  private UserMemCom maker;

  @Column(name = "HO_MAKER_STATUS")
  private Boolean makerStatus;

  @Column(name = "WORK_FLOW_ID")
  private Integer workFlowId;

  @Column(name = "ACTIVE_ON")
  private LocalDateTime activeOn;

  @Column(name = "DEACTIVE_ON")
  private LocalDateTime deactiveOn;
}
