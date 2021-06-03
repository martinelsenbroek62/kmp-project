package com.nseindia.mc.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class WithdrawalMbrDtlsResponse {
  private List<WithdrawalStatusMemberDto> withdrawalStatusMemberList;
  String withdrawalSqlQuery;
}
