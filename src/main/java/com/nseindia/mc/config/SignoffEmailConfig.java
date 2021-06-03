package com.nseindia.mc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tasks.signoff.mail")
public class SignoffEmailConfig {
  private String from;
  private String subject;
  private String body;
  private String to;
}
