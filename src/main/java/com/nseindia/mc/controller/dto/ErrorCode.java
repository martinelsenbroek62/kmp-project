package com.nseindia.mc.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorCode {

	private String code;
	private String message;
	private Map<String, Object> parameters;
}
