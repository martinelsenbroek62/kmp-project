package com.nseindia.mc.repository;

import com.nseindia.mc.model.VerificationDetails;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Verification Details.
 */

@Repository
public interface VerificationDetailsRepository extends JpaRepository<VerificationDetails, Long> {
}