package com.nseindia.mc.controller.dto;

import lombok.Data;

@Data
public class ReviewCommentsRequest {
  private Long checkerId;
  private String makerCommentsAnnexureFile;
  private String makerCommentsApprovalNote;
  private String checkerCommentsAnnexureFile;
  private String checkerCommentsApprovalNote;
  private Long penaltyRecId;
}
