package com.nseindia.mc.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.nseindia.mc.controller.dto.CumulativeDetailsDtoInterface;
import com.nseindia.mc.controller.dto.MTRDetailRecordDtoInterface;
import com.nseindia.mc.controller.dto.MaxAllowableExposureDtoInterface;
import com.nseindia.mc.controller.dto.MaxClientAllowableExposureDtoInterface;
import com.nseindia.mc.model.MTRDetailRecord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;

import static org.hibernate.annotations.QueryHints.READ_ONLY;
import static org.hibernate.jpa.QueryHints.HINT_CACHEABLE;
import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

public interface MTRDetailRecordRepository extends JpaRepository<MTRDetailRecord, Long> {
  String SQL_LIST_CUMULATIVE_DTLS =
      "SELECT " +
          "a.freshExposureForMonth, a.exposureLiquidatedForMonth, a.numberOfBrokers, a.numberOfScripts, a.year, a.month, " +
          "b.totalOutstandingForMonth as totalOutstandingForMonth, c.netOutstandingExposures as netOutstandingExposures " +
          "FROM " +
          "(SELECT  " +
          "SUM(FUNDED_AMOUNT_DURING_DAY) as freshExposureForMonth,  " +
          "SUM(FUNDED_AMOUNT_LIQUIDATED_DURING_DAY) as exposureLiquidatedForMonth,  " +
          "COUNT(DISTINCT m.MEM_CD) as numberOfBrokers,  " +
          "COUNT(DISTINCT r.SYMBOL) as numberOfScripts,  " +
          "to_char(f.REPORTING_DATE, 'YYYY') AS year, " +
          "to_char(f.REPORTING_DATE, 'MM') AS month  " +
          "FROM     TBL_MTR_DETAIL_RECORD r " +
          "INNER JOIN TBL_MTR_DAILY_FILE f ON r.MTR_FILE_ID = f.id     " +
          "INNER JOIN TBL_MEMBER_MASTER m ON m.MEM_ID = f.MEMBER_ID " +
          "WHERE f.REPORTING_DATE >= TO_DATE(:fromDate, 'DD-MM-YYYY' ) " +
          "and f.REPORTING_DATE < TO_DATE(:toDate, 'DD-MM-YYYY' )  " +
          "GROUP BY to_char(f.REPORTING_DATE, 'YYYY'), to_char(f.REPORTING_DATE, 'MM')) a " +
          "LEFT JOIN " +
          "(SELECT  " +
          "SUM(FUNDED_AMOUNT_BEGIN_DAY) as totalOutstandingForMonth, " +
          "to_char(f.REPORTING_DATE, 'YYYY') AS year, " +
          "to_char(f.REPORTING_DATE, 'MM') AS month FROM     " +
          "TBL_MTR_DETAIL_RECORD r " +
          "INNER JOIN TBL_MTR_DAILY_FILE f ON r.MTR_FILE_ID = f.id     " +
          "INNER JOIN TBL_MEMBER_MASTER m ON m.MEM_ID = f.MEMBER_ID " +
          "WHERE to_char(f.REPORTING_DATE, 'DD-MM-YYYY') IN ( :firstBusinessDays ) " +
          "GROUP BY to_char(f.REPORTING_DATE, 'YYYY'), to_char(f.REPORTING_DATE, 'MM')) b " +
          "ON a.year=b.year and a.month=b.month " +
          "LEFT JOIN " +
          "(SELECT  " +
          "SUM(FUNDED_AMOUNT_END_DAY) as netOutstandingExposures,  " +
          "to_char(f.REPORTING_DATE, 'YYYY') AS year, " +
          "to_char(f.REPORTING_DATE, 'MM') AS month FROM     " +
          "TBL_MTR_DETAIL_RECORD r " +
          "INNER JOIN TBL_MTR_DAILY_FILE f ON r.MTR_FILE_ID = f.id     " +
          "INNER JOIN TBL_MEMBER_MASTER m ON m.MEM_ID = f.MEMBER_ID " +
          "WHERE to_char(f.REPORTING_DATE, 'DD-MM-YYYY') IN ( :lastBusinessDays ) " +
          "GROUP BY to_char(f.REPORTING_DATE, 'YYYY'), to_char(f.REPORTING_DATE, 'MM')) c " +
          "ON a.year=c.year and a.month=c.month";
  @Query(value = SQL_LIST_CUMULATIVE_DTLS,
      nativeQuery = true
  )
  List<CumulativeDetailsDtoInterface> listCumulativeDtls(String fromDate, String toDate,
                                                List<String> firstBusinessDays, List<String> lastBusinessDays);

