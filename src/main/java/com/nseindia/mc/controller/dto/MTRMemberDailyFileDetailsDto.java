package com.nseindia.mc.controller.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MTRMemberDailyFileDetailsDto {

  private String referenceNumber;
  private LocalDateTime reportingDate;
  private LocalDateTime submissionDate;
  private String submittedFilename;
  private String responseFilename;
  private String nillSubmissionStatus;
  private boolean uploaded;
}
