package com.nseindia.mc.controller.penalty;

import com.nseindia.mc.controller.dto.CommonMessageDto;
import com.nseindia.mc.controller.dto.GenerateApprovalNoteAnnexureRequest;
import com.nseindia.mc.controller.dto.InspectionPenaltyFileRequest;
import com.nseindia.mc.controller.dto.InspectionPenaltyFileResponse;
import com.nseindia.mc.controller.dto.ListMemberPenaltyResponse;
import com.nseindia.mc.controller.dto.PenaltyApprovalRequest;
import com.nseindia.mc.controller.dto.PenaltyDisputeRequest;
import com.nseindia.mc.controller.dto.PenaltyLetterDetailsResponse;
import com.nseindia.mc.controller.dto.PenaltyLetterEmailRequest;
import com.nseindia.mc.controller.dto.PenaltyLettersRequest;
import com.nseindia.mc.controller.dto.ReviewCommentsDto;
import com.nseindia.mc.controller.dto.ReviewCommentsRequest;
import com.nseindia.mc.controller.dto.SubmissionCycleDto;
import com.nseindia.mc.model.PenaltyType;
import com.nseindia.mc.service.penalty.PenaltyService;

import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.NoArgsConstructor;

/**
 * This controller handles the penalty management requests
 */
@RestController
@RequestMapping("/penalty")
@NoArgsConstructor
public class PenaltyController {

  /**
   * The penalty service to use to manage penalty data
   */
  @Autowired
  private PenaltyService penaltyService;

  /**
   * List computed penalty for members
   *
   * @param nseOfficialId the nse official id.
   * @param submissionYear the submission year
   * @param submissionMonth the submission month
   * @param penaltyType The penalty type
   * @return ListMemberPenaltyResponse
   */
  @GetMapping("{penaltyType}/penaltyMembers")
  public ResponseEntity<ListMemberPenaltyResponse> listMemberPenalty(
      @RequestHeader("nseOfficialId") long nseOfficialId,
      @RequestParam(value = "submissionYear", required = true) int submissionYear,
      @RequestParam(value = "submissionMonth", required = true) int submissionMonth,
      @PathVariable("penaltyType") PenaltyType penaltyType
      ) {
    return ResponseEntity.ok(
      penaltyService.listMemberPenalty(submissionYear, submissionMonth, penaltyType)
    );
  }

  /**
   * Generate approval note annexure
   *
   * @param nseOfficialId the nse official id.
   * @param request GenerateApprovalNoteAnnexureRequest
   * @param penaltyType The penalty type
   * @return response of generateApprovalNoteAnnexure
   */
  @PostMapping("{penaltyType}/annexureApprovalNote")
  public ResponseEntity<CommonMessageDto> generateApprovalNoteAnnexure(
      @RequestHeader("nseOfficialId") long nseOfficialId,
      @RequestBody GenerateApprovalNoteAnnexureRequest request,
      @PathVariable("penaltyType") PenaltyType penaltyType
  ) {
    return ResponseEntity.ok(
        penaltyService.generateApprovalNoteAnnexure(request, nseOfficialId, penaltyType)
    );
  }

  /**
   * Get review comments
   *
   * @param nseOfficialId the nse official id.
   * @param submissionYear the submission year
   * @param submissionMonth the submission month
   * @param penaltyType The penalty type
   * @return ReviewCommentsDto
   */
  @GetMapping("{penaltyType}/reviewComments")
  public ResponseEntity<ReviewCommentsDto> getReviewComments(
      @RequestHeader("nseOfficialId") long nseOfficialId,
      @RequestParam(value = "submissionYear", required = true) int submissionYear,
      @RequestParam(value = "submissionMonth", required = true) int submissionMonth,
      @PathVariable("penaltyType") PenaltyType penaltyType
  ) {
    return ResponseEntity.ok(
        penaltyService.getReviewComments(submissionYear, submissionMonth, penaltyType)
    );
  }

