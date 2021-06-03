package com.nseindia.mc.repository;

import com.nseindia.mc.model.PenaltyDocMaster;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PenaltyDocRepository
    extends JpaRepository<PenaltyDocMaster, Long>,
        JpaSpecificationExecutor<PenaltyDocMaster> {

  List<PenaltyDocMaster>
      findByPenalty_PenaltyYearAndPenalty_penaltyMonthAndPenaltyDocTypeName(
          int year, int month, String typeName);
}
