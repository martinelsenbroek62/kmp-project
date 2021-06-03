package com.nseindia.mc.controller.dto;

import com.nseindia.mc.value.MtrRecordValidateResult;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MTRValidateRecordSummary {
  private String recordType;
  private MtrRecordValidateResult result;
  private int errorCount;
}
