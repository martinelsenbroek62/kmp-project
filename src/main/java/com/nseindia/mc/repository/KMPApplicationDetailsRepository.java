package com.nseindia.mc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nseindia.mc.model.KMPApplicationDetails;

/** The JpaRepository for the KmpApplicationDetail Entity */
@Repository
public interface KMPApplicationDetailsRepository extends JpaRepository<KMPApplicationDetails, Long>{

	List<KMPApplicationDetails> findByMemberNameMemberCodeAndMemberType(String memberName, String memberCode, String memberType);
}
