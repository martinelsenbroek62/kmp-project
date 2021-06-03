package com.nseindia.mc.controller.dto;

import java.time.LocalDateTime;

public interface LenderWiseExposureDtoInterface {
  Long getDailyFileId();
  String getMemberCode();
  String getMemberName();
  LocalDateTime getSubmissionDate();
  Long getLenderCategory();
  Double getTotalBorrowedFunds();
  Double getTotalBorrowedFundsOnPreviousDay();
  Boolean getLimitExceeded();
}
