package com.nseindia.mc.controller.kmp;

import com.nseindia.mc.controller.dto.AddKMPDetails;
import com.nseindia.mc.controller.dto.CommonMessageDto;
import com.nseindia.mc.controller.dto.CreateKMPRequestBody;
import com.nseindia.mc.controller.dto.DeleteKMPDetailsRequestBody;
import com.nseindia.mc.controller.dto.EditKMPDetails;
import com.nseindia.mc.controller.dto.KMPApplicationsDetailDto;
import com.nseindia.mc.controller.dto.KeyManagementPersonnelDto;
import com.nseindia.mc.controller.dto.KmpConfigDataDto;
import com.nseindia.mc.controller.dto.OtpSendResponse;
import com.nseindia.mc.controller.dto.PanValidationResponse;
import com.nseindia.mc.controller.dto.PanValidationStatus;
import com.nseindia.mc.controller.dto.RoleBasedKMPDataDto;
import com.nseindia.mc.controller.dto.UpdateKMPsRequestBody;
import com.nseindia.mc.model.KMPAction;
import com.nseindia.mc.service.kmp.KMPService;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nseindia.mc.controller.dto.CommonMessageDto;
import com.nseindia.mc.controller.dto.CreateKMPRequestBody;
import com.nseindia.mc.controller.dto.DeleteKMPDetailsRequestBody;
import com.nseindia.mc.controller.dto.KeyManagementPersonnelDto;
import com.nseindia.mc.controller.dto.KmpConfigDataDto;
import com.nseindia.mc.controller.dto.PanValidationResponse;
import com.nseindia.mc.controller.dto.PanValidationStatus;
import com.nseindia.mc.controller.dto.RoleBasedKMPDataDto;
import com.nseindia.mc.controller.dto.UpdateKMPsRequestBody;
import com.nseindia.mc.service.kmp.KMPService;

import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

/** This controller handles the requests related to KMP management */
@RestController
@RequestMapping("/mc-kmp/v1") 
@CrossOrigin(origins = "*", allowedHeaders = "*") 
public class KMPController {

  /** The KMP service to use to manage KMP data in the backend */
  @Autowired private KMPService service;

  /**
   * Searches KMPs based on the specified search criteria Can search all KMPs without filters Or
   * search KMPs as on selected date Or search KMPs audit trail with date range
   *
   * @param tradingMemberId The trading member id
   * @param selectedDateStr The string representation of the selected date
   * @param fromDateStr The date lower bound to search audit trail from
   * @param toDateStr The date upper bound to search audit trail to
   * @param pan The pan by which to get the KMP members
   * @param includeDeleted The flag indicating whether to include the deleted KMPs in the response
   * @return The ResponseEntity instance with the List of KMPs as body
   */
  @GetMapping("/kmpMembers")
  public ResponseEntity<List<KeyManagementPersonnelDto>> listKmpMembers(
      @RequestHeader("tradingMemberId") @Min(1) Long tradingMemberId,
      @RequestParam(value = "selectedDate", required = false) String selectedDateStr,
      @RequestParam(value = "fromDate", required = false) String fromDateStr,
      @RequestParam(value = "toDate", required = false) String toDateStr,
      @RequestParam(value = "pan", required = false) String pan,
      @RequestParam(value = "includeDeleted", required = false) boolean includeDeleted) {
    return ResponseEntity.ok(
        service.listKmpMembers(tradingMemberId, selectedDateStr, fromDateStr, toDateStr, pan, includeDeleted));
  }

  /**
   * Get the static configuration data for the KMP module
   *
   * @return ResponseEntity with Configuration data as body
   */
  @GetMapping("/configData")
  public ResponseEntity<KmpConfigDataDto> getConfigData() {
    return ResponseEntity.ok(service.getConfigData());
  }

  /**
   * Gets the members by the specified role (potential KMPs)
   *
   * @param tradingMemberId The trading member id
   * @param role The role by which to search
   * @return ResponseEntity with the list of KMP data as body
   */
  @GetMapping("/roleToKMPMapping/{role}")
  public ResponseEntity<List<RoleBasedKMPDataDto>> getMembersByRole(
      @RequestHeader("tradingMemberId") @Min(1) Long tradingMemberId,
      @PathVariable("role") String role) {
    return ResponseEntity.ok(service.getMembersByRole(tradingMemberId, role));
  }

