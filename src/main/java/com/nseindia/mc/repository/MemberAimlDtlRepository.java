package com.nseindia.mc.repository;

import com.nseindia.mc.model.MemberAimlDtl;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** The spring data repository for storing the member aiml dtl entity */
public interface MemberAimlDtlRepository
    extends JpaRepository<MemberAimlDtl, Long>, JpaSpecificationExecutor {

  /**
   * Find member aiml by memberId and quarter.
   *
   * @param memId
   * @param quarterName
   * @return an optional of MemberAimlDtl
   */
  Optional<MemberAimlDtl> findByMemIdAndQuarterName(final long memId, final String quarterName);

  /**
   * Find list of member aiml by memberId.
   *
   * @param memId
   * @return the list of member aiml
   */
  List<MemberAimlDtl> findByMemId(final long memId);

  /**
   * Find list of member aiml by submissionDone.
   *
   * @param submissionDone
   * @return the list of member aiml
   */
  List<MemberAimlDtl> findBySubmissionDone(final String submissionDone);

  /**
   * Find list of member aiml by eligibleFlag.
   *
   * @param eligibleFlag
   * @return the list of member aiml
   */
  List<MemberAimlDtl> findByEligibleFlagAndQuarterName(
      final String eligibleFlag, final String quarterName);
  
  List<MemberAimlDtl> findByNilFlagAndQuarterName(
	      final String nilFlag, final String quarterName);
  
  List<MemberAimlDtl> findByNilFlagAndSubmissionDoneAndQuarterName(
	      final String nilFlag,final String submissionDone, final String quarterName);
}
