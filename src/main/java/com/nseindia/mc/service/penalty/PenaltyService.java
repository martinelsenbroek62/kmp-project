package com.nseindia.mc.service.penalty;

import static com.google.common.collect.Lists.newArrayList;
import static com.nseindia.mc.util.CommonUtils.addFileToZip;
import static com.nseindia.mc.util.CommonUtils.jsonToObjectList;
import static com.nseindia.mc.util.CommonUtils.objectToJson;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import com.nseindia.mc.model.MTRMonthlyJobStatus;
import com.nseindia.mc.repository.MTRMonthlyJobStatusRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.nseindia.mc.controller.dto.CommonMessageDto;
import com.nseindia.mc.controller.dto.GenerateApprovalNoteAnnexureRequest;
import com.nseindia.mc.controller.dto.GenerateApprovalNoteAnnexureRequest.Option;
import com.nseindia.mc.controller.dto.InspectionPenaltyFileRequest;
import com.nseindia.mc.controller.dto.InspectionPenaltyFileResponse;
import com.nseindia.mc.controller.dto.ListMemberPenaltyResponse;
import com.nseindia.mc.controller.dto.MemberPenaltyDetailsDto;
import com.nseindia.mc.controller.dto.NonSubmissionCountRequest;
import com.nseindia.mc.controller.dto.NonSubmissionCountResponse;
import com.nseindia.mc.controller.dto.PenaltyApprovalRequest;
import com.nseindia.mc.controller.dto.PenaltyDisputeRequest;
import com.nseindia.mc.controller.dto.PenaltyLetterDetailsDto;
import com.nseindia.mc.controller.dto.PenaltyLetterDetailsResponse;
import com.nseindia.mc.controller.dto.PenaltyLetterEmailRequest;
import com.nseindia.mc.controller.dto.PenaltyLettersRequest;
import com.nseindia.mc.controller.dto.ReviewCommentsDto;
import com.nseindia.mc.controller.dto.ReviewCommentsRequest;
import com.nseindia.mc.controller.dto.ReviewCommentMessage;
import com.nseindia.mc.controller.dto.SubmissionCycleDto;
import com.nseindia.mc.controller.dto.WithdrawalMbrDtlsResponse;
import com.nseindia.mc.controller.dto.WithdrawalStatusMemberDto;
import com.nseindia.mc.exception.BaseServiceException;
import com.nseindia.mc.model.MTRDailyFile;
import com.nseindia.mc.model.MTRHoMaker;
import com.nseindia.mc.model.MTRMemberList;
import com.nseindia.mc.model.PenaltyMember;
import com.nseindia.mc.model.PenaltyReview;
import com.nseindia.mc.model.MemComApplication;
import com.nseindia.mc.model.MemberMaster;
import com.nseindia.mc.model.Penalty;
import com.nseindia.mc.model.PenaltyApplicationStatus;
import com.nseindia.mc.model.PenaltyApplicationSubStatus;
import com.nseindia.mc.model.PenaltyLetterStatus;
import com.nseindia.mc.model.PenaltyLetterType;
import com.nseindia.mc.model.PenaltyReviewReasonType;
import com.nseindia.mc.model.PenaltyStatus;
import com.nseindia.mc.model.PenaltyType;
import com.nseindia.mc.model.UserMemCom;
import com.nseindia.mc.repository.MTRDailyFileRepository;
import com.nseindia.mc.repository.MTRHoMakerRepository;
import com.nseindia.mc.repository.PenaltyMemberRepository;
import com.nseindia.mc.repository.PenaltyRepository;
import com.nseindia.mc.repository.PenaltyReviewRepository;
import com.nseindia.mc.repository.MemComApplicationRepository;
import com.nseindia.mc.repository.MemberListRepository;
import com.nseindia.mc.repository.UserMemComRepository;
import com.nseindia.mc.service.MailService;
import com.nseindia.mc.util.CommonUtils;
import com.nseindia.mc.util.report.ReportInputData;
import com.nseindia.mc.util.report.ReportInputTableParameter;
import com.nseindia.mc.util.report.ReportUtils;
import com.nseindia.mc.value.PenaltyFileType;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/** The service manage Penalty related operations. */
@Slf4j
@Service
public class PenaltyService {

  private static final int WITHDRAWAL_THRESHOLD = 5;

  @Autowired private MemberListRepository memberListRepository;
  @Autowired private MTRDailyFileRepository dailyFileRepository;
  @Autowired private PenaltyRepository penaltyRepository;
  @Autowired private PenaltyMemberRepository penaltyMemberRepository;
  @Autowired private PenaltyReviewRepository penaltyReviewRepository;
  @Autowired private UserMemComRepository userMemComRepository;
  @Autowired private MailService mailService;
  @Autowired private MTRHoMakerRepository hoMakerRepository;
  @Autowired private MemComApplicationRepository memComApplicationRepository;
  @Autowired
  private MTRMonthlyJobStatusRepository monthlyJobStatusRepository;

  @Value("classpath:Annexure_MTR_TEMPLATE.xlsx")
  private Resource annexureTemplate;

  @Value("classpath:Approval_Note_MTR_TEMPLATE.docx")
  private Resource approvalNoteTemplate;

  @Value("classpath:Penalty_letter_template.docx")
  private Resource penaltyLetterTemplate;

  @Value("classpath:Penalty_Reversal_letter_template.docx")
  private Resource penaltyReversalLetterTemplate;

  @Value("${penalty.amountRateCard.oneInstance}")
  private double oneInstanceRate;

  @Value("${penalty.amountRateCard.twoToFiveInstances}")
  private double twoToFiveInstancesRate;

  @Value("${penalty.tollFreePhoneNumber}")
  private String tollFreePhoneNumber;

  @Value("${dms.response.path}")
  private String resFileDirectory;

  /** Cron task to trigger monthly penalty calculation on the 1st day of month. */
  @Scheduled(cron = "0 0 ${penalty.scheduledTime.hour} 1 * *")
  public void penaltyCalculationMonthlyJob() {
    log.info("start scheduledPenaltyCalculation");
    LocalDate yesterday = LocalDate.now().minusDays(1);
    penaltyCalculationMonthlyJob(yesterday.getYear(), yesterday.getMonthValue());
  }

  public void penaltyCalculationMonthlyJob(int year, int month) {
    log.info("Start Penalty Calculation (Monthly Job)");
    MTRMonthlyJobStatus jobStatus = monthlyJobStatusRepository.findByJobYearAndJobMonthAndJobType(year, month, "P")
      .orElseGet(() -> {
        MTRMonthlyJobStatus newRecord = new MTRMonthlyJobStatus();
        newRecord.setReRunCounter(0);
        newRecord.setJobYear(year);
        newRecord.setJobMonth(month);
        newRecord.setJobType("P");
        return newRecord;
      });
    if (!"S".equals(jobStatus.getLastRunStatus())) {
      jobStatus.setReRunCounter(jobStatus.getReRunCounter() + 1);
      jobStatus.setLastRunStart(LocalDateTime.now());
      StringBuilder lastRunRemark = new StringBuilder();
      try {
        computePenaltyMTR(year, month);
        jobStatus.setLastRunStatus("S");
      } catch (Exception e) {
        jobStatus.setLastRunStatus("F");
        String message = String.format("Penalty Calculation (Monthly Job) Fail... %s ", e.getMessage());
        lastRunRemark.append(message);
        log.error(message, e);
      }

      jobStatus.setLastRunEnd(LocalDateTime.now());
      if (lastRunRemark.length() == 0) {
        lastRunRemark.append("All transactions completed, no need to re-run the Job");
      }
      jobStatus.setLastRunRemark(lastRunRemark.toString());
      monthlyJobStatusRepository.save(jobStatus);
      log.info("Penalty Calculation (Monthly Job) Completed, {}", jobStatus);
    } else {
      log.info("Penalty Calculation (Monthly Job) last run success no need to process again, {}", jobStatus);
    }
  }

  /** Compute penalty for each eligible member for the specified year and month */
  public CommonMessageDto computePenaltyMTR(int year, int month) {
    log.info("computing penalty MTR for year={}, month={}", year, month);

    if (penaltyRepository.findFirstByPenaltyTypeAndPenaltyYearAndPenaltyMonth(PenaltyType.MTR, year, month).isPresent()) {
      throw new BaseServiceException(
          String.format("the penalty record for year=%s month=%s has already existed", year, month),
          BAD_REQUEST);
    }

    MTRHoMaker hoMaker = hoMakerRepository.findFirstByMakerStatus(true).orElse(null);

    MemComApplication memComApplication = new MemComApplication();
    memComApplication.setAppId(UUID.randomUUID().toString());
    memComApplication.setAppNo(String.valueOf(RandomUtils.nextInt()));
    memComApplication.setAppStatus(PenaltyApplicationStatus.APPLICATION_UNDER_REVIEW);
    memComApplication.setSubStatus(PenaltyApplicationSubStatus.REVIEW_PENDING);
    LocalDateTime now = LocalDateTime.now();
    memComApplication.setAppStartDt(now);
    memComApplication.setAppSubmitDt(now);
    if (hoMaker != null) {
      memComApplication.setMaker(hoMaker.getMaker());
      memComApplication.setMakerAssignedDt(hoMaker.getUpdatedDate());
    }
    memComApplication.setMainRequest("MTR_PENALTY");
    memComApplication.setSubRequest(
        String.format("MTR Penalty Approval %s", CommonUtils.getAbbrMonthAndYear(year, month)));
    memComApplication.setCreatedBy(now.toString());
    memComApplication.setCreatedDate(now);
    memComApplication.setUpdatedBy(now.toString());
    memComApplication.setUpdatedDate(now);
    memComApplicationRepository.save(memComApplication);

    Penalty penalty = new Penalty();
    penalty.setPenaltyYear(year);
    penalty.setPenaltyMonth(month);
    if (hoMaker != null) {
      penalty.setMaker(hoMaker.getMaker());
    }
    penalty.setMemComApplication(memComApplication);
    now = LocalDateTime.now();
    penalty.setCreatedBy(now.toString());
    penalty.setCreatedDate(now);
    penalty.setUpdatedBy(now.toString());
    penalty.setUpdatedDate(now);
    penalty.setPenaltyType(PenaltyType.MTR);
    penaltyRepository.save(penalty);

    List<LocalDate> businessDates = CommonUtils.getBusinessDaysForYearMonth(year, month);
    memberListRepository
        .findByEligibleMemberMtrStatus(true)
        .stream()
        .forEach(
            member -> {
              computePenaltyMTRByMember(businessDates, member, penalty);
            });
    return new CommonMessageDto(200, "MTR penalty calculation successfully completed, Ok");
  }

