package com.nseindia.mc.repository;

import com.nseindia.mc.model.MTRDailyJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Optional;

public interface MTRDailyJobStatusRepository
  extends JpaRepository<MTRDailyJobStatus, Long>, JpaSpecificationExecutor<MTRDailyJobStatus> {

  Optional<MTRDailyJobStatus> findByReportingDate(
    LocalDate reportingDate);
}
