package com.nseindia.mc.repository;

import com.nseindia.mc.model.PenaltyMember;
import com.nseindia.mc.model.PenaltyType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PenaltyMemberRepository
    extends JpaRepository<PenaltyMember, Long>,
    JpaSpecificationExecutor<PenaltyMember> {

    List<PenaltyMember> findByPenaltyPenaltyTypeAndPenaltyPenaltyYearAndPenaltyPenaltyMonth(PenaltyType penaltyType, Integer year, Integer month);
    Optional<PenaltyMember> findByMember_MemIdAndPenalty_PenaltyYearAndPenalty_PenaltyMonth(
        Long memId, Integer year, Integer month);
    
    List<PenaltyMember> findByMemberMemIdAndPenaltyPenaltyType(long memberId, PenaltyType penaltyType);

    Optional<PenaltyMember> findByIdAndPenaltyPenaltyType(long penaltyMemberId, PenaltyType penaltyType);
}