  String SQL_LIST_CUMULATIVE_DTLS_MEMBER_WISE =
      "SELECT " +
          "a.memberName as memberName, a.memberCode as memberCode, " +
          "a.freshExposureForMonth, a.exposureLiquidatedForMonth, a.numberOfBrokers, a.numberOfScripts, a.year, a.month, " +
          "b.totalOutstandingForMonth as totalOutstandingForMonth, c.netOutstandingExposures as netOutstandingExposures " +
          "FROM " +
          "(SELECT  " +
          "m.MEM_NAME as memberName, m.MEM_CD as memberCode, " +
          "SUM(FUNDED_AMOUNT_DURING_DAY) as freshExposureForMonth,  " +
          "SUM(FUNDED_AMOUNT_LIQUIDATED_DURING_DAY) as exposureLiquidatedForMonth,  " +
          "COUNT(DISTINCT m.MEM_CD) as numberOfBrokers,  " +
          "COUNT(DISTINCT r.SYMBOL) as numberOfScripts,  " +
          "to_char(f.REPORTING_DATE, 'YYYY') AS year, " +
          "to_char(f.REPORTING_DATE, 'MM') AS month  " +
          "FROM     TBL_MTR_DETAIL_RECORD r " +
          "INNER JOIN TBL_MTR_DAILY_FILE f ON r.MTR_FILE_ID = f.id     " +
          "INNER JOIN TBL_MEMBER_MASTER m ON m.MEM_ID = f.MEMBER_ID " +
          "WHERE f.REPORTING_DATE >= TO_DATE(:fromDate, 'DD-MM-YYYY' ) " +
          "and f.REPORTING_DATE < TO_DATE(:toDate, 'DD-MM-YYYY' )  " +
          "GROUP BY to_char(f.REPORTING_DATE, 'YYYY'), to_char(f.REPORTING_DATE, 'MM'), m.MEM_NAME, m.MEM_CD) a " +
          "LEFT JOIN " +
          "(SELECT  " +
          "m.MEM_NAME as memberName, m.MEM_CD as memberCode, " +
          "SUM(FUNDED_AMOUNT_BEGIN_DAY) as totalOutstandingForMonth, " +
          "to_char(f.REPORTING_DATE, 'YYYY') AS year, " +
          "to_char(f.REPORTING_DATE, 'MM') AS month FROM     " +
          "TBL_MTR_DETAIL_RECORD r " +
          "INNER JOIN TBL_MTR_DAILY_FILE f ON r.MTR_FILE_ID = f.id     " +
          "INNER JOIN TBL_MEMBER_MASTER m ON m.MEM_ID = f.MEMBER_ID " +
          "WHERE to_char(f.REPORTING_DATE, 'DD-MM-YYYY') IN ( :firstBusinessDays ) " +
          "GROUP BY to_char(f.REPORTING_DATE, 'YYYY'), to_char(f.REPORTING_DATE, 'MM'), m.MEM_NAME, m.MEM_CD) b " +
          "ON a.year=b.year and a.month=b.month and a.memberName=b.memberName and a.memberCode=b.memberCode " +
          "LEFT JOIN " +
          "(SELECT  " +
          "m.MEM_NAME as memberName, m.MEM_CD as memberCode, " +
          "SUM(FUNDED_AMOUNT_END_DAY) as netOutstandingExposures,  " +
          "to_char(f.REPORTING_DATE, 'YYYY') AS year, " +
          "to_char(f.REPORTING_DATE, 'MM') AS month FROM     " +
          "TBL_MTR_DETAIL_RECORD r " +
          "INNER JOIN TBL_MTR_DAILY_FILE f ON r.MTR_FILE_ID = f.id     " +
          "INNER JOIN TBL_MEMBER_MASTER m ON m.MEM_ID = f.MEMBER_ID " +
          "WHERE to_char(f.REPORTING_DATE, 'DD-MM-YYYY') IN ( :lastBusinessDays ) " +
          "GROUP BY to_char(f.REPORTING_DATE, 'YYYY'), to_char(f.REPORTING_DATE, 'MM'), m.MEM_NAME, m.MEM_CD) c " +
          "ON a.year=c.year and a.month=c.month and a.memberName=c.memberName and a.memberCode=c.memberCode";
  @Query(value = SQL_LIST_CUMULATIVE_DTLS_MEMBER_WISE,
      nativeQuery = true
  )
  List<CumulativeDetailsDtoInterface> listCumulativeDtlsMemberWise(String fromDate, String toDate,
                                                         List<String> firstBusinessDays, List<String> lastBusinessDays);

