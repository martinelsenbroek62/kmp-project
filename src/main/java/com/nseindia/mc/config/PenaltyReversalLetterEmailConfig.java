package com.nseindia.mc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "penalty.mail.penalty-reversal-letter")
public class PenaltyReversalLetterEmailConfig {
  private String from;
  private String subject;
  private String body;
  private String to;
}
