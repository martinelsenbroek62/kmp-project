package com.nseindia.mc.repository;

import java.util.List;
import java.util.Optional;

import com.nseindia.mc.model.ComplianceOfficer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/** The JpaRepository for the ComplianceOfficer Entity */
@Repository
public interface ComplianceOfficerRepository extends JpaRepository<ComplianceOfficer, Long> {
  /**
   * Gets the entity by the given pan
   * 
   * @param pan The pan for to search by
   * @return the matched entity
   */
  Optional<ComplianceOfficer> findByPan(String pan);

  /**
   * Gets the entity by the given member id and pan
   * @param memberId The member id to search by
   * @param pan The pan to search by
   * @return The optional entity
   */
  Optional<ComplianceOfficer> findByMemberMemIdAndPan(Long memberId, String pan);

  /**
   * Lists all Compliance officers that are not member KMPs
   * 
   * @param memberId The member id to search by
   * @return The list of Compliance officers that are not KMPs of the member
   */
  @Query(value = "SELECT * FROM TBL_COMPLIANCE_OFFICER_KMP tco WHERE tco.MEMBER_ID = ?1 AND tco.COMP_PAN_NO NOT IN (SELECT tk.PAN FROM TBL_KMP tk WHERE tk.MEM_ID = ?1 AND tk.\"ROLE\" = 'Compliance Officer' AND tk.DELETED_DT IS NULL)",
         nativeQuery = true)
  List<ComplianceOfficer> getNonKmpMemberComplianceOfficers(Long memberId);

  /**
   * Checks whether the Compliance Officer exists for the member id and comp status
   * 
   * @param memberId The member to check
   * @param compStatus The comp status to check
   * @return true if such compliance officer exists, false otherwise
   */
  boolean existsByMemberMemIdAndCompStatus(long memberId, int compStatus);
}
