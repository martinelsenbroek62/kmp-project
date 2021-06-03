package com.nseindia.mc.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReviewCommentsDto {
  private Long makerId;
  private Long checkerId;
  private String makerFirstName;
  private String makerLastName;
  private String checkerFirstName;
  private String checkerLastName;
  private String reviewStatus;
  private String userType;
  private String statusAnnexureFile;
  private String statusApprovalNote;
  private List<ReviewCommentMessage> makerCommentsAnnexureFile;
  private List<ReviewCommentMessage> makerCommentsApprovalNote;
  private List<ReviewCommentMessage> checkerCommentsAnnexureFile;
  private List<ReviewCommentMessage> checkerCommentsApprovalNote;
  private String annexureFile;
  private String approvalNote;
  private String inspectionFileName;
  private Long penaltyRecId;
}
