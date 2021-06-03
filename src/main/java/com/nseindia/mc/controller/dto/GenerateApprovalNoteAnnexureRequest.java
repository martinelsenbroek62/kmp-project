package com.nseindia.mc.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class GenerateApprovalNoteAnnexureRequest {
  public enum Option {
    ONLY_APPROVAL_NOTE,
    ONLY_ANNEXURE
  };

  int submissionYear;
  int submissionMonth;
  List<MemberPenaltyDetailsDto> memberPenaltyDetails;

  /** The option. null to generate both. */
  private Option option;
}
