package com.nseindia.mc.repository;

import com.nseindia.mc.model.MTRMemberList;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberListRepository extends JpaRepository<MTRMemberList, Long> {

  Optional<MTRMemberList> findTopByMember_MemId(Long memberId);

  Optional<MTRMemberList> findTopByMember_MemNameOrMember_MemCd(
      String memberName, String memberCode);

  List<MTRMemberList> findByEligibleMemberMtrStatus(Boolean eligible);

  String SQL_FIND_WITHDRAWAL_BETWEEN = "select * " +
      "from TBL_MTR_MEMBER_LIST " +
      "where  WITHDRAWAL_REASON = :withdrawal " +
      "and ((COOLING_PERIOD_START_DATE >= TO_DATE(:startDate, 'DD-MM-YYYY') and COOLING_PERIOD_START_DATE <= TO_DATE(:endDate, 'DD-MM-YYYY')) " +
    "or (COOLING_PERIOD_END_DATE >= TO_DATE(:startDate, 'DD-MM-YYYY') and COOLING_PERIOD_START_DATE < TO_DATE(:startDate, 'DD-MM-YYYY')) )";
  @Query(value = SQL_FIND_WITHDRAWAL_BETWEEN,
      nativeQuery = true
  )
  List<MTRMemberList> findByWithdrawalReasonAndCoolingPeriodStartDateBetween(
      Boolean withdrawal, String startDate, String endDate);

  Integer countByEligibleMemberMtrStatusTrue();
}
