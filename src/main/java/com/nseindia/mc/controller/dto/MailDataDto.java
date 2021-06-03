package com.nseindia.mc.controller.dto;

import lombok.Data;

@Data
public class MailDataDto {
  private String from;
  private String to;
  private String cc;
  private String attachment;
  private String subject;
  private String body;
}
