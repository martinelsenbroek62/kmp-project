package com.nseindia.mc.model;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "TBL_MTR_MEMBER_LIST")
public class MTRMemberList extends IdentifiableEntity {

  @ManyToOne
  @JoinColumn(name = "MEMBER_ID")
  private MemberMaster member;

  @Column(name = "ELIGIBLE_MEMBER_MTR_STATUS")
  private Boolean eligibleMemberMtrStatus;

  @Column(name = "ELIGIBLE_MEMBER_MTR_FROM")
  private LocalDate eligibleMemberMtrFrom;

  @Column(name = "MEMBER_STATUS")
  private String memberStatus;

  @Column(name = "WITHDRAWAL_REASON")
  private Boolean withdrawalReason;

  @Column(name = "COOLING_PERIOD_START_DATE")
  private LocalDate coolingPeriodStartDate;

  @Column(name = "COOLING_PERIOD_END_DATE")
  private LocalDate coolingPeriodEndDate;
}
