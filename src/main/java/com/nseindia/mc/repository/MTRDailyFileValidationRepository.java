package com.nseindia.mc.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import com.nseindia.mc.model.MTRDailyFileValidation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MTRDailyFileValidationRepository
    extends JpaRepository<MTRDailyFileValidation, Long>, JpaSpecificationExecutor<MTRDailyFileValidation> {
    Optional<MTRDailyFileValidation> findFirstByMember_MemIdAndReportingDateOrderByBatchNoDesc(Long memberId, LocalDateTime reportingDate);
}