  /**
   * Post review comments
   *
   * @param nseOfficialId the nse official id.
   * @param request       ReviewCommentsRequest
   * @param penaltyType The penalty type
   * @return response of postReviewComments
   */
  @PostMapping("{penaltyType}/reviewComments")
  public ResponseEntity<CommonMessageDto> postReviewComments(
      @RequestHeader("nseOfficialId") long nseOfficialId,
      @RequestBody ReviewCommentsRequest request,
      @PathVariable("penaltyType") PenaltyType penaltyType
  ) {
    return ResponseEntity.ok(
        penaltyService.postReviewComments(request, nseOfficialId, penaltyType)
    );
  }

  /**
   * To update penalty if member have any dispute.
   * @param nseOfficialId the nse official id.
   * @param request       PenaltyDisputeRequest
   * @param penaltyType The penalty type
   * @return response of putPenaltyDispute
   */
  @PutMapping("{penaltyType}/penaltyDispute")
  public ResponseEntity<CommonMessageDto> putPenaltyDispute(
      @RequestHeader("nseOfficialId") long nseOfficialId,
      @RequestBody PenaltyDisputeRequest request,
      @PathVariable("penaltyType") PenaltyType penaltyType
  ) {
    return ResponseEntity.ok(
        penaltyService.putPenaltyDispute(request, nseOfficialId, penaltyType)
    );
  }

  /**
   * To list penalty letter details of every members.
   * @param nseOfficialId the nse official id.
   * @param submissionYear the submission year
   * @param submissionMonth the submission month
   * @param penaltyLetterType Penalty Type indicates Penalty Letter or Penalty Reverse Letter
   * @param penaltyType The penalty type (MTR or KMP)
   * @return PenaltyLetterDetailsResponse
   */
  @GetMapping("{penaltyType}/penaltyLetterDetails")
  public ResponseEntity<PenaltyLetterDetailsResponse> getPenaltyLetterDetails(
      @RequestHeader("nseOfficialId") long nseOfficialId,
      @RequestParam(value = "submissionYear", required = true) int submissionYear,
      @RequestParam(value = "submissionMonth", required = true) int submissionMonth,
      @RequestParam(value = "penaltyType", required = true) String penaltyLetterType,
      @PathVariable("penaltyType") PenaltyType penaltyType
  ) {
    return ResponseEntity.ok(
        penaltyService.getPenaltyLetterDetails(submissionYear, submissionMonth, penaltyLetterType, penaltyType)
    );
  }

   /**
   * Generate penalty letters for members.
   * @param nseOfficialId the nse official id.
   * @param request       PenaltyLettersRequest
   * @param penaltyType The penalty type (MTR or KMP)
   * @return PenaltyLetterDetailsResponse
   */
  @PostMapping("{penaltyType}/penaltyLetters")
  public ResponseEntity<PenaltyLetterDetailsResponse> generatePenaltyLetters(
      @RequestHeader("nseOfficialId") long nseOfficialId,
      @RequestBody PenaltyLettersRequest request,
      @PathVariable("penaltyType") PenaltyType penaltyType
  ) {
    return ResponseEntity.ok(
        penaltyService.generatePenaltyLetters(request, nseOfficialId, penaltyType)
    );
  }

  /**
   * Download Inspection Penalty File
   * @param nseOfficialId the nse official id.
   * @param request       InspectionPenaltyFileRequest
   * @param penaltyType The penalty type (MTR or KMP)
   * @return InspectionPenaltyFileResponse
   */
  @PostMapping("{penaltyType}/inspectionPenaltyFile")
  public ResponseEntity<InspectionPenaltyFileResponse> downloadInspectionPenaltyFile(
      @RequestHeader("nseOfficialId") long nseOfficialId,
      @RequestBody InspectionPenaltyFileRequest request,
      @PathVariable("penaltyType") PenaltyType penaltyType
  ) {
    return ResponseEntity.ok(
        penaltyService.downloadInspectionPenaltyFile(request, nseOfficialId, penaltyType)
    );
  }

