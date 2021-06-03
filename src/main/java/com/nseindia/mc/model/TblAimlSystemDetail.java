package com.nseindia.mc.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "TBL_AIML_SYSTEM_DETAILS")
public class TblAimlSystemDetail {
  @Id
  @Column(name = "APP_SYSTEM_ID")
  private String appSystemId;

  @Column(name = "SYS_TYPE")
  private String sysType;

  @Column(name = "MEMBER_ID")
  private Long memberId;

  @Column(name = "APP_SYSTEM_NAME")
  private String appSystemName;

  @Column(name = "APP_SYSTEM_USED_DATE")
  private Date appSystemUsedDate;

  @Column(name = "AREA_TYPE_AIML_USED")
  private String areaTypeAimlUsed;

  @Column(name = "CLAIM_AIML_APP_SYSTEM")
  private String claimAimlAppSystem;

  @Column(name = "TOOLS_TECHNOLOGY_CATEGORY")
  private String ToolsTechnologyCategory;

  @Column(name = "PROJECT_IMPLEMENTED")
  private String projectImplemented;

  @Column(name = "CICULAR_SEBI_SECURITY_CONTROL")
  private String cicularSebiSecurityControl;

  @Column(name = "SYSTEM_INCLUDE_AUDIT")
  private String systemIncludeAudit;

  @Column(name = "APP_SYSTEM_USED")
  private String appSystemUsed;

  @Column(name = "SAFEGUARD_APP")
  private String safeguardApp;

  @Column(name = "SYSTEM_COMPLY_SEBI")
  private String systemComplySebi;

  @Column(name = "SYSTEM_INVOLVES_ORDER")
  private String systemInvolvesOrder;

  @Column(name = "SYSTEM_FALLS_DI_PM")
  private String systemFallsDiPm;

  @Column(name = "SYSTEM_DISSEMINATE")
  private String systemDisseminate;

  @Column(name = "APP_SYSTEM_CYBER_SECURITY")
  private String appSystemCyberSecurity;

  @Column(name = "CONTROL_POINTS")
  private String controlPoints;

  @Column(name = "APP_SYSTEM_PRODUCT_OFFER")
  private String appSystemProductOffer;

  @Column(name = "CREATED_BY")
  private String createdBy;

  @Column(name = "CREATED_DT")
  private Date createdDt;

  @Column(name = "UPDATED_BY")
  private String updatedBy;
  
  @Column(name = "QUARTER")
  private String quarterName;

  @Column(name = "UPDATED_DT")
  private Date updateDt;
}
