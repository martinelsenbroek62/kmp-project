package com.nseindia.mc.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "TBL_MTR_COLLATERALS_SCRIPS_DETAIL_RECORD")
public class MTRCollateralsScripsDetailRecord {

  public static final String INSERT_SQL =
      "INSERT INTO TBL_MTR_COLLATERALS_SCRIPS_DETAIL_RECORD ("
          + "AMOUNT_END_DAY"
          + ",CREATED_BY"
          + ",QUANTITY_END_DAY"
          + ",SERIES"
          + ",CLIENT_NAME"
          + ",CREATED_DT"
          + ",SUBMISSION_DATE"
          + ",ERROR_CODES"
          + ",ROW_STATUS"
          + ",STOCK_EXCHANGE"
          + ",SYMBOL"
          + ",RECORD_TYPE"
          + ",UPDATED_BY"
          + ",UPDATED_DT"
          + ",MTR_FILE_ID"
          + ",PAN"
          + ",CATEGORY_OF_HOLDING"
          + ") VALUES ("
          + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?"
          + ")";

  public static void fillPreparedStatement(PreparedStatement ps, MTRCollateralsScripsDetailRecord record)
      throws SQLException {
    int paramIndex = 1;
    ps.setObject(paramIndex++, record.getAmountEndDay(), java.sql.Types.DOUBLE);
    ps.setString(paramIndex++, record.getCreatedBy());
    ps.setObject(paramIndex++, record.getQuantityEndDay(), java.sql.Types.INTEGER);
    ps.setString(paramIndex++, record.getSeries());
    ps.setString(paramIndex++, record.getClientName());
    ps.setTimestamp(
        paramIndex++,
        record.getCreatedDate() == null
            ? null
            : java.sql.Timestamp.valueOf(record.getCreatedDate()));
    ps.setDate(
            paramIndex++,
            record.getSubmissionDate() == null
                ? null
                : java.sql.Date.valueOf(record.getSubmissionDate()));
    ps.setString(paramIndex++, record.getErrorCodes());
    ps.setObject(paramIndex++, record.getRowStatus(), java.sql.Types.SMALLINT);
    ps.setString(paramIndex++, record.getStockExchange());
    ps.setString(paramIndex++, record.getSymbol());
    ps.setObject(paramIndex++, record.getRecordType(), java.sql.Types.INTEGER);
    ps.setString(paramIndex++, record.getUpdatedBy());
    ps.setTimestamp(
        paramIndex++,
        record.getUpdatedDate() == null
            ? null
            : java.sql.Timestamp.valueOf(record.getUpdatedDate()));
    ps.setLong(paramIndex++, record.getMtrFile() == null ? null : record.getMtrFile().getId());
    ps.setString(paramIndex++, record.getPan());
    ps.setObject(paramIndex++, record.getCategoryOfHolding(), java.sql.Types.SMALLINT);
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

  @Column(name = "SYMBOL")
  private String symbol;

  @Column(name = "SERIES")
  private String series;

  @Column(name = "QUANTITY_END_DAY")
  private Integer quantityEndDay;

  @Column(name = "AMOUNT_END_DAY")
  private Double amountEndDay;

  @Column(name = "CATEGORY_OF_HOLDING")
  private Boolean categoryOfHolding;

  @Column(name = "STOCK_EXCHANGE")
  private String stockExchange;
}
