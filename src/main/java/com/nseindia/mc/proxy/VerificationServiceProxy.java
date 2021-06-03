package com.nseindia.mc.proxy;

import com.nseindia.mc.controller.dto.NameMatchingRequest;
import com.nseindia.mc.controller.dto.PanDataRequest;
import com.nseindia.mc.controller.dto.PanVerificationResponse;
import com.nseindia.mc.controller.dto.VerificationNameMatchingResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * The verification service service proxy.
 * Used to verify the pan and access name matching api
 */
@FeignClient(name = "verification-service")
public interface VerificationServiceProxy {

  /**
   * To verify the PAN detail.
   * 
   * @param PanVerificationPayload The pan verification payload
   * @return PanVerificationResponse
   */
  @PostMapping("/api/v1/verification/pan")
  ResponseEntity<PanVerificationResponse> verifyPan(@RequestBody PanDataRequest panDataRequest);

  /**
   * @param NameMatchingRequest
   * @return VerificationNameMatchingResponse
   */
    @PostMapping("/api/v1/verification/namematching")
    ResponseEntity<VerificationNameMatchingResponse> verifyNameMatching(@RequestBody NameMatchingRequest nameMatchingRequest);
}
