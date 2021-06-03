package com.nseindia.mc.repository;

import com.nseindia.mc.model.MTRCollateralsSummaryRecord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MTRCollateralSummaryRecordRepository
    extends JpaRepository<MTRCollateralsSummaryRecord, Long> {
        @Modifying
        @Query("delete from MTRCollateralsSummaryRecord r where r.mtrFile.id=:mtrFileId")
        void deleteByMTRDailyFileId(Long mtrFileId);
    }
