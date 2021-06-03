package com.nseindia.mc.value;

public enum MtrRecordType {
  ControlRecord("Control Record"),
  SummaryRecord("Summary Record"),
  DetailRecord("Detail Record"),
  CollateralRecord("Collateral Record"),
  CollateralScriptDetails("Collateral Script Details");
  String code;

  MtrRecordType(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
