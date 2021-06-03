package com.nseindia.mc.repository;

import com.nseindia.mc.model.Director;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/** The JpaRepository for the Director Entity */
@Repository
public interface DirectorRepository extends JpaRepository<Director, Long> {

  /**
   * Gets the entity by the given pan
   * 
   * @param pan The pan for to search by
   * @return the matched entity
   */
  Optional<Director> findByPan(String pan);

  /**
   * Gets the entity by the given member id and pan
   * @param memberId The member id to search by
   * @param pan The pan to search by
   * @return The optional entity
   */
  Optional<Director> findByMemberMemIdAndPan(Long memberId, String pan);

  /**
   * Lists all directors that are not member KMPs
   * 
   * @param memberId The member id to search by
   * @return The list of Directors that are not KMPs of the member
   */
  @Query(value = "SELECT * FROM TBL_DIRECTOR_KMP td WHERE td.MEM_ID = ?1 AND td.PAN_NUMBER NOT IN (SELECT tk.PAN FROM TBL_KMP tk WHERE tk.MEM_ID = ?1 AND tk.\"ROLE\" = 'Director' AND tk.DELETED_DT IS NULL)",
         nativeQuery = true)
  List<Director> getNonKmpMemberDirectors(Long memberId);
}
