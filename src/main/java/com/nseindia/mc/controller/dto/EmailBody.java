package com.nseindia.mc.controller.dto;

import java.util.List;

import lombok.*;

@AllArgsConstructor
@Builder
@Getter
@Setter
@NoArgsConstructor
public class EmailBody {
  private List<String> to;
  private String subject;
  private String text;
  private String html;
  private String intermediateReport;
  private int templateId;
  private String from;
  private String bulkId;
  private String replyTo;
}