  /**
   * Creates a KMP using the specified input data
   *
   * @param tradingMemberId The trading member id
   * @param request The request for creating the KMP
   * @return ResponseEntity with CommonMessageDto instance as body
   */
  @PostMapping("/kmpDetails")
  public ResponseEntity<CommonMessageDto> createKMP(
      @RequestHeader("tradingMemberId") @Min(1) Long tradingMemberId,
      @RequestBody @Valid CreateKMPRequestBody request) {
    service.createKMP(tradingMemberId, request);
    return ResponseEntity.ok(
        new CommonMessageDto(HttpStatus.OK.value(), "KMP details successfully saved"));
  }

  /**
   * Updates a KMP using the specified input data
   *
   * @param tradingMemberId The trading member id
   * @param request The request for updating the KMP
   * @return ResponseEntity with CommonMessageDto instance as body
   */
  @PutMapping("/kmpDetails")
  public ResponseEntity<CommonMessageDto> updateKMPs(
      @RequestHeader("tradingMemberId") @Min(1) Long tradingMemberId,
      @RequestBody @Valid UpdateKMPsRequestBody request) {
    service.updateKMPs(tradingMemberId, request);

    return ResponseEntity.ok(
        new CommonMessageDto(HttpStatus.OK.value(), "KMP details successfully updated"));
  }

  /**
   * Validate the PAN number
   *
   * @param tradingMemberId the trading member id
   * @param pan The PAN number to validate
   * @param kmpName The entered KMP name
   * @param kmpOfOtherMember The flag indicating whether to check if the pan holder is kmp of other member
   * @return The validation result
   */
  @GetMapping("/panValidation")
  public ResponseEntity<PanValidationResponse> validatePan(
      @RequestHeader("tradingMemberId") @Min(1) Long tradingMemberId,
      @RequestParam("pan") @NotBlank String pan,
      @RequestParam("kmpName") @NotBlank String kmpName,
      @RequestParam(value = "kmpOfOtherMember", required = false) boolean kmpOfOtherMember) {

    PanValidationResponse response = service.validatePan(tradingMemberId, pan, kmpName, kmpOfOtherMember);
    if(response.getPanStatus().equals(PanValidationStatus.INVALID)) {
      return ResponseEntity.badRequest().body(response);
    } else {
      return ResponseEntity.ok(response);
    }
  }

  /**
   * Deletes multiple KMPs identified in the input request
   *
   * @param tradingMemberId The trading member id
   * @param request The request for deleting the KMP records
   * @return ResponseEntity with CommonMessageDto as body
   */
  @DeleteMapping("/kmpDetails")
  public ResponseEntity<CommonMessageDto> deleteKMPDetails(
      @RequestHeader("tradingMemberId") @Min(1) Long tradingMemberId,
      @RequestBody @Valid DeleteKMPDetailsRequestBody request) {
    service.deleteKMPDetails(tradingMemberId, request);
    return ResponseEntity.ok(
        new CommonMessageDto(HttpStatus.OK.value(), "KMP details successfully deleted"));
  }

  /**
   * Deletes multiple KMPs identified in the input request
   *
   * @param tradingMemberId The trading member id
   * @param verifierId The verifierId
   * @param verifierName The verifierName
   * @param mobileNo The mobileNo to send otp
   * @return ResponseEntity with OtpSendResponse as body
   */
  @GetMapping("/OTPSend")
  public ResponseEntity<OtpSendResponse> sendOTP(
          @RequestHeader("tradingMemberId") @Min(1) Long tradingMemberId,
          @RequestParam("verifierId") @NotBlank String verifierId,
          @RequestParam("verifierName") @NotBlank String verifierName,
          @RequestParam("mobileNo") @NotBlank String mobileNo) {
    OtpSendResponse response = service.sendOtp(tradingMemberId, verifierId, verifierName, mobileNo);
    return ResponseEntity.ok(response);
  }

