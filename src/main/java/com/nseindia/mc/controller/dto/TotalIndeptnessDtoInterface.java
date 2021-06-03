package com.nseindia.mc.controller.dto;

import java.time.LocalDateTime;

public interface TotalIndeptnessDtoInterface {
  Long getDailyFileId();
  String getMemberCode();
  String getMemberName();
  LocalDateTime getSubmissionDate();
  Double getNetWorth();
  Double getTotalBorrowedFunds();
  Boolean getLimitExceeded();
}
