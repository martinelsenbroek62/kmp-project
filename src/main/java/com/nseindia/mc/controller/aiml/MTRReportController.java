package com.nseindia.mc.controller.aiml;

import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import javax.transaction.Transactional;

import com.nseindia.mc.controller.dto.AssignHoMakerRequest;
import com.nseindia.mc.controller.dto.CommonMessageDto;
import com.nseindia.mc.controller.dto.ComputePenaltyMTRRequest;
import com.nseindia.mc.controller.dto.HoMakerDto;
import com.nseindia.mc.controller.dto.LeverageReportResponse;
import com.nseindia.mc.controller.dto.ListCumulativeDetailsResponse;
import com.nseindia.mc.controller.dto.MTRDailyFileDetailsDto;
import com.nseindia.mc.controller.dto.MTRMemberDailyFileDetailsDto;
import com.nseindia.mc.controller.dto.MTRValidateFileDto;
import com.nseindia.mc.controller.dto.MainRequestOption;
import com.nseindia.mc.controller.dto.MemberDetailsDto;
import com.nseindia.mc.controller.dto.MemberUploadStatus;
import com.nseindia.mc.controller.dto.NonSubmissionCountRequest;
import com.nseindia.mc.controller.dto.NonSubmissionCountResponse;
import com.nseindia.mc.controller.dto.NonSubmissionMbrDtlsResponse;
import com.nseindia.mc.controller.dto.SubmitMTRFileResponse;
import com.nseindia.mc.controller.dto.UploadCutOffPeriod;
import com.nseindia.mc.controller.dto.WithdrawalMbrDtlsResponse;
import com.nseindia.mc.exception.BaseServiceException;
import com.nseindia.mc.model.MTRDailyFile;
import com.nseindia.mc.service.mtrReport.MtrFileValidateService;
import com.nseindia.mc.service.mtrReport.MtrReportService;
import com.nseindia.mc.service.penalty.PenaltyService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.NoArgsConstructor;

/** The controller holds endpoints related to MTR report. */
@RestController
@RequestMapping("/mtrReport")
@NoArgsConstructor
public class MTRReportController {

  @Autowired private MtrReportService service;
  @Autowired private MtrFileValidateService validateService;
  @Autowired private PenaltyService penaltyService;
  
  @Value("${dms.response.path}")
  private String resFileDirectory;

  /**
   * Get main request options.
   *
   * @return the main request options.
   */
  @GetMapping("mainRequestOptions")
  public ResponseEntity<List<MainRequestOption>> getMainRequestOptions() {
    return ResponseEntity.ok(service.getMainRequestOptions());
  }

  /**
   * Get upload cutoff period.
   *
   * @return the upload cutoff period.
   */
  @GetMapping("upLoadCutOffPeriod")
  public ResponseEntity<UploadCutOffPeriod> getUploadCutOffPeriod() {
    return ResponseEntity.ok(service.getUploadCutOffPeriod());
  }

  /**
   * Get member upload status.
   *
   * @param memberId the member id.
   * @return the member upload status.
   */
  @GetMapping("memberUploadStatus")
  public ResponseEntity<MemberUploadStatus> getMemberUploadStatus(
      @RequestHeader("tradingMemberId") long memberId) {
    return ResponseEntity.ok(service.getMemberUploadStatus(memberId));
  }

  /**
   * New member on board.
   *
   * @param memberId the member id.
   * @param uploadStatus the upload status.
   * @return no content respoonse.
   */
  @PostMapping("newMembersOnboard")
  public ResponseEntity<?> newMembersOnboard(
      @RequestHeader("tradingMemberId") long memberId,
      @RequestParam(value = "uploadStatus", required = true) boolean uploadStatus) {
    service.newMembersOnboard(memberId, uploadStatus);
    return ResponseEntity.noContent().build();
  }

