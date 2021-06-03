package com.nseindia.mc.util.report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/** The report utility provides methods to generate the Excel/Word/PDF reports. */
@UtilityClass
public class ReportUtils {
  private static final String PARAMETER_START = "<";
  private static final String PARAMETER_END = ">";

  // The below suffixes are effective for Excel templates only
  private static final String PARAMETER_TABLE_COLUMN_HEADERS_SUFFIX = "-columnHeaders";
  private static final String PARAMETER_TABLE_ROWS_SUFFIX = "-rows";

  private static final DataFormatter dataFormatter = new DataFormatter();

  /**
   * Generate the Excel report.
   *
   * @param inputData the input data
   * @param reportTemplate the report template
   * @return the BASE-64 encoded content
   * @throws IOException any I/O occurs
   */
  public static String generateExcelReport(
      @NonNull ReportInputData inputData, @NonNull InputStream reportTemplate) throws IOException {
    try (Workbook workbook = new XSSFWorkbook(reportTemplate)) {
      // Get the first sheet
      Sheet sheet = workbook.getSheetAt(0);

      // Loop through all rows and cells
      for (int rowNum = sheet.getFirstRowNum(); rowNum < sheet.getLastRowNum(); rowNum++) {
        Row row = sheet.getRow(rowNum);
        if (row == null) {
          continue;
        }

        for (short cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
          Cell cell = row.getCell(cellNum);
          if (cell == null) {
            continue;
          }

          // Individuals
          replaceIndividuals(cell, inputData.getIndividuals());

          // Table column headers
          replaceTableColumnHeaders(cell, inputData.getTables());

          // Table rows
          replaceTableRows(cell, inputData.getTables());
        }
      }

      // Write to byte array
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      workbook.write(outputStream);

      // Encode
      return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }
  }

  /**
   * Generate the Word report.
   *
   * @param inputData the input data
   * @param reportTemplate the report template
   * @return the BASE-64 encoded content
   * @throws IOException any I/O occurs
   */
  public static String generateWordReport(
      @NonNull ReportInputData inputData, @NonNull InputStream reportTemplate) throws IOException {
    try (XWPFDocument document = new XWPFDocument(reportTemplate)) {

      // Process the individual parameters in all paragraphs
      List<XWPFParagraph> paragraphs = new ArrayList<XWPFParagraph>(document.getParagraphs());
      for (XWPFTable tbl : document.getTables()) {
        for (XWPFTableRow row : tbl.getRows()) {
          for (XWPFTableCell cell : row.getTableCells()) {
            paragraphs.addAll(cell.getParagraphs());

            // Make the cell "wrap text" so the PDF export won't hide it
            CTTc cttc = cell.getCTTc();
            if (cttc != null) {
              CTTcPr pr = cttc.getTcPr();
              if (pr != null & pr.isSetNoWrap()) {
                pr.unsetNoWrap();
              }
            }
          }
        }
      }

      for (XWPFParagraph paragraph : paragraphs) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs == null) {
          continue;
        }

        // Word may separate a parameter name into several runs, we need to prepare to merge some
        // runs
        List<Integer> runsToMerge = new ArrayList<>();
        StringBuilder runsToMergeContent = new StringBuilder();

        for (int runIndex = 0; runIndex < paragraph.getRuns().size(); runIndex++) {
          XWPFRun run = paragraph.getRuns().get(runIndex);

          // Try to replace text inside the run
          String text = replaceIndividuals(run, inputData.getIndividuals());

          // Check if there's still the "<" in the text
          if (text.contains(PARAMETER_START) && !text.contains(PARAMETER_END)) {
            // This run may contain the start of the parameter
            runsToMerge.clear();
            runsToMerge.add(runIndex);

            runsToMergeContent.setLength(0);
            runsToMergeContent.append(text);
          } else if (!runsToMerge.isEmpty()) {
            runsToMerge.add(runIndex);
            runsToMergeContent.append(text);

            if (text.contains(PARAMETER_END)) {
              // Contain individual parameter, merge the other runs into the first
              int firstRunIndex = runsToMerge.get(0);
              XWPFRun firstRun = paragraph.getRuns().get(firstRunIndex);
              firstRun.setText(runsToMergeContent.toString(), 0);

              // Replace for the first run
              replaceIndividuals(firstRun, inputData.getIndividuals());

              runsToMerge.remove(0);

              // Clear the content of the other runs
              for (int otherRunIndex : runsToMerge) {
                XWPFRun otherRun = paragraph.getRuns().get(otherRunIndex);
                otherRun.setText("", 0);
              }

              runsToMerge.clear();
              runsToMergeContent.setLength(0);
            }
          }
        }
      }

