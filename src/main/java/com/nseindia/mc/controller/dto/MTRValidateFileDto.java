package com.nseindia.mc.controller.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class MTRValidateFileDto {
  private List<LocalDate> missedDates;
  private String successSubmissionExists;
  private String validMtrFile;
  private List<MTRValidateRecordSummary> recordSummary;
  private String responseFileURL;
  private String uploadedFileName;
  private Long fileValidationId;
  
  private List<MTRDailyFileErrorDto> dailyFileErrors;
}
