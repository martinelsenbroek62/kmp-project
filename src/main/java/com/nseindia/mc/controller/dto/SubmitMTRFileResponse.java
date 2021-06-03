package com.nseindia.mc.controller.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SubmitMTRFileResponse {
	
	private int status;
	private String message;
	
	private LocalDateTime submittedDate;
	private LocalDateTime reportingDate;
}
