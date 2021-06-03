package com.nseindia.mc.controller.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class LenderWiseExposureDto {
  private String memberCode;
  private String memberName;
  private LocalDateTime submissionDate;
  private Long lenderCategory;
  private Double totalBorrowedFunds;
  private Double totalBorrowedFundsOnPreviousDay;
}
