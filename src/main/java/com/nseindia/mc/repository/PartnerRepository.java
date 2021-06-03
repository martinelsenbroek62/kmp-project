package com.nseindia.mc.repository;

import java.util.List;
import java.util.Optional;

import com.nseindia.mc.model.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/** The JpaRepository for the Partner Entity */
@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {
  /**
   * Gets the entity by the given pan
   * 
   * @param pan The pan for to search by
   * @return the matched entity
   */
  Optional<Partner> findByPan(String pan);

  /**
   * Gets the entity by the given member id and pan
   * @param memberId The member id to search by
   * @param pan The pan to search by
   * @return The optional entity
   */
  Optional<Partner> findByMemberIdAndPan(Long memberId, String pan);

  /**
   * Lists all Partners that are not member KMPs
   * 
   * @param memberId The member id to search by
   * @return The list of partners that are not KMPs of the member
   */
  @Query(value = "SELECT * FROM TBL_PARTNER tp WHERE tp.MEM_ID = ?1 AND tp.PAN_NUMBER NOT IN (SELECT tk.PAN FROM TBL_KMP tk WHERE tk.MEM_ID = ?1 AND tk.\"ROLE\" = 'Partner' AND tk.DELETED_DT IS NULL)",
         nativeQuery = true)
  List<Partner> getNonKmpMemberPartners(Long memberId);
}
