package com.nseindia.mc.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import com.nseindia.mc.model.MTRControlRecord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MTRControlRecordRepository
    extends JpaRepository<MTRControlRecord, Long>, JpaSpecificationExecutor<MTRControlRecord> {

  Optional<MTRControlRecord> findTopByMtrFile_IdOrderByMtrFile_ReportingDateDesc(Long mtrFileId);

  @Query(
      "SELECT coalesce(SUM(cr.totalAmountFunded), 0) FROM MTRControlRecord cr WHERE cr.mtrFile.reportingDate = :reportingDate")
  Double sumTotalAmountFunded(LocalDateTime reportingDate);

  @Modifying
  @Query("delete from MTRControlRecord r where r.mtrFile.id=:mtrFileId")
  void deleteByMTRDailyFileId(Long mtrFileId);
}
