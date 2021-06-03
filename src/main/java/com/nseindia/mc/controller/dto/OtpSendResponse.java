package com.nseindia.mc.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OtpSendResponse {
  private int status;
  private String message;
  private Long verifyId;
}