      // Process the table parameters
      int alreadyProcessedTableIndex = -1;
      for (int tableIndex = 0; tableIndex < document.getTables().size(); tableIndex++) {
        // No more parameter
        if (inputData.getTables().size() <= alreadyProcessedTableIndex + 1) {
          break;
        }
        // Get the table parameter
        ReportInputTableParameter tableParameter =
            inputData.getTables().get(alreadyProcessedTableIndex + 1);

        XWPFTable table = document.getTables().get(tableIndex);
        if (table.getRows().size() != 2) {
          continue;
        }

        XWPFTableRow headerRow = table.getRow(0);
        XWPFTableRow contentTemplateRow = table.getRow(1);

        if (headerRow.getTableCells().size() != tableParameter.getColumnHeaders().size()) {
          // The columns don't match
          continue;
        }

        // Check if table has content in the second row
        boolean tableHasContent = false;
        for (XWPFTableCell cell : contentTemplateRow.getTableCells()) {
          String text = cell.getText();
          if (text != null && text.length() > 0) {
            tableHasContent = true;
            break;
          }
        }
        if (tableHasContent) {
          continue;
        }

        // Replace the header
        for (int cellIndex = 0; cellIndex < headerRow.getTableCells().size(); cellIndex++) {
          XWPFTableCell cell = headerRow.getTableCells().get(cellIndex);

          String text = cell.getText();
          if (text != null && text.length() > 0) {
            // This cell has content, just skip
            break;
          }

          XWPFRun run = cell.getParagraphs().get(0).createRun();
          run.setText(tableParameter.getColumnHeaders().get(cellIndex));
          run.setBold(true);
        }

        // Replace the content rows
        for (int contentRowIndex = 1;
            contentRowIndex < tableParameter.getRows().size() + 1;
            contentRowIndex++) {
            boolean isLastRow = contentRowIndex == tableParameter.getRows().size();
          List<String> rowValues = tableParameter.getRows().get(contentRowIndex - 1);

          XWPFTableRow tableRow;
          if (contentRowIndex > 1) {
            CTRow ctRow = CTRow.Factory.newInstance();
            ctRow.set(contentTemplateRow.getCtRow());
            tableRow = new XWPFTableRow(ctRow, table);
          } else {
            tableRow = table.getRow(contentRowIndex);
          }

          for (int cellIndex = 0; cellIndex < rowValues.size(); cellIndex++) {
            XWPFParagraph paragraph = tableRow.getCell(cellIndex).getParagraphs().get(0);
            XWPFRun run = paragraph.createRun();
            run.setText(rowValues.get(cellIndex));
            
            if (isLastRow && tableParameter.isHavingFooter()) {
              // Bold it
              run.setBold(true);
            }

            // Remove old text (copied from template row)
            if (paragraph.getRuns().size() > 1) {
              paragraph.removeRun(0);
            }
          }

          if (contentRowIndex > 1) {
            table.addRow(tableRow);
          }
        }

        // Mark the input table as processed
        alreadyProcessedTableIndex += 1;
      }

      // Write to byte array
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      document.write(outputStream);

