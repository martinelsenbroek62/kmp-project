package com.nseindia.mc.controller.dto;

import lombok.Data;

@Data
public class CumulativeDetailsDto {
  private String memberCode;
  private String memberName;
  private String month;
  private String year;
  private Double totalOutstandingForMonth;
  private Double freshExposureForMonth;
  private Double exposureLiquidatedForMonth;
  private Double netOutstandingExposures;
  private Integer numberOfBrokers;
  private Integer numberOfScripts;
}
