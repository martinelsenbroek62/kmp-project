package com.nseindia.mc.controller.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

/** The request body for update KMP details request */
@Data
public class UpdateKMPsRequestBody {

  /**
   * The list of KMPs to update
   */
  @NotEmpty
  @Valid
  List<UpdateKmpData> kmps;
}
