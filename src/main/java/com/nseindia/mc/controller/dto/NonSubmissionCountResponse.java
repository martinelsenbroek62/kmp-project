package com.nseindia.mc.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NonSubmissionCountResponse {
  private int status;
  private String downloadMemberNonSubmissionFile;
}
