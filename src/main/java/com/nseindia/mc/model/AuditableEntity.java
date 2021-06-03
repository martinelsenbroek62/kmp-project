package com.nseindia.mc.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
public abstract class AuditableEntity {
  @Column(name = "CREATED_BY")
  private String createdBy;

  @Column(name = "CREATED_DT")
  private LocalDateTime createdDate;

  @Column(name = "UPDATED_BY")
  private String updatedBy;

  @Column(name = "UPDATED_DT")
  private LocalDateTime updatedDate;
}
