package com.nseindia.mc.repository;

import com.nseindia.mc.model.UserMemCom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserMemComRepository
    extends JpaRepository<UserMemCom, Long>, JpaSpecificationExecutor<UserMemCom> {}
