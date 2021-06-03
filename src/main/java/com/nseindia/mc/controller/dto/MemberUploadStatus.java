package com.nseindia.mc.controller.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MemberUploadStatus {
  private String memberName;
  private String memberCode;
  private String memberType;
  private String approvedStatus;
  private String eligibilityFlag;
  private String memberStatus;

  @JsonProperty("eligibility_date")
  private LocalDate eligibilityDate;
  private LocalDate approvedDate;

  private LocalDateTime lastMTRSubmittedDate;
  private LocalDateTime lastMTRReportingDate;
  private boolean lastSubmissionIsNil;
  private double lastTotalAmountFunded;
  private List<LocalDate> missedDates;
}
