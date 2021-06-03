package com.nseindia.mc.controller.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

/** The details for deleting a KMP record */
@Data
public class DeleteKMPDetails {

  /** The KMP application id */
  @NotNull private Long appId;

  /** The KMP id */
  @NotNull private Long kmpId;
}
