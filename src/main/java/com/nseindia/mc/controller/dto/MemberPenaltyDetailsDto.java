package com.nseindia.mc.controller.dto;

import lombok.Data;

@Data
public class MemberPenaltyDetailsDto {
  private Long memberId;
  private String memberCode;
  private String memberName;
  private Double originalPenalty;
  private Integer nonSubmissionCount;
  private Double newPenalty;
  private Boolean reviewStatus;
  private String reviewReason;
  private Double revisedAmount;
  private String remark;
  private String agendaMinutes;
  private boolean reversalPenaltyLetterSent;
  private Long penaltyMemberId;
}