      // Encode
      return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }
  }

  /**
   * Generate the PDF report.
   *
   * @param inputData the input data
   * @param reportTemplate the report template
   * @return the BASE-64 encoded content
   * @throws IOException any I/O occurs
   */
  public static String generatePdfReport(
      @NonNull ReportInputData inputData, @NonNull InputStream reportTemplate) throws IOException {
    // Get the Word first
    String wordContent = generateWordReport(inputData, reportTemplate);
    ByteArrayInputStream inputStream =
        new ByteArrayInputStream(Base64.getDecoder().decode(wordContent));

    try (XWPFDocument document = new XWPFDocument(inputStream)) {
      // Convert to byte array
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      PdfOptions options = PdfOptions.create();
      PdfConverter.getInstance().convert(document, outputStream, options);

      // Encode
      return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }
  }

  private String replaceIndividuals(XWPFRun run, List<ReportInputIndividualParameter> individuals) {
    String text = run.getText(0);
    if (text == null) {
      return "";
    }

    text = replaceIndividuals(text, individuals);
    run.setText(text, 0);

    return text;
  }

  private void replaceIndividuals(Cell cell, List<ReportInputIndividualParameter> individuals) {
    String cellValue = getStringCellValue(cell);
    if (cellValue == null || cellValue.trim().length() == 0) {
      return;
    }

    cell.setCellValue(replaceIndividuals(cellValue, individuals));
  }

  private String replaceIndividuals(String text, List<ReportInputIndividualParameter> individuals) {
    for (ReportInputIndividualParameter individual : individuals) {
      String param = PARAMETER_START + individual.getFieldName() + PARAMETER_END;

      if (text.contains(param)) {
        String value = individual.getFieldValue();
        if (value == null) {
          value = "";
        }
        text = text.replaceAll(param, value);
      }
    }

    return text;
  }

  private static void replaceTableColumnHeaders(Cell cell, List<ReportInputTableParameter> tables) {
    String cellValue = getStringCellValue(cell);
    if (cellValue == null || cellValue.trim().length() == 0) {
      return;
    }

    for (ReportInputTableParameter table : tables) {
      String param =
          PARAMETER_START
              + table.getTableName()
              + PARAMETER_TABLE_COLUMN_HEADERS_SUFFIX
              + PARAMETER_END;

      if (param.equals(cellValue)) {
        // The table headers starts at this cell
        int column = cell.getColumnIndex();
        Row row = cell.getRow();

        for (String header : table.getColumnHeaders()) {
          Cell currentCell = row.getCell(column);
          if (currentCell == null) {
            currentCell = row.createCell(column);
          }

          currentCell.setCellValue(header);
          column++;
        }

        // Auto row height
        row.setHeight((short) -1);
      }
    }
  }

  private static void replaceTableRows(Cell cell, List<ReportInputTableParameter> tables) {
    String cellValue = getStringCellValue(cell);
    if (cellValue == null || cellValue.trim().length() == 0) {
      return;
    }

    for (ReportInputTableParameter table : tables) {
      String param =
          PARAMETER_START + table.getTableName() + PARAMETER_TABLE_ROWS_SUFFIX + PARAMETER_END;

      // The table rows starts at this cell
      if (param.equals(cellValue)) {
        // Clear the cell value
        cell.setCellValue("");

        // Get the current sheet and start column / row index
        XSSFSheet sheet = (XSSFSheet) cell.getSheet();
        int startColumnIndex = cell.getColumnIndex();
        int startRowIndex = cell.getRowIndex();

        // Prepare the rows
        int rowCount = table.getRows().size();
        if (rowCount > 1) {
          sheet.shiftRows(startRowIndex, cell.getSheet().getLastRowNum(), rowCount - 1);

          // Copy the styles
          for (int i = 0; i < rowCount - 1; i++) {
            sheet.copyRows(
                startRowIndex + rowCount - 1,
                startRowIndex + rowCount - 1,
                startRowIndex + i,
                new CellCopyPolicy());
          }
        }

        // Set values to each row
        for (int i = 0; i < rowCount; i++) {
          Row row = sheet.getRow(startRowIndex + i);
          List<String> rowValues = table.getRows().get(i);

          int cellNum = startColumnIndex;
          for (String value : rowValues) {
            Cell currentCell = row.getCell(cellNum);
            if (currentCell == null) {
              currentCell = row.createCell(cellNum);
            }

            currentCell.setCellValue(value);
            cellNum++;
          }

          // Auto row height
          row.setHeight((short) -1);
        }
      }
    }
  }

  private String getStringCellValue(Cell cell) {
    return cell == null ? null : dataFormatter.formatCellValue(cell);
  }
}
