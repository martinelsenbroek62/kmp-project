package com.nseindia.mc.service.mtrReport;

import com.nseindia.mc.controller.dto.CumulativeDetailsDto;
import com.nseindia.mc.controller.dto.CumulativeDetailsDtoInterface;
import com.nseindia.mc.controller.dto.HoMakerDto;
import com.nseindia.mc.controller.dto.LenderWiseExposureDto;
import com.nseindia.mc.controller.dto.LenderWiseExposureDtoInterface;
import com.nseindia.mc.controller.dto.LeverageReportResponse;
import com.nseindia.mc.controller.dto.ListCumulativeDetailsResponse;
import com.nseindia.mc.controller.dto.MTRDailyFileDetailsDto;
import com.nseindia.mc.controller.dto.MTRMemberDailyFileDetailsDto;
import com.nseindia.mc.controller.dto.MainRequestOption;
import com.nseindia.mc.controller.dto.MaxAllowableExposureDto;
import com.nseindia.mc.controller.dto.MaxAllowableExposureDtoInterface;
import com.nseindia.mc.controller.dto.MaxClientAllowableExposureDto;
import com.nseindia.mc.controller.dto.MaxClientAllowableExposureDtoInterface;
import com.nseindia.mc.controller.dto.MemberDetailsDto;
import com.nseindia.mc.controller.dto.MemberUploadStatus;
import com.nseindia.mc.controller.dto.NonSubmissionMbrDtlsDto;
import com.nseindia.mc.controller.dto.NonSubmissionMbrDtlsDtoInterface;
import com.nseindia.mc.controller.dto.NonSubmissionMbrDtlsResponse;
import com.nseindia.mc.controller.dto.PenaltyDocDto;
import com.nseindia.mc.controller.dto.TotalIndebtednessDto;
import com.nseindia.mc.controller.dto.TotalIndeptnessDtoInterface;
import com.nseindia.mc.controller.dto.UploadCutOffPeriod;
import com.nseindia.mc.exception.BaseServiceException;
import com.nseindia.mc.model.MTRControlRecord;
import com.nseindia.mc.model.MTRDailyFile;
import com.nseindia.mc.model.MTRDailyJobStatus;
import com.nseindia.mc.model.MTRDetailRecord;
import com.nseindia.mc.model.MTRHoMaker;
import com.nseindia.mc.model.MTRMemberList;
import com.nseindia.mc.model.MTRMonthlyJobStatus;
import com.nseindia.mc.model.MTRMrgTradingReport;
import com.nseindia.mc.model.PenaltyDocMaster;
import com.nseindia.mc.model.MemberMaster;
import com.nseindia.mc.model.UserMemCom;
import com.nseindia.mc.repository.MTRControlRecordRepository;
import com.nseindia.mc.repository.MTRDailyFileRepository;
import com.nseindia.mc.repository.MTRDailyJobStatusRepository;
import com.nseindia.mc.repository.MTRDetailRecordRepository;
import com.nseindia.mc.repository.MTRHoMakerRepository;
import com.nseindia.mc.repository.MTRMonthlyJobStatusRepository;
import com.nseindia.mc.repository.MTRMrgTradingReportRepository;
import com.nseindia.mc.repository.PenaltyRepository;
import com.nseindia.mc.repository.MTRSummaryRecordRepository;
import com.nseindia.mc.repository.MemberListRepository;
import com.nseindia.mc.repository.MemberMasterRepository;
import com.nseindia.mc.repository.PenaltyDocRepository;
import com.nseindia.mc.repository.UserMemComRepository;
import com.nseindia.mc.service.MailService;
import com.nseindia.mc.service.penalty.PenaltyService;
import com.nseindia.mc.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import static com.google.common.collect.Lists.newArrayList;
import static com.nseindia.mc.util.CommonUtils.*;

/** The service manage MTR Report related operations. */
@Slf4j
@Service
public class MtrReportService {
  private final int MARGIN_TRADING_REPORT_FOOTER_LINE_NUMBER = 2;
  private final int MARGIN_TRADING_REPORT_HEADER_LINE_NUMBER = 11;

  @Value("#{'${tasks.type}'.split(',')}")
  private List<String> submissionTypes;

  @Value("${upload.cutoff.start}")
  private String uploadCutOfffStartTime;

  @Value("${upload.cutoff.end}")
  private String uploadCutOffEndTime;

  @Autowired private MemberMasterRepository memberMasterRepository;

  @Autowired private MemberListRepository memberListRepository;

  @Autowired private MTRDailyFileRepository dailyFileRepository;

  @Autowired private UserMemComRepository userMemComRepository;
  @Autowired private MTRHoMakerRepository mtrHoMakerRepository;
  @Autowired private PenaltyDocRepository penaltyDocRepository;
  @Autowired private MTRControlRecordRepository controlRecordRepository;
  @Autowired private MTRDetailRecordRepository detailRecordRepository;
  @Autowired private MTRSummaryRecordRepository summaryRecordRepository;
  @Autowired private MailService mailService;
  @Autowired private PenaltyService penaltyService;
  @Autowired
  private MTRMrgTradingReportRepository mrgTradingReportRepository;
  @Autowired
  private PenaltyRepository penaltyRepository;
  @Autowired
  private MTRDailyJobStatusRepository dailyJobStatusRepository;
  @Autowired
  private MTRMonthlyJobStatusRepository monthlyJobStatusRepository;

  @Value("classpath:Nil_Submission_Response_Template.csv")
  private Resource responseTemplate;

  @Value("classpath:Nil_Submission_Template.csv")
  private Resource submissionTemplate;

  @Value("classpath:MRG_Trading_Final.csv")
  private Resource tradingFinalReportTemplate;

  @Value("classpath:MRG_Trading_Provisional.csv")
  private Resource tradingProvisionalReportTemplate;

  @Value("${dms.response.path}")
  private String resFileDirectory;

  /**
   * Get main request options.
   *
   * @return the main request options.
   */
  public List<MainRequestOption> getMainRequestOptions() {
    return submissionTypes.stream()
        .map(item -> new MainRequestOption(item))
        .collect(Collectors.toList());
  }

  /**
   * Get upload cutoff period.
   *
   * @return the upload cutoff period.
   */
  public UploadCutOffPeriod getUploadCutOffPeriod() {
    return new UploadCutOffPeriod(uploadCutOfffStartTime, uploadCutOffEndTime);
  }

  /**
   * Get member upload status.
   *
   * @param memberId the member id.
   * @return the upload status.
   */
  public MemberUploadStatus getMemberUploadStatus(long memberId) {
    MTRMemberList memberList =
        memberListRepository
            .findTopByMember_MemId(memberId)
            .orElseThrow(
                () ->
                    new BaseServiceException(
                      "the tradingMemberId is not found", HttpStatus.BAD_REQUEST));

    MemberUploadStatus res = new MemberUploadStatus();
    res.setApprovedStatus(memberList.getMember().getFullStatus());
    res.setEligibilityFlag(memberList.getEligibleMemberMtrStatus() ? "Y" : "N");
    res.setEligibilityDate(memberList.getEligibleMemberMtrFrom());
    res.setApprovedDate(memberList.getMember().getMemApprovalDate() == null ? null : memberList.getMember().getMemApprovalDate().toInstant()
    .atZone(ZoneId.systemDefault())
    .toLocalDate());
    res.setMemberStatus(memberList.getMemberStatus());
    res.setMemberCode(memberList.getMember().getMemCd());
    res.setMemberName(memberList.getMember().getMemName());
    res.setMemberType(memberList.getMember().getMemType());
    LocalDate today = LocalDate.now();
    LocalDate currentReportingDate = getLastBusinessDay(today);
    List<LocalDate> missedDates = processMissedDates(memberList, currentReportingDate, false);
    
    if (missedDates.isEmpty()) {
    	missedDates.add(currentReportingDate);
    }
    
    res.setMissedDates(missedDates);
    dailyFileRepository
        .findTopByDailyFileStatusTrueAndMember_MemIdOrderByReportingDateDesc(memberId)
        .ifPresent(
            file -> {
              res.setLastMTRSubmittedDate(file.getDailyFileSubmissionDate());
              res.setLastMTRReportingDate(file.getReportingDate());
              res.setLastSubmissionIsNil(file.getNilSubmissionStatus() != null && file.getNilSubmissionStatus());
              controlRecordRepository
                  .findTopByMtrFile_IdOrderByMtrFile_ReportingDateDesc(file.getId())
                  .ifPresent(
                      record -> {
                        res.setLastTotalAmountFunded(record.getTotalAmountFunded());
                      });
            });
    return res;
  }

