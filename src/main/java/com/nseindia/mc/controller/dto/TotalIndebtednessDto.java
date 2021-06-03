package com.nseindia.mc.controller.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TotalIndebtednessDto {
  private String memberCode;
  private String memberName;
  private LocalDateTime submissionDate;
  private Double netWorth;
  private Double totalBorrowedFunds;
  private Boolean limitExceeded;
}