  List<MTRDetailRecord> findByMtrFile_ReportingDate(LocalDateTime reportingDate);

  List<MTRDetailRecord> findByMtrFile_IdIn(Set<Long> ids);

  @QueryHints(value = {
    @QueryHint(name = HINT_FETCH_SIZE, value = "" + 1000),
    @QueryHint(name = HINT_CACHEABLE, value = "false"),
    @QueryHint(name = READ_ONLY, value = "true")
  })
  @Query(value =
    "select CLIENT_NAME || ':' || PAN || ':' || SYMBOL as mapKey, FUNDED_QUANTITY_END_DAY as fundedQuantityEndDay, " +
      "FUNDED_AMOUNT_END_DAY as fundedAmountEndDay from TBL_MTR_DETAIL_RECORD where MTR_FILE_ID=:id ",
    nativeQuery = true
  )
  Stream<MTRDetailRecordDtoInterface> findByMtrFile_Id(Long id);

  @QueryHints(value = {
    @QueryHint(name = HINT_FETCH_SIZE, value = "" + 1000),
    @QueryHint(name = HINT_CACHEABLE, value = "false"),
    @QueryHint(name = READ_ONLY, value = "true")
  })
  @Query(value =
    "select CLIENT_NAME || ':' || PAN || ':' || SYMBOL as mapKey, FUNDED_QUANTITY_END_DAY as fundedQuantityEndDay, " +
      "FUNDED_AMOUNT_END_DAY as fundedAmountEndDay from TBL_MTR_DETAIL_RECORD where MTR_FILE_ID=:id " +
      "AND HASH_CODE_CLIENT_NAME_PAN_SYMBOL IN (:keys)",
    nativeQuery = true
  )
  Stream<MTRDetailRecordDtoInterface> findByMtrFile_IdAndKeys(Long id, List<Integer> keys);

  @Modifying
  @Query("delete from MTRDetailRecord r where r.mtrFile.id=:mtrFileId")
  void deleteByMTRDailyFileId(Long mtrFileId);

  @Query(value = "SELECT " +
        " SUM(FUNDED_AMOUNT_DURING_DAY) as freshExposureForMonth, " + 
        " SUM(FUNDED_AMOUNT_LIQUIDATED_DURING_DAY) as exposureLiquidatedForMonth, " + 
        " COUNT(DISTINCT m.MEM_CD) as numberOfBrokers, " + 
        " COUNT(DISTINCT r.SYMBOL) as numberOfScripts, " + 
        " to_char(f.REPORTING_DATE, 'YYYY') AS \"year\", to_char(f.REPORTING_DATE, 'MM') AS \"month\" " + 
        " FROM " + 
        "    TBL_MTR_DETAIL_RECORD r INNER JOIN TBL_MTR_DAILY_FILE f ON r.MTR_FILE_ID = f.id " + 
        "    INNER JOIN TBL_MEMBER_MASTER m ON m.MEM_ID = f.MEMBER_ID " +
        " WHERE f.REPORTING_DATE >= :startDate and f.REPORTING_DATE < :endDate " + 
        " GROUP BY to_char(f.REPORTING_DATE, 'YYYY'), to_char(f.REPORTING_DATE, 'MM')", 
      nativeQuery = true
   )
  List<CumulativeDetailsDtoInterface> aggregateByStartDateEndDate(LocalDateTime startDate, LocalDateTime endDate);

