package com.nseindia.mc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "email.ibes.mail")
public class IBESReportEmailConfig {
  private String from;
  private String subject;
  private String to;
  private String body;
  private String fileExtension;
}
