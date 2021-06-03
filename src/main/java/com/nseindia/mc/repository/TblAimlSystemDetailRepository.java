package com.nseindia.mc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.nseindia.mc.model.TblAimlSystemDetail;

/** The spring data repository for storing the system detail entity */
public interface TblAimlSystemDetailRepository extends JpaRepository<TblAimlSystemDetail, String>,JpaSpecificationExecutor {
  /**
   * Find aiml system detail by member id.
   *
   * @param memberId
   * @return list of TblAimlSystemDetail
   */
  List<TblAimlSystemDetail> findByMemberId(final long memberId);
  
  List<TblAimlSystemDetail> findByMemberIdAndQuarterName(final long member,final String quater);

  /**
   * Find aiml system detail by list of member id.
   *
   * @param memberIds
   * @return list of TblAimlSystemDetail
   */
  List<TblAimlSystemDetail> findByMemberIdIn(final List<Long> memberIds);

  /**
   * Delete aiml system detail by member id.
   *
   * @param member
   */
  void deleteByMemberId(final long member);
  
  void deleteByMemberIdAndQuarterName(final long member,final String quater);

  
  
}
