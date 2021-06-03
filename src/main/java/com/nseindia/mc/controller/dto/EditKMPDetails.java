package com.nseindia.mc.controller.dto;

import java.time.LocalDateTime;

import com.sun.istack.NotNull;

import lombok.Data;

@Data
public class EditKMPDetails {
	@NotNull private Long appId;
	private String memberName;
	private Long memberCode;
	private String memberType;
	private String requestType;
	private String status;
	private LocalDateTime applicationStartedOn;
	private LocalDateTime applicationSubmittedOn;
	private String remarks;
}
