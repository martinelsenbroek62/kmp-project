package com.nseindia.mc.controller.dto;

import lombok.Data;

@Data
public class NonSubmissionMbrDtlsDto {
  private long memberId;
  private String memberCode;
  private String memberName;
  private Integer nonSubmissionCount;
  private Integer year;
  private Integer month;
}
