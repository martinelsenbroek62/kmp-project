package com.nseindia.mc.controller.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubmissionDto {
  private long id;
  private String quarter;
  private Date quarterStartDate;
  private Date quarterEndDate;
  private Date submissionDate;
  private long memberId;
  private String memberCode;
  private String memberName;
  private boolean submissionDone;
  private boolean nilSubmission;
}
