package com.nseindia.mc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * The KMP configuration data. It holds the static configuration data as guidelines, infotips and
 * exchange text.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "kmp.config")
public class KmpConfigData {
  /** The configured guidelines */
  private String guidelines;

  /** The Corporate/Bank info tip for existing role */
  private String corporateBankExistingRoleInfoTip;

  /** The LLP/Partnership/Individual info tip for existing role */
  private String llpPartnershipIndividualInfoTip;

  /** The name of KMP info tip */
  private String nameOfKmpInfoTip;

  /** The declaration date info tip */
  private String dateOfDeclarationInfoTip;

  /** The additional designation info tip */
  private String additionalDesignationInfoTip;

  /** The mobile number info tip */
  private String mobileNumberInfoTip;

  /** The email id info tip */
  private String emailIdInfoTip;

  /** The exchange text info tip */
  private String exchangeText;
}
