package com.nseindia.mc.repository;

import com.nseindia.mc.model.MTRMonthlyJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MTRMonthlyJobStatusRepository
  extends JpaRepository<MTRMonthlyJobStatus, Long>, JpaSpecificationExecutor<MTRMonthlyJobStatus> {

  Optional<MTRMonthlyJobStatus> findByJobYearAndJobMonthAndJobType(int jobYear, int jobMonth, String jobType);
}
