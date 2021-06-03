package com.nseindia.mc.controller.dto;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

/** The request body for deleting multiple KMP details */
@Data
public class DeleteKMPDetailsRequestBody {

  /** The list containing the details of KMPs to delete */
  @NotEmpty private List<DeleteKMPDetails> deleteKMPDetails;
}
