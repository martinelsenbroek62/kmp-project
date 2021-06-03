package com.nseindia.mc.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommonMessageDto {
  private int status;
  private String message;
}
