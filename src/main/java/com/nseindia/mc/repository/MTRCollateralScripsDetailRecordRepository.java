package com.nseindia.mc.repository;

import com.nseindia.mc.model.MTRCollateralsScripsDetailRecord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MTRCollateralScripsDetailRecordRepository
    extends JpaRepository<MTRCollateralsScripsDetailRecord, Long> {
        @Modifying
        @Query("delete from MTRCollateralsScripsDetailRecord r where r.mtrFile.id=:mtrFileId")
        void deleteByMTRDailyFileId(Long mtrFileId);
    }
