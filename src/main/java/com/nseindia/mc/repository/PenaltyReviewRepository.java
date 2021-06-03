package com.nseindia.mc.repository;

import com.nseindia.mc.model.Penalty;
import com.nseindia.mc.model.PenaltyReview;
import com.nseindia.mc.model.PenaltyType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PenaltyReviewRepository
    extends JpaRepository<PenaltyReview, Long>,
    JpaSpecificationExecutor<PenaltyReview> {

    Optional<PenaltyReview> findFirstByPenaltyPenaltyTypeAndPenaltyPenaltyYearAndPenaltyPenaltyMonth(PenaltyType penaltyType, int year, int month);

    Optional<PenaltyReview> findFirstByPenalty(Penalty penalty);
}
