package com.nseindia.mc.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class LeverageReportResponse {
  private List<TotalIndebtednessDto> totalIndebtedness;
  private List<MaxAllowableExposureDto> maxAllowableExposure;
  private List<MaxClientAllowableExposureDto> maxClientAllowableExposure;
  private List<LenderWiseExposureDto> lenderWiseExposure;
  String sqlQuery;
}