  /** List member penalty for the specified year, month and penalty type */
  public ListMemberPenaltyResponse listMemberPenalty(int year, int month, PenaltyType penaltyType) {
    log.info("list member penalty for year={}, month={}, panaltyType={}", year, month, penaltyType.value);
    List<MemberPenaltyDetailsDto> memberPenaltyDetailsList =
        penaltyMemberRepository
            .findByPenaltyPenaltyTypeAndPenaltyPenaltyYearAndPenaltyPenaltyMonth(penaltyType, year, month)
            .stream()
            .map(
                penaltyMember -> {
                  MemberPenaltyDetailsDto dto = new MemberPenaltyDetailsDto();
                  dto.setMemberId(penaltyMember.getMember().getMemId());
                  dto.setMemberCode(penaltyMember.getMember().getMemCd());
                  dto.setMemberName(penaltyMember.getMember().getMemName());
                  dto.setOriginalPenalty(penaltyMember.getOriginalPenaltyAmt());
                  dto.setNonSubmissionCount(penaltyMember.getNonSubmissionCount());
                  dto.setNewPenalty(penaltyMember.getNewPenaltyAmt());
                  dto.setReviewStatus(penaltyMember.getReviewStatus());
                  dto.setReviewReason(
                      penaltyMember.getReviewReasonType() != null
                          ? penaltyMember.getReviewReasonType().name()
                          : null);
                  dto.setRevisedAmount(penaltyMember.getRevisedAmt());
                  dto.setRemark(penaltyMember.getRemark());
                  dto.setAgendaMinutes(penaltyMember.getAgendaMinutesFileName());
                  dto.setReversalPenaltyLetterSent(
                      PenaltyLetterStatus.SENT_TO_MEMBER
                          == penaltyMember.getGenerateReversalLetterStatus());
                  dto.setPenaltyMemberId(penaltyMember.getId());
                  return dto;
                })
            .collect(Collectors.toList());
    ListMemberPenaltyResponse response = new ListMemberPenaltyResponse();
    response.setMemberPenaltyDetails(memberPenaltyDetailsList);
    return response;
  }

  private List<PenaltyMember> updatePenaltyMembers(
      List<MemberPenaltyDetailsDto> memberPenaltyDetailsList,
      List<PenaltyMember> penaltyMemberList) {
    Map<Long, PenaltyMember> penaltyMemberMap =
        penaltyMemberList.stream().collect(Collectors.toMap(m -> m.getMember().getMemId(), m -> m));
    List<PenaltyMember> penaltyMembers = newArrayList();
    for (MemberPenaltyDetailsDto dto : memberPenaltyDetailsList) {
      PenaltyMember penaltyMember = penaltyMemberMap.get(dto.getMemberId());
      if (penaltyMember == null) {
        throw new BaseServiceException(
            String.format("the penalty member record for %s is not found", dto.getMemberId()),
            BAD_REQUEST);
      }
      double chargeAmount = penaltyMember.getOriginalPenaltyAmt();
      if (dto.getNewPenalty() != null) {
        chargeAmount = dto.getNewPenalty();
        if (penaltyMember.getOriginalPenaltyAmt() != 0
            && dto.getNewPenalty() > penaltyMember.getOriginalPenaltyAmt()) {
          throw new BaseServiceException(
              String.format(
                  "new penalty amt cannot be greater than original penalty amt, "
                      + "memberId=%s, newPenalty=%.2f, originalPenalty=%.2f",
                  dto.getMemberId(), dto.getNewPenalty(), penaltyMember.getOriginalPenaltyAmt()),
              BAD_REQUEST);
        }
      }
      if (dto.getRevisedAmount() != null) {
        chargeAmount = dto.getNewPenalty();
        if (dto.getRevisedAmount() > chargeAmount) {
          throw new BaseServiceException(
              String.format(
                  "revised penalty amt cannot be greater than previous penalty amt, "
                      + "memberId=%s, newPenalty=%.2f, previousPenalty=%.2f",
                  dto.getMemberId(), dto.getRevisedAmount(), chargeAmount),
              BAD_REQUEST);
        }
      }
      penaltyMember.setNewPenaltyAmt(dto.getNewPenalty());
      penaltyMember.setReviewStatus(dto.getReviewStatus());
      penaltyMember.setReviewReasonType(PenaltyReviewReasonType.fromName(dto.getReviewReason()));
      penaltyMember.setRevisedAmt(dto.getRevisedAmount());
      penaltyMember.setRemark(dto.getRemark());
      penaltyMember.setAgendaMinutesFileName(dto.getAgendaMinutes());

      LocalDateTime now = LocalDateTime.now();
      penaltyMember.setUpdatedBy(now.toString());
      penaltyMember.setUpdatedDate(now);
      penaltyMemberRepository.save(penaltyMember);
      penaltyMembers.add(penaltyMember);
    }
    return penaltyMembers;
  }

  public CommonMessageDto generateApprovalNoteAnnexure(
      GenerateApprovalNoteAnnexureRequest request, long nseOfficialId, PenaltyType penaltyType) {

    int year = request.getSubmissionYear();
    int month = request.getSubmissionMonth();

    List<PenaltyMember> penaltyMemberList =
        penaltyMemberRepository.findByPenaltyPenaltyTypeAndPenaltyPenaltyYearAndPenaltyPenaltyMonth(penaltyType, year, month);
    if (request.getMemberPenaltyDetails() != null) {
      penaltyMemberList =
          updatePenaltyMembers(request.getMemberPenaltyDetails(), penaltyMemberList);
      UserMemCom maker =
          userMemComRepository
              .findById(nseOfficialId)
              .orElseThrow(
                  () ->
                      new BaseServiceException(
                          String.format(
                              "the user mem com record for id=%s is not found", nseOfficialId),
                          BAD_REQUEST));
      Penalty penalty = penaltyMemberList.get(0).getPenalty();
      penalty.setMaker(maker);
      LocalDateTime now = LocalDateTime.now();
      penalty.setUpdatedBy(now.toString());
      penalty.setUpdatedDate(now);
      penaltyRepository.save(penalty);
    }

    return processApprovalNoteAnnexureCreation(penaltyMemberList, year, month, request.getOption());
  }

  private Double getChargeAmount(PenaltyMember penaltyMember) {
    Double chargeAmount = penaltyMember.getRevisedAmt();
    if (chargeAmount == null) {
      chargeAmount = penaltyMember.getNewPenaltyAmt();
    }
    if (chargeAmount == null) {
      chargeAmount = penaltyMember.getOriginalPenaltyAmt();
    }
    return chargeAmount;
  }

  private CommonMessageDto processApprovalNoteAnnexureCreation(
      List<PenaltyMember> penaltyMembers, int year, int month, Option option) {
    LocalDate date = LocalDate.of(year, month, 1);
    String fullMonthYear = CommonUtils.getFullMonthAndYear(date);
    String abbrMonthYear = CommonUtils.getAbbrMonthAndYear(date);
    Penalty penalty = penaltyMembers.get(0).getPenalty();

    // Penalty Members Excel Report
    if (option == null || option == Option.ONLY_ANNEXURE) {
      generateAnnexureReport(penalty, penaltyMembers, fullMonthYear, abbrMonthYear);
    }

    // Approval Note PDF report
    if (option == null || option == Option.ONLY_APPROVAL_NOTE) {
      generateApprovalNoteReport(penalty, penaltyMembers, year, fullMonthYear, abbrMonthYear);
    }

    // Save the penalty
    LocalDateTime now = LocalDateTime.now();
    penalty.setUpdatedBy(now.toString());
    penalty.setUpdatedDate(now);
    penaltyRepository.save(penalty);

    // Response
    String messageFormat = "%s been generated successfully";
    String message = null;
    if (option == Option.ONLY_ANNEXURE) {
      message = String.format(messageFormat, "Annexure file has");
    } else if (option == Option.ONLY_APPROVAL_NOTE) {
      message = String.format(messageFormat, "Approval Note file has");
    } else {
      message = String.format(messageFormat, "Approval Note and Annexure files have");
    }

    return new CommonMessageDto(200, message);
  }

