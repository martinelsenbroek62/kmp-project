package com.nseindia.mc.controller.dto;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LineValidationResult {
	
	private String line;
	
	private String type;
	
	private List<String> fields;

	private List<ErrorCode> errors = new ArrayList<>();
	private Set<String> errorCodes = new HashSet<>();

	public LineValidationResult(String line, String type, List<String> fields) {
		this.line = line;
		this.type = type;
		this.fields = fields;
	}

	public void addError(String code, Object... parameters) {
		ErrorCode errorCode = new ErrorCode();
		errorCode.setCode(code);
		Map<String, Object> params = new HashMap<>();
		for (int i = 0; i < parameters.length / 2; i++) {
			params.put(parameters[2 * i].toString(), parameters[2 * i + 1]);
		}
		errorCode.setParameters(params);
		this.errors.add(errorCode);
		this.errorCodes.add(errorCode.getCode());
	}
}
