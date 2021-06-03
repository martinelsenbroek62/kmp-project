package com.nseindia.mc.util.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * The input table parameter which is used to generate (replace) the table in the report template.
 */
@Getter
@Setter
@NoArgsConstructor
public class ReportInputTableParameter {
  /** The table name. */
  private String tableName;

  /** The column headers. */
  @NonNull private List<String> columnHeaders = new ArrayList<>();

  /** The rows. The number of items of each row should match the number of columns. */
  @NonNull private List<List<String>> rows = new ArrayList<>();

  /** True if the last row is the table footer which should be bold. */
  private boolean havingFooter;

  /**
   * Add a new column header.
   *
   * @param columnHeader the column header
   */
  public void addColumnHeader(String columnHeader) {
    this.columnHeaders.add(columnHeader);
  }

  /**
   * Add a new row.
   *
   * @param values the row values
   */
  public void addRow(String... values) {
    this.rows.add(Arrays.asList(values));
  }
}