  private void generateApprovalNoteReport(
      Penalty penalty,
      List<PenaltyMember> penaltyMembers,
      int year,
      String fullMonthYear,
      String abbrMonthYear) {
    // Build report input data
    ReportInputData approvalNoteReportInputData = new ReportInputData();
    approvalNoteReportInputData.addIndividual("approval note no.", String.valueOf(penalty.getId()));
    approvalNoteReportInputData.addIndividual("year", String.valueOf(year));
    approvalNoteReportInputData.addIndividual(
        "Today’s Date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/YYYY")));
    approvalNoteReportInputData.addIndividual("penalty month-year", fullMonthYear);
    approvalNoteReportInputData.addIndividual("Annexure Name", penalty.getAnnexureFileName());

    String makerName = "";
    if (penalty.getMaker() != null) {
      makerName =
          String.format(
              "%s %s", penalty.getMaker().getFirstName(), penalty.getMaker().getLastName());
    }
    approvalNoteReportInputData.addIndividual("Maker name", makerName);

    String checkerName = "";
    if (PenaltyStatus.APPROVED.equals(penalty.getPenaltyStatus()) && penalty.getChecker() != null) {
      checkerName =
          String.format(
              "%s %s", penalty.getChecker().getFirstName(), penalty.getChecker().getLastName());
    }
    approvalNoteReportInputData.addIndividual("Checker name", checkerName);

    // Category I, II, III tables
    ReportInputTableParameter categoryOneTable = approvalNoteReportInputData.addTable(4);
    categoryOneTable.setHavingFooter(true);
    ReportInputTableParameter categoryTwoTable = approvalNoteReportInputData.addTable(5);
    categoryTwoTable.setHavingFooter(true);
    ReportInputTableParameter categoryThreeTable = approvalNoteReportInputData.addTable(4);

    Double categoryOneTotalChargeAmount = 0.0;
    Double categoryTwoTotalChargeAmount = 0.0;
    Double categoryThreeTotalChargeAmount = 0.0;

    // Build the content rows
    for (PenaltyMember penaltyMember : penaltyMembers) {
      int nonSubmissionCount = penaltyMember.getNonSubmissionCount();

      Double chargeAmount = getChargeAmount(penaltyMember);
      if (chargeAmount == null) {
        chargeAmount = penaltyMember.getOriginalPenaltyAmt();
      }
      String chargeAmountString = String.format("%.2f", chargeAmount);

      if (nonSubmissionCount > 5) {
        categoryThreeTotalChargeAmount += chargeAmount;
        categoryThreeTable.addRow(
            String.valueOf(categoryThreeTable.getRows().size() + 1), // Sr. No.
            penaltyMember.getMember().getMemName(),
            String.valueOf(nonSubmissionCount),
            penaltyMember.getRemark() != null ? penaltyMember.getRemark() : "");
      } else if (nonSubmissionCount > 1) {
        categoryTwoTotalChargeAmount += chargeAmount;
        categoryTwoTable.addRow(
            String.valueOf(categoryTwoTable.getRows().size() + 1), // Sr. No.
            penaltyMember.getMember().getMemCd(),
            penaltyMember.getMember().getMemName(),
            String.valueOf(nonSubmissionCount),
            chargeAmountString);
      } else if (nonSubmissionCount == 1) {
        categoryOneTotalChargeAmount += chargeAmount;
        categoryOneTable.addRow(
            String.valueOf(categoryOneTable.getRows().size() + 1), // Sr. No.
            penaltyMember.getMember().getMemCd(),
            penaltyMember.getMember().getMemName(),
            chargeAmountString);
      }
    }

    approvalNoteReportInputData.addIndividual(
        "category1-count",
        categoryOneTable.getRows().isEmpty()
            ? "-"
            : String.valueOf(categoryOneTable.getRows().size()));
    approvalNoteReportInputData.addIndividual(
        "category2-count",
        categoryTwoTable.getRows().isEmpty()
            ? "-"
            : String.valueOf(categoryTwoTable.getRows().size()));
    approvalNoteReportInputData.addIndividual(
        "category3-count",
        categoryThreeTable.getRows().isEmpty()
            ? "-"
            : String.valueOf(categoryThreeTable.getRows().size()));

    // Build the total row
    if (categoryOneTable.getRows().isEmpty()) {
      // Add "-" rows
      categoryOneTable.addRow("-", "-", "-", "-");
      categoryOneTable.addRow("", "Total", "", "-");
    } else {
      categoryOneTable.addRow("", "Total", "", String.format("%.2f", categoryOneTotalChargeAmount));
    }
    if (categoryTwoTable.getRows().isEmpty()) {
      // Add "-" rows
      categoryTwoTable.addRow("-", "-", "-", "-", "-");
      categoryTwoTable.addRow("", "Total", "", "", "-");
    } else {
      categoryTwoTable.addRow(
          "", "Total", "", "", String.format("%.2f", categoryTwoTotalChargeAmount));
    }

    // Generate Word and PDF reports
    try {
      String reportContent =
          ReportUtils.generateWordReport(
              approvalNoteReportInputData, approvalNoteTemplate.getInputStream());
      
      File filePath = new File(System.getProperty("user.dir") + File.separator + resFileDirectory);
      if (!filePath.isDirectory()) filePath.mkdirs();
      
      Files.write(
          Paths.get(
              System.getProperty("user.dir"),
              resFileDirectory,
              String.format("Approval Note %s %s.docx", penalty.getPenaltyType().value, abbrMonthYear.replaceAll(" ", ""))),
          Base64.getDecoder().decode(reportContent),
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);

      reportContent =
          ReportUtils.generatePdfReport(
              approvalNoteReportInputData, approvalNoteTemplate.getInputStream());
      Path path =
          Paths.get(
              System.getProperty("user.dir"),
              resFileDirectory,
              String.format("Approval Note %s %s.pdf", penalty.getPenaltyType().value, abbrMonthYear.replaceAll(" ", "")));
      Files.write(
          path,
          Base64.getDecoder().decode(reportContent),
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);

      penalty.setApprovalFileName(path.getFileName().toString());
      penalty.setApprovalNoteDmsIndex(path.toString());
    } catch (IOException ex) {
      log.error("generate approval note fail", ex);
      throw new BaseServiceException(
          "generate approval note error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private void generateAnnexureReport(
      Penalty penalty,
      List<PenaltyMember> penaltyMembers,
      String fullMonthYear,
      String abbrMonthYear) {
    // Build report input data
    ReportInputData penaltyReportInputData = new ReportInputData();

    // Submission month year
    penaltyReportInputData.addIndividual("Submission Month-Year", fullMonthYear);

    // Penalty members table
    ReportInputTableParameter penaltyMemberTable = penaltyReportInputData.addTable();
    penaltyMemberTable.setTableName("tableName-1");
    penaltyMemberTable.addColumnHeader("MEMBER ID");
    penaltyMemberTable.addColumnHeader("MEMBER CODE");
    penaltyMemberTable.addColumnHeader("MEMBER NAME");
    penaltyMemberTable.addColumnHeader("CENTRE");
    penaltyMemberTable.addColumnHeader("CHARGES FOR \nNON SUBMISSION");
    penaltyMemberTable.addColumnHeader("Reason");
    penaltyMemberTable.addColumnHeader("Additional Remarks");

    double totalChargeAmount = 0.0;
    for (PenaltyMember penaltyMember : penaltyMembers) {
      List<String> row = new ArrayList<>();
      penaltyMemberTable.getRows().add(row);

      row.add(penaltyMember.getMember().getMemId().toString());
      row.add(penaltyMember.getMember().getMemCd());
      row.add(penaltyMember.getMember().getMemName());
      row.add(penaltyMember.getMember().getMemCentre());

      Double chargeAmount = getChargeAmount(penaltyMember);
      row.add(String.format("%.2f", chargeAmount));

      String reason =
          penaltyMember.getReviewReasonType() != null
              ? penaltyMember.getReviewReasonType().name()
              : "";
      row.add(reason);

      row.add(penaltyMember.getRemark());

      totalChargeAmount += chargeAmount;
    }

    // Total
    penaltyReportInputData.addIndividual("total", String.format("%.2f", totalChargeAmount));

    // Generate Excel report
    try {
      String reportContent =
          ReportUtils.generateExcelReport(
              penaltyReportInputData, annexureTemplate.getInputStream());
      
      File filePath = new File(System.getProperty("user.dir") + File.separator + resFileDirectory);
      if (!filePath.isDirectory()) filePath.mkdirs();
      
      Path path =
          Paths.get(
              System.getProperty("user.dir"),
              resFileDirectory,
              String.format("Annexure %s %s.xlsx", penalty.getPenaltyType().value,  abbrMonthYear.replaceAll(" ", "")));

      Files.write(
          path,
          Base64.getDecoder().decode(reportContent),
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);

      penalty.setAnnexureFileName(path.getFileName().toString());
      penalty.setAnnexureFileDmsIndex(path.toString());
    } catch (IOException ex) {
      log.error("generate annexure fail", ex);
      throw new BaseServiceException("generate annexure error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public NonSubmissionCountResponse downloadMemberNonSubmissionFile(
      NonSubmissionCountRequest request) {
    Optional<PenaltyMember> penaltyMember =
        penaltyMemberRepository.findByMember_MemIdAndPenalty_PenaltyYearAndPenalty_PenaltyMonth(
            request.getMemberId(), request.getSubmissionYear(), request.getSubmissionMonth());
    if (penaltyMember.isEmpty()) {
      throw new BaseServiceException(
          String.format("the penalty member record for %s is not found", request.getMemberId()),
          BAD_REQUEST);
    }
    try {
      StringWriter stringWriter = new StringWriter();
      String[] header = {
        "MEMBER ID", "MEMBER CODE", "MEMBER NAME", "CENTRE", "Date of non-submission"
      };
      CSVPrinter csvPrinter = new CSVPrinter(stringWriter, CSVFormat.DEFAULT.withHeader(header));
      MemberMaster member = penaltyMember.get().getMember();
      List<LocalDate> businessDates =
          CommonUtils.getBusinessDaysForYearMonth(
              request.getSubmissionYear(), request.getSubmissionMonth());
      List<LocalDate> nonSubmissionDates = calculateNonSubmissionDates(businessDates, member);
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
      for (LocalDate date : nonSubmissionDates) {
        csvPrinter.printRecord(
            member.getMemId(),
            member.getMemCd(),
            member.getMemName(),
            member.getMemCentre(),
            date.format(formatter));
      }
      return new NonSubmissionCountResponse(
          200, new String(Base64.getEncoder().encode(stringWriter.toString().getBytes("UTF-8"))));
    } catch (IOException e) {
      log.error("download member non submission file fail", e);
      throw new BaseServiceException(
          "download member non submission file error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public ReviewCommentsDto getReviewComments(int year, int month, PenaltyType penaltyType) {
    Penalty penalty =
        penaltyRepository
            .findFirstByPenaltyTypeAndPenaltyYearAndPenaltyMonth(penaltyType, year, month)
            .orElseThrow(
                () ->
                    new BaseServiceException(
                        String.format(
                            "the penalty record for penaltyType =%s year=%s month=%s is not found", penaltyType.value, year, month),
                        BAD_REQUEST));
    PenaltyReview review =
        penaltyReviewRepository
            .findFirstByPenaltyPenaltyTypeAndPenaltyPenaltyYearAndPenaltyPenaltyMonth(penaltyType, year, month)
            .orElse(new PenaltyReview());
    ReviewCommentsDto response = new ReviewCommentsDto();
    if (penalty.getMaker() != null) {
      response.setMakerId(penalty.getMaker().getId());
      response.setMakerFirstName(penalty.getMaker().getFirstName());
      response.setMakerLastName(penalty.getMaker().getLastName());
    }
    if (penalty.getChecker() != null) {
      response.setCheckerId(penalty.getChecker().getId());
      response.setCheckerFirstName(penalty.getChecker().getFirstName());
      response.setCheckerLastName(penalty.getChecker().getLastName());
    }
    if (review.getReviewStatus() != null) {
      response.setReviewStatus(review.getReviewStatus().getValue());
      response.setStatusAnnexureFile(review.getReviewStatus().getValue());
      response.setStatusApprovalNote(review.getReviewStatus().getValue());
    } else {
      response.setReviewStatus(PenaltyStatus.NOT_SENT_TO_CHECKER.getValue());
      response.setStatusAnnexureFile(PenaltyStatus.NOT_SENT_TO_CHECKER.getValue());
      response.setStatusApprovalNote(PenaltyStatus.NOT_SENT_TO_CHECKER.getValue());
    }
    if (review.getUserType() != null) {
      response.setUserType(review.getUserType().name());
    }
    response.setMakerCommentsAnnexureFile(jsonToObjectList(review.getMakerCommentsAnnexureFile()));
    response.setMakerCommentsApprovalNote(jsonToObjectList(review.getMakerCommentsNote()));
    response.setCheckerCommentsAnnexureFile(
        jsonToObjectList(review.getCheckerCommentsAnnexureFile()));
    response.setCheckerCommentsApprovalNote(jsonToObjectList(review.getCheckerCommentsNote()));
    response.setAnnexureFile(penalty.getAnnexureFileName());
    response.setApprovalNote(penalty.getApprovalFileName());
    response.setInspectionFileName(penalty.getInspectionFileName());
    response.setPenaltyRecId(penalty.getId());
    return response;
  }

  public CommonMessageDto postReviewComments(
      ReviewCommentsRequest request, long nseOfficialId, PenaltyType penaltyType) {
    Penalty penalty =
        penaltyRepository
            .findById(request.getPenaltyRecId())
            .orElseThrow(
                () ->
                    new BaseServiceException(
                        String.format(
                            "the penalty record for id=%s is not found", request.getPenaltyRecId()),
                        BAD_REQUEST));
    if (penalty.getMaker() == null) {
      throw new BaseServiceException(
          String.format("penalty has not been assigned maker yet"), BAD_REQUEST);
    }
    if (request.getCheckerId() == penalty.getMaker().getId()) {
      throw new BaseServiceException(
          String.format(
              "Maker[id=%d] should select a different checker[id=%d]",
              penalty.getMaker().getId(), request.getCheckerId()),
          BAD_REQUEST);
    }
    LocalDateTime now = LocalDateTime.now();
    if (penalty.getMaker().getId() == nseOfficialId && request.getCheckerId() != null) {
      UserMemCom checker =
          userMemComRepository
              .findById(request.getCheckerId())
              .orElseThrow(
                  () ->
                      new BaseServiceException(
                          String.format(
                              "the user mem com record for id=%s is not found",
                              request.getCheckerId()),
                          BAD_REQUEST));
      if (penalty.getChecker() == null || penalty.getChecker().getId() != request.getCheckerId()) {
        penalty.setChecker(checker);
        penalty.setUpdatedBy(now.toString());
        penalty.setUpdatedDate(now);
        penaltyRepository.save(penalty);
      }
    }
    if (penalty.getChecker() == null) {
      throw new BaseServiceException(
          String.format(
              "maker[%d] haven't assign a checker for this penalty in the request",
              penalty.getMaker().getId()),
          BAD_REQUEST);
    }
    PenaltyReview review =
        penaltyReviewRepository
            .findFirstByPenalty(penalty)
            .orElseGet(
                () -> {
                  PenaltyReview newReview = new PenaltyReview();
                  newReview.setCreatedBy(now.toString());
                  newReview.setCreatedDate(now);
                  newReview.setPenalty(penalty);
                  return newReview;
                });
    review.setPenalty(penalty);

    long timestamp = Instant.now().getEpochSecond();
    if (nseOfficialId == penalty.getMaker().getId()) {
      review.setReviewStatus(PenaltyStatus.FOR_CHECKER_REVIEW);
      review.setMakerCommentsAnnexureFile(
          appendComment(
              review.getMakerCommentsAnnexureFile(),
              request.getMakerCommentsAnnexureFile(),
              timestamp));
      review.setMakerCommentsNote(
          appendComment(
              review.getMakerCommentsNote(), request.getMakerCommentsApprovalNote(), timestamp));
    } else if (nseOfficialId == penalty.getChecker().getId()) {
      review.setReviewStatus(PenaltyStatus.FOR_MAKER_REVIEW);
      review.setCheckerCommentsAnnexureFile(
          appendComment(
              review.getCheckerCommentsAnnexureFile(),
              request.getCheckerCommentsAnnexureFile(),
              timestamp));
      review.setCheckerCommentsNote(
          appendComment(
              review.getCheckerCommentsNote(),
              request.getCheckerCommentsApprovalNote(),
              timestamp));
    } else {
      throw new BaseServiceException(
          String.format(
              "nseOfficialId[%d] is neither maker[id=%d] nor checker[id=%d] of this penalty",
              nseOfficialId, penalty.getMaker().getId(), penalty.getChecker().getId()),
          BAD_REQUEST);
    }
    review.setUpdatedBy(now.toString());
    review.setUpdatedDate(now);
    penaltyReviewRepository.save(review);
    return new CommonMessageDto(200, "Review Comments added successfully, OK");
  }

  private String appendComment(String json, String comment, long timestamp) {
    if (StringUtils.isNotBlank(comment)) {
      List<ReviewCommentMessage> comments = jsonToObjectList(json);
      ReviewCommentMessage message = new ReviewCommentMessage();
      message.setTimestamp(timestamp);
      message.setComment(comment);
      comments.add(message);
      return objectToJson(comments);
    } else {
      return json;
    }
  }

  public WithdrawalMbrDtlsResponse getWithdrawalMbrDtls(
      LocalDate fromDate, LocalDate toDate, LocalDate currentDate) {
    if (fromDate == null || toDate == null) {
      if (currentDate == null) {
        throw new BaseServiceException(
            "Invalid input request, user must provide either [fromDate, toDate] or currentDate.",
            BAD_REQUEST);
      } else {
        fromDate = currentDate;
        toDate = currentDate;
      }
    }

    String fromDateStr = CommonUtils.getDatabaseDateStr(fromDate);
    String toDateStr = CommonUtils.getDatabaseDateStr(toDate);

    List<MTRMemberList> mtrMemberLists =
        memberListRepository.findByWithdrawalReasonAndCoolingPeriodStartDateBetween(
            true, fromDateStr, toDateStr);
    List<WithdrawalStatusMemberDto> withdrawalStatusMemberList =
        mtrMemberLists
            .stream()
            .map(
                m -> {
                  WithdrawalStatusMemberDto dto = new WithdrawalStatusMemberDto();
                  dto.setMemberName(m.getMember().getMemName());
                  dto.setMemberCode(m.getMember().getMemCd());
                  dto.setForMonth(CommonUtils.getAbbrMonthAndYear(m.getCoolingPeriodStartDate()));
                  dto.setCoolingPeriodStartDate(
                      CommonUtils.getNumberedDateStr(m.getCoolingPeriodStartDate()));
                  dto.setCoolingPeriodEndDate(
                      CommonUtils.getNumberedDateStr(m.getCoolingPeriodEndDate()));
                  return dto;
                })
            .collect(Collectors.toList());
    WithdrawalMbrDtlsResponse response = new WithdrawalMbrDtlsResponse();
    response.setWithdrawalStatusMemberList(withdrawalStatusMemberList);

    String sql = memberListRepository.SQL_FIND_WITHDRAWAL_BETWEEN;
    Map<String, String> replacementStrings =
        Map.of(
            ":withdrawal", "1",
            ":startDate", String.format("'%s'", fromDateStr),
            ":endDate", String.format("'%s'", toDateStr));
    for (Map.Entry<String, String> e : replacementStrings.entrySet()) {
      sql = sql.replace(e.getKey(), e.getValue());
    }
    response.setWithdrawalSqlQuery(sql);

    return response;
  }

  public List<LocalDate> calculateNonSubmissionDates(
      int year, int month, MemberMaster memberMaster) {
    List<LocalDate> businessDates = CommonUtils.getBusinessDaysForYearMonth(year, month);
    return calculateNonSubmissionDates(businessDates, memberMaster);
  }

  public List<LocalDate> calculateNonSubmissionDates(
      List<LocalDate> businessDates, MemberMaster memberMaster) {
    List<MTRDailyFile> submissions =
        dailyFileRepository.findByDailyFileStatusTrueAndMember_MemIdAndReportingDateBetween(
            memberMaster.getMemId(),
            LocalTime.MIN.atDate(businessDates.get(0)),
            LocalTime.MAX.atDate(businessDates.get(businessDates.size() - 1)));
    Set<LocalDate> reportingDates =
        submissions
            .stream()
            .map(f -> f.getReportingDate().toLocalDate())
            .collect(Collectors.toSet());
    log.info("reportingDates={}", reportingDates);
    Set<LocalDate> dateSet = new HashSet<>(businessDates);
    dateSet.removeAll(reportingDates);
    List<LocalDate> nonSubmissionDates = new ArrayList<>(dateSet);
    Collections.sort(nonSubmissionDates);
    log.info("nonSubmissionDates={}", nonSubmissionDates);
    return nonSubmissionDates;
  }

  public Map<MemberMaster, List<LocalDate>> calculateNonSubmissionDates(
      List<LocalDate> businessDates) {

    List<MTRDailyFile> submissions =
        dailyFileRepository.findByDailyFileStatusTrueAndReportingDateBetween(
            LocalTime.MIN.atDate(businessDates.get(0)),
            LocalTime.MAX.atDate(businessDates.get(businessDates.size() - 1)));
    Map<Long, List<LocalDate>> result = new HashMap<>();
    Map<MemberMaster, List<LocalDate>> finalResult = new HashMap<>();
    Map<Long, MemberMaster> memberMapping = new HashMap<>();
    for (MTRDailyFile file : submissions) {
      memberMapping.put(file.getMember().getMemId(), file.getMember());
      List<LocalDate> list = result.get(file.getMember().getMemId());
      if (list == null) {
        list = new ArrayList<>();
        result.put(file.getMember().getMemId(), list);
      }
      list.add(file.getReportingDate().toLocalDate());
    }
    for (Map.Entry<Long, List<LocalDate>> entry : result.entrySet()) {
      Long memberId = entry.getKey();
      Set<LocalDate> dateSet = new HashSet<>(businessDates);
      dateSet.removeAll(entry.getValue());
      result.put(memberId, new ArrayList<>(dateSet));
      finalResult.put(memberMapping.get(memberId), new ArrayList<>(dateSet));
    }
    return finalResult;
  }

  private void computePenaltyMTRByMember(
      List<LocalDate> businessDates, MTRMemberList mtrMember, Penalty penalty) {
    log.info("compute penalty for member={}", mtrMember);
    
    List<LocalDate> validDates = businessDates.stream().filter(
    		f -> !f.isBefore(mtrMember.getEligibleMemberMtrFrom())).collect(Collectors.toList());

    if (validDates.isEmpty()) {
      // No penalty
      return;
    }
    
    List<LocalDate> nonSubmissionDates =
        calculateNonSubmissionDates(validDates, mtrMember.getMember());
    if (nonSubmissionDates.isEmpty()) {
      // No penalty
      return;
    }

    double penaltyAmount =
          oneInstanceRate
              + twoToFiveInstancesRate
                  * Math.min(WITHDRAWAL_THRESHOLD - 1, nonSubmissionDates.size() - 1);

    if (nonSubmissionDates.size() > WITHDRAWAL_THRESHOLD) {
      mtrMember.setWithdrawalReason(true);
      LocalDate coolingPeriodStartDate = nonSubmissionDates.get(WITHDRAWAL_THRESHOLD - 1);
      mtrMember.setCoolingPeriodStartDate(coolingPeriodStartDate);
      mtrMember.setCoolingPeriodEndDate(coolingPeriodStartDate.plusMonths(6));
      log.info("withdrawal member from MTR for member={}", mtrMember);
      LocalDateTime now = LocalDateTime.now();
      mtrMember.setUpdatedBy(now.toString());
      mtrMember.setUpdatedDate(now);
      memberListRepository.save(mtrMember);
    }

    PenaltyMember penaltyMember = new PenaltyMember();
    penaltyMember.setMember(mtrMember.getMember());
    penaltyMember.setPenalty(penalty);
    penaltyMember.setNonSubmissionCount(nonSubmissionDates.size());
    penaltyMember.setOriginalPenaltyAmt(penaltyAmount);
    penaltyMember.setReviewStatus(false);
    LocalDateTime now = LocalDateTime.now();
    penaltyMember.setCreatedBy(now.toString());
    penaltyMember.setCreatedDate(now);
    penaltyMember.setUpdatedBy(now.toString());
    penaltyMember.setUpdatedDate(now);
    penaltyMemberRepository.save(penaltyMember);
  }

  public CommonMessageDto postPenaltyApproval(PenaltyApprovalRequest request, long nseOfficialId, PenaltyType penaltyType) {
    int year = request.getSubmissionYear();
    int month = request.getSubmissionMonth();
    Penalty penalty =
        penaltyRepository
            .findFirstByPenaltyTypeAndPenaltyYearAndPenaltyMonth(penaltyType, year, month)
            .orElseThrow(
                () ->
                    new BaseServiceException(
                        String.format(
                            "the penalty record for year=%s month=%s is not found", year, month),
                        BAD_REQUEST));
    if (penalty.getChecker() == null) {
      throw new BaseServiceException(
          String.format("penalty [year=%d, month=%d] hasn't assign checker yet.", year, month),
          BAD_REQUEST);
    }

    if (penalty.getChecker().getId() != nseOfficialId) {
      throw new BaseServiceException(
          String.format(
              "user [id=%d] is not the assigned checker [id=%d] for this penalty [year=%d, month=%d]",
              nseOfficialId, penalty.getChecker().getId(), year, month),
          BAD_REQUEST);
    }
    PenaltyReview review =
        penaltyReviewRepository
            .findFirstByPenaltyPenaltyTypeAndPenaltyPenaltyYearAndPenaltyPenaltyMonth(penaltyType, year, month)
            .orElseThrow(
                () ->
                    new BaseServiceException(
                        String.format(
                            "penalty review record is not found for year=%d, month=%d",
                            year, month),
                        BAD_REQUEST));

    penalty.setPenaltyStatus(PenaltyStatus.APPROVED);
    LocalDateTime now = LocalDateTime.now();
    penalty.setUpdatedBy(now.toString());
    penalty.setUpdatedDate(now);
    penaltyRepository.save(penalty);

    List<PenaltyMember> penaltyMemberList =
        penaltyMemberRepository.findByPenaltyPenaltyTypeAndPenaltyPenaltyYearAndPenaltyPenaltyMonth(penaltyType, year, month);
    penaltyMemberList.forEach(
        pm -> {
          pm.setReviewStatus(true);
          LocalDateTime pmNow = LocalDateTime.now();
          pm.setUpdatedBy(pmNow.toString());
          pm.setUpdatedDate(pmNow);
          penaltyMemberRepository.save(pm);
        });

    review.setReviewStatus(PenaltyStatus.APPROVED);
    review.setStatusAnnexureFile(PenaltyStatus.APPROVED.name());
    review.setStatusApprovalNote(PenaltyStatus.APPROVED.name());
    now = LocalDateTime.now();
    review.setUpdatedBy(now.toString());
    review.setUpdatedDate(now);
    penaltyReviewRepository.save(review);

    return new CommonMessageDto(200, String.format("%s penalty successfully posted for approval, OK", penaltyType.value));
  }

  public CommonMessageDto putPenaltyDispute(PenaltyDisputeRequest request, long nseOfficialId, PenaltyType penaltyType) {

    int year = request.getSubmissionYear();
    int month = request.getSubmissionMonth();
    Penalty penalty =
        penaltyRepository
            .findFirstByPenaltyTypeAndPenaltyYearAndPenaltyMonth(penaltyType, year, month)
            .orElseThrow(
                () ->
                    new BaseServiceException(
                        String.format(
                            "the penalty record for penaltyType=%s year=%s month=%s is not found", penaltyType.value, year, month),
                        BAD_REQUEST));
    Set<Long> validOperators = new HashSet<>();
    if (penalty.getMaker() != null) {
      validOperators.add(penalty.getMaker().getId());
    }
    if (penalty.getChecker() != null) {
      validOperators.add(penalty.getChecker().getId());
    }
    if (!validOperators.contains(nseOfficialId)) {
      throw new BaseServiceException(
          String.format(
              "user [id=%d] is not the assigned maker or checker for this penalty [year=%d, month=%d]",
              nseOfficialId, year, month),
          BAD_REQUEST);
    }

    List<PenaltyMember> penaltyMemberList =
        penaltyMemberRepository.findByPenaltyPenaltyTypeAndPenaltyPenaltyYearAndPenaltyPenaltyMonth(penaltyType, year, month);
    List<PenaltyMember> penaltyMembers =
        updatePenaltyMembers(request.getMemberPenaltyDetails(), penaltyMemberList);

    return processApprovalNoteAnnexureCreation(penaltyMembers, year, month, null);
  }

  public PenaltyLetterDetailsResponse getPenaltyLetterDetails(
      int year, int month, String penaltyLetterTypeStr, PenaltyType penaltyType) {

    List<PenaltyLetterDetailsDto> penaltyLetterDetails = newArrayList();
    PenaltyLetterType letterType = PenaltyLetterType.fromName(penaltyLetterTypeStr);
    List<PenaltyMember> penaltyMemberList =
        penaltyMemberRepository.findByPenaltyPenaltyTypeAndPenaltyPenaltyYearAndPenaltyPenaltyMonth(penaltyType, year, month);
    penaltyMemberList.forEach(
        pm -> {
          PenaltyLetterDetailsDto dto = new PenaltyLetterDetailsDto();
          dto.setMemberId(pm.getMember().getMemId());
          dto.setMemberCode(pm.getMember().getMemCd());
          dto.setMemberName(pm.getMember().getMemName());
          dto.setPenaltyAmount(getChargeAmount(pm));
          dto.setPenaltySubmissionCycle(CommonUtils.getAbbrMonthAndYear(year, month));
          dto.setPenaltyLetterType(letterType.name());
          String letterStatus = PenaltyLetterStatus.NOT_GENERATED.name();
          switch (letterType) {
            case PENALTY_LETTER:
              if (pm.getGenerateLetterStatus() != null) {
                letterStatus = pm.getGenerateLetterStatus().name();
              }
              dto.setPenaltyLetterFileName(pm.getPenaltyLetterFileName());
              break;
            case PENALTY_REVERSAL_LETTER:
              if (pm.getGenerateReversalLetterStatus() != null) {
                letterStatus = pm.getGenerateReversalLetterStatus().name();
              }
              dto.setPenaltyLetterFileName(pm.getPenaltyReversalLetterFileName());
              break;
          }
          dto.setPenaltyLetterStatus(letterStatus);
          if (pm.getReviewReasonType() != null) {
            dto.setAction(pm.getReviewReasonType().name());
          }
          penaltyLetterDetails.add(dto);
        });

    PenaltyLetterDetailsResponse response = new PenaltyLetterDetailsResponse();
    response.setPenaltyLetterDetails(penaltyLetterDetails);
    return response;
  }

  public PenaltyLetterDetailsResponse generatePenaltyLetters(
      PenaltyLettersRequest request, long nseOfficialId, PenaltyType penaltyType) {

    int year = request.getSubmissionYear();
    int month = request.getSubmissionMonth();
    List<PenaltyLetterDetailsDto> penaltyLetterDetails = newArrayList();
    Map<Long, PenaltyMember> penaltyMemberMap =
        penaltyMemberRepository
            .findByPenaltyPenaltyTypeAndPenaltyPenaltyYearAndPenaltyPenaltyMonth(penaltyType, year, month)
            .stream()
            .collect(Collectors.toMap(m -> m.getMember().getMemId(), m -> m));
    request
        .getMemberPenaltyDetails()
        .forEach(
            dto -> {
              PenaltyMember pm = penaltyMemberMap.get(dto.getMemberId());
              if (pm == null) {
                throw new BaseServiceException(
                    String.format(
                        "the penalty member record for %s is not found", dto.getMemberId()),
                    BAD_REQUEST);
              }

              PenaltyLetterType letterType = PenaltyLetterType.fromName(dto.getPenaltyLetterType());

              if (PenaltyLetterType.PENALTY_LETTER.equals(letterType)) {
                // Not allow to re-generate the letter
                if (pm.getGenerateLetterStatus() != null && pm.getGenerateLetterStatus() != PenaltyLetterStatus.NOT_GENERATED) {
                  throw new BaseServiceException(
                          String.format(
                              "the penalty letter has been generated for %s already", dto.getMemberId()),
                          BAD_REQUEST);
                }

                try {
                  // Penalty letter
                  Path pdfFilePath = generatePenaltyLetter(pm, year, month);

                  // Update
                  pm.setGenerateLetterStatus(PenaltyLetterStatus.NOT_SENT);
                  pm.setPenaltyLetterFileName(pdfFilePath.getFileName().toString());
                  pm.setPenaltyLetterDmsIndex(pdfFilePath.toString());

                  dto.setPenaltyLetterStatus(PenaltyLetterStatus.NOT_SENT.name());
                  dto.setPenaltyLetterFileName(pm.getPenaltyLetterFileName());
                } catch (IOException e) {
                  log.error("generate penalty letter fail", e);
                  throw new BaseServiceException(
                      "generate penalty letter error", HttpStatus.INTERNAL_SERVER_ERROR);
                }
              } else if (PenaltyLetterType.PENALTY_REVERSAL_LETTER.equals(letterType)) {
                // Not allow to re-generate the letter
                if (pm.getGenerateReversalLetterStatus() != null && pm.getGenerateReversalLetterStatus() != PenaltyLetterStatus.NOT_GENERATED) {
                    throw new BaseServiceException(
                            String.format(
                                "the penalty reveral letter has been generated for %s already", dto.getMemberId()),
                            BAD_REQUEST);
                }

                try {
                  // Penalty Reversal letter
                  Path pdfFilePath = generatePenaltyReversalLetter(pm, year, month);

                  // Update
                  pm.setGenerateReversalLetterStatus(PenaltyLetterStatus.NOT_SENT);
                  pm.setPenaltyReversalLetterFileName(pdfFilePath.getFileName().toString());
                  pm.setPenaltyReversalLetterDmsIndex(pdfFilePath.toString());

                  dto.setPenaltyLetterStatus(PenaltyLetterStatus.NOT_SENT.name());
                  dto.setPenaltyLetterFileName(pm.getPenaltyReversalLetterFileName());
                } catch (IOException e) {
                  log.error("generate penalty reversal letter fail", e);
                  throw new BaseServiceException(
                      "generate penalty reversal letter error", HttpStatus.INTERNAL_SERVER_ERROR);
                }
              }

              // Save
              LocalDateTime now = LocalDateTime.now();
              pm.setUpdatedBy(now.toString());
              pm.setUpdatedDate(now);
              penaltyMemberRepository.save(pm);

              dto.setMemberCode(pm.getMember().getMemCd());
              dto.setMemberName(pm.getMember().getMemName());
              dto.setPenaltyAmount(getChargeAmount(pm));
              dto.setPenaltySubmissionCycle(CommonUtils.getAbbrMonthAndYear(year, month));
              if (pm.getReviewReasonType() != null) {
                dto.setAction(pm.getReviewReasonType().name());
              }
              penaltyLetterDetails.add(dto);
            });

    // Generate inspection penalty file if all penalty letters are sent to all members
    try {
      generateInspectionPenaltyFile(penaltyMemberMap.values().stream().collect(Collectors.toList()), year, month);
    } catch (IOException ex) {
      log.error("failed to generate inspection penalty file", ex);
      throw new BaseServiceException(
          "failed to generate inspection penalty file", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    PenaltyLetterDetailsResponse response = new PenaltyLetterDetailsResponse();
    response.setPenaltyLetterDetails(penaltyLetterDetails);
    return response;
  }

  private Path generatePenaltyReversalLetter(PenaltyMember penaltyMember, int year, int month)
      throws IOException {
    ReportInputData reportInputData = new ReportInputData();

    reportInputData.addIndividual("TM ID", String.valueOf(penaltyMember.getMember().getMemId()));
    reportInputData.addIndividual("outward no", String.valueOf(penaltyMember.getId()));
    reportInputData.addIndividual("today date", CommonUtils.getNumberedDateStr(LocalDate.now()));
    reportInputData.addIndividual("member name", penaltyMember.getMember().getMemName());
    reportInputData.addIndividual(
        "Office address line 1",
        Objects.toString(penaltyMember.getMember().getMemAddressPart1(), ""));
    reportInputData.addIndividual(
        "Office address line 2",
        Objects.toString(penaltyMember.getMember().getMemAddressPart2(), ""));
    reportInputData.addIndividual(
        "Office address line 3",
        Objects.toString(penaltyMember.getMember().getMemAddressPart3(), ""));
    reportInputData.addIndividual("month/year", CommonUtils.getFullMonthAndYear(year, month));
    reportInputData.addIndividual("amount", String.format("%.2f", getChargeAmount(penaltyMember)));
    reportInputData.addIndividual("toll free no.", tollFreePhoneNumber);

    // Word
    String reportContent =
        ReportUtils.generateWordReport(
            reportInputData, penaltyReversalLetterTemplate.getInputStream());
    
    File filePath = new File(System.getProperty("user.dir") + File.separator + resFileDirectory);
    if (!filePath.isDirectory()) filePath.mkdirs();
    
    Files.write(
        Paths.get(
            System.getProperty("user.dir"),
            resFileDirectory,
            String.format(
                "%s_%s_rev_penalty.docx",
                penaltyMember.getMember().getMemCd(),
                CommonUtils.getFullMonthAndYear(year, month))),
        Base64.getDecoder().decode(reportContent),
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING);

    // PDF
    reportContent =
        ReportUtils.generatePdfReport(
            reportInputData, penaltyReversalLetterTemplate.getInputStream());
    Path pdfFilePath =
        Paths.get(
            System.getProperty("user.dir"),
            resFileDirectory,
            String.format(
                "%s_%s_rev_penalty.pdf",
                penaltyMember.getMember().getMemCd(),
                CommonUtils.getFullMonthAndYear(year, month)));
    Files.write(
        pdfFilePath,
        Base64.getDecoder().decode(reportContent),
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING);
    return pdfFilePath;
  }

  private Path generatePenaltyLetter(PenaltyMember penaltyMember, int year, int month)
      throws IOException {
    ReportInputData reportInputData = new ReportInputData();

    reportInputData.addIndividual("TM ID", String.valueOf(penaltyMember.getMember().getMemId()));
    reportInputData.addIndividual("outward no", String.valueOf(penaltyMember.getId()));
    reportInputData.addIndividual("Today Date", CommonUtils.getNumberedDateStr(LocalDate.now()));
    reportInputData.addIndividual("member name", penaltyMember.getMember().getMemName());
    reportInputData.addIndividual(
        "Office address line 1",
        Objects.toString(penaltyMember.getMember().getMemAddressPart1(), ""));
    reportInputData.addIndividual(
        "Office address line 2",
        Objects.toString(penaltyMember.getMember().getMemAddressPart2(), ""));
    reportInputData.addIndividual(
        "Office address line 3",
        Objects.toString(penaltyMember.getMember().getMemAddressPart3(), ""));

    String fullMonthYear = CommonUtils.getFullMonthAndYear(LocalDate.of(year, month, 1));
    reportInputData.addIndividual("penalty month", fullMonthYear);

    reportInputData.addIndividual("toll free", tollFreePhoneNumber);

    String nonSubmissionDates =
        String.join(
            "\n",
            calculateNonSubmissionDates(year, month, penaltyMember.getMember())
                .stream()
                .map(date -> CommonUtils.getFullMonthDateStr(date))
                .collect(Collectors.toList()));
    reportInputData.addIndividual("Date of non-submission", nonSubmissionDates);

    reportInputData.addIndividual(
        "Amount to be charged", String.format("Rs. %.2f", getChargeAmount(penaltyMember)));
    
    File filePath = new File(System.getProperty("user.dir") + File.separator + resFileDirectory);
    if (!filePath.isDirectory()) filePath.mkdirs();

    // Word
    String reportContent =
        ReportUtils.generateWordReport(reportInputData, penaltyLetterTemplate.getInputStream());
    Files.write(
        Paths.get(
            System.getProperty("user.dir"),
            resFileDirectory,
            String.format(
                "%s_%s_penalty.docx",
                penaltyMember.getMember().getMemCd(),
                CommonUtils.getFullMonthAndYear(year, month))),
        Base64.getDecoder().decode(reportContent),
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING);

    // PDF
    reportContent =
        ReportUtils.generatePdfReport(reportInputData, penaltyLetterTemplate.getInputStream());
    Path pdfFilePath =
        Paths.get(
            System.getProperty("user.dir"),
            resFileDirectory,
            String.format(
                "%s_%s_penalty.pdf",
                penaltyMember.getMember().getMemCd(),
                CommonUtils.getFullMonthAndYear(year, month)));
    Files.write(
        pdfFilePath,
        Base64.getDecoder().decode(reportContent),
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING);
    return pdfFilePath;
  }

  public InspectionPenaltyFileResponse downloadInspectionPenaltyFile(
      InspectionPenaltyFileRequest request, long nseOfficialId, PenaltyType penaltyType) {
    int year = request.getSubmissionYear();
    int month = request.getSubmissionMonth();

    Optional<Penalty> penalty =
        penaltyRepository.findFirstByPenaltyTypeAndPenaltyYearAndPenaltyMonth(penaltyType ,year, month);

    if (penalty.isEmpty() || StringUtils.isEmpty(penalty.get().getInspectionFileDmsIndex())) {
      throw new BaseServiceException(
          String.format(
              "there is no penalty inspection file for penaltyType=%s year=%s month=%s", penaltyType.value, year, month),
          BAD_REQUEST);
    }

    Path filePath = Paths.get(penalty.get().getInspectionFileDmsIndex());
    try {
      return new InspectionPenaltyFileResponse(
          200, new String(Base64.getEncoder().encode(Files.readAllBytes(filePath))));
    } catch (IOException ex) {
      log.error("failed to read inspection penalty file", ex);
      throw new BaseServiceException(
          "failed to read the inspection penalty file", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public CommonMessageDto sendPenaltyLetterEmail(
      PenaltyLetterEmailRequest request, long nseOfficialId, PenaltyType penaltyType) {
    int year = request.getSubmissionYear();
    int month = request.getSubmissionMonth();
    Map<Long, PenaltyMember> penaltyMemberMap =
        penaltyMemberRepository
            .findByPenaltyPenaltyTypeAndPenaltyPenaltyYearAndPenaltyPenaltyMonth(penaltyType, year, month)
            .stream()
            .collect(Collectors.toMap(m -> m.getMember().getMemId(), m -> m));

    List<Long> failedToSendMemberIds = new ArrayList<Long>();

    request
        .getMemberPenaltyDetails()
        .forEach(
            dto -> {
              PenaltyMember pm = penaltyMemberMap.get(dto.getMemberId());
              if (pm == null) {
                throw new BaseServiceException(
                    String.format(
                        "the penalty member record for %s is not found", dto.getMemberId()),
                    BAD_REQUEST);
              }

              PenaltyLetterType letterType = PenaltyLetterType.fromName(dto.getPenaltyLetterType());

              String filePath = null;
              switch (letterType) {
                case PENALTY_LETTER:
                  filePath = pm.getPenaltyLetterDmsIndex();
                  break;
                case PENALTY_REVERSAL_LETTER:
                  filePath = pm.getPenaltyReversalLetterDmsIndex();
                  break;
              }

              boolean sent = mailService.sendPenaltyLetterEmail(
                  pm.getMember().getEmail(),
                  letterType,
                  CommonUtils.getFullMonthAndYear(year, month),
                  new File(filePath));
              if (!sent) {
                failedToSendMemberIds.add(dto.getMemberId());
                return;
              }

              switch (letterType) {
                case PENALTY_LETTER:
                  pm.setGenerateLetterStatus(PenaltyLetterStatus.SENT_TO_MEMBER);
                  break;
                case PENALTY_REVERSAL_LETTER:
                  pm.setGenerateReversalLetterStatus(PenaltyLetterStatus.SENT_TO_MEMBER);
                  break;
              }

              LocalDateTime now = LocalDateTime.now();
              pm.setUpdatedBy(now.toString());
              pm.setUpdatedDate(now);
              penaltyMemberRepository.save(pm);
            });

    if (failedToSendMemberIds.isEmpty()) {
      return new CommonMessageDto(
          200, "Emails have been sent successfully for all selected members, OK");
    } else {
      return new CommonMessageDto(
          200, "Failed to send emails to members: " + StringUtils.join(failedToSendMemberIds, ", "));
    }
  }

  private void generateInspectionPenaltyFile(
      List<PenaltyMember> penaltyMemberList, int year, int month) throws IOException {
    if (penaltyMemberList.isEmpty()) {
      return;
    }

    Penalty penalty = penaltyMemberList.get(0).getPenalty();

    // Generate inspection file
    StringWriter stringWriter = new StringWriter();
    String[] header = {
      "MEMBER TYPE",
      "MEMBER CODE",
      "SEGMENT",
      "PENALTY CODE",
      "AMOUNT",
      "PERCENTAGE MODIFICATION",
      "FROM DATE (dd-MM-YYYY)",
      "TO DATE (dd-MM-YYYY)"
    };

    try (CSVPrinter csvPrinter =
        new CSVPrinter(stringWriter, CSVFormat.DEFAULT.withHeader(header))) {
      for (PenaltyMember pm : penaltyMemberList) {
        csvPrinter.printRecord(
            "M", pm.getMember().getMemCd(), "CM", "MTD", getChargeAmount(pm), "", "", "");
      }

      String fileName =
          String.format(
              "%s_penalty_Inspection file.csv", CommonUtils.getAbbrMonthAndYear(year, month));
      Path filePath = Paths.get(System.getProperty("user.dir"), resFileDirectory, fileName);
      FileUtils.write(filePath.toFile(), stringWriter.toString(), "UTF-8");

      // Update database
      penalty.setInspectionFileDmsIndex(filePath.toString());
      penalty.setInspectionFileName(filePath.getFileName().toString());
      LocalDateTime now = LocalDateTime.now();
      penalty.setUpdatedBy(now.toString());
      penalty.setUpdatedDate(now);
      penaltyRepository.save(penalty);
    }
  }

  public ByteArrayOutputStream downloadPenaltyFiles(
      int year, int month, List<String> fileTypes, long nseOfficialId, PenaltyType penaltyType) {

    Penalty penalty =
        penaltyRepository
            .findFirstByPenaltyTypeAndPenaltyYearAndPenaltyMonth(penaltyType, year, month)
            .orElse(null);
    List<ImmutablePair<String, String>> filePairs = newArrayList();
    if (penalty != null) {
      String abbrMonthYear = CommonUtils.getAbbrMonthAndYear(penalty.getPenaltyYear(), penalty.getPenaltyMonth());
      List<PenaltyMember> penaltyMemberList =
        penaltyMemberRepository.findByPenaltyPenaltyTypeAndPenaltyPenaltyYearAndPenaltyPenaltyMonth(penaltyType, year, month);
      for (String fileType : fileTypes) {
        PenaltyFileType penaltyFileType = PenaltyFileType.valueOf(fileType);
        switch (penaltyFileType) {
          case AnnexureFile:
            filePairs.add(
              ImmutablePair.of(
                String.format("%s_penalty_Annexure file.%s",
                  abbrMonthYear,
                  FilenameUtils.getExtension(penalty.getAnnexureFileName())),
                penalty.getAnnexureFileDmsIndex()));
            break;
          case ApprovalNote:
            filePairs.add(
              ImmutablePair.of(
                String.format("%s_penalty_Approval Note.%s",
                  abbrMonthYear,
                  FilenameUtils.getExtension(penalty.getApprovalFileName())),
                penalty.getApprovalNoteDmsIndex()));
            break;
          case PenaltyInspectionFile:
            filePairs.add(
              ImmutablePair.of(
                String.format("%s_penalty_Inspection file.%s",
                  abbrMonthYear,
                  FilenameUtils.getExtension(penalty.getInspectionFileName())),
                penalty.getInspectionFileDmsIndex()));
            break;
          case PenaltyLetters:
            penaltyMemberList.forEach(
              pm -> {
                filePairs.add(
                  ImmutablePair.of(
                    String.format("%s_Penalty letter.%s",
                      pm.getMember().getMemCd(),
                      FilenameUtils.getExtension(pm.getPenaltyLetterFileName())),
                    pm.getPenaltyLetterDmsIndex()));
              });
            break;
          case PenaltyResolutionAgendaMinutes:
            penaltyMemberList.forEach(
              pm -> {
                filePairs.add(
                  ImmutablePair.of(
                    String.format("%s_Penalty_Resolution_Minutes.%s",
                      abbrMonthYear,
                      FilenameUtils.getExtension(pm.getAgendaMinutesFileName())),
                    pm.getAgendaMinutesDmsIndex()));
              });
            break;
          case PenaltyReversalLetter:
            penaltyMemberList.forEach(
              pm -> {
                filePairs.add(
                  ImmutablePair.of(
                    String.format("%s_penalty reversal letter.%s",
                      pm.getMember().getMemCd(),
                      FilenameUtils.getExtension(pm.getPenaltyReversalLetterFileName())),
                    pm.getPenaltyReversalLetterDmsIndex()));
              });
            break;
          case All:
            filePairs.addAll(Arrays.asList(
              ImmutablePair.of(
                String.format("%s_penalty_Annexure file.%s",
                  abbrMonthYear,
                  FilenameUtils.getExtension(penalty.getAnnexureFileName())),
                penalty.getAnnexureFileDmsIndex()),
              ImmutablePair.of(
                String.format("%s_penalty_Approval Note.%s",
                  abbrMonthYear,
                  FilenameUtils.getExtension(penalty.getApprovalFileName())),
                penalty.getApprovalNoteDmsIndex()),
              ImmutablePair.of(
                String.format("%s_penalty_Inspection file.%s",
                  abbrMonthYear,
                  FilenameUtils.getExtension(penalty.getInspectionFileName())),
                penalty.getInspectionFileDmsIndex())
            ));
            penaltyMemberList.forEach(
              pm -> {
                filePairs.addAll(Arrays.asList(
                  ImmutablePair.of(
                    String.format("%s_Penalty letter.%s",
                      pm.getMember().getMemCd(),
                      FilenameUtils.getExtension(pm.getPenaltyLetterFileName())),
                    pm.getPenaltyLetterDmsIndex()),
                  ImmutablePair.of(
                    String.format("%s_Penalty_Resolution_Minutes.%s",
                      abbrMonthYear,
                      FilenameUtils.getExtension(pm.getAgendaMinutesFileName())),
                    pm.getAgendaMinutesDmsIndex()),
                  ImmutablePair.of(
                    String.format("%s_penalty reversal letter.%s",
                      pm.getMember().getMemCd(),
                      FilenameUtils.getExtension(pm.getPenaltyReversalLetterFileName())),
                    pm.getPenaltyReversalLetterDmsIndex())
                ));
              });
            break;
        }
      }
    }
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ZipOutputStream out = new ZipOutputStream(bos);

      for (ImmutablePair<String, String> pair : filePairs) {
        if (pair.getLeft() == null || pair.getRight() == null) {
          continue;
        }
        boolean success = addFileToZip(pair.getLeft(), pair.getRight(), out);
        if (!success) {
          log.warn("addFileToZip fail for {}, ignore it", pair.getRight());
        }
      }
      out.close();
      return bos;
    } catch (IOException e) {
      throw new BaseServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public ByteArrayOutputStream downloadPenaltyLetters(long memberId, PenaltyType penaltyType) {
    List<ImmutablePair<String, String>> filePairs = newArrayList();
    penaltyMemberRepository
        .findByMemberMemIdAndPenaltyPenaltyType(memberId, penaltyType)
        .stream()
        .forEach(
            pm -> {
              System.out.println(pm);
              if (PenaltyLetterStatus.SENT_TO_MEMBER.equals(pm.getGenerateLetterStatus())) {
                filePairs.add(
                    ImmutablePair.of(pm.getPenaltyLetterFileName(), pm.getPenaltyLetterDmsIndex()));
              }
              if (PenaltyLetterStatus.SENT_TO_MEMBER.equals(pm.getGenerateReversalLetterStatus())) {
                filePairs.add(
                    ImmutablePair.of(
                        pm.getPenaltyReversalLetterFileName(),
                        pm.getPenaltyReversalLetterDmsIndex()));
              }
            });
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ZipOutputStream out = new ZipOutputStream(bos);

      for (ImmutablePair<String, String> pair : filePairs) {
        if (pair.getLeft() == null || pair.getRight() == null) {
          continue;
        }
        boolean success = addFileToZip(pair.getLeft(), pair.getRight(), out);
        if (!success) {
          log.warn("addFileToZip fail for {}, ignore it", pair.getRight());
        }
      }
      out.close();
      return bos;
    } catch (IOException e) {
      throw new BaseServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public List<SubmissionCycleDto> getSubmissionCycles(PenaltyType penaltyType) {
    return penaltyRepository
        .findByPenaltyType(penaltyType)
        .stream()
        .map(
            p -> {
              SubmissionCycleDto dto = new SubmissionCycleDto();
              dto.setYear(p.getPenaltyYear());
              dto.setMonth(p.getPenaltyMonth());
              return dto;
            })
        .collect(Collectors.toList());
  }

  public CommonMessageDto uploadAgendaMinutes(long penaltyMemberId, MultipartFile file, PenaltyType penaltyType) {

    PenaltyMember pm = penaltyMemberRepository.findByIdAndPenaltyPenaltyType(penaltyMemberId, penaltyType)
      .orElseThrow(() -> new BaseServiceException(String.format(
          "the penalty member record for id[%d], penaltyType[%s] is not found", penaltyMemberId, penaltyType.value), BAD_REQUEST));
    String uploadedFilePath = CommonUtils.getFilePath(resFileDirectory,
      String.format("%s_Penalty_Resolution_Minutes.%s",
        CommonUtils.getAbbrMonthAndYear(pm.getPenalty().getPenaltyYear(), pm.getPenalty().getPenaltyMonth()),
        FilenameUtils.getExtension(file.getOriginalFilename())
      ));
    File uploadedFile = new File(uploadedFilePath);
    try {
      try (OutputStream output = new FileOutputStream(uploadedFile, false)) {
        file.getInputStream().transferTo(output);
      }
    } catch (IOException e) {
      throw new BaseServiceException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
    pm.setAgendaMinutesFileName(uploadedFile.getName());
    pm.setAgendaMinutesDmsIndex(uploadedFilePath);
    penaltyMemberRepository.save(pm);
    return new CommonMessageDto(200, "uploadAgendaMinutes successfully completed, Ok");
  }
}