  /**
   * Download mtr files.
   *
   * @param fileId the daily file id.
   * @param responseFile download response file or not
   * @return the mtr file.
   */
  @GetMapping("downloadMtrFile")
  public ResponseEntity<?> downloadMtrFile(
      @RequestParam(value = "fileId", required = true) long fileId,
      @RequestParam(value = "responseFile", required = false) boolean responseFile) {
    FileInputStream fis = null;
    try {
      MTRDailyFile dailyFile = service.getMtrFileById(fileId);
      fis =
          new FileInputStream(
              responseFile ? dailyFile.getDmsResIndex() : dailyFile.getDmsDocIndex());
      return ResponseEntity.ok()
          .contentType(APPLICATION_OCTET_STREAM)
          .header(
              HttpHeaders.CONTENT_DISPOSITION,
              "attachment; filename=\"" + dailyFile.getDailyFileName())
          .body(IOUtils.toByteArray(fis));
    } catch (IOException e) {
      throw new BaseServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
  }

  /**
   * Get member details.
   *
   * @param memberId the member id.
   * @param memberName the member name.
   * @param memberCode the member code.
   * @return the member details.
   */
  @GetMapping("mtrMemberDetails")
  public ResponseEntity<MemberDetailsDto> getMemberDetails(
      @RequestHeader("tradingMemberId") long memberId,
      @RequestParam(value = "memberName", required = false) String memberName,
      @RequestParam(value = "memberCode", required = false) String memberCode) {
    return ResponseEntity.ok(service.getMemberDetails(memberId, memberName, memberCode));
  }

  /**
   * List MTR Daily Files for member.
   *
   * @param memberId the member id.
   * @param memberName the member name.
   * @param reportFromDate the reporting from date.
   * @param reportToDate the reporting to date.
   * @return the daily files.
   */
  @GetMapping("listMtrDailyFilesMember")
  public ResponseEntity<List<MTRMemberDailyFileDetailsDto>> listMtrDailyFilesMember(
      @RequestHeader("tradingMemberId") long memberId,
      @RequestParam(value = "memberName", required = true) String memberName,
      @RequestParam(value = "reportFromDate", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate reportFromDate,
      @RequestParam(value = "reportToDate", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate reportToDate) {
    return ResponseEntity.ok(
        service.listMtrDailyFilesMember(
            memberId,
            memberName,
            reportFromDate.atStartOfDay(),
            LocalTime.MAX.atDate(reportToDate)));
  }

  /**
   * List MTR daily files for NSE.
   *
   * @param nseOfficialId the nse official id.
   * @param memberId the member id.
   * @param reportingDate the reporting date.
   * @return the daily files.
   */
  @GetMapping("listMtrDailyFilesNSE")
  public ResponseEntity<List<MTRDailyFileDetailsDto>> listMtrDailyFilesNse(
      @RequestHeader("nseOfficialId") long nseOfficialId,
      @RequestParam(value = "memberId", required = false) Long memberId,
      @RequestParam("reportingDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate reportingDate) {
    return ResponseEntity.ok(service.listMtrDailyFilesNse(memberId, reportingDate));
  }

  /**
   * List Ho Maker.
   *
   * @param nseOfficialId the nse official id.
   * @return the Ho maker list.
   */
  @GetMapping("listHoMakerMTR")
  public ResponseEntity<List<HoMakerDto>> listHoMakerMtr(
      @RequestHeader("nseOfficialId") long nseOfficialId) {
    return ResponseEntity.ok(service.listHoMakerMtr());
  }

  /**
   * Assign HO Maker.
   *
   * @param nseOfficialId the nse official id.
   * @param request the assign maker request.
   * @return no content response.
   */
  @PostMapping("assignHoMakerMTR")
  public ResponseEntity<?> assignHoMakerMtr(
      @RequestHeader("nseOfficialId") long nseOfficialId,
      @RequestBody AssignHoMakerRequest request) {

    service.assignHoMaker(request.getHoMakerId());
    return ResponseEntity.noContent().build();
  }

  /**
   * Download MTR file to NSE.
   *
   * @param nseOfficialId the nse official id.
   * @param memberId the member id.
   * @param submissionFromDate the submission from date.
   * @param submissionToDate the submission to date.
   * @param fileTypes the file types.
   * @return the downloaded mtr files.
   */
  @GetMapping("downloadMtrFileNSE")
  public ResponseEntity<?> downloadMtrFileNse(
      @RequestHeader("nseOfficialId") long nseOfficialId,
      @RequestParam(value = "memberId", required = false) Long memberId,
      @RequestParam(value = "submissionFromDate", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate submissionFromDate,
      @RequestParam(value = "submissionToDate", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate submissionToDate,
      @RequestParam(value = "fileType", required = false) List<String> fileTypes) {

    return ResponseEntity.ok()
        .contentType(APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"mtr_files.zip\"")
        .body(
            service
                .downloadMtrFiles(memberId, submissionFromDate, submissionToDate, fileTypes)
                .toByteArray());
  }

  /**
   * List MTR penality file types.
   *
   * @param nseOfficialId the nse official id.
   * @return the penality file types.
   */
  @GetMapping("listMtrPenaltyFileTypes")
  public ResponseEntity<?> listMtrPenaltyFileTypes(
      @RequestHeader("nseOfficialId") long nseOfficialId) {
    return ResponseEntity.ok(service.listMtrPenaltyFileTypes());
  }

  /**
   * Validate member can make nil submission or not..
   *
   * @param memberId the member id.
   * @return no content response.
   */
  @GetMapping("validateNilSubmission")
  public ResponseEntity<?> validateNilSubmission(
      @RequestHeader("tradingMemberId") long memberId) {
    if (service.getMemberUploadStatus(memberId).getLastTotalAmountFunded() > 0) {
      throw new BaseServiceException(
        "Validations failed, please verify if you margin trading exposure is 0.",
        HttpStatus.FORBIDDEN);
    }
    return ResponseEntity.noContent().build();
  }

  /**
   * Submit a nil submission.
   *
   * @param memberId the member id.
   * @return no content response.
   */
  @PostMapping("submitNil")
  public ResponseEntity<?> submitNilSubmission(
      @RequestHeader("tradingMemberId") long memberId) {
    service.submitNilSubmission(memberId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Validate mrt file.
   *
   * @param memberId the member id
   * @param file the reporting file
   * @return response of validateMtrFile
   */
  @PostMapping("validateMtrFile")
  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public ResponseEntity<MTRValidateFileDto> validateMtrFile(
      @RequestHeader("tradingMemberId") long memberId, @RequestParam("file") MultipartFile file) {
    return ResponseEntity.ok(validateService.validateMtrFile(file, memberId));
  }

  /**
   * Submit mtr file.
   *
   * @param memberId the member id
   * @return response of submitMtrFile
   */
  @PostMapping("submitMtrFile")
  @Transactional
  public ResponseEntity<SubmitMTRFileResponse> submitMtrFile(
      @RequestHeader("tradingMemberId") long memberId, @RequestParam("validationId") long validationId) {
    return ResponseEntity.ok(validateService.submitMtrFile(memberId, validationId));
  }

  /**
   * mock of validation response file download
   *
   * @param fileName the response file name.
   * @return the mtr file.
   */
  @GetMapping("downloadFile")
  public ResponseEntity<?> downloadFile(
      @RequestParam(value = "fileName", required = true) String fileName) {
    FileInputStream fis = null;
    try {
      File path = new File(System.getProperty("user.dir")+ File.separator +resFileDirectory + File.separator + fileName);
      fis = new FileInputStream(path);
      return ResponseEntity.ok()
          .contentType(APPLICATION_OCTET_STREAM)
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
          .body(IOUtils.toByteArray(fis));
    } catch (IOException e) {
      throw new BaseServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
  }

  /**
   * Trigger monthly penalty calculation
   *
   * @param year year
   * @param month month
   * @return
   */
  @GetMapping("computePenaltyJob")
  public ResponseEntity<?> computePenaltyJob(
    @RequestParam(value = "year") int year,
    @RequestParam(value = "month") int month
  ) {
    penaltyService.penaltyCalculationMonthlyJob(year, month);
    return ResponseEntity.noContent().build();
  }

  /**
   * Details of the cumulative member data based on whether it should be member wise or not.
   *
   * @param fromDate the member id.
   * @param toDate the reporting date.
   * @param isMemberWiseFlag member wise or not
   * @return the cumulative details list
   */
  @GetMapping("cumulativeDtls")
  public ResponseEntity<ListCumulativeDetailsResponse> listCumulativeDtls(
      @RequestParam(value = "fromDate", required = true)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fromDate,
      @RequestParam(value = "toDate", required = true)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate toDate,
      @RequestParam(value = "isMemberWiseFlag", required = true) Boolean isMemberWiseFlag) {
        return ResponseEntity.ok(
          service.listCumulativeDtls(fromDate, toDate, isMemberWiseFlag)
        );
  }

  /**
   * Details of the cumulative member data based on whether it should be member wise or not.
   *
   * @param fromDate the member id.
   * @param toDate the reporting date.
   * @return the cumulative details list
   */
  @GetMapping("leverageReport")
  public ResponseEntity<LeverageReportResponse> leverageReport(
      @RequestParam(value = "fromDate", required = true)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fromDate,
      @RequestParam(value = "toDate", required = true)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate toDate,
      @RequestParam(value = "reportType", required = true) String reportType) {
        LeverageReportResponse response;
        if (reportType.equals("totalIndebtedness")) {
          response = service.leverageTotalIndebtednessReport(fromDate, toDate);
        } else if (reportType.equals("maxAllowableExposure")) {
          response = service.leverageMaxAllowableExposureReport(fromDate, toDate);
        } else if (reportType.equals("maxClientAllowableExposure")) {
          response = service.leverageMaxClientAllowableExposureReport(fromDate, toDate);
        } else if (reportType.equals("lenderWiseExposure")) {
          response = service.leverageLenderWiseExposureReport(fromDate, toDate);
        } else {
          throw new BaseServiceException("invalid report type", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(response);
  }

    /**
   * Details of the cumulative member data based on whether it should be member wise or not.
   *
   * @return the cumulative details list
   */
  @GetMapping("nonSubmissionMbrDtls")
  public ResponseEntity<NonSubmissionMbrDtlsResponse> getNonSubmissionMbrDtls(
      @RequestParam(value = "year", required = true) int year,
      @RequestParam(value = "month", required = true) int month,
      @RequestParam(value = "memberCode", required = false) String memberCode,
      @RequestParam(value = "memberName", required = false) String memberName) {
    return ResponseEntity.ok(
        service.getNonSubmissionMbrDtls(year, month, memberCode, memberName)
    );
  }



  /**
   * Get details of member non submission records
   *
   * @param nseOfficialId the nse official id.
   * @param request       NonSubmissionCountRequest
   * @return NonSubmissionCountResponse
   */
  @PostMapping("nonSubmissionCount")
  public ResponseEntity<NonSubmissionCountResponse> downloadMemberNonSubmissionFile(
      @RequestHeader("nseOfficialId") long nseOfficialId,
      @RequestBody NonSubmissionCountRequest request
  ) {
    return ResponseEntity.ok(
        penaltyService.downloadMemberNonSubmissionFile(request)
    );
  }



  /**
   * Get details of the Withdrawal status of the members in the selected date range
   * @param fromDate from date
   * @param toDate to date
   * @param currentDate current date
   * @return WithdrawalMbrDtlsResponse
   */
  @GetMapping("withdrawalMbrDtls")
  public ResponseEntity<WithdrawalMbrDtlsResponse> getWithdrawalMbrDtls(
      @RequestParam(value = "fromDate", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate fromDate,
      @RequestParam(value = "toDate", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate toDate,
      @RequestParam(value = "currentDate", required = false) 
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate currentDate
  ) {
    return ResponseEntity.ok(
        penaltyService.getWithdrawalMbrDtls(fromDate, toDate, currentDate)
    );
  }

  /**
   * Download Mtr File Validation Response and error code
   * @param memberId
   * @param validationId
   * @return zip file
   */
  @GetMapping("downloadMtrFileValidationResponse")
  public ResponseEntity<?> downloadMtrFileValidationResponse(
      @RequestHeader("tradingMemberId") long memberId,
      @RequestParam(value = "validationId", required = true) long validationId) {
    return ResponseEntity.ok()
        .contentType(APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"mtr_file_validation_response.zip\"")
        .body(
            validateService.downloadMtrFileValidationResponse(
                memberId, validationId)
                .toByteArray());
  }

  /**
   * Testing API for cron job method to carry over the nil submission and insert not-uploaded data submission record
   *
   * @return
   */
  @PostMapping("processDailyNonSubmissionRecords")
  public ResponseEntity<CommonMessageDto> processDailyNonSubmissionRecords(
  ) {
    service.processDailyNonSubmissionRecords();
    return ResponseEntity.ok(new CommonMessageDto(200, "done"));
  }

  /**
   * Testing API for cron job method to Generate margin trading report.
   *
   * @return
   */
  @GetMapping("generateMarginTradingReport")
  public ResponseEntity<CommonMessageDto> generateMarginTradingReport(
  ) {
    service.generateMarginTradingReport();
    return ResponseEntity.ok(new CommonMessageDto(200, "done"));
  }

  /**
   * Testing API for cron job method to publish NSE web
   *
   * @return
   */
  @GetMapping("publishNSEWeb")
  public ResponseEntity<CommonMessageDto> publishNSEWeb(
  ) {
    service.publishNSEWeb();
    return ResponseEntity.ok(new CommonMessageDto(200, "done"));
  }

  /**
   * Re run mtrDailySignOffJob
   *
   * @param reportingDate reporting date
   * @return
   */
  @GetMapping("mtrDailySignOffJob")
  public ResponseEntity<?> mtrDailySignOffJob(
    @RequestParam(value = "reportingDate")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate reportingDate) {
    service.signOffDailyJob(reportingDate);
    return ResponseEntity.noContent().build();
  }

  /**
   * Re run mtrHoMakerNotifyJob
   *
   * @param year year
   * @param month month
   * @return
   */
  @GetMapping("mtrHoMakerNotifyJob")
  public ResponseEntity<?> mtrDailySignOffJob(
    @RequestParam(value = "year") int year,
    @RequestParam(value = "month") int month
  ) {
    service.hoMakerNotifyMonthlyJob(year, month);
    return ResponseEntity.noContent().build();
  }
}
