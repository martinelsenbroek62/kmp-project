package com.nseindia.mc.controller.dto;

import lombok.Data;

@Data
public class PenaltyLetterDetailsDto {
  private Long memberId;
  private String memberCode;
  private String memberName;
  private Double penaltyAmount;
  private String penaltySubmissionCycle;
  private String penaltyLetterType;
  private String penaltyLetterStatus;
  private String penaltyLetterFileName;
  private String action;
}
