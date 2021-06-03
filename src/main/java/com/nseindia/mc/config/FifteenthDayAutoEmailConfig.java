package com.nseindia.mc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "email.fifteenth-business-day.mail")
public class FifteenthDayAutoEmailConfig {
	
	private String from;
	private String subject;
	private String body;
	private String to;

}
