package com.nseindia.mc.controller.dto;

import java.util.List;

import lombok.Data;

@Data
public class ListCumulativeDetailsResponse {
  private List<CumulativeDetailsDto> cumulativeMarginTradingDetailsList;
  String cumulativeSqlQuery;
}
