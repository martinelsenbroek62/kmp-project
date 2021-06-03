package com.nseindia.mc.util.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * The input individual parameter which is used to generate (replace) the individual field in the
 * report template.
 */
@Getter
@Setter
@AllArgsConstructor
public class ReportInputIndividualParameter {
  @NonNull private String fieldName;
  private String fieldValue;
}
