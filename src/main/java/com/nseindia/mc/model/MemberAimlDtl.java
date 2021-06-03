package com.nseindia.mc.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "MEMBER_AIML_DTL")
public class MemberAimlDtl {
  @Id
  @Column(name = "ID")
  private Long id;

  @Column(name = "MEM_ID")
  private Long memId;

  @Column(name = "MEM_CD_SEGMT")
  private String memCdSegmt;

  @Column(name = "NIL_FLAG")
  private String nilFlag;

  @Column(name = "ELIGIBLE_FLAG")
  private String eligibleFlag;

  @Column(name = "QUARTER_NAME")
  private String quarterName;

  @Column(name = "QUARTER_START_DATE")
  private Date quarterStartDate;

  @Column(name = "QUARTER_END_DATE")
  private Date quarterEndDate;

  @Column(name = "SUBMISSION_DONE")
  private String submissionDone;

  @Column(name = "CREATED_BY")
  private String createdBy;

  @Column(name = "CREATED_DT")
  private Date createdDt;

  @Column(name = "UPDATED_BY")
  private String updatedBy;

  @Column(name = "UPDATED_DT")
  private Date updateDt;
}
