package com.nseindia.mc.service.namematching;

import java.util.List;

import com.nseindia.mc.constants.ServiceConstants;
import com.nseindia.mc.controller.dto.NameMatchingPayload;
import com.nseindia.mc.controller.dto.NameMatchingRequest;
import com.nseindia.mc.controller.dto.ScoreResponse;
import com.nseindia.mc.controller.dto.VerificationNameMatchingResponse;
import com.nseindia.mc.exception.BaseServiceException;
import com.nseindia.mc.model.VerificationDetails;
import com.nseindia.mc.proxy.VerificationServiceProxy;
import com.nseindia.mc.repository.VerificationDetailsRepository;
import com.nseindia.mc.util.CommonUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

/**
 * The name matching service, it uses VerificationServiceProxy to interact with external verification service
 */
@Service
public class NameMatchingService {

  /**
   * The verification service proxy
   */
  @Autowired
  VerificationServiceProxy verificationServiceProxy;

  /**
   * The Verification details repository
   */
  @Autowired
  VerificationDetailsRepository verificationDetailsRepository;

  /**
   * Performs the name matching for the given customer id, nameFromResponse and entered name
   * 
   * @param customerId The customer id
   * @param pan The pan value
   * @param nameFromResponse The name from the PAN validation service
   * @param enteredName The name entered by the user
   * @return The flag indicating whether the names match or not based on the minimum confidence score
   */
  @Retryable(value = {BaseServiceException.class}, maxAttempts = 3)
  public boolean verifyNameMatching(String customerId, String pan, String nameFromResponse, String enteredName) {
    NameMatchingRequest nameMatchingRequest = NameMatchingRequest.builder()
       .nameMatchingPayload(NameMatchingPayload.builder()
          .customerId(customerId)
          .name1(nameFromResponse)
          .name2(enteredName).build())
       .build();
    
    // verify the name matching by calling the external service
    VerificationNameMatchingResponse verificationNameMatchingResponse = 
       (VerificationNameMatchingResponse) CommonUtils
                   .handleServiceErrors(verificationServiceProxy.verifyNameMatching(nameMatchingRequest),
                                        ServiceConstants.EXTERNAL_NAME_MATCHING_VERIFICATION_ERROR_MESSAGE);

    // The status code is not 200-OK, this means that the the names do not match
    if(!verificationNameMatchingResponse.getStatusCode().equals(String.format("%d",HttpStatus.OK.value()))) {
      return nameFromResponse != null && nameFromResponse.equals(enteredName);
    }

    boolean panNameMatch = true;

    List<ScoreResponse> scores = verificationNameMatchingResponse.getNameMatchingResponse().getScores();
        // If any score of any of the matching algorithms is less than the minimum required, then names do not match
        if (scores != null && !scores.isEmpty()) {
          for(ScoreResponse score : scores) {
            double parseDouble = Double.parseDouble(score.getScore());
            if (parseDouble < ServiceConstants.NAME_MATCHING_MINIMUM_SCORE ) {
              panNameMatch = false;
              break;
            }
          }
        }

        // If the names match, create the verification details entity
        if(panNameMatch) {
          VerificationDetails verificationDetails = new VerificationDetails();
          verificationDetails.setName(nameFromResponse);
          verificationDetails.setVerifyType(ServiceConstants.PAN);
          verificationDetails.setVerifyEntity(pan);
          verificationDetails.setVerifiedFlag(true);
          verificationDetails.setVerifiedStatus(ServiceConstants.SUCCESS);
          verificationDetails.setReason(ServiceConstants.SUCCESS);
          verificationDetailsRepository.save(verificationDetails);
        }
        return panNameMatch;
  }
}