    @Query(value = "SELECT " + 
    " m.MEM_NAME as memberName, m.MEM_CD as memberCode, " + 
    " SUM(FUNDED_AMOUNT_DURING_DAY) as freshExposureForMonth, " + 
    " SUM(FUNDED_AMOUNT_LIQUIDATED_DURING_DAY) as exposureLiquidatedForMonth, " + 
    " COUNT(DISTINCT m.MEM_CD) as numberOfBrokers, " + 
    " COUNT(DISTINCT r.SYMBOL) as numberOfScripts, " + 
    " to_char(f.REPORTING_DATE, 'YYYY') AS \"year\", to_char(f.REPORTING_DATE, 'MM') AS \"month\" " + 
    " FROM " + 
    "    TBL_MTR_DETAIL_RECORD r INNER JOIN TBL_MTR_DAILY_FILE f ON r.MTR_FILE_ID = f.id " + 
    "    INNER JOIN TBL_MEMBER_MASTER m ON m.MEM_ID = f.MEMBER_ID " +
    " WHERE f.REPORTING_DATE >= :startDate and f.REPORTING_DATE < :endDate " + 
    " GROUP BY to_char(f.REPORTING_DATE, 'YYYY'), to_char(f.REPORTING_DATE, 'MM'), m.MEM_NAME, m.MEM_CD", 
  nativeQuery = true
  )
  List<CumulativeDetailsDtoInterface> aggregateByStartDateEndDateMemberWise(LocalDateTime startDate, LocalDateTime endDate);

  @Query(value = "SELECT " + 
  " SUM(FUNDED_AMOUNT_BEGIN_DAY) as totalOutstandingForMonth, " +
  " SUM(FUNDED_AMOUNT_END_DAY) as netOutstandingExposures, " + 
  " to_char(f.REPORTING_DATE, 'YYYY') AS \"year\", to_char(f.REPORTING_DATE, 'MM') AS \"month\" " + 
  " FROM " + 
  "    TBL_MTR_DETAIL_RECORD r INNER JOIN TBL_MTR_DAILY_FILE f ON r.MTR_FILE_ID = f.id " + 
  "    INNER JOIN TBL_MEMBER_MASTER m ON m.MEM_ID = f.MEMBER_ID " +
  " WHERE to_char(f.REPORTING_DATE, 'DD-MM-YYYY') = :reportDate "+
  " GROUP BY to_char(f.REPORTING_DATE, 'YYYY'), to_char(f.REPORTING_DATE, 'MM')", 
nativeQuery = true
)
  List<CumulativeDetailsDtoInterface> aggregateByReportDate(String reportDate);

  @Query(value = "SELECT " + 
  " m.MEM_NAME as memberName, m.MEM_CD as memberCode, " + 
  " SUM(FUNDED_AMOUNT_BEGIN_DAY) as totalOutstandingForMonth, " +
  " SUM(FUNDED_AMOUNT_END_DAY) as netOutstandingExposures, " + 
  " to_char(f.REPORTING_DATE, 'YYYY') AS \"year\", to_char(f.REPORTING_DATE, 'MM') AS \"month\" " + 
  " FROM " + 
  "    TBL_MTR_DETAIL_RECORD r INNER JOIN TBL_MTR_DAILY_FILE f ON r.MTR_FILE_ID = f.id " + 
  "    INNER JOIN TBL_MEMBER_MASTER m ON m.MEM_ID = f.MEMBER_ID " +
  " WHERE to_char(f.REPORTING_DATE, 'DD-MM-YYYY') = :reportDate "+
  " GROUP BY to_char(f.REPORTING_DATE, 'YYYY'), to_char(f.REPORTING_DATE, 'MM'), m.MEM_NAME, m.MEM_CD", 
nativeQuery = true
)
  List<CumulativeDetailsDtoInterface> aggregateByReportDateMemberWise(String reportDate);

