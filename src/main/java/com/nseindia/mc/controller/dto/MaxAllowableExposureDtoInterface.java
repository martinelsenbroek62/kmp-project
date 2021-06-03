package com.nseindia.mc.controller.dto;

import java.time.LocalDateTime;

public interface MaxAllowableExposureDtoInterface {
  Long getDailyFileId();
  String getMemberCode();
  String getMemberName();
  LocalDateTime getSubmissionDate();
  Double getMaxAllowableExposure();
  Double getNetWorth();
  Double getTotalBorrowedFunds();
  Boolean getLimitExceeded();
}
