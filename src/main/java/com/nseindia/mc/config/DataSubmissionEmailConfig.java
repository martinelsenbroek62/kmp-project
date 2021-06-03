package com.nseindia.mc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "email.data-submission.mail")
public class DataSubmissionEmailConfig {
  private String from;
  private String subject;
  private String body;
}
