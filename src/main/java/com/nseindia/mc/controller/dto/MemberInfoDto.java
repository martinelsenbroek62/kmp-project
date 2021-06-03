package com.nseindia.mc.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberInfoDto {
  private long memberId;
  private String memberCode;
  private String memberName;
}
