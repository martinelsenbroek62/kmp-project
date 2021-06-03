package com.nseindia.mc.controller.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class MemberDetailsDto {
  private Long memberId;
  private String memberName;
  private String mtrActiveStatus;
  private LocalDate mtrActiveFromDate;
}
