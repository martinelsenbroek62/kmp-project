package com.nseindia.mc.model;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "TBL_MTR_DETAIL_RECORD")
public class MTRDetailRecord {
  public static final String INSERT_SQL =
      "INSERT INTO TBL_MTR_DETAIL_RECORD ("
          + "CATEGORY_OF_HOLDING, "
          + "CLIENT_NAME, "
          + "CREATED_BY, "
          + "CREATED_DT, "
          + "ERROR_CODES, "
          + "FUNDED_AMOUNT_BEGIN_DAY, "
          + "FUNDED_AMOUNT_DURING_DAY, "
          + "FUNDED_AMOUNT_END_DAY, "
          + "FUNDED_AMOUNT_LIQUIDATED_DURING_DAY, "
          + "FUNDED_QUANTITY_BEGIN_DAY, "
          + "FUNDED_QUANTITY_DURING_DAY, "
          + "FUNDED_QUANTITY_END_DAY, "
          + "FUNDED_QUANTITY_LIQUIDATED_DURING_DAY, "
          + "MTR_FILE_ID, "
          + "PAN, "
          + "RECORD_TYPE, "
          + "ROW_STATUS, "
          + "SERIES, "
          + "STOCK_EXCHANGE, "
          + "SUBMISSION_DATE, "
          + "SYMBOL, "
          + "HASH_CODE_CLIENT_NAME_PAN_SYMBOL, "
          + "UPDATED_BY, "
          + "UPDATED_DT"
          + ") VALUES ("
          + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  ?"
          + ")";

  public static void fillPreparedStatement(PreparedStatement ps, MTRDetailRecord record)
      throws SQLException {
    int paramIndex = 1;
    ps.setObject(paramIndex++, record.getCategoryOfHolding(), java.sql.Types.SMALLINT);
    ps.setString(paramIndex++, record.getClientName());
    ps.setString(paramIndex++, record.getCreatedBy());
    ps.setTimestamp(
        paramIndex++,
        record.getCreatedDate() == null
            ? null
            : java.sql.Timestamp.valueOf(record.getCreatedDate()));
    ps.setString(paramIndex++, record.getErrorCodes());
    ps.setObject(paramIndex++, record.getFundedAmountBeginDay(), java.sql.Types.DOUBLE);
    ps.setObject(paramIndex++, record.getFundedAmountDuringDay(), java.sql.Types.DOUBLE);
    ps.setObject(paramIndex++, record.getFundedAmountEndDay(), java.sql.Types.DOUBLE);
    ps.setObject(paramIndex++, record.getFundedAmountLiquidatedDuringDay(), java.sql.Types.DOUBLE);
    ps.setObject(paramIndex++, record.getFundedQuantityBeginDay(), java.sql.Types.DOUBLE);
    ps.setObject(paramIndex++, record.getFundedQuantityDuringDay(), java.sql.Types.DOUBLE);
    ps.setObject(paramIndex++, record.getFundedQuantityEndDay(), java.sql.Types.DOUBLE);
    ps.setObject(
        paramIndex++, record.getFundedQuantityLiquidatedDuringDay(), java.sql.Types.DOUBLE);
    ps.setLong(paramIndex++, record.getMtrFile() == null ? null : record.getMtrFile().getId());
    ps.setString(paramIndex++, record.getPan());
    ps.setObject(paramIndex++, record.getRecordType(), java.sql.Types.INTEGER);
    ps.setObject(paramIndex++, record.getRowStatus(), java.sql.Types.SMALLINT);
    ps.setString(paramIndex++, record.getSeries());
    ps.setString(paramIndex++, record.getStockExchange());
    ps.setDate(
        paramIndex++,
        record.getSubmissionDate() == null
            ? null
            : java.sql.Date.valueOf(record.getSubmissionDate()));
    ps.setString(
        paramIndex++, record.getSymbol() == null ? null : record.getSymbol().getSymbolCode());
    ps.setObject(
      paramIndex++,
      createHashCodeClientNamePanSymbol(
        record.getClientName(), record.getPan(), record.getSymbol().getSymbolCode()
      ),
      java.sql.Types.INTEGER);
    ps.setString(paramIndex++, record.getUpdatedBy());
    ps.setTimestamp(
        paramIndex++,
        record.getUpdatedDate() == null
            ? null
            : java.sql.Timestamp.valueOf(record.getUpdatedDate()));
  }

  public static String createClientNamePanSymbol(String clientName, String pan, String symbol) {
    return String.format("%s:%s:%s", clientName, pan, symbol);
  }

  public static Integer createHashCodeClientNamePanSymbol(String clientName, String pan, String symbol) {
    return createClientNamePanSymbol(clientName, pan, symbol).hashCode();
  }

  /** The id. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "MTR_FILE_ID")
  private MTRDailyFile mtrFile;

  @Column(name = "RECORD_TYPE")
  private Integer recordType;

  @Column(name = "ROW_STATUS")
  private Boolean rowStatus;

  @Column(name = "ERROR_CODES")
  private String errorCodes;

  @Column(name = "SUBMISSION_DATE")
  private LocalDate submissionDate;

  @Column(name = "CREATED_BY")
  private String createdBy;

  @Column(name = "CREATED_DT")
  private LocalDateTime createdDate;

  @Column(name = "UPDATED_BY")
  private String updatedBy;

  @Column(name = "UPDATED_DT")
  private LocalDateTime updatedDate;

  @Column(name = "CLIENT_NAME")
  private String clientName;

  @Column(name = "PAN")
  private String pan;

  @ManyToOne
  @JoinColumn(name = "SYMBOL")
  private MTRSymbolName symbol;

  @Column(name = "HASH_CODE_CLIENT_NAME_PAN_SYMBOL")
  private Integer hashCodeClientNamePanSymbol;

  @Column(name = "SERIES")
  private String series;

  @Column(name = "FUNDED_QUANTITY_BEGIN_DAY")
  private Integer fundedQuantityBeginDay;

  @Column(name = "FUNDED_AMOUNT_BEGIN_DAY")
  private Double fundedAmountBeginDay;

  @Column(name = "FUNDED_QUANTITY_DURING_DAY")
  private Integer fundedQuantityDuringDay;

  @Column(name = "FUNDED_AMOUNT_DURING_DAY")
  private Double fundedAmountDuringDay;

  @Column(name = "FUNDED_QUANTITY_LIQUIDATED_DURING_DAY")
  private Integer fundedQuantityLiquidatedDuringDay;

  @Column(name = "FUNDED_AMOUNT_LIQUIDATED_DURING_DAY")
  private Double fundedAmountLiquidatedDuringDay;

  @Column(name = "FUNDED_QUANTITY_END_DAY")
  private Integer fundedQuantityEndDay;

  @Column(name = "FUNDED_AMOUNT_END_DAY")
  private Double fundedAmountEndDay;

  @Column(name = "CATEGORY_OF_HOLDING")
  private Boolean categoryOfHolding;

  @Column(name = "STOCK_EXCHANGE")
  private String stockExchange;
}