  /**
   * Deletes multiple KMPs identified in the input request
   *
   * @param tradingMemberId The trading member id
   * @param verifyId The verifyId
   * @param verifierId The verifierId
   * @param verifierName The verifierName
   * @param mobileNo The mobileNo received the otp
   * @param otp The otp
   * @return ResponseEntity with CommonMessageDto as body
   */
  @GetMapping("/OTPValidate")
  public ResponseEntity<CommonMessageDto> validateOTP(
          @RequestHeader("tradingMemberId") @Min(1) Long tradingMemberId,
          @RequestParam("verifyId") @NotNull Long verifyId,
          @RequestParam("verifierId") @NotBlank String verifierId,
          @RequestParam("verifierName") @NotBlank String verifierName,
          @RequestParam("mobileNo") @NotBlank String mobileNo,
          @RequestParam("otp") @NotBlank String otp) {
    CommonMessageDto response = service.validateOtp(tradingMemberId, verifyId, verifierId, verifierName, mobileNo, otp);
    return ResponseEntity.ok(response);
  }

  /**
   * Searches KMPs based on the specified search criteria Can search all KMPs without filters Or
   * search KMPs as on selected date Or search KMPs audit trail with date range
   *
   * @param tradingMemberId The trading member id
   * @param selectedDateStr The string representation of the selected date
   * @param fromDateStr The date lower bound to search audit trail from
   * @param toDateStr The date upper bound to search audit trail to
   * @param action The action to search
   * @param memIds The particular members id
   * @return csv file
   */
  @GetMapping("/kmpList")
  public ResponseEntity<?> listKmp(
          @RequestHeader("tradingMemberId") @Min(1) Long tradingMemberId,
          @RequestParam(value = "selectedDate", required = false) String selectedDateStr,
          @RequestParam(value = "fromDate", required = false) String fromDateStr,
          @RequestParam(value = "toDate", required = false) String toDateStr,
          @RequestParam(value = "action", required = false) String action,
          @RequestParam(value = "memId", required = false) String memIds) {
    return ResponseEntity.ok()
            .contentType(APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"kmp_list.csv\"")
            .body(service.listKmpMembers(tradingMemberId, selectedDateStr, fromDateStr, toDateStr, action, memIds).getBytes());
  }
  
  @GetMapping("/kmpRequestlist")
  public ResponseEntity<List<KMPApplicationsDetailDto>> kmpRequestlist(
          @RequestHeader("tradingMemberId") @Min(1) Long tradingMemberId,
          @RequestParam("memberName") @NotBlank String memberName,
          @RequestParam("memberCode") @NotBlank String memberCode,
          @RequestParam("memberType") @NotBlank String memberType) {
	  List<KMPApplicationsDetailDto> response = service.kmpRequestlist(tradingMemberId, memberName, memberCode, memberType);
    return ResponseEntity.ok(response);
  }
  
  @PostMapping("/requestToAddKmp")
  public ResponseEntity<CommonMessageDto> createKMPApplication(
      @RequestHeader("tradingMemberId") @Min(1) Long tradingMemberId,
      @RequestBody @Valid AddKMPDetails request) {
    
    return ResponseEntity.ok(
    		service.createKMPApplication(tradingMemberId, request));
  }
  
  @PutMapping("/requestToEditKmp")
  public ResponseEntity<CommonMessageDto> requestToEditKmp(
      @RequestHeader("tradingMemberId") @Min(1) Long tradingMemberId,
      @RequestBody @Valid EditKMPDetails request) {
    service.updateKMPApplication(tradingMemberId, request);

    return ResponseEntity.ok(
        new CommonMessageDto(HttpStatus.OK.value(), ""));
  }
  
  @DeleteMapping("/requestToDeleteKmp")
  public ResponseEntity<CommonMessageDto> requestToDeleteKmp(
      @RequestHeader("tradingMemberId") @Min(1) Long tradingMemberId,
      @RequestBody @Valid DeleteKMPDetailsRequestBody request) {
    service.deleteKMPApplication(tradingMemberId, request);
    return ResponseEntity.ok(
        new CommonMessageDto(HttpStatus.OK.value(), ""));
  }

  @Value("${dms.response.path}")
  private String resFileDirectory;


  @GetMapping("/uploadPanDummyDoc")
	public ResponseEntity<Object> uploadPanDummyDoc(){
		FileOutputStream fos = null;
		try {
			FileInputStream fis = new FileInputStream(ResourceUtils.getFile("classpath:pan_doc_dummy.pdf"));
			BufferedInputStream bis = new BufferedInputStream(fis);
			fos = new FileOutputStream(System.getProperty("user.dir")+ File.separator +resFileDirectory + File.separator + "pan_doc_dummy.pdf");
			try {
				fos.write(bis.readAllBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				fos.flush();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("File transfered");
		return new ResponseEntity<>(HttpStatus.OK);

	}

}
