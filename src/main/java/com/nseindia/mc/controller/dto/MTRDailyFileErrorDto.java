package com.nseindia.mc.controller.dto;

import lombok.Data;

import java.util.Map;

@Data
public class MTRDailyFileErrorDto {

	private String errorCode;

	private String errorMessage;

	private Map<String, Object> parameters;

}
