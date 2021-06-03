package com.nseindia.mc.service.panvalidation;

import com.nseindia.mc.constants.ServiceConstants;
import com.nseindia.mc.controller.dto.PanDataRequest;
import com.nseindia.mc.controller.dto.PanPayload;
import com.nseindia.mc.controller.dto.PanVerificationResponse;
import com.nseindia.mc.exception.BaseServiceException;
import com.nseindia.mc.proxy.VerificationServiceProxy;
import com.nseindia.mc.util.CommonUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

/**
 * The pan validation service, it uses VerificationServiceProxy to interact with external verification service
 */
@Service
public class PanValidationService {

  /**
   * The verification service proxy
   */
  @Autowired
  VerificationServiceProxy verificationServiceProxy;

  /**
   * Performs the pan validation externally for the given customer id and pan
   * 
   * @param customerId The customer id
   * @param pan The pan value
   * @return The PanVerificationResponse instance
   */
  @Retryable(value = {BaseServiceException.class}, maxAttempts = 3)
  public PanVerificationResponse validatePan(String customerId, String pan) {
    return (PanVerificationResponse) CommonUtils.handleServiceErrors(verificationServiceProxy
                                                   .verifyPan(PanDataRequest.builder()
                                                              .panPayload(PanPayload.builder()
                                                              .panNumber(pan)
                                                              .customerId(customerId)
                                                              .build()).build()), ServiceConstants.PAN_EXTERNAL_VALIDATION_ERROR_MESSAGE);
    
  }
}
