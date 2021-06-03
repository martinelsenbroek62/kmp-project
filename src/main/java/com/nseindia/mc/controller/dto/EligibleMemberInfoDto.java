package com.nseindia.mc.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EligibleMemberInfoDto {
  private String entitySEBIRegNumber;
  private String regEntityCategory;
  private String entityName;
  private String entityPANNo;
}
