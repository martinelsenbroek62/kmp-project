package com.nseindia.mc.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "TBL_PENALTY_REVIEW")
public class PenaltyReview extends IdentifiableEntity {

  @ManyToOne
  @JoinColumn(name = "PENALTY_REC_ID")
  private Penalty penalty;

  @Column(name = "REVIEW_STATUS")
  private PenaltyStatus reviewStatus;

  @Column(name = "USER_TYPE")
  private UserType userType;

  @Column(name = "STATUS_ANNEXURE_FILE")
  private String statusAnnexureFile;

  @Column(name = "STATUS_APPROVAL_NOTE")
  private String statusApprovalNote;

  @Column(name = "MAKER_COMMENTS_ANNEXURE_FILE")
  private String makerCommentsAnnexureFile;

  @Column(name = "MAKER_COMMENTS_NOTE")
  private String makerCommentsNote;

  @Column(name = "CHECKER_COMMENTS_ANNEXURE_FILE")
  private String checkerCommentsAnnexureFile;

  @Column(name = "CHECKER_COMMENTS_NOTE")
  private String checkerCommentsNote;
}
