package com.nseindia.mc.controller.dto;

public interface CumulativeDetailsDtoInterface {
  String getMemberCode();
  String getMemberName();
  String getMonth();
  String getYear();
  Double getFreshExposureForMonth();
  Double getTotalOutstandingForMonth();
  Double getExposureLiquidatedForMonth();
  Double getNetOutstandingExposures();
  Integer getNumberOfBrokers();
  Integer getNumberOfScripts();
}
