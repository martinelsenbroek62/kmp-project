package com.nseindia.mc.controller.dto;

import lombok.Data;

@Data
public class NonSubmissionCountRequest {
  private int submissionYear;
  private int submissionMonth;
  private long memberId;
}
