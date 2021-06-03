package com.nseindia.mc.controller.dto;

import lombok.Data;

/** The data transfer object for KMP static configuration values */
@Data
public class KmpConfigDataDto {
  /** The KMP guidelines */
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

  /** The email info tip */
  private String emailIdInfoTip;

  /** The exchange text info tip */
  private String exchangeText;
}
