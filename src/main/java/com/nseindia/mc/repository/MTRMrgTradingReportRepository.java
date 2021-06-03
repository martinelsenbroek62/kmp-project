package com.nseindia.mc.repository;

import com.nseindia.mc.model.MTRMrgTradingReport;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MTRMrgTradingReportRepository
    extends JpaRepository<MTRMrgTradingReport, Long>,
        JpaSpecificationExecutor<MTRMrgTradingReport> {
  Optional<MTRMrgTradingReport> findByReportingDate(LocalDate reportingDate);

  List<MTRMrgTradingReport> findByReportingDateIn(Set<LocalDate> dates);

  List<MTRMrgTradingReport> findByMtrCounterDate(LocalDate counterDate);
}
