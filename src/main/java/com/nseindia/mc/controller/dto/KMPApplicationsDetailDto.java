package com.nseindia.mc.controller.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
@Getter
@Setter
public class KMPApplicationsDetailDto {
	private Long appId;
	private String memberName;
	private Long memberCode;
	private String memberType;
	private String requestType;
	private String status;
	private LocalDateTime applicationStartedOn;
	private LocalDateTime applicationSubmittedOn;
	private String remarks;
}
