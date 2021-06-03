package com.nseindia.mc.controller.aiml;

import com.nseindia.mc.controller.dto.*;
import com.nseindia.mc.service.aiml.AimlService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

@RestController
public class AimlController {

    private final AimlService service;

    public AimlController(final AimlService service) {
        this.service = service;
    }

    /**
     * Get for eligible member.
     *
     * @param memberId
     * @return the eligible member info
     */
    @GetMapping("aimlreporting/getforeligiblemember")
    public ResponseEntity<ForEligibleMemberDto> getForEligibleMember(
            @RequestHeader("tradingMemberId") long memberId) {
        return ResponseEntity.ok(service.getForEligibleMember(memberId));
    }

    /**
     * Get submission members info.
     *
     * @return the list of member info
     */
    @GetMapping("aimlreporting/memberInfo")
    public ResponseEntity<List<MemberInfoDto>> getMemberInfo() {
        return ResponseEntity.ok(service.getMemberInfo());
    }

    /**
     * Get member quarters.
     *
     * @param memberId
     * @return the list of member quarter
     */
    @GetMapping("aimlreporting/quarters")
    public ResponseEntity<List<String>> getQuarters(
            @RequestHeader(value = "tradingMemberId", required = false) Long memberId) {
        return ResponseEntity.ok(service.getQuarters(memberId));
    }

    /**
     * Generate report to client.
     *
     * @param quarter
     * @return report file
     */
    @PostMapping("aimlreporting/generateReport")
    public ResponseEntity<?> generateReport(
            @RequestParam("quarter") String quarter,
            @RequestParam(value = "tradingMemberId", required = false) Long tradingMemberId) {
        return ResponseEntity.ok()
                .contentType(APPLICATION_OCTET_STREAM)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + quarter + "report.xlsx\"")
                .body(service.generateReport(quarter, tradingMemberId));
    }


    /**
     * Submit nil submission.
     *
     * @param memberId
     * @return 204
     */
    @PostMapping("aimlreporting/nilsubmission")
    public ResponseEntity<?> postNilSubmission(@RequestHeader("tradingMemberId") long memberId) {
        service.updateNilSubmission(memberId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get data submissions.
     *
     * @param memberId
     * @return the list of data submission
     */
    @GetMapping("aimlreporting/datasubmission")
    public ResponseEntity<List<TblAimlSystemDetailNewDto>> getDataSubmissions(
            @RequestHeader("tradingMemberId") long memberId,@RequestHeader(value = "quarter", required = false) String quarter) {
        return ResponseEntity.ok(service.getDataSubmissions(memberId,quarter));
    }

    /**
     * Update member's data submissions.
     *
     * @param memberId
     * @param systemDetails
     * @return 204
     */
    @PostMapping("aimlreporting/datasubmission")
    public ResponseEntity<?> updateDataSubmission(
            @RequestHeader("tradingMemberId") long memberId,
            @Valid @RequestBody List<TblAimlSystemDetailDto> systemDetails) {
        service.updateDataSubmission(memberId, systemDetails);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get submissions.
     *
     * @param memberId
     * @param quarter
     * @param startDate
     * @param endDate
     * @return the list of submission
     */
    @GetMapping("aimlreporting/submissions")
    public ResponseEntity<List<SubmissionDto>> getSubmissions(
            @RequestHeader(value = "tradingMemberId", required = false) Long memberId,
            @RequestParam(value = "quarter", required = false) String quarter,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    Date startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    Date endDate) {
        return ResponseEntity.ok(service.getSubmissions(memberId, quarter, startDate, endDate));
    }



  /**
   * Send a generate report to ibes.
   *
   * @param quarter
   * @return 204
   */
  @PostMapping("aimlreporting/sendReport")
  public ResponseEntity<?> sendReport(
      @RequestParam("quarter") String quarter,
      @RequestParam(value = "tradingMemberId", required = false) Long tradingMemberId) {
    service.sendReport(quarter, tradingMemberId);
    return ResponseEntity.noContent().build();
  }
}