  /**
   * List MTR Daily files to NSE.
   *
   * @param memberId the member id.
   * @param reportingDate the reporting date.
   * @return the daily files.
   */
  public List<MTRDailyFileDetailsDto> listMtrDailyFilesNse(Long memberId, LocalDate reportingDate) {
    final List<MemberMaster> members = new ArrayList<>();
    if (memberId != null) {
      MemberMaster member = memberMasterRepository
          .findById(memberId)
          .orElseThrow(
              () ->
                  new BaseServiceException(
                      "the tradingMemberId is not found", HttpStatus.BAD_REQUEST));
      members.add(member);
    } else {
        memberListRepository.findByEligibleMemberMtrStatus(true).forEach(
            mtrMemberList -> members.add(mtrMemberList.getMember())
        );
    }
    Map<Long, MTRDailyFile> map = dailyFileRepository
        .findByMemberIdsAndReportingDate(
            members.stream().map(m -> m.getMemId()).collect(Collectors.toList()),
            CommonUtils.getDatabaseDateStr(reportingDate)
        )
        .stream()
        .collect(Collectors.toMap(f -> f.getMember().getMemId(), f -> f));
    return members.stream()
        .map(m -> {
            if (map.containsKey(m.getMemId())) {
                return map.get(m.getMemId());
            } else {
                MTRDailyFile emptyFile = new MTRDailyFile();
                emptyFile.setMember(m);
                emptyFile.setDailyFileStatus(false);
                return emptyFile;
            }})
        .map(
            item -> {
              MTRDailyFileDetailsDto entity = new MTRDailyFileDetailsDto();

              entity.setMemberId(item.getMember().getMemId());
              entity.setMemberName(item.getMember().getMemName());
              entity.setMemberCode(item.getMember().getMemCd());

              entity.setResponseFilename(item.getResponseFileName());
              entity.setSubmittedFilename(item.getDailyFileName());
              entity.setFileSubmissionStatus(item.getDailyFileStatus() ? "Y" : "N");
              entity.setSubmissionDate(item.getDailyFileSubmissionDate());
              entity.setReferenceNumber(String.valueOf(item.getId()));

              return entity;
            })
        .collect(Collectors.toList());
  }

  /**
   * List ho maker.
   *
   * @return the ho maker list.
   */
  public List<HoMakerDto> listHoMakerMtr() {
    return userMemComRepository.findAll().stream()
        .map(
            item -> {
              HoMakerDto entity = new HoMakerDto();
              entity.setHoMakerId(item.getId());
              entity.setHoMakerName(item.getHo());
              return entity;
            })
        .collect(Collectors.toList());
  }

  /**
   * Assign ho maker.
   *
   * @param makerId the maker id.
   */
  public void assignHoMaker(Long makerId) {
      UserMemCom maker =
          userMemComRepository
              .findById(makerId)
              .orElseThrow(
                  () -> new BaseServiceException("The make isn't found.", HttpStatus.BAD_REQUEST));
      LocalDateTime now = LocalDateTime.now();
      mtrHoMakerRepository.findFirstByMakerStatus(true).ifPresent(e -> {
          e.setMakerStatus(false);
          e.setDeactiveOn(now);
          e.setUpdatedBy(now.toString());
          e.setUpdatedDate(now);
          mtrHoMakerRepository.save(e);
      });
      MTRHoMaker entity = new MTRHoMaker();
      entity.setMaker(maker);
      entity.setMakerStatus(true);
      entity.setUpdatedBy(now.toString());
      entity.setUpdatedDate(now);
      mtrHoMakerRepository.save(entity);

      penaltyRepository.updateUnassignedPenaltyWithMaker(makerId);
  }

  /**
   * List MTR penality file types.
   *
   * @return the penality file types.
   */
  public List<PenaltyDocDto> listMtrPenaltyFileTypes() {
    return penaltyDocRepository.findAll().stream()
        .map(
            item -> {
            	PenaltyDocDto dto = new PenaltyDocDto();
              dto.setDocId(item.getId());
              dto.setDocName(item.getPenaltyDocTypeName());
              return dto;
            })
        .collect(Collectors.toList());
  }

  /**
   * List MTR daily files.
   *
   * @param memberId the member id.
   * @param memberName the member name.
   * @param reportDateFrom the report from date.
   * @param reportDateTo the report to date.
   * @return the daily files.
   */
  public List<MTRMemberDailyFileDetailsDto> listMtrDailyFilesMember(
      Long memberId, String memberName, LocalDateTime reportDateFrom, LocalDateTime reportDateTo) {
    memberListRepository
            .findTopByMember_MemId(memberId)
            .orElseThrow(
                () ->
                    new BaseServiceException(
                      "the tradingMemberId is not found", HttpStatus.BAD_REQUEST));
    
    List<MTRMemberDailyFileDetailsDto> submittedDailyFiles =
        dailyFileRepository
            .findByMember_MemIdAndReportingDateBetween(memberId, reportDateFrom, reportDateTo)
            .stream()
            .map(
                item -> {
                  MTRMemberDailyFileDetailsDto dto = new MTRMemberDailyFileDetailsDto();
                  dto.setReferenceNumber(String.valueOf(item.getId()));
                  dto.setReportingDate(item.getReportingDate());
                  dto.setNillSubmissionStatus((item.getNilSubmissionStatus() != null && item.getNilSubmissionStatus() == true) ? "Y" : "N");
                  dto.setResponseFilename(item.getResponseFileName());
                  dto.setSubmissionDate(item.getDailyFileSubmissionDate());
                  dto.setSubmittedFilename(item.getDailyFileName());
                  dto.setUploaded(item.getDailyFileStatus());
                  return dto;
                })
            .collect(Collectors.toList());

    return submittedDailyFiles;
  }

  /**
   * Get member details by member name or code.
   *
   * @param memberId the member id.
   * @param memberName the member name.
   * @param memberCode the member code.
   * @return the member details.
   */
  public MemberDetailsDto getMemberDetails(long memberId, String memberName, String memberCode) {
    memberMasterRepository
        .findById(memberId)
        .orElseThrow(
            () ->
                new BaseServiceException(
                    "the tradingMemberId is not found", HttpStatus.BAD_REQUEST));
    return memberListRepository
        .findTopByMember_MemNameOrMember_MemCd(memberName, memberCode)
        .map(
            item -> {
              MemberDetailsDto dto = new MemberDetailsDto();
              dto.setMemberId(item.getMember().getMemId());
              dto.setMemberName(item.getMember().getMemName());
              dto.setMtrActiveFromDate(item.getEligibleMemberMtrFrom());
              dto.setMtrActiveStatus(item.getEligibleMemberMtrStatus() ? "Active" : "Inactive");
              return dto;
            })
        .orElseThrow(
            () ->
                new BaseServiceException(
                    "can not find member for given name and code", HttpStatus.BAD_REQUEST));
  }

  /**
   * Eligible new member on board.
   *
   * @param memberId the member id.
   * @param uploadStatus the upload status.
   */
  public void newMembersOnboard(long memberId, boolean uploadStatus) {
    MTRMemberList memberList =
        memberListRepository
            .findTopByMember_MemId(memberId)
            .orElseThrow(
                () ->
                    new BaseServiceException(
                        "the tradingMemberId is not found", HttpStatus.NOT_FOUND));

    // update the eligible information.
    memberList.setEligibleMemberMtrFrom(CommonUtils.getLastBusinessDay(LocalDate.now()));
    memberList.setEligibleMemberMtrStatus(uploadStatus);

    memberListRepository.save(memberList);
    insertNotUploadedSubmission(memberList.getMember(), memberList.getEligibleMemberMtrFrom());

    // send email
    mailService.sendCutOffWindowsAutoEmail(
        memberList.getMember().getEmail(), uploadCutOffEndTime, false);
  }

  /**
   * Get mtr file by reporting date.
   *
   * @param memberId the member id.
   * @param reportingBusinessDate the reporting business date.
   * @return mtr daily file.
   */
  public MTRDailyFile getMtrFileByReportingDate(long memberId, LocalDate reportingBusinessDate) {
    memberMasterRepository
        .findById(memberId)
        .orElseThrow(
            () ->
                new BaseServiceException(
                    "the tradingMemberId is not found", HttpStatus.BAD_REQUEST));
    return dailyFileRepository
        .findTopByDailyFileStatusTrueAndMember_MemIdAndReportingDateBetweenOrderByDailyFileSubmissionDateDesc(
            memberId,
            reportingBusinessDate.atStartOfDay(),
            LocalTime.MAX.atDate(reportingBusinessDate))
        .orElseThrow(
            () ->
                new BaseServiceException(
                    "The file for given date hasn't been uploaded yet.", HttpStatus.NOT_FOUND));
  }

