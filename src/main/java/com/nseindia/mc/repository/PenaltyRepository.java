package com.nseindia.mc.repository;

import com.nseindia.mc.model.Penalty;
import com.nseindia.mc.model.PenaltyType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PenaltyRepository
    extends JpaRepository<Penalty, Long>,
    JpaSpecificationExecutor<Penalty> {

    Optional<Penalty> findFirstByPenaltyTypeAndPenaltyYearAndPenaltyMonth(PenaltyType penaltyType, int year, int month);

    List<Penalty> findByPenaltyType(PenaltyType penaltyType);

    @Transactional
    @Modifying
    @Query(value = "update TBL_PENALTY set HO_MAKER_ID=:hoMakerId where HO_MAKER_ID is null", nativeQuery = true)
    void updateUnassignedPenaltyWithMaker(Long hoMakerId);
}