  String SQL_LEVERAGE_MAX_ALLOWABLE_EXPOSURE_REPORT =
    "SELECT a.totalBorrowedFunds AS totalBorrowedFunds, " +
      "a.dailyFileId AS dailyFileId, " +
      "a.memberName AS memberName, " +
      "a.memberCode as memberCode, " +
      "a.submissionDate AS submissionDate," +
      "b.maxAllowableExposure as maxAllowableExposure " +
      "FROM (SELECT SUM(r.AMOUNT_FUNDED) AS totalBorrowedFunds," +
      "     f.id as dailyFileId, " +
      "     MIN(m.MEM_NAME) AS memberName, MIN(m.MEM_CD) AS memberCode," +
      "     MIN(f.REPORTING_DATE) AS submissionDate" +
      " FROM" +
      "    TBL_MTR_SUMMARY_RECORD r " +
      "    INNER JOIN TBL_MTR_DAILY_FILE f ON f.id = r.MTR_FILE_ID " +
      "    INNER JOIN TBL_MEMBER_MASTER m on f.MEMBER_ID = m.MEM_ID" +
      " WHERE r.LENDER_CATEGORY != 1 " +
      "   AND f.REPORTING_DATE >= TO_DATE(:startReportDate, 'DD-MM-YYYY') AND f.REPORTING_DATE < TO_DATE(:endReportDate, 'DD-MM-YYYY')" +
      " GROUP BY f.id ) a " +
      "INNER JOIN " +
      "( SELECT SUM(r.FUNDED_AMOUNT_END_DAY) AS maxAllowableExposure," +
      "     f.id as dailyFileId, " +
      "     MIN(m.MEM_NAME) AS memberName, MIN(m.MEM_CD) AS memberCode," +
      "     MIN(f.REPORTING_DATE) AS submissionDate" +
      " FROM" +
      "    TBL_MTR_DETAIL_RECORD r " +
      "    INNER JOIN TBL_MTR_DAILY_FILE f ON f.id = r.MTR_FILE_ID " +
      "    INNER JOIN TBL_MEMBER_MASTER m on f.MEMBER_ID = m.MEM_ID" +
      " WHERE " +
      "   f.REPORTING_DATE >= TO_DATE(:startReportDate, 'DD-MM-YYYY') AND f.REPORTING_DATE < TO_DATE(:endReportDate, 'DD-MM-YYYY')" +
      " GROUP BY f.id ) b " +
      "ON a.dailyFileId = b.dailyFileId ";
  @Query(value = SQL_LEVERAGE_MAX_ALLOWABLE_EXPOSURE_REPORT, nativeQuery = true)
  List<MaxAllowableExposureDtoInterface> aggregateByReportDateGroupByDailyFileId(String startReportDate, String endReportDate);

  String SQL_LEVERAGE_MAX_CLIENT_ALLOWABLE_EXPOSURE_REPORT =
    "SELECT a.totalBorrowedFunds as totalBorrowedFunds, " +
      "b.exposureToClient as exposureToClient, " +
      "b.clientName as clientName, " +
      "b.clientPAN as clientPAN, " +
      "b.dailyFileId as dailyFileId, " +
      "b.memberName as memberName, " +
      "b.memberCode as memberCode, " +
      "b.submissionDate as submissionDate " +
      "FROM ( SELECT SUM(r.AMOUNT_FUNDED) AS totalBorrowedFunds," +
      "     f.id as dailyFileId, " +
      "     MIN(m.MEM_NAME) AS memberName, MIN(m.MEM_CD) AS memberCode," +
      "     MIN(f.REPORTING_DATE) AS submissionDate" +
      " FROM" +
      "    TBL_MTR_SUMMARY_RECORD r " +
      "    INNER JOIN TBL_MTR_DAILY_FILE f ON f.id = r.MTR_FILE_ID " +
      "    INNER JOIN TBL_MEMBER_MASTER m on f.MEMBER_ID = m.MEM_ID" +
      " WHERE r.LENDER_CATEGORY != 1 " +
      "   AND f.REPORTING_DATE >= TO_DATE(:startReportDate, 'DD-MM-YYYY') AND f.REPORTING_DATE < TO_DATE(:endReportDate, 'DD-MM-YYYY')" +
      " GROUP BY f.id ) a " +
      "INNER JOIN " +
      "( SELECT SUM(r.FUNDED_AMOUNT_DURING_DAY) AS exposureToClient," +
      "     r.CLIENT_NAME as clientName, r.PAN as clientPAN, " +
      "     f.id as dailyFileId, " +
      "     MIN(m.MEM_NAME) AS memberName, MIN(m.MEM_CD) AS memberCode," +
      "     MIN(f.REPORTING_DATE) AS submissionDate" +
      " FROM" +
      "    TBL_MTR_DETAIL_RECORD r " +
      "    INNER JOIN TBL_MTR_DAILY_FILE f ON f.id = r.MTR_FILE_ID " +
      "    INNER JOIN TBL_MEMBER_MASTER m on f.MEMBER_ID = m.MEM_ID" +
      " WHERE " +
      "   f.REPORTING_DATE >= TO_DATE(:startReportDate, 'DD-MM-YYYY') AND f.REPORTING_DATE < TO_DATE(:endReportDate, 'DD-MM-YYYY')" +
      " GROUP BY f.id, r.CLIENT_NAME, r.PAN ) b " +
      "ON a.dailyFileId = b.dailyFileId";
  @Query(value = SQL_LEVERAGE_MAX_CLIENT_ALLOWABLE_EXPOSURE_REPORT, nativeQuery = true)
  List<MaxClientAllowableExposureDtoInterface> aggregateByReportDateGroupByDailyFileIdAndClient(String startReportDate, String endReportDate);

}
