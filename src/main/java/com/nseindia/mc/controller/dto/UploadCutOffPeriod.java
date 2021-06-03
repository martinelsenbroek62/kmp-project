package com.nseindia.mc.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadCutOffPeriod {
  private String cutoffStartTime;
  private String cutoffEndTime;
}
