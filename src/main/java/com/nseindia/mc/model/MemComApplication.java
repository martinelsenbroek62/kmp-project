package com.nseindia.mc.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Table(name = "TBL_MEM_COM_APPLICATION_KMP")
public class MemComApplication {
  @Id
  @Column(name = "APP_ID")
  private String appId;

  @ManyToOne
  @JoinColumn(name = "MEM_ID")
  private MemberMaster member;

  @ManyToOne
  @JoinColumn(name = "MAKER_ID")
  private UserMemCom maker;

  @ManyToOne
  @JoinColumn(name = "CHECKER_ID")
  private UserMemCom checker;

  @Column(name = "APP_NO", unique = true)
  private String appNo;

  @Column(name = "APP_STATUS")
  private PenaltyApplicationStatus appStatus;

  @Column(name = "SUB_STATUS")
  private PenaltyApplicationSubStatus subStatus;

  @Column(name = "APP_START_DT")
  private LocalDateTime appStartDt;

  @Column(name = "APP_SUBMIT_DT")
  private LocalDateTime appSubmitDt;

  @Column(name = "APP_APPROVAL_DT")
  private LocalDateTime appApprovalDt;

  @Column(name = "MAKER_ASSIGNED_DT")
  private LocalDateTime makerAssignedDt;

  @Column(name = "MAIN_REQUEST")
  private String mainRequest;

  @Column(name = "SUB_REQUEST")
  private String subRequest;

  @Column(name = "PAGE_ID")
  private String pageId;

  @Column(name = "MEMBER_UNDERTAKING")
  private String memberUndertaking;

  @Column(name = "CID_PROCESSING_FEES")
  private String cidProcessingFees;

  @Column(name = "ASSIGNED_ID")
  private Long assignedId;

  @Column(name = "MEM_APPROVAL_DATE")
  private Date memApprovalDate;

  @Column(name = "ASSIGNED_TYPE")
  private String assignedType;

  @Column(name = "APP_COMPLETED_DATE")
  private LocalDateTime appCompletedDate;

  @Column(name = "AP_PROCESSING_FEES_BREAK_DOWN")
  private Byte[] apProcessingFeesBreakDown;

  @Column(name = "AP_DECLARATIONS")
  private Byte[] apDeclarations;

  @Column(name = "CONTACT_ID")
  private Long contactId;

  @Column(name = "WORKFLOW_ID")
  private Long workflowId;

  @Column(name = "CREATED_BY")
  private String createdBy;

  @Column(name = "CREATED_DT")
  private LocalDateTime createdDate;

  @Column(name = "UPDATED_BY")
  private String updatedBy;

  @Column(name = "UPDATED_DT")
  private LocalDateTime updatedDate;
}
