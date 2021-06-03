package com.nseindia.mc.controller.dto;

import java.util.Date;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class TblAimlSystemDetailDto {
  private String appSystemId;
  private String sysType;
  private Long memberId;
  @NotEmpty private String appSystemName;
  private Date appSystemUsedDate;
  private String areaTypeAimlUsed;
  private String claimAimlAppSystem;
  private String ToolsTechnologyCategory;
  private String projectImplemented;
  private String cicularSebiSecurityControl;
  private String systemIncludeAudit;
  private String appSystemUsed;
  private String safeguardApp;
  private String systemComplySebi;
  private String systemInvolvesOrder;
  private String systemFallsDiPm;
  private String systemDisseminate;
  private String appSystemCyberSecurity;
  private String controlPoints;
  private String appSystemProductOffer;
  private String createdBy;
  private Date createdDt;
  private String updatedBy;
  private Date updateDt;
}
