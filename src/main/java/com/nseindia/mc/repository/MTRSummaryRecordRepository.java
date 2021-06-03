package com.nseindia.mc.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.nseindia.mc.controller.dto.LenderWiseExposureDtoInterface;
import com.nseindia.mc.controller.dto.TotalIndeptnessDtoInterface;
import com.nseindia.mc.model.MTRSummaryRecord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MTRSummaryRecordRepository extends JpaRepository<MTRSummaryRecord, Long> {
    @Modifying
    @Query("delete from MTRSummaryRecord r where r.mtrFile.id=:mtrFileId")
    void deleteByMTRDailyFileId(Long mtrFileId);

    String SQL_LEVERAGE_TOTAL_INDEBTEDNESS_REPORT =
      " SELECT SUM(r.AMOUNT_FUNDED) AS totalBorrowedFunds," +
        "     f.id as dailyFileId, " +
        "     MIN(m.MEM_NAME) AS memberName, MIN(m.MEM_CD) AS memberCode," +
        "     MIN(f.REPORTING_DATE) AS submissionDate" +
        " FROM" +
        "    TBL_MTR_SUMMARY_RECORD r " +
        "    INNER JOIN TBL_MTR_DAILY_FILE f ON f.id = r.MTR_FILE_ID " +
        "    INNER JOIN TBL_MEMBER_MASTER m on f.MEMBER_ID = m.MEM_ID" +
        " WHERE r.LENDER_CATEGORY != 1 " +
        "   AND f.REPORTING_DATE >= TO_DATE(:startReportDate, 'DD-MM-YYYY') AND f.REPORTING_DATE < TO_DATE(:endReportDate, 'DD-MM-YYYY')" +
        " GROUP BY f.id ";
    @Query(value = SQL_LEVERAGE_TOTAL_INDEBTEDNESS_REPORT, nativeQuery = true)
    List<TotalIndeptnessDtoInterface> aggregateByReportDate(String startReportDate, String endReportDate);

  String SQL_LEVERAGE_LENDER_WISE_EXPOSURE_REPORT =
    "SELECT a.totalBorrowedFunds as totalBorrowedFunds, " +
      "a.dailyFileId as dailyFileId, " +
      "a.lenderCategory as lenderCategory, " +
      "a.memberName as memberName, " +
      "a.memberCode as memberCode, " +
      "a.submissionDate as submissionDate, " +
      "b.totalBorrowedFunds as totalBorrowedFundsOnPreviousDay " +
      "FROM ( SELECT SUM(r.AMOUNT_FUNDED) AS totalBorrowedFunds," +
      "     f.id as dailyFileId, " +
      "     r.LENDER_CATEGORY as lenderCategory, " +
      "     MIN(m.MEM_NAME) AS memberName, m.MEM_CD AS memberCode," +
      "     f.REPORTING_DATE AS submissionDate, " +
      "     row_number() over (Partition By m.MEM_CD order by m.MEM_CD, f.REPORTING_DATE) as seqnum " +
      " FROM" +
      "    TBL_MTR_SUMMARY_RECORD r " +
      "    INNER JOIN TBL_MTR_DAILY_FILE f ON f.id = r.MTR_FILE_ID " +
      "    INNER JOIN TBL_MEMBER_MASTER m on f.MEMBER_ID = m.MEM_ID" +
      " WHERE r.LENDER_CATEGORY != 1 " +
      "   AND f.REPORTING_DATE >= TO_DATE(:previousStartReportDate, 'DD-MM-YYYY') AND f.REPORTING_DATE <= TO_DATE(:endReportDate, 'DD-MM-YYYY')" +
      " GROUP BY f.id, r.LENDER_CATEGORY, f.REPORTING_DATE, m.MEM_CD) a " +
      "LEFT JOIN " +
      "( SELECT SUM(r.AMOUNT_FUNDED) AS totalBorrowedFunds," +
      "     f.id as dailyFileId, " +
      "     r.LENDER_CATEGORY as lenderCategory, " +
      "     MIN(m.MEM_NAME) AS memberName, m.MEM_CD AS memberCode," +
      "     f.REPORTING_DATE AS submissionDate, " +
      "     row_number() over (Partition By m.MEM_CD order by m.MEM_CD, f.REPORTING_DATE) as seqnum " +
      " FROM" +
      "    TBL_MTR_SUMMARY_RECORD r " +
      "    INNER JOIN TBL_MTR_DAILY_FILE f ON f.id = r.MTR_FILE_ID " +
      "    INNER JOIN TBL_MEMBER_MASTER m on f.MEMBER_ID = m.MEM_ID" +
      " WHERE r.LENDER_CATEGORY != 1 " +
      "   AND f.REPORTING_DATE >= TO_DATE(:previousStartReportDate, 'DD-MM-YYYY') AND f.REPORTING_DATE <= TO_DATE(:endReportDate, 'DD-MM-YYYY')" +
      " GROUP BY f.id, r.LENDER_CATEGORY, f.REPORTING_DATE, m.MEM_CD ) b " +
      "ON a.memberCode = b.memberCode AND a.seqnum = b.seqnum + 1 AND a.lenderCategory = b.lenderCategory " +
      "where a.submissionDate >= TO_DATE(:startReportDate, 'DD-MM-YYYY') ";
    @Query(value = SQL_LEVERAGE_LENDER_WISE_EXPOSURE_REPORT, nativeQuery = true)
    List<LenderWiseExposureDtoInterface> aggregateByReportDateGroupByLenderCategory(
      String startReportDate, String endReportDate, String previousStartReportDate);
}
