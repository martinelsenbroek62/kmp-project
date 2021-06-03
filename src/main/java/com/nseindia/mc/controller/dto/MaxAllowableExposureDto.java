package com.nseindia.mc.controller.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MaxAllowableExposureDto {
  private String memberCode;
  private String memberName;
  private Double maxAllowableExposure;
  private LocalDateTime submissionDate;
  private Double netWorth;
  private Double totalBorrowedFunds;
  private Boolean limitExceeded;
}
