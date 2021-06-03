package com.nseindia.mc.repository;

import com.nseindia.mc.model.MTRHoMaker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MTRHoMakerRepository
    extends JpaRepository<MTRHoMaker, Long>, JpaSpecificationExecutor<MTRHoMaker> {

    Optional<MTRHoMaker> findFirstByMakerStatus(boolean status);
}
