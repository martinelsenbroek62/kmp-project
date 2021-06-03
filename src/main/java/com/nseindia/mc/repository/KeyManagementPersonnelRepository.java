package com.nseindia.mc.repository;

import com.nseindia.mc.model.KeyManagementPersonnel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/** The JpaRepository for KeyManagementPersonnel */
@Repository
public interface KeyManagementPersonnelRepository
    extends JpaRepository<KeyManagementPersonnel, Long>, JpaSpecificationExecutor<KeyManagementPersonnel> {

  /**
   * Finds the KeyManagementPersonnel instances by trading member id and selected date Results are
   * sorted by action date in descending order
   *
   * @param tradingMemberId The trading member id to search with
   * @param selectedDate The selected date to searhc by
   * @return The list of KeyManagementPersonnel matching the search criteria
   */
  List<KeyManagementPersonnel> findByMemIdAndActionDateLessThanEqualOrderByActionDateDesc(
      Long tradingMemberId, LocalDateTime selectedDate);

  /**
   * Searches KeyManagementPersonnel instances by date range (action date)
   *
   * @param tradingMemberId The trading member id to search with
   * @param fromDate The from date to search with
   * @param toDate The to date search with
   * @return The list of KeyManagementPersonnel matching the search criteria
   */
  List<KeyManagementPersonnel> findByMemIdAndActionDateBetweenOrderByActionDateDesc(
      Long tradingMemberId, LocalDateTime fromDate, LocalDateTime toDate);

  /**
   * Searches the KMPs by trading member id
   *
   * @param tradingMemberId The trading member id to search with
   * @return The list of KeyManagementPersonnel matching the search criteria
   */
  List<KeyManagementPersonnel> findByMemIdOrderByActionDateDesc(Long tradingMemberId);

  /**
   * Finds the KMPs by id and trading member id
   *
   * @param id The id to search with
   * @param tradingMemberId The trading member id to search with
   * @return The Optional KeyManagementPersonnel matching the search criteria
   */
  Optional<KeyManagementPersonnel> findByIdAndMemId(Long id, Long tradingMemberId);

  /**
   * Search KMPs by pan number
   *
   * @param pan The PAN number to search by
   * @return The Optional KMP matching the input PAN number
   */
  List<KeyManagementPersonnel> findByPan(String pan);

  /**
   * Searches the KMPs by id and application id.
   *
   * @param id The id to search by
   * @param appId The application id to search with
   * @return The Optional KMP instance matching the search criteria
   */
  Optional<KeyManagementPersonnel> findByIdAndAppIdAndDeletedDateNull(Long id, Long appId);

  /**
   * Search KMP by member id and pan number
   *
   * @param memberId The member id to search by
   * @param pan The PAN number to search by
   * @return The Optional KMP matching the input PAN number
   */
  Optional<KeyManagementPersonnel> findByMemIdAndPan(Long memberId, String pan);


  /**
   * Finds the non deleted KeyManagementPersonnel instances by trading member id and selected date Results are
   * sorted by action date in descending order
   *
   * @param tradingMemberId The trading member id to search with
   * @param selectedDate The selected date to searhc by
   * @return The list of KeyManagementPersonnel matching the search criteria
   */
  List<KeyManagementPersonnel> findByMemIdAndActionDateLessThanEqualAndDeletedDateNullOrderByActionDateDesc(
      Long tradingMemberId, LocalDateTime selectedDate);

      
  /**
   * Searches non deleted KeyManagementPersonnel instances by date range (action date)
   *
   * @param tradingMemberId The trading member id to search with
   * @param fromDate The from date to search with
   * @param toDate The to date search with
   * @return The list of KeyManagementPersonnel matching the search criteria
   */
  List<KeyManagementPersonnel> findByMemIdAndDeletedDateNullAndActionDateBetweenOrderByActionDateDesc(
        Long tradingMemberId, LocalDateTime fromDate, LocalDateTime toDate);

  /**
   * Searches the non deleted KMPs by trading member id
   *
   * @param tradingMemberId The trading member id to search with
   * @return The list of KeyManagementPersonnel matching the search criteria
   */
  List<KeyManagementPersonnel> findByMemIdAndDeletedDateNullOrderByActionDateDesc(Long tradingMemberId);

  /**
   * Search non deleted KMP by member id and pan number
   *
   * @param memberId The member id to search by
   * @param pan The PAN number to search by
   * @return The Optional KMP matching the input PAN number
   */
  Optional<KeyManagementPersonnel> findByMemIdAndPanAndDeletedDateNull(Long memberId, String pan);

}
