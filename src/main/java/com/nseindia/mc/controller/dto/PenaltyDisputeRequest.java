package com.nseindia.mc.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class PenaltyDisputeRequest {
  private int submissionYear;
  private int submissionMonth;
  private List<MemberPenaltyDetailsDto> memberPenaltyDetails;
}
