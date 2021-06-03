package com.nseindia.mc.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Positive;

@Data
@Entity
@Table(name = "TBL_PENALTY_MEMBER")
public class PenaltyMember extends IdentifiableEntity {

  @ManyToOne
  @JoinColumn(name = "PENALTY_REC_ID")
  private Penalty penalty;

  @ManyToOne
  @JoinColumn(name = "MEMBER_ID")
  private MemberMaster member;

  @Column(name = "REVIEW_REASON_TYPE")
  private PenaltyReviewReasonType reviewReasonType;

  @Column(name = "REASON_TYPE")
  private PenaltyReasonType reasonType;

  @Column(name = "NOTIFY_PENALTY_STATUS")
  private Boolean notifyPenaltyStatus;

  @Column(name = "NOTIFY_PENALTY_REVERSAL_STATUS")
  private Boolean notifyPenaltyReversalStatus;

  @Column(name = "NON_SUBMISSION_COUNT")
  @Positive
  private Integer nonSubmissionCount;

  @Column(name = "ORIGINAL_PENALTY_AMT")
  private Double originalPenaltyAmt;

  @Column(name = "NEW_PENALTY_AMT")
  private Double newPenaltyAmt;

  @Column(name = "REVIEW_STATUS")
  private Boolean reviewStatus;

  @Column(name = "REVISED_AMT")
  private Double revisedAmt;

  @Column(name = "REMARK")
  private String remark;

  @Column(name = "AGENDA_MINUTES_FILE_NAME")
  private String agendaMinutesFileName;

  @Column(name = "AGENDA_MINUTES_DMS_INDEX")
  private String agendaMinutesDmsIndex;

  @Column(name = "GENERATE_LETTER_STATUS")
  private PenaltyLetterStatus generateLetterStatus;

  @Column(name = "GENERATE_REVERSAL_LETTER_STATUS")
  private PenaltyLetterStatus generateReversalLetterStatus;

  @Column(name = "PENALTY_LETTER_FILE_NAME")
  private String penaltyLetterFileName;

  @Column(name = "PENALTY_REVERSAL_LETTER_FILE_NAME")
  private String penaltyReversalLetterFileName;

  @Column(name = "PENALTY_LETTER_DMS_INDEX")
  private String penaltyLetterDmsIndex;

  @Column(name = "PENALTY_REVERSAL_LETTER_DMS_INDEX")
  private String penaltyReversalLetterDmsIndex;
}