  public MTRDailyFile getMtrFileById(long fileId) {
    return dailyFileRepository
        .findById(fileId)
        .orElseThrow(
            () ->
                new BaseServiceException(
                    "The file for given date hasn't been uploaded yet.", HttpStatus.NOT_FOUND));
  }

  /**
   * Download mtr files.
   *
   * @param memberId the member id.
   * @param submissionFromDate the submission from date.
   * @param submissionToDate the submission end date.
   * @param fileTypes the file types.
   * @return the output stream.
   */
  public ByteArrayOutputStream downloadMtrFiles(
      long memberId, LocalDate submissionFromDate, LocalDate submissionToDate, List<String> fileTypes) {
    memberMasterRepository
        .findById(memberId)
        .orElseThrow(
            () ->
                new BaseServiceException(
                    "the tradingMemberId is not found", HttpStatus.BAD_REQUEST));

    List<MTRDailyFile> files =
        dailyFileRepository.findByDailyFileStatusTrueAndMember_MemIdAndDailyFileSubmissionDateBetween(
            memberId, submissionFromDate.atStartOfDay(), LocalTime.MAX.atDate(submissionToDate));
    Set<String> fileTypeSet = new HashSet<>(fileTypes);

    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ZipOutputStream out = new ZipOutputStream(bos);

      for (MTRDailyFile file : files) {
        if (fileTypeSet.contains("All") || fileTypeSet.contains("Member")) {
          String fileName = file.getMember().getMemCd() + "_" + file.getDailyFileName();
          boolean success = addFileToZip(fileName, file.getDmsDocIndex(), out);
          if (!success) {
              log.warn("addFileToZip fail for {}, ignore it", file.getDmsDocIndex());
          }
        }
        if (fileTypeSet.contains("All") || fileTypeSet.contains("Response")) {
          boolean success = addFileToZip(file.getResponseFileName(), file.getDmsResIndex(), out);
          if (!success) {
              log.warn("addFileToZip fail for {}, ignore it", file.getDmsResIndex());
          }
        }
      }
      out.close();
      return bos;
    } catch (IOException e) {
      throw new BaseServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Download mtr penality files.
   *
   * @param penalityMonthYear the penality month year.
   * @param fileType the file type.
   * @return the output stream.
   */
  public ByteArrayOutputStream downloadMtrPenaltyFiles(
      String penalityMonthYear, String fileType) {
    int pYear = Integer.parseInt(penalityMonthYear.split(" ")[0]);
    int pMonth = Integer.parseInt(penalityMonthYear.split(" ")[1]);
    List<PenaltyDocMaster> docs =
        penaltyDocRepository
            .findByPenalty_PenaltyYearAndPenalty_penaltyMonthAndPenaltyDocTypeName(
                pYear, pMonth, fileType);
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ZipOutputStream out = new ZipOutputStream(bos);

      int indent = 0;
      for (PenaltyDocMaster doc : docs) {
        String fileName = "Penalty " + indent++;
        addFileToZip(fileName, doc.getPenaltyDocIndex(), out);
      }
      out.close();
      return bos;
    } catch (IOException e) {
      throw new BaseServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Validate the exporsure in previous submission equals to 0 or not.
   *
   * @param memberId the member id.
   * @param reportingBusinessDate the reporting date.
   * @return the member can make a nil submission or not.
   */
  public boolean validateNilSubmission(Long memberId, LocalDate reportingBusinessDate) {
    memberMasterRepository
        .findById(memberId)
        .orElseThrow(
            () ->
                new BaseServiceException(
                    "the tradingMemberId is not found", HttpStatus.BAD_REQUEST));

    MTRControlRecord record = null;
    Optional<MTRDailyFile> file =
        dailyFileRepository.findTopByDailyFileStatusTrueAndMember_MemIdOrderByReportingDateDesc(memberId);
    if (file.isPresent()) {
      record =
          controlRecordRepository
              .findTopByMtrFile_IdOrderByMtrFile_ReportingDateDesc(file.get().getId())
              .orElse(null);
    }
    Double totalFundAmount = record == null ? 0 : record.getTotalAmountFunded();

    return Double.compare(totalFundAmount, 0) == 0;
  }

  /**
   * Submit a nil submission.
   *
   * @param memberId the member id.
   */
  public void submitNilSubmission(Long memberId) {
    MemberUploadStatus memberUploadStatus = getMemberUploadStatus(memberId);
    if (memberUploadStatus.getLastTotalAmountFunded() > 0) {
      throw new BaseServiceException(
        "Validations failed, please verify if you margin trading exposure is 0.",
        HttpStatus.FORBIDDEN);
    }
    if (memberUploadStatus.isLastSubmissionIsNil()) {
      throw new BaseServiceException(
        "Your last submission is Nil submission.",
        HttpStatus.FORBIDDEN);
    }
    if (memberUploadStatus.getMissedDates().size() == 0) {
      // do nothing
      return;
    }
    submitNilSubmissionInternally(memberId, memberUploadStatus.getMissedDates().get(0));
  }

  public void submitNilSubmissionInternally(Long memberId, LocalDate reportingBusinessDate) {
      try {
          MemberMaster member =
                  memberMasterRepository
                          .findById(memberId)
                          .orElseThrow(
                                  () ->
                                          new BaseServiceException(
                                                  "the tradingMemberId is not found", HttpStatus.BAD_REQUEST));

          Integer batchNo = 1;

          MTRDailyFile file = dailyFileRepository
        	        .findByMemberIdAndReportingDateIncludingAutoGeneratedFiles(memberId, CommonUtils.getDatabaseDateStr(reportingBusinessDate))
        	        .orElse(new MTRDailyFile());

          File filePath = new File(System.getProperty("user.dir") + File.separator + resFileDirectory);
          if (!filePath.isDirectory()) filePath.mkdirs();
          String resFileName = generateNilSubmission(filePath, member.getMemCd(), reportingBusinessDate, true);
          String submissionFileName =
                  generateNilSubmission(filePath, member.getMemCd(), reportingBusinessDate, false);

          file.setMember(member);
          file.setReportingDate(reportingBusinessDate.atStartOfDay());
          file.setNilSubmissionDate(LocalDateTime.now());
          file.setNilSubmissionStatus(true);
          file.setCreatedDate(LocalDateTime.now());
          file.setCreatedBy(member.getMemName());
          file.setBatchNo(batchNo);
          file.setResponseFileName(getFileName(member.getMemCd(), reportingBusinessDate, batchNo, true));
          file.setDmsResIndex(resFileName);
          file.setDailyFileName(getFileName(null, reportingBusinessDate, batchNo, false));
          file.setDailyFileStatus(true);
          file.setDmsDocIndex(submissionFileName);
          file.setDailyFileSubmissionDate(LocalDateTime.now());

          dailyFileRepository.save(file);
      } catch (Exception e) {
          log.error("Submit a nil submission for MTR", e);
          throw new BaseServiceException(
                  "Submit a nil submission for MTR", HttpStatus.INTERNAL_SERVER_ERROR);
      }
  }

  /**
   * List the cumulative details of the specific time range.
   *
   * @param fromDate the start date of the time range.
   * @param toDate the end date of the time range.
   * @param isMemberWiseFlag if true, aggregate by member.
   */
  public ListCumulativeDetailsResponse listCumulativeDtls(LocalDate fromDate,
                                                          LocalDate toDate, Boolean isMemberWiseFlag) {
      String fromDateStr = getDatabaseDateStr(fromDate);
      String toDateStr = getDatabaseDateStr(toDate);
      LocalDate date = fromDate;
      List<String> firstBusinessDays = newArrayList();
      List<String> lastBusinessDays = newArrayList();
      while(date.isBefore(toDate)) {
          firstBusinessDays.add(getFirstBusinessDayOfMonthISOStr(date));
          lastBusinessDays.add(getLastBusinessDayOfMonthISOStr(date));
          date = date.plusMonths(1);
      }
      List<CumulativeDetailsDto> cumulativeDetailsList = new ArrayList<>();
      ListCumulativeDetailsResponse response = new ListCumulativeDetailsResponse();
      response.setCumulativeMarginTradingDetailsList(cumulativeDetailsList);
      List<CumulativeDetailsDtoInterface> cumulativeDetails = null;
      String sql = "";
    if (isMemberWiseFlag) {
        cumulativeDetails = detailRecordRepository.listCumulativeDtlsMemberWise(
            fromDateStr, toDateStr, firstBusinessDays, lastBusinessDays);
        sql = detailRecordRepository.SQL_LIST_CUMULATIVE_DTLS_MEMBER_WISE;
    } else {
        cumulativeDetails = detailRecordRepository.listCumulativeDtls(
            fromDateStr, toDateStr, firstBusinessDays, lastBusinessDays);
        sql = detailRecordRepository.SQL_LIST_CUMULATIVE_DTLS;
    }

      Map<String, String> replacementStrings = Map.of(
          ":fromDate", String.format("'%s'", fromDateStr),
          ":toDate", String.format("'%s'", toDateStr),
          ":firstBusinessDays", String.join(
              ",", firstBusinessDays.stream().map(s -> String.format("'%s'", s)).collect(Collectors.toList())),
          ":lastBusinessDays", String.join(
              ",", lastBusinessDays.stream().map(s -> String.format("'%s'", s)).collect(Collectors.toList()))
      );
      for (Map.Entry<String, String> e : replacementStrings.entrySet()) {
          sql = sql.replace(e.getKey(), e.getValue());
      }
      response.setCumulativeSqlQuery(sql);

      for (CumulativeDetailsDtoInterface dtoInterface : cumulativeDetails) {
      CumulativeDetailsDto dto = new CumulativeDetailsDto();
      dto.setTotalOutstandingForMonth(nullOrZero(dtoInterface.getTotalOutstandingForMonth()));
      dto.setNetOutstandingExposures(nullOrZero(dtoInterface.getNetOutstandingExposures()));
      dto.setFreshExposureForMonth(nullOrZero(dtoInterface.getFreshExposureForMonth()));
      dto.setExposureLiquidatedForMonth(nullOrZero(dtoInterface.getExposureLiquidatedForMonth()));
      dto.setMemberName(dtoInterface.getMemberName());
      dto.setMemberCode(dtoInterface.getMemberCode());
      dto.setMonth(dtoInterface.getMonth());
      dto.setYear(dtoInterface.getYear());
      dto.setNumberOfBrokers(dtoInterface.getNumberOfBrokers());
      dto.setNumberOfScripts(dtoInterface.getNumberOfScripts());
      cumulativeDetailsList.add(dto);
    }
    return response;
  }

  private Double nullOrZero(Double v) {
    return v == null ? 0.0 : v;
  }

  public LeverageReportResponse leverageTotalIndebtednessReport(LocalDate fromDate, LocalDate toDate) {
    LeverageReportResponse response = new LeverageReportResponse();
    LocalDateTime fromDateTime = fromDate.withDayOfMonth(1).atStartOfDay();
    LocalDateTime toDateTime = toDate.withDayOfMonth(toDate.lengthOfMonth()).plusDays(1).atStartOfDay();
    String fromDateTimeStr = CommonUtils.getDatabaseDateStr(fromDateTime.toLocalDate());
    String toDateTimeStr = CommonUtils.getDatabaseDateStr(toDateTime.toLocalDate());
    List<TotalIndeptnessDtoInterface> dtoInterfaces = summaryRecordRepository.aggregateByReportDate(fromDateTimeStr, toDateTimeStr);
    List<TotalIndebtednessDto> dtos = new ArrayList<>();
    for (TotalIndeptnessDtoInterface dtoInterface : dtoInterfaces) {
      TotalIndebtednessDto dto = new TotalIndebtednessDto();
      dto.setMemberCode(dtoInterface.getMemberCode());
      dto.setMemberName(dtoInterface.getMemberName());
      dto.setNetWorth(0.0);
      dto.setTotalBorrowedFunds(dtoInterface.getTotalBorrowedFunds());
      dto.setSubmissionDate(dtoInterface.getSubmissionDate());
      dto.setLimitExceeded(dto.getTotalBorrowedFunds() > dto.getNetWorth() * 5);
      dtos.add(dto);
    }
    response.setTotalIndebtedness(dtos);
    String sql = summaryRecordRepository.SQL_LEVERAGE_TOTAL_INDEBTEDNESS_REPORT;
    Map<String, String> replacementStrings = Map.of(
      ":startReportDate", String.format("'%s'", fromDateTimeStr),
      ":endReportDate", String.format("'%s'", toDateTimeStr)
    );
    for (Map.Entry<String, String> e : replacementStrings.entrySet()) {
      sql = sql.replace(e.getKey(), e.getValue());
    }
    response.setSqlQuery(sql);
    return response;
  }

  public LeverageReportResponse leverageLenderWiseExposureReport(LocalDate fromDate, LocalDate toDate) {
    LeverageReportResponse response = new LeverageReportResponse();
    LocalDateTime fromDateTime = CommonUtils.getFirstBusinessDayOfMonth(fromDate).atStartOfDay();
    LocalDateTime toDateTime = CommonUtils.getLastBusinessDayOfMonth(toDate).atTime(LocalTime.MAX);
    LocalDateTime previousFromDateTime = CommonUtils.getLastBusinessDay(fromDateTime.toLocalDate()).atStartOfDay();
    String fromDateTimeStr = CommonUtils.getDatabaseDateStr(fromDateTime.toLocalDate());
    String toDateTimeStr = CommonUtils.getDatabaseDateStr(toDateTime.toLocalDate());
    String previousFromDateTimeStr = CommonUtils.getDatabaseDateStr(previousFromDateTime.toLocalDate());
    List<LenderWiseExposureDtoInterface> dtoInterfaces = summaryRecordRepository
          .aggregateByReportDateGroupByLenderCategory(
            fromDateTimeStr, toDateTimeStr, previousFromDateTimeStr);
    List<LenderWiseExposureDto> dtos = new ArrayList<>();
    for (LenderWiseExposureDtoInterface dtoInterface : dtoInterfaces) {
        LenderWiseExposureDto dto = new LenderWiseExposureDto();
        dto.setMemberCode(dtoInterface.getMemberCode());
        dto.setMemberName(dtoInterface.getMemberName());
        dto.setLenderCategory(dtoInterface.getLenderCategory());
        dto.setTotalBorrowedFunds(dtoInterface.getTotalBorrowedFunds());
        if (dtoInterface.getTotalBorrowedFundsOnPreviousDay() != null) {
          dto.setTotalBorrowedFundsOnPreviousDay(dtoInterface.getTotalBorrowedFundsOnPreviousDay());
        } else {
            dto.setTotalBorrowedFundsOnPreviousDay(0.0);
        }
        dto.setSubmissionDate(dtoInterface.getSubmissionDate());
        dtos.add(dto);
    }
    response.setLenderWiseExposure(dtos);
    String sql = summaryRecordRepository.SQL_LEVERAGE_LENDER_WISE_EXPOSURE_REPORT;
    Map<String, String> replacementStrings = Map.of(
      ":startReportDate", String.format("'%s'", fromDateTimeStr),
      ":endReportDate", String.format("'%s'", toDateTimeStr),
      ":previousStartReportDate", String.format("'%s'", previousFromDateTimeStr)
    );
    for (Map.Entry<String, String> e : replacementStrings.entrySet()) {
      sql = sql.replace(e.getKey(), e.getValue());
    }
    response.setSqlQuery(sql);
    return response;
  }

  public LeverageReportResponse leverageMaxAllowableExposureReport(LocalDate fromDate, LocalDate toDate) {
    LeverageReportResponse response = new LeverageReportResponse();
    LocalDateTime fromDateTime = fromDate.withDayOfMonth(1).atStartOfDay();
    LocalDateTime toDateTime = toDate.withDayOfMonth(toDate.lengthOfMonth()).plusDays(1).atStartOfDay();
    String fromDateTimeStr = CommonUtils.getDatabaseDateStr(fromDateTime.toLocalDate());
    String toDateTimeStr = CommonUtils.getDatabaseDateStr(toDateTime.toLocalDate());
    List<MaxAllowableExposureDtoInterface> exposureDtoInterfaces = detailRecordRepository.aggregateByReportDateGroupByDailyFileId(fromDateTimeStr, toDateTimeStr);
    List<MaxAllowableExposureDto> dtos = new ArrayList<>();
    for (MaxAllowableExposureDtoInterface dtoInterface : exposureDtoInterfaces) {
      MaxAllowableExposureDto dto = new MaxAllowableExposureDto();
      dto.setMemberCode(dtoInterface.getMemberCode());
      dto.setMemberName(dtoInterface.getMemberName());
      dto.setNetWorth(0.0);
      Double maxAllowableExposure = dtoInterface.getMaxAllowableExposure();
      dto.setMaxAllowableExposure(maxAllowableExposure == null ? 0 : maxAllowableExposure);
      dto.setTotalBorrowedFunds(dtoInterface.getTotalBorrowedFunds());
      dto.setSubmissionDate(dtoInterface.getSubmissionDate());
      dto.setLimitExceeded(dto.getMaxAllowableExposure() > dto.getTotalBorrowedFunds() + 0.5 * dto.getNetWorth());
      dtos.add(dto);
    }
    response.setMaxAllowableExposure(dtos);
    String sql = detailRecordRepository.SQL_LEVERAGE_MAX_ALLOWABLE_EXPOSURE_REPORT;
    Map<String, String> replacementStrings = Map.of(
      ":startReportDate", String.format("'%s'", fromDateTimeStr),
      ":endReportDate", String.format("'%s'", toDateTimeStr)
    );
    for (Map.Entry<String, String> e : replacementStrings.entrySet()) {
      sql = sql.replace(e.getKey(), e.getValue());
    }
    response.setSqlQuery(sql);
    return response;
  }

  public LeverageReportResponse leverageMaxClientAllowableExposureReport(LocalDate fromDate, LocalDate toDate) {
    LeverageReportResponse response = new LeverageReportResponse();
    LocalDateTime fromDateTime = fromDate.withDayOfMonth(1).atStartOfDay();
    LocalDateTime toDateTime = toDate.withDayOfMonth(toDate.lengthOfMonth()).plusDays(1).atStartOfDay();
    String fromDateTimeStr = CommonUtils.getDatabaseDateStr(fromDateTime.toLocalDate());
    String toDateTimeStr = CommonUtils.getDatabaseDateStr(toDateTime.toLocalDate());
    List<MaxClientAllowableExposureDtoInterface> exposureDtoInterfaces = detailRecordRepository.aggregateByReportDateGroupByDailyFileIdAndClient(fromDateTimeStr, toDateTimeStr);

    List<MaxClientAllowableExposureDto> dtos = new ArrayList<>();
    for (MaxClientAllowableExposureDtoInterface dtoInterface : exposureDtoInterfaces) {
      MaxClientAllowableExposureDto dto = new MaxClientAllowableExposureDto();
      dto.setClientName(dtoInterface.getClientName());
      dto.setClientPan(dtoInterface.getClientPan());
      dto.setMemberCode(dtoInterface.getMemberCode());
      dto.setMemberName(dtoInterface.getMemberName());
      dto.setExposureToClient(dtoInterface.getExposureToClient() == null ? 0 : dtoInterface.getExposureToClient());
      dto.setNetWorth(0.0);
      Double totalBorrowedFund = dtoInterface.getTotalBorrowedFunds();
      dto.setTotalBorrowedFunds(totalBorrowedFund == null ? 0 : totalBorrowedFund);
      dto.setSubmissionDate(dtoInterface.getSubmissionDate());
      dto.setLimitExceeded(dto.getExposureToClient() > 0.1 * (dto.getTotalBorrowedFunds()  + 0.5 * dto.getNetWorth()));
      dtos.add(dto);
    }
    response.setMaxClientAllowableExposure(dtos);
    String sql = detailRecordRepository.SQL_LEVERAGE_MAX_CLIENT_ALLOWABLE_EXPOSURE_REPORT;
    Map<String, String> replacementStrings = Map.of(
      ":startReportDate", String.format("'%s'", fromDateTimeStr),
      ":endReportDate", String.format("'%s'", toDateTimeStr)
    );
    for (Map.Entry<String, String> e : replacementStrings.entrySet()) {
      sql = sql.replace(e.getKey(), e.getValue());
    }
    response.setSqlQuery(sql);
    return response;
  }

  /**
   * Generate nil submission response.
   *
   * @param memberCode the member code.
   * @param reportingDate the reporting date.
   * @return the file name.
   */
  public String generateNilSubmission(File filePath,
      String memberCode, LocalDate reportingDate, boolean isResponseFile) {
      String fileName = filePath + File.separator+  UUID.randomUUID().toString() + ".csv";
    try(PrintWriter fos = new PrintWriter(fileName)) {
      Reader reader =
          new InputStreamReader(
              isResponseFile
                  ? responseTemplate.getInputStream()
                  : submissionTemplate.getInputStream(),
              StandardCharsets.UTF_8);

      CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT);
      List<List<String>> data = new ArrayList<>();
      for (CSVRecord record : parser) {
        List<String> rowValues = IteratorUtils.toList(record.iterator());
        List<String> row = new ArrayList<>();
        for (String cell : rowValues) {
          if ("Enter member Code".equals(cell)) {
            row.add(memberCode);
          } else if ("Date".equals(cell)) {
            row.add(reportingDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")));
          } else {
            row.add(cell);
          }
        }
        data.add(row);
      }

      CSVPrinter csvPrinter = new CSVPrinter(fos, CSVFormat.DEFAULT);
      for (List<String> row : data) {
        csvPrinter.printRecord(row);
      }
      csvPrinter.flush();
      csvPrinter.close();

      return fileName;
    } catch (Exception e) {
      log.error("generate nil submission or response file fail", e);
      throw new BaseServiceException(
          "generate nil submission or reponse error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Generate file name.
   *
   * @param memberCode the member code.
   * @param reportingDate the reporting date.
   * @param batchNo the batch number.
   * @return the file name.
   */
  public String getFileName(
      String memberCode, LocalDate reportingDate, int batchNo, boolean isResponseFile) {
    return isResponseFile
        ? String.format(
            "%s_MTR_%s.R%02d",
            memberCode, reportingDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")), batchNo)
        : String.format(
            "MTR_%s.T%02d", reportingDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")), batchNo);
  }

  /** Cron task to send email when cutoff windows open. */
  @Scheduled(cron = "0 0 9 ? * MON-FRI")
  public void sendNotificationWhenCutOffWindowOpen() {
    memberListRepository.findByEligibleMemberMtrStatus(true).stream()
        .forEach(
            member -> {
              dailyFileRepository
                  .findTopByDailyFileStatusTrueAndMember_MemIdOrderByReportingDateDesc(
                      member.getMember().getMemId())
                  .ifPresentOrElse(
                      dailyFile -> {
                        mailService.sendCutOffWindowsAutoEmail(
                            member.getMember().getEmail(),
                            uploadCutOffEndTime,
                            dailyFile.getNilSubmissionStatus());
                      },
                      () -> {
                        mailService.sendCutOffWindowsAutoEmail(
                            member.getMember().getEmail(), uploadCutOffEndTime, false);
                      });
            });
  }

  public List<LocalDate> processMissedDates(
    MTRMemberList memberList, LocalDate reportDate, boolean autoFillMissingRecord) {
    MemberMaster memberMaster = memberList.getMember();
    LocalDate earliestReportingDate = memberList.getEligibleMemberMtrFrom();
    List<LocalDate> missedDates = new ArrayList<>();
    Optional<MTRDailyFile> lastSuccessRecord =
      dailyFileRepository.findFirstByMember_MemIdAndDailyFileStatusOrderByReportingDateDesc(
        memberMaster.getMemId(), true);
    LocalDateTime now = LocalDateTime.now();
    if (lastSuccessRecord.isPresent()) {
      List<LocalDate> businessDays = getBusinessDaysInclusive(
        lastSuccessRecord.get().getReportingDate().toLocalDate().plusDays(1), reportDate);
      if (Boolean.TRUE.equals(lastSuccessRecord.get().getNilSubmissionStatus())) {
        if (autoFillMissingRecord) {
          businessDays.forEach(day -> {
            submitNilSubmissionInternally(memberMaster.getMemId(), day);
          });
        }
      } else {
        missedDates.addAll(businessDays);
      }
    } else {
      if (memberList.getEligibleMemberMtrFrom() != null) {
        missedDates.addAll(getBusinessDaysInclusive(memberList.getEligibleMemberMtrFrom(), reportDate));
      }
    }
    missedDates = missedDates.stream().filter(d -> !d.isBefore(earliestReportingDate)).collect(Collectors.toList());

    if (autoFillMissingRecord) {
      List<MTRDailyFile> existedFiles = dailyFileRepository
        .findByMemberIdAndReportingDatesIncludingAutoGeneratedFiles(
          memberMaster.getMemId(), missedDates.stream().map(d -> getDatabaseDateStr(d)).collect(Collectors.toList())
        );
      Set<LocalDate> existedDates = existedFiles.stream().map(f -> f.getReportingDate().toLocalDate())
        .collect(Collectors.toSet());
      missedDates.forEach(day -> {
        if (!existedDates.contains(day)) {
          insertNotUploadedSubmission(memberMaster, day);
        }
      });
    }

    return missedDates;
  }

  private void insertNotUploadedSubmission(MemberMaster memberMaster, LocalDate reportDate) {
    MTRDailyFile mtrDailyFile = new MTRDailyFile();
    mtrDailyFile.setDailyFileStatus(false);
    mtrDailyFile.setMember(memberMaster);
    mtrDailyFile.setBatchNo(1);
    mtrDailyFile.setReportingDate(reportDate.atStartOfDay());
    mtrDailyFile.setNilSubmissionStatus(false);
    mtrDailyFile.setCreatedBy(memberMaster.getMemId().toString());
    mtrDailyFile.setCreatedDate(LocalDateTime.now());
    dailyFileRepository.save(mtrDailyFile);
  }

  /**
   * Job Name: MTR Sign-Off (Daily Job)
   */
  @Scheduled(cron = "0 ${cronJob.signOffDailyJob.startMinute} ${cronJob.signOffDailyJob.startHour} * * MON-FRI")
  public void signOffDailyJob() {
    LocalDate today = LocalDate.now();
    LocalDate currentReportingDate = getLastBusinessDay(today);
    signOffDailyJob(currentReportingDate);
  }

  public void signOffDailyJob(LocalDate currentReportingDate) {
    log.info("Start MTR Sign-off Job");
    MTRDailyJobStatus dailyJobStatus = dailyJobStatusRepository.findByReportingDate(currentReportingDate)
      .orElseGet(() -> {
        MTRDailyJobStatus newRecord = new MTRDailyJobStatus();
        newRecord.setReRunCounter(0);
        newRecord.setReportingDate(currentReportingDate);
        return newRecord;
      });
    dailyJobStatus.setReRunCounter(dailyJobStatus.getReRunCounter() + 1);
    dailyJobStatus.setLastRunStart(LocalDateTime.now());
    StringBuilder lastRunRemark = new StringBuilder();

    if (!"S".equals(dailyJobStatus.getDailyFileProcessStatus())) {
      log.info("Start TRANSACTION-1(DAILY_FILE_PROCESS_STATUS)");
      try{
        processDailyNonSubmissionRecords(currentReportingDate);
        dailyJobStatus.setDailyFileProcessStatus("S");
        log.info("TRANSACTION-1 Success...");
      } catch (Exception e) {
        dailyJobStatus.setDailyFileProcessStatus("F");
        String message = String.format("TRANSACTION-1 Fail... %s ", e.getMessage());
        lastRunRemark.append(message);
        log.error(message, e);
      }
    }

    if (!"S".equals(dailyJobStatus.getMrgTradingReportStatus())) {
      log.info("Start TRANSACTION-2 (MRG_TRADING_REPORT_STATUS)");
      try{
        generateMarginTradingReport(currentReportingDate, currentReportingDate);
        dailyJobStatus.setMrgTradingReportStatus("S");
        log.info("TRANSACTION-2 Success...");
      } catch (Exception e) {
        dailyJobStatus.setMrgTradingReportStatus("F");
        String message = String.format("TRANSACTION-2 Fail... %s ", e.getMessage());
        lastRunRemark.append(message);
        log.error(message, e);
      }
    }

    if (!"S".equals(dailyJobStatus.getNseWebsitePublishStatus())) {
      log.info("Start TRANSACTION-3 (NSE_WEBSITE_PUBLISH_STATUS)");
      try{
        publishNSEWeb(currentReportingDate);
        dailyJobStatus.setNseWebsitePublishStatus("S");
        log.info("TRANSACTION-3 Success...");
      } catch (Exception e) {
        dailyJobStatus.setNseWebsitePublishStatus("F");
        String message = String.format("TRANSACTION-3 Fail... %s ", e.getMessage());
        lastRunRemark.append(message);
        log.error(message, e);
      }
    }

    if (!"S".equals(dailyJobStatus.getSebiFileTransferStatus())) {
      log.info("Start TRANSACTION-4 (SEBI_FILE_TRANSFER_STATUS)");
      // TODO:
      dailyJobStatus.setSebiFileTransferStatus("S");
      log.info("TRANSACTION-4 Success...");
    }

    if (!"S".equals(dailyJobStatus.getNseComplianceNotifyStatus())) {
      log.info("Start TRANSACTION-5 (NSE_COMPLIANCE_NOTIFY_STATUS)");
      try{
        // send sign off email
        mailService.sendDailySignOffEmail(currentReportingDate);
        dailyJobStatus.setNseComplianceNotifyStatus("S");
        log.info("TRANSACTION-5 Success...");
      } catch (Exception e) {
        dailyJobStatus.setNseComplianceNotifyStatus("F");
        String message = String.format("TRANSACTION-5 Fail... %s ", e.getMessage());
        lastRunRemark.append(message);
        log.error(message, e);
      }
    }

    dailyJobStatus.setLastRunEnd(LocalDateTime.now());
    if (lastRunRemark.length() == 0) {
      lastRunRemark.append("All transactions completed, no need to re-run the Job");
    }
    dailyJobStatus.setLastRunRemark(lastRunRemark.toString());
    dailyJobStatusRepository.save(dailyJobStatus);
    log.info("MTR Sign-off Job Completed, {}", dailyJobStatus);
  }

  public void processDailyNonSubmissionRecords() {
    log.info("start process daily non submission records");
    LocalDate currentReportingDate = LocalDate.now();
    processDailyNonSubmissionRecords(currentReportingDate);
  }

  public void processDailyNonSubmissionRecords(LocalDate currentReportingDate) {
    List<MTRMemberList> mtrMembers = memberListRepository.findByEligibleMemberMtrStatus(true);
    mtrMembers.forEach(mtrMember -> {
      processMissedDates(mtrMember, currentReportingDate, true);
    });
  }

  /** Cron task to send email in first weekday of every month. */
  @Scheduled(cron = "0 0 10 ? * MON-FRI")
  public void hoMakerNotifyMonthlyJob() {
    if (isFirstWeekDayOfMonth()) {
      LocalDate today = LocalDate.now();
      LocalDate lastMonth = today.minusMonths(1);
      hoMakerNotifyMonthlyJob(lastMonth.getYear(), lastMonth.getMonthValue());
    }
  }

  public void hoMakerNotifyMonthlyJob(int year, int month) {
    log.info("Start HO Maker Notify (Monthly Job)");
    MTRMonthlyJobStatus jobStatus = monthlyJobStatusRepository.findByJobYearAndJobMonthAndJobType(year, month, "H")
      .orElseGet(() -> {
        MTRMonthlyJobStatus newRecord = new MTRMonthlyJobStatus();
        newRecord.setReRunCounter(0);
        newRecord.setJobYear(year);
        newRecord.setJobMonth(month);
        newRecord.setJobType("H");
        return newRecord;
      });
    if (!"S".equals(jobStatus.getLastRunStatus())) {
      jobStatus.setReRunCounter(jobStatus.getReRunCounter() + 1);
      jobStatus.setLastRunStart(LocalDateTime.now());
      StringBuilder lastRunRemark = new StringBuilder();
      try {
        mailService.sendFirstWeekDayOfMonthAutoEmail(year, month);
        jobStatus.setLastRunStatus("S");
      } catch (Exception e) {
        jobStatus.setLastRunStatus("F");
        String message = String.format("HO Maker Notify (Monthly Job) Fail... %s ", e.getMessage());
        lastRunRemark.append(message);
        log.error(message, e);
      }

      jobStatus.setLastRunEnd(LocalDateTime.now());
      if (lastRunRemark.length() == 0) {
        lastRunRemark.append("All transactions completed, no need to re-run the Job");
      }
      jobStatus.setLastRunRemark(lastRunRemark.toString());
      monthlyJobStatusRepository.save(jobStatus);
      log.info("HO Maker Notify (Monthly Job) Completed, {}", jobStatus);
    } else {
      log.info("HO Maker Notify (Monthly Job) last run success no need to process again, {}", jobStatus);
    }
  }

  public void sendNotificationWhenFirstWeekDayOfMonth() {
    if (isFirstWeekDayOfMonth()) {
      mailService.sendFirstWeekDayOfMonthAutoEmail();
    }
  }

  private List<MTRDetailRecord> getMTRDetailRecord(List<MTRDailyFile> submissions) {
    Set<Long> recordIds = submissions.stream()
        .map(f -> f.getId())
        .collect(Collectors.toSet());
    return detailRecordRepository.findByMtrFile_IdIn(recordIds);
  }

  public void generateMarginTradingReport() {
    LocalDate currentReportingDate = LocalDate.now();

    generateMarginTradingReport(currentReportingDate, currentReportingDate);
  }

  public void generateMarginTradingReport(
    LocalDate submissionDate,
    LocalDate currentReportingDate) {
    List<MTRDailyFile> submissions =
        dailyFileRepository.findByDailyFileStatusTrueAndDailyFileSubmissionDateBetweenAndReportingDateBefore(
          submissionDate.atStartOfDay(),
          LocalTime.MAX.atDate(submissionDate),
          LocalTime.MAX.atDate(currentReportingDate));
    // group all submission by reporting date.
    Map<LocalDate, List<MTRDailyFile>> submissionsPerReportingDate =
        submissions.stream()
            .collect(Collectors.groupingBy(i -> i.getReportingDate().toLocalDate()));
    if (submissionsPerReportingDate.entrySet().isEmpty()) {
      submissionsPerReportingDate.put(currentReportingDate, Collections.emptyList());
    }
    submissionsPerReportingDate
        .entrySet()
        .forEach(
            entry -> {
              // check all member submitted for each reporting date
              LocalDate reportingDate = entry.getKey();
              List<MTRDailyFile> files = dailyFileRepository.findByDailyFileStatusTrueAndReportingDate(
                reportingDate.atStartOfDay());
              if (currentReportingDate.equals(reportingDate)) {
                // T-1 reporting date

                // Override existing report to make this method behavior idempotent if been executed multiple times within a day
                MTRMrgTradingReport report = mrgTradingReportRepository.findByReportingDate(reportingDate)
                  .orElse(new MTRMrgTradingReport());

                report.setCreatedDate(LocalDateTime.now());
                report.setReportingDate(reportingDate);
                report.setMtrCounterDate(LocalDate.now());
                try {
                  List<MTRMemberList> eligibleMTRMembers = memberListRepository.findByEligibleMemberMtrStatus(true);
                  int totalMember = eligibleMTRMembers.size();
                  report.setMtrCounterTotal(totalMember);

                  Set<Long> submittedMemberIds = files.stream()
                    .map(f -> f.getMember().getMemId()).collect(Collectors.toSet());
                  List<Long> nonSubmittedMemberIds = eligibleMTRMembers.stream()
                    .map(m -> m.getMember().getMemId())
                    .filter(id -> !submittedMemberIds.contains(id))
                    .collect(Collectors.toUnmodifiableList());
                  // set counter information
                  report.setMtrCounterLatest(nonSubmittedMemberIds.size());

                  List<MTRDailyFile> previousFiles = dailyFileRepository
                    .findLastSuccessfulSubmissionByMemberIds(nonSubmittedMemberIds);
                  List<MTRDetailRecord> provisionalRecords = getMTRDetailRecord(previousFiles);
                  List<MTRDetailRecord> submittedRecords = getMTRDetailRecord(files);

                  List<MTRDetailRecord> allRecords = new ArrayList<>();
                  allRecords.addAll(submittedRecords);
                  allRecords.addAll(provisionalRecords);

                  String outputFile =
                      writeToMarginTradingReportFile(reportingDate, allRecords,
                        totalMember == submittedRecords.size(), null);

                  if (report.getMtrCounterLatest() == report.getMtrCounterTotal()) {
                    // all member submitted.
                    report.setMrgTradingFinalStatus(outputFile != null);
                    report.setFinalReportDmsDocIndex(outputFile);
                  } else {
                    // not all member submitted.
                    report.setMrgTradingProvisionalStatus(outputFile != null);
                    report.setProvisionalReportDmsDocIndex(outputFile);
                  }

                  report.setNotifyDailySignoffStatus(true);

                  mrgTradingReportRepository.saveAndFlush(report);
                } catch (Exception e) {
                  log.error("Error occurs when generate mrg trading report.", e);
                  // ignore the error.
                }
              } else {
                // for later submission.
                mrgTradingReportRepository.findByReportingDate(reportingDate)
                    .ifPresentOrElse(
                        report -> {
                          // update the counter
                          List<MTRDailyFile> allFiles = dailyFileRepository
                            .findByReportingDate(reportingDate.atStartOfDay());
                          List<MTRDailyFile> submittedFiles = allFiles.stream()
                            .filter(f -> f.getDailyFileStatus())
                            .collect(Collectors.toUnmodifiableList());
                          List<MTRDetailRecord> submittedRecords = getMTRDetailRecord(submittedFiles);

                          report.setMtrCounterLatest(allFiles.size() - submittedFiles.size());
                          report.setMtrCounterDate(LocalDate.now());
                          if (report.getMtrCounterLatest() == 0) {
                            // all member submitted
                            List<List<String>> provisionalData = report.getProvisionalReportDmsDocIndex() == null ? null : parseCsvFile(report.getProvisionalReportDmsDocIndex());
                            String outputFile = writeToMarginTradingReportFile(reportingDate, submittedRecords,
                              true, provisionalData);

                            if (outputFile != null) {
                              report.setMrgTradingFinalStatus(true);
                              report.setFinalReportDmsDocIndex(outputFile);
                            }
                          }
                          mrgTradingReportRepository.saveAndFlush(report);
                        },
                        () -> {
                          // ignore this date and log an error
                          log.error(
                              "The mrg trading report hasn't been created for reporting date "
                                  + CommonUtils.getDatabaseDateStr(reportingDate));
                        });
              }
            });
  }

  private List<List<String>> parseCsvFile(String filePath) {
    List<List<String>> data = new ArrayList<>();
    File file = new File(filePath);
    if (!file.exists()) {
      // ignore this date and log an error
      log.error("file[%s] does not exist", filePath);
      return data;
    }


    try {
      Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
      CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT);
      for (CSVRecord record : parser) {
        List<String> rowValues = IteratorUtils.toList(record.iterator());
        List<String> row = new ArrayList<>();
        for (String cell : rowValues) {
            row.add(cell);
        }
        data.add(row);
      }
    } catch (Exception e) {
      log.error("read csv failed", e);
    }

    return data;
  }

  public String writeToMarginTradingReportFile(
    LocalDate reportingDate, List<MTRDetailRecord> records, boolean isFinalReport, List<List<String>> provisionalData) {
    try {
      List<String> params =
        isFinalReport
          ? new ArrayList<>() {
          {
            add(reportingDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
          }
        }
          : new ArrayList<>();
      params.addAll(
        Arrays.asList(
          new String[] {
            reportingDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            String.format("%.2f",
                    records.stream().mapToDouble(MTRDetailRecord::getFundedAmountBeginDay).sum()),
                String.format("%.2f",
                    records.stream().mapToDouble(MTRDetailRecord::getFundedAmountDuringDay).sum()),
                String.format("%.2f",
                    records.stream()
                        .mapToDouble(MTRDetailRecord::getFundedAmountLiquidatedDuringDay)
                        .sum()),
                String.format("%.2f",
                    controlRecordRepository.sumTotalAmountFunded(reportingDate.atStartOfDay()))
              }));

      List<List<String>> outputData =
          fillDateWithTemplate(
              params,
              records,
              isFinalReport ? tradingFinalReportTemplate : tradingProvisionalReportTemplate);

      List<List<String>> finalOutputData;
      if (provisionalData == null) {
        finalOutputData = outputData;
      } else {
        finalOutputData = new ArrayList<>();

        List<List<String>> provisionalSymbols = provisionalData.subList(
          MARGIN_TRADING_REPORT_HEADER_LINE_NUMBER,
          provisionalData.size() - MARGIN_TRADING_REPORT_FOOTER_LINE_NUMBER
        );
        List<List<String>> finalSymbols = outputData.subList(
          MARGIN_TRADING_REPORT_HEADER_LINE_NUMBER,
          outputData.size() - MARGIN_TRADING_REPORT_FOOTER_LINE_NUMBER
        );
        Map<String, List<String>> finalSymbolLineMap = finalSymbols.stream()
          .collect(Collectors.toMap(
            lines -> lines.get(0),
            lines -> lines
          ));
        for (int i = 0; i < finalSymbols.size(); i++) {
          finalSymbols.set(i, null);
        }
        for (int i = 0; i < provisionalSymbols.size(); i++) {
          if (i < finalSymbols.size()) {
            List<String> matchedLine = finalSymbolLineMap.remove(provisionalSymbols.get(i).get(0));
            if (matchedLine != null) {
              finalSymbols.set(i, matchedLine);
            }
          }
        }
        List<Integer> emptySlots = new ArrayList<>();
        for (int i = 0; i < finalSymbols.size(); i++) {
          if (finalSymbols.get(i) == null) {
            emptySlots.add(i);
          }
        }
        List<List<String>> unMatchedLines = finalSymbolLineMap.values().stream()
          .sorted(Comparator.comparing(o -> o.get(0)))
          .collect(Collectors.toUnmodifiableList());
        if (emptySlots.size() != unMatchedLines.size()) {
          log.error("Something wrong here, emptySlots.size()[{}] does not equal unMatchedLines.size()[{}]",
            emptySlots.size(), unMatchedLines.size());
        } else {
          for (int i = 0; i < unMatchedLines.size(); i++) {
            finalSymbols.set(emptySlots.get(i), unMatchedLines.get(i));
          }
        }

        List<List<String>> rightPart = new ArrayList<>();
        rightPart.addAll(outputData.subList(0, MARGIN_TRADING_REPORT_HEADER_LINE_NUMBER));
        rightPart.addAll(finalSymbols);
        rightPart.addAll(outputData.subList(outputData.size() - MARGIN_TRADING_REPORT_FOOTER_LINE_NUMBER, outputData.size()));

        outputData = rightPart;

        List<List<String>> shorterList;
        if (outputData.size() > provisionalData.size()) {
          shorterList = provisionalData;
        } else {
          shorterList = outputData;
        }
        int linesToAdd = Math.abs(outputData.size() - provisionalData.size());
        for (int i = 0; i < linesToAdd; i++) {
          shorterList.add(
            Collections.nCopies(
              shorterList.get(shorterList.size() - 1).size(),
              ""));
        }
        for (int i = 0; i < outputData.size(); i++) {
          List<String> list = new ArrayList<>();
          list.addAll(provisionalData.get(i));
          list.addAll(Collections.nCopies(2, ""));
          list.addAll(outputData.get(i));
          finalOutputData.add(list);
        }
      }

      // print to a file
      String outputFile = CommonUtils.getFilePath(resFileDirectory, UUID.randomUUID().toString() + ".csv");
      PrintWriter fos = new PrintWriter(outputFile);
      CSVPrinter csvPrinter = new CSVPrinter(fos, CSVFormat.DEFAULT);
      for (List<String> row : finalOutputData) {
        csvPrinter.printRecord(row);
      }
      csvPrinter.flush();
      csvPrinter.close();
      return outputFile;
    } catch (Exception e) {
      log.error("Error occurs when generate mrg trading report.", e);
      return null;
    }
  }

  private List<List<String>> fillDateWithTemplate(
      List<String> params, List<MTRDetailRecord> items, Resource template) throws IOException {
    Reader reader = new InputStreamReader(template.getInputStream(), StandardCharsets.UTF_8);
    CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT);
    List<List<String>> data = new ArrayList<>();
    int paramIndex = 0;
    for (CSVRecord record : parser) {
      List<String> rowValues = IteratorUtils.toList(record.iterator());
      List<String> row = new ArrayList<>();
      for (String cell : rowValues) {
        if (cell.contains("%s")) {
          row.add(String.format(cell, params.get(paramIndex++)));
        } else {
          row.add(cell);
        }
      }
      data.add(row);
    }
    List<List<String>> footer = new ArrayList<>();
    for (int i = 0; i < MARGIN_TRADING_REPORT_FOOTER_LINE_NUMBER; i++) {
      footer.add(data.remove(data.size() - 1));
    }
    Collections.reverse(footer);

    items.stream()
      .collect(Collectors.groupingBy(MTRDetailRecord::getSymbol))
      .entrySet()
      .stream()
      .forEach(e -> {
        data.add(
          Arrays.asList(
            new String[] {
              e.getKey().getSymbolCode(),
              e.getKey().getSymbolName(),
              String.valueOf(
                e.getValue().stream().mapToInt(MTRDetailRecord::getFundedQuantityEndDay).sum()
              ),
              String.format("%.2f",
                e.getValue().stream().mapToDouble(MTRDetailRecord::getFundedAmountEndDay).sum()
              )
            }));
      });

    data.addAll(footer);
    return data;
  }

  public void publishNSEWeb() {
    LocalDate currentReportingDate = getLastBusinessDay(LocalDate.now());
    publishNSEWeb(currentReportingDate);
  }

  public void publishNSEWeb(LocalDate currentReportingDate) {
    mrgTradingReportRepository
        .findByMtrCounterDate(LocalDate.now())
        .forEach(
            report -> {
              if (Boolean.TRUE.equals(report.getMrgTradingFinalStatus())) {
                // final report generated.
                boolean sent = mailService.sendPublishToNSEWebEmail(new File(report.getFinalReportDmsDocIndex()));

                if (sent) {
                  report.setNotifyMrgTradingStatus(true);
                  mrgTradingReportRepository.saveAndFlush(report);
                }
              }

              if (currentReportingDate.equals(report.getReportingDate())
                  && (Boolean.TRUE.equals(report.getMrgTradingProvisionalStatus()))) {
                mailService.sendPublishToNSEWebEmail(
                    new File(report.getProvisionalReportDmsDocIndex()));
              }
            });
  }

  public NonSubmissionMbrDtlsResponse getNonSubmissionMbrDtls(int year, int month, String memberCode, String memberName) {
      NonSubmissionMbrDtlsResponse response = new NonSubmissionMbrDtlsResponse();
      List<NonSubmissionMbrDtlsDto> dtos = new ArrayList<>();
      response.setNonSubmissionMemberList(dtos);
      List<LocalDate> businessDates = CommonUtils.getBusinessDaysForYearMonth(year, month);
      int businessDays = businessDates.size();
      String startDate = getDatabaseDateStr(businessDates.get(0));
      String endDate = getDatabaseDateStr(businessDates.get(businessDays - 1));
      List<NonSubmissionMbrDtlsDtoInterface> dtoInterfaces;
      if (StringUtils.hasText(memberCode) || StringUtils.hasText(memberName)) {
          dtoInterfaces = dailyFileRepository.getNonSubmissionMbrDtlsByMemberCodeOrMemberName(
              year, month, businessDays, startDate, endDate, memberCode, memberName
          );
          String sql = dailyFileRepository.SQL_NON_SUBMISSION_MBR_DTLS_BY_MEMBER_CODE_OR_MEMBER_NAME;
          Map<String, String> replacementStrings = Map.of(
              ":year", String.format("%d", year),
              ":month", String.format("%d", month),
              ":businessDays", String.format("%d", businessDays),
              ":startDate", String.format("'%s'", startDate),
              ":endDate", String.format("'%s'", endDate),
              ":memberCode", String.format("'%s'", memberCode),
              ":memberName", String.format("'%s'", memberName)
          );
          for (Map.Entry<String, String> e : replacementStrings.entrySet()) {
              sql = sql.replace(e.getKey(), e.getValue());
          }
          response.setNonSubmissionSqlQuery(sql);
      } else {
          dtoInterfaces = dailyFileRepository.getNonSubmissionMbrDtls(
              year, month, businessDays, startDate, endDate
          );
          String sql = dailyFileRepository.SQL_NON_SUBMISSION_MBR_DTLS;
          Map<String, String> replacementStrings = Map.of(
              ":year", String.format("%d", year),
              ":month", String.format("%d", month),
              ":businessDays", String.format("%d", businessDays),
              ":startDate", String.format("'%s'", startDate),
              ":endDate", String.format("'%s'", endDate)
          );
          for (Map.Entry<String, String> e : replacementStrings.entrySet()) {
              sql = sql.replace(e.getKey(), e.getValue());
          }
          response.setNonSubmissionSqlQuery(sql);
      }
      for (NonSubmissionMbrDtlsDtoInterface item : dtoInterfaces) {
          NonSubmissionMbrDtlsDto dto = new NonSubmissionMbrDtlsDto();
          dto.setMemberName(item.getMemberName());
          dto.setMemberCode(item.getMemberCode());
          dto.setMemberId(item.getMemberId());
          dto.setNonSubmissionCount(item.getNonSubmissionCount());
          dto.setYear(year);
          dto.setMonth(month);
          dtos.add(dto);
      }
      return response;
  }

}
