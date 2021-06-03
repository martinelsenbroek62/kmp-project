package com.nseindia.mc.controller.dto;

import lombok.Data;

@Data
public class ForEligibleMemberDto {
  private Boolean eligible;
  private Boolean expired;
  private String quarter;
  private EligibleSubmissionDto submission;
  private EligibleMemberInfoDto memberInfo;
}
