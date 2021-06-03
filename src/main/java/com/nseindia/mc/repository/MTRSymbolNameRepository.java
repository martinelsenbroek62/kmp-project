package com.nseindia.mc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.nseindia.mc.model.MTRSymbolName;

public interface MTRSymbolNameRepository extends JpaRepository<MTRSymbolName, String>,
  JpaSpecificationExecutor<MTRSymbolName> {

}
