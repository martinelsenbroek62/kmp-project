package com.nseindia.mc.controller.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MaxClientAllowableExposureDto {
  private String memberCode;
  private String memberName;
  private String clientName;
  private String clientPan;
  private Double exposureToClient;
  private LocalDateTime submissionDate;
  private Double netWorth;
  private Double totalBorrowedFunds;
  private Boolean limitExceeded;
}
