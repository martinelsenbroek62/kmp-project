package com.nseindia.mc.controller.dto;

import java.util.List;

import lombok.Data;

@Data
public class NonSubmissionMbrDtlsResponse {
  private List<NonSubmissionMbrDtlsDto> nonSubmissionMemberList;
  String nonSubmissionSqlQuery;
}