  /**
   * Send penalty letters to members via e-mail.
   * @param nseOfficialId the nse official id.
   * @param request       PenaltyLetterEmailRequest
   * @param penaltyType The penalty type (MTR or KMP)
   * @return response of sendPenaltyLetterEmail
   */
  @PostMapping("{penaltyType}/penaltyLetterEmail")
  public ResponseEntity<CommonMessageDto> sendPenaltyLetterEmail(
      @RequestHeader("nseOfficialId") long nseOfficialId,
      @RequestBody PenaltyLetterEmailRequest request,
      @PathVariable("penaltyType") PenaltyType penaltyType
  ) {
    return ResponseEntity.ok(
        penaltyService.sendPenaltyLetterEmail(request, nseOfficialId, penaltyType)
    );
  }

  /**
   * Download Penalty Files for particular year and month and penalty type
   * @param nseOfficialId the nse official id.
   * @param penaltyYear penalty year
   * @param penaltyMonth penalty month
   * @param fileTypes file types
   * @param penaltyType The penalty type (MTR or KMP)
   * @return zip file
   */
  @GetMapping("{penaltyType}/downloadPenaltyFiles")
  public ResponseEntity<?> downloadPenaltyFiles(
      @RequestHeader("nseOfficialId") long nseOfficialId,
      @RequestParam(value = "penaltyYear", required = true) int penaltyYear,
      @RequestParam(value = "penaltyMonth", required = true) int penaltyMonth,
      @RequestParam(value = "fileType", required = true) List<String> fileTypes,
      @PathVariable("penaltyType") PenaltyType penaltyType) {
    return ResponseEntity.ok()
        .contentType(APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"penalty_files.zip\"")
        .body(
            penaltyService.downloadPenaltyFiles(
                penaltyYear, penaltyMonth, fileTypes, nseOfficialId, penaltyType)
                .toByteArray());
  }

  /**
   * Download member's all penalty letters till current date in a zip file.
   *
   * @param memberId trading member id.
   * @param penaltyType The penalty type (MTR or KMP)
   * @return zip file
   */
  @GetMapping("{penaltyType}/downloadPenaltyLetters")
  public ResponseEntity<?> downloadPenaltyLetters(
      @RequestHeader("tradingMemberId") long memberId,
      @PathVariable("penaltyType") PenaltyType penaltyType
  ) {
    return ResponseEntity.ok()
        .contentType(APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"penalty_files.zip\"")
        .body(
            penaltyService.downloadPenaltyLetters(memberId, penaltyType)
                .toByteArray());
  }

  /**
   * Get submission cycles from the penalty table
   * 
   * @param penaltyType The penalty type (MTR or KMP)
   *
   * @return list of submission cycles for the given penalty type
   */
  @GetMapping("{penaltyType}/submissionCycles")
  public ResponseEntity<List<SubmissionCycleDto>> getSubmissionCycles(
    @PathVariable("penaltyType") PenaltyType penaltyType
  ) {
    return ResponseEntity.ok(penaltyService.getSubmissionCycles(penaltyType));
  }

  /**
   * Upload Agenda Minutes
   *
   * @param penaltyMemberId the penaltyMember db record id
   * @param file the uploaded file
   * @param penaltyType The penalty type (MTR or KMP)
   * @return response of validateMtrFile
   */
  @PostMapping("{penaltyType}/uploadAgendaMinutes")
  public ResponseEntity<CommonMessageDto> uploadAgendaMinutes(
    @RequestParam("penaltyMemberId") long penaltyMemberId,
    @RequestParam("file") MultipartFile file,
    @PathVariable("penaltyType") PenaltyType penaltyType) {
    return ResponseEntity.ok(penaltyService.uploadAgendaMinutes(penaltyMemberId, file, penaltyType));
  }

  /**
   * The checker approves the Approval Note and Annexure File for penalty submission cycle.
   * @param nseOfficialId the nse official id.
   * @param request       PenaltyApprovalRequest
   * @param penaltyType The penalty type (MTR or KMP)
   * @return response of postPenaltyApproval
   */
  @PostMapping("{penaltyType}/penaltyApproval")
  public ResponseEntity<CommonMessageDto> postPenaltyApproval(
      @RequestHeader("nseOfficialId") long nseOfficialId,
      @RequestBody PenaltyApprovalRequest request,
      @PathVariable("penaltyType") PenaltyType penaltyType
  ) {
    return ResponseEntity.ok(
        penaltyService.postPenaltyApproval(request, nseOfficialId, penaltyType)
    );
  }
}
