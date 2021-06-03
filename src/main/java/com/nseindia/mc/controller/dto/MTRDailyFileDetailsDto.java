package com.nseindia.mc.controller.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MTRDailyFileDetailsDto {
  private Long memberId;
  private String memberName;
  private String memberCode;
  private LocalDateTime submissionDate;
  private String submittedFilename;
  private String responseFilename;
  private String fileSubmissionStatus;
  private String referenceNumber;
}
