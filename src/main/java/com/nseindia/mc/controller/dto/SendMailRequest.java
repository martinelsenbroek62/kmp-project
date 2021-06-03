package com.nseindia.mc.controller.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class SendMailRequest {

  private LocalDate reportingDate;
  private String memberId;
  private MailDataDto mailData;
}
