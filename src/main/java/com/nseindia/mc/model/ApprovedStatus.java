package com.nseindia.mc.model;

public enum ApprovedStatus {
  APPROVED("Approved"),
  REJECTED("Rejected"),
  PENDING("Pending");

  public final String code;

  private ApprovedStatus(String code) {
    this.code = code;
  }
}
