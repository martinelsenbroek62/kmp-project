package com.nseindia.mc.controller.dto;

import java.time.LocalDateTime;

public interface MaxClientAllowableExposureDtoInterface {
  Long getDailyFileId();
  String getMemberCode();
  String getMemberName();
  LocalDateTime getSubmissionDate();
  String getClientName();
  String getClientPan();
  Double getExposureToClient();
  Double getNetWorth();
  Double getTotalBorrowedFunds();
  Boolean getLimitExceeded();
}
