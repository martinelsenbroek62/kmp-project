package com.nseindia.mc.controller.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import lombok.Data;

@Data
public class DroolsEnv implements Serializable {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -8287464533083572951L;

	private String memberCode;
	private String batchDate;
	private Integer batchNum;
	private Integer summaryCount;
	private Integer detailCount;
	private Double summaryAmount;
	private Set<String> validSymbolCodes;
	private Double scriptAmount;
	private Map<String, MTRDetailRecordDtoInterface> records;
	private LocalDate previousDate;
	private LocalDate currentDate;
}
