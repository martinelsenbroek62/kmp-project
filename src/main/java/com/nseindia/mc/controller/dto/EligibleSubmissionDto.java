package com.nseindia.mc.controller.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EligibleSubmissionDto {
  private Date lastSubmissionDate;
  private Boolean nilSubmission;
}
