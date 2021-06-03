package com.nseindia.mc.util.report;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/** The input data to generate reports against a specified Excel/Word template. */
@Getter
@Setter
@NoArgsConstructor
public class ReportInputData {
  @NonNull private List<ReportInputIndividualParameter> individuals = new ArrayList<>();
  @NonNull private List<ReportInputTableParameter> tables = new ArrayList<>();

  /**
   * Add a new individual parameter.
   *
   * @param fieldName the field name
   * @param fieldValue the field value
   */
  public void addIndividual(String fieldName, String fieldValue) {
    this.individuals.add(new ReportInputIndividualParameter(fieldName, fieldValue));
  }

  /**
   * Add a new table parameter.
   *
   * @param table the table
   * @return the new table
   */
  public ReportInputTableParameter addTable(ReportInputTableParameter table) {
    this.tables.add(table);
    return table;
  }

  /**
   * Create and add a new table parameter.
   *
   * @return the new table
   */
  public ReportInputTableParameter addTable() {
    ReportInputTableParameter table = new ReportInputTableParameter();
    return this.addTable(table);
  }

  /**
   * Create and add a new table parameter with empty column headers.
   *
   * @param headerCount the number of headers
   * @return the new table
   */
  public ReportInputTableParameter addTable(int headerCount) {
    ReportInputTableParameter table = this.addTable();
    for (int i = 0; i < headerCount; i++) {
      table.addColumnHeader("");
    }
    return table;
  }
}
