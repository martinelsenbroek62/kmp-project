package com.nseindia.mc.service.mtrReport;

import com.google.common.collect.ImmutableMap;
import com.nseindia.common.drools.service.DrlFileRulesService;
import com.nseindia.common.drools.service.DroolsUtil;
import com.nseindia.common.drools.service.ExcelRulesService;
import com.nseindia.mc.controller.dto.DroolsEnv;
import com.nseindia.mc.controller.dto.ErrorCode;
import com.nseindia.mc.controller.dto.LineValidationResult;
import com.nseindia.mc.controller.dto.MTRDailyFileErrorDto;
import com.nseindia.mc.controller.dto.MTRDetailRecordDtoInterface;
import com.nseindia.mc.controller.dto.MTRFileValidationResult;
import com.nseindia.mc.controller.dto.MTRValidateFileDto;
import com.nseindia.mc.controller.dto.MTRValidateRecordSummary;
import com.nseindia.mc.controller.dto.MemberUploadStatus;
import com.nseindia.mc.controller.dto.SubmitMTRFileResponse;
import com.nseindia.mc.exception.BaseServiceException;
import com.nseindia.mc.model.AuditableEntity;
import com.nseindia.mc.model.BaseRecord;
import com.nseindia.mc.model.MTRCollateralsScripsDetailRecord;
import com.nseindia.mc.model.MTRCollateralsSummaryRecord;
import com.nseindia.mc.model.MTRControlRecord;
import com.nseindia.mc.model.MTRDailyFile;
import com.nseindia.mc.model.MTRDailyFileValidation;
import com.nseindia.mc.model.MTRDetailRecord;
import com.nseindia.mc.model.MTRSummaryRecord;
import com.nseindia.mc.model.MTRSymbolName;
import com.nseindia.mc.model.MemberMaster;
import com.nseindia.mc.repository.MTRCollateralScripsDetailRecordRepository;
import com.nseindia.mc.repository.MTRCollateralSummaryRecordRepository;
import com.nseindia.mc.repository.MTRControlRecordRepository;
import com.nseindia.mc.repository.MTRDailyFileRepository;
import com.nseindia.mc.repository.MTRDailyFileValidationRepository;
import com.nseindia.mc.repository.MTRDetailRecordRepository;
import com.nseindia.mc.repository.MTRErrorCodeMasterRepository;
import com.nseindia.mc.repository.MTRErrorDailyFileRepository;
import com.nseindia.mc.repository.MTRSummaryRecordRepository;
import com.nseindia.mc.repository.MTRSymbolNameRepository;
import com.nseindia.mc.repository.MemberListRepository;
import com.nseindia.mc.repository.MemberMasterRepository;
import com.nseindia.mc.util.CommonUtils;
import com.nseindia.mc.util.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.text.StringSubstitutor;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.nseindia.mc.util.CommonUtils.addFileToZip;
import static com.nseindia.mc.value.MtrRecordValidateResult.Reject;
import static com.nseindia.mc.value.MtrRecordValidateResult.Success;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
public class MtrFileValidateService {
  private static final String DECLARATION = "I hereby confirm that my exposure towards the margin trading facility has not exceeded the borrowed funds (if any) and 50% of my networth.";
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");
  private static final List<String> RULES_FILES = Arrays.asList(
    "aiml-service-mtr-collateral-rules.drl",
    "aiml-service-mtr-collateral-script-rules.drl",
    "aiml-service-mtr-control-rules.drl",
    "aiml-service-mtr-detail-rules.drl",
    "aiml-service-mtr-summary-rules.drl"
  );

  private static final Map<String, List<String>> ORDER_HEADER = new ImmutableMap.Builder().put("00", newArrayList("10"))
      .put("10", newArrayList("10", "30")).put("30", newArrayList("30", "20")).put("20", newArrayList("20", "40"))
      .put("40", newArrayList("40", "50")).put("50", newArrayList("50")).build();
  @Autowired
  private MemberMasterRepository memberMasterRepository;
  @Autowired
  private MemberListRepository memberListRepository;
  @Autowired
  private ExcelRulesService excelRulesService;
  @Autowired
  private MtrReportService mtrReportService;

  @Autowired
  private DrlFileRulesService drlRulesService;
  @Autowired
  private DroolsUtil droolsUtil;

  @Autowired
  private MTRDailyFileRepository dailyFileRepository;
  @Autowired
  private MTRErrorDailyFileRepository errorDailyFileRepository;
  @Autowired
  private MTRControlRecordRepository controlRecordRepository;
  @Autowired
  private MTRSummaryRecordRepository summaryRecordRepository;
  @Autowired
  private MTRDetailRecordRepository detailRecordRepository;
  @Autowired
  private MTRCollateralSummaryRecordRepository collateralSummaryRecordRepository;
  @Autowired
  private MTRDailyFileValidationRepository dailyFileValidationRepository;

  @Autowired
  private MTRCollateralScripsDetailRecordRepository collateralScripsDetailRecordRepository;

  @Autowired
  private MTRErrorCodeMasterRepository mtrErrorCodeMasterRepository;

  @Autowired
  private MTRSymbolNameRepository symbolNameRepository;

  @Value("${dms.response.path}")
  private String resFileDirectory;

  @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
  private int batchSize;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private KieContainer kieContainer;

  @PostConstruct
  private void initializeKieContainer() {
    log.info("start initializeKieContainer");
    KieServices kieServices = KieServices.Factory.get();
    KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
    RULES_FILES.forEach(droolsFile -> {
      kieFileSystem.write(ResourceFactory.newFileResource(CommonUtils.getRulesFilePath(droolsFile)));
    });
    KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
    kieBuilder.buildAll();
    KieModule kieModule = kieBuilder.getKieModule();
    kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());
    log.info("finish initializeKieContainer");
  }

  /**
   * Validate MTR file
   *
   * @param file     the validate file
   * @param memberId the member id
   * @return validateFileDto
   */
  public MTRValidateFileDto validateMtrFile(final MultipartFile file, final Long memberId) {
    log.info("start validateMtrFile for file[{}] and member[{}]", file.getOriginalFilename(), memberId);
    MemberMaster member = memberMasterRepository.findById(memberId)
        .orElseThrow(() -> new BaseServiceException("the tradingMemberId is not found", HttpStatus.BAD_REQUEST));
    
    try {
      MTRFileValidationResult fileResult = validate(member, file.getOriginalFilename().toUpperCase(), file.getSize() > 0 ? file.getInputStream() : null, false);

      boolean validateSuccessful = !fileResult.isFailed();

      LocalDate reportDate = getReportDate(file.getOriginalFilename());

      String uploadedFilePath = CommonUtils.getFilePath(resFileDirectory, UUID.randomUUID().toString() + ".csv");
      File uploadedFile = new File(uploadedFilePath);
      try (OutputStream output = new FileOutputStream(uploadedFile, false)) {
        file.getInputStream().transferTo(output);
      }
      MTRDailyFileValidation validationEntity = new MTRDailyFileValidation();
      validationEntity.setMember(member);
      validationEntity.setReportingDate(reportDate.atStartOfDay());
      validationEntity.setDailyFileStatus(validateSuccessful);
      validationEntity.setResponseFileName(fileResult.getResponseFileName());
      validationEntity.setDmsResIndex(fileResult.getResponseFilePath());
      validationEntity.setDailyFileName(file.getOriginalFilename());
      validationEntity.setErrorCodeFileName(fileResult.getErrorCodeFileName());
      validationEntity.setErrorCodeFileDmsIndex(fileResult.getErrorCodeFilePath());
      validationEntity.setDmsDocIndex(uploadedFilePath);
      validationEntity.setBatchNo(fileResult.getMaxBatchNo() + 1);
      addAudit(validationEntity, memberId);

      // save validation entry
      validationEntity = dailyFileValidationRepository.saveAndFlush(validationEntity);
      log.info("{}", validationEntity);

      MTRValidateFileDto dto = new MTRValidateFileDto();
      dto.setUploadedFileName(file.getOriginalFilename());
      dto.setResponseFileURL(fileResult.getResponseFileName());
      dto.setFileValidationId(validationEntity.getId());
      dto.setRecordSummary(fileResult.getRecordSummaryList());
      if (validateSuccessful) {
        dto.setValidMtrFile("Y");
      } else {
        dto.setValidMtrFile("N");
        dto.setMissedDates(fileResult.getMissedDates());
        dto.setDailyFileErrors(fileResult.getDailyFileErrors());
      }

      return dto;
    } catch (IOException | RuntimeException e) {
      log.error("validateMtrFile failed unexpectedly", e);
      throw new BaseServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public ByteArrayOutputStream downloadMtrFileValidationResponse(long memberId, long validationId) {

    MTRDailyFileValidation validation = dailyFileValidationRepository.findById(validationId)
        .orElseThrow(() -> new BaseServiceException("The file hasn't been validated yet.", HttpStatus.NOT_FOUND));

    if (validation.getMember().getMemId() != memberId) {
      throw new BaseServiceException(
          String.format("the validation[%d] does not belong to member[%d]", validationId, memberId),
          HttpStatus.BAD_REQUEST);
    }

    List<ImmutablePair<String, String>> filePairs = newArrayList();
    filePairs.add(ImmutablePair.of(
        validation.getResponseFileName(),
        validation.getDmsResIndex()
    ));
    filePairs.add(ImmutablePair.of(
        validation.getErrorCodeFileName(),
        validation.getErrorCodeFileDmsIndex()
    ));
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

  private void printResponseRecords(List<LineValidationResult> records, PrintWriter printWriter) throws IOException {
    for (LineValidationResult line : records) {
      printWriter.println(line.getLine() + "," + (line.getErrorCodes().isEmpty() ? "S" : "R") +
        (line.getErrorCodes().isEmpty() ? "" : "," + String.join(",", line.getErrorCodes())));
    }
  }

  private void csvPrintErrorCodeRecords(List<LineValidationResult> records, CSVPrinter csvPrinter, Map<String, String> messageTemplates) throws IOException {
    for (LineValidationResult l : records) {
      for (ErrorCode error : l.getErrors()) {
        String template = messageTemplates.getOrDefault(error.getCode(), error.getCode());
        String message = new StringSubstitutor(error.getParameters(), "<%", ">").replace(template);
        csvPrinter.printRecord(error.getCode(), message);
      }
    }
  }

  private void printCachedRecordsAndDeleteCacheFile(String cacheFilePath, PrintWriter printWriter) throws IOException {
    File cacheFile = new File(cacheFilePath);
    LineIterator detailRecordResponseIt = FileUtils.lineIterator(cacheFile, "UTF-8");
    while (detailRecordResponseIt.hasNext()) {
      printWriter.println(detailRecordResponseIt.nextLine());
    }
    detailRecordResponseIt.close();
    cacheFile.delete();
    printWriter.flush();
  }

  private void batchValidate(List<LineValidationResult> batchRecords,
                             PrintWriter detailRecordResponsePrintWriter,
                             CSVPrinter detailRecordErrorCodeCsvPrinter,
                             PrintWriter collateralScripsDetailRecordResponsePrintWriter,
                             CSVPrinter collateralScripsDetailRecordErrorCodeCsvPrinter,
                             MutableInt newDetailRecordsErrorCount,
                             MutableInt newCollateralScriptsDetailRecordsErrorCount,
                             DroolsEnv env,
                             Map<String, String> messageTemplates) throws IOException {
    KieSession kieSession = kieContainer.newKieSession();
    kieSession.setGlobal("env", env);
    batchRecords.stream().forEach(l ->
      kieSession.insert(l)
    );
    kieSession.fireAllRules();
    kieSession.dispose();
    for (LineValidationResult line : batchRecords) {
      if ("20".equals(line.getType())) {
        updateFileAndCounter(line, detailRecordResponsePrintWriter, detailRecordErrorCodeCsvPrinter,
          newDetailRecordsErrorCount, messageTemplates);
      } else {
        updateFileAndCounter(line, collateralScripsDetailRecordResponsePrintWriter,
          collateralScripsDetailRecordErrorCodeCsvPrinter, newCollateralScriptsDetailRecordsErrorCount, messageTemplates);
      }
    }
  }

  private void updateFileAndCounter(
    LineValidationResult line,
    PrintWriter currentResponsePrintWriter,
    CSVPrinter currentErrorCodeCsvPrinter,
    MutableInt newErrorCount,
    Map<String, String> messageTemplates
  ) throws IOException {
    currentResponsePrintWriter.println(line.getLine() + "," + (line.getErrorCodes().isEmpty() ? "S" : "R") +
      (line.getErrorCodes().isEmpty() ? "" : "," + String.join(",", line.getErrorCodes())));
    if (!line.getErrorCodes().isEmpty()) {
      newErrorCount.increment();
      for (ErrorCode error : line.getErrors()) {
        String template = messageTemplates.getOrDefault(error.getCode(), error.getCode());
        String message = new StringSubstitutor(error.getParameters(), "<%", ">").replace(template);
        currentErrorCodeCsvPrinter.printRecord(error.getCode(), message);
      }
    }
  }

  /**
   * Submit MTR file.
   *
   * @param memberId the member id
   * @return submit message
   */
  public SubmitMTRFileResponse submitMtrFile(Long memberId, Long validationId) {
    log.info("start submitMtrFile for member[{}] and validation[{}]", memberId, validationId);
    memberMasterRepository.findById(memberId)
        .orElseThrow(() -> new BaseServiceException("the tradingMemberId is not found", HttpStatus.BAD_REQUEST));

    MTRDailyFileValidation validation = dailyFileValidationRepository.findById(validationId)
        .orElseThrow(() -> new BaseServiceException("The file hasn't been valiated yet.", HttpStatus.NOT_FOUND));

    if (!validation.getDailyFileStatus()) {
      throw new BaseServiceException("The submitted file doesn't pass the validation.", HttpStatus.BAD_REQUEST);
    }
    
    if (!memberId.equals(validation.getMember().getMemId())) {
    	throw new BaseServiceException("The submitted file doesn't belong to current user.", HttpStatus.BAD_REQUEST);	
    }
    
    log.info("start reading submission file from {}", validation.getDailyFileName());
    InputStream fileInputStream;
    try {
      fileInputStream = new FileInputStream(new File(validation.getDmsDocIndex()));
    } catch (FileNotFoundException e) {
      throw new BaseServiceException("The submitted file is corrupted.", HttpStatus.BAD_REQUEST);
    }
    LineIterator it;
    try {
      it = IOUtils.lineIterator(fileInputStream, "UTF-8");
    } catch (IOException e) {
      throw new BaseServiceException(String.format("Reading submitted file failed: %s", e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    final List<MTRControlRecord> controlRecords = new ArrayList<>();
    final List<MTRSummaryRecord> summaryRecords = new ArrayList<>();
    final List<MTRDetailRecord> detailRecords = new ArrayList<>();
    final List<MTRCollateralsSummaryRecord> collateralSummaryRecords = new ArrayList<>();
    final List<MTRCollateralsScripsDetailRecord> collateralScripsDetailRecords = new ArrayList<>();

    MTRDailyFile mtrDailyFile = dailyFileRepository
      .findByMemberIdAndReportingDateIncludingAutoGeneratedFiles(memberId, CommonUtils.getDatabaseDateStr(validation.getReportingDate()))
      .orElse(new MTRDailyFile());
    mtrDailyFile.setMember(validation.getMember());
    mtrDailyFile.setBatchNo(validation.getBatchNo());
    mtrDailyFile.setDailyFileName(validation.getDailyFileName());
    mtrDailyFile.setDmsDocIndex(validation.getDmsDocIndex());
    mtrDailyFile.setDailyFileSubmissionDate(LocalDateTime.now());
    mtrDailyFile.setReportingDate(validation.getReportingDate());
    mtrDailyFile.setResponseFileName(validation.getResponseFileName());
    mtrDailyFile.setDmsResIndex(validation.getDmsResIndex());
    mtrDailyFile.setNilSubmissionStatus(false);
    mtrDailyFile.setDailyFileStatus(true);
    addAudit(mtrDailyFile, memberId);
    dailyFileRepository.saveAndFlush(mtrDailyFile);

    log.info("start deleting old records");
    controlRecordRepository.deleteByMTRDailyFileId(mtrDailyFile.getId());
    summaryRecordRepository.deleteByMTRDailyFileId(mtrDailyFile.getId());
    detailRecordRepository.deleteByMTRDailyFileId(mtrDailyFile.getId());
    collateralSummaryRecordRepository.deleteByMTRDailyFileId(mtrDailyFile.getId());
    collateralScripsDetailRecordRepository.deleteByMTRDailyFileId(mtrDailyFile.getId());
    log.info("finish deleting old records");

    log.info("start processing file lines");
    int lineNum = 0;
    while (it.hasNext()) {
      lineNum++;
      if (lineNum % 10000 == 0) {
        log.info("Processed lines {}", lineNum);
      }
      String line = StringUtils.trim(it.nextLine());
      if (StringUtils.isBlank(line)) {
        continue;
      }
      List<String> fields = asList(line.split(",")).stream().map(f -> StringUtils.trim(f)).collect(toList());
      String type = fields.get(0);
      if ("10".equals(type)) {
        MTRControlRecord record = parseControlRecord(fields);
        fillRecord(record, memberId, mtrDailyFile);
        controlRecords.add(record);
      } else if ("30".equals(type)) {
        MTRSummaryRecord record = parseSummaryRecord(fields);
        fillRecord(record, memberId, mtrDailyFile);
        summaryRecords.add(record);
      } else if ("20".equals(type)) {
        MTRDetailRecord record = parseDetailRecord(fields);
        fillRecord(record, memberId, mtrDailyFile);
        detailRecords.add(record);
        if (detailRecords.size() == batchSize) {
          batchInsertDetailRecords(detailRecords);
          detailRecords.clear();
        }
      } else if ("40".equals(type)) {
        MTRCollateralsSummaryRecord record = parseCollateralRecord(fields);
        fillRecord(record, memberId, mtrDailyFile);
        collateralSummaryRecords.add(record);
      } else if ("50".equals(type)) {
        MTRCollateralsScripsDetailRecord record = parseCollateralScriptRecord(fields);
        fillRecord(record, memberId, mtrDailyFile);
        collateralScripsDetailRecords.add(record);
        if (collateralScripsDetailRecords.size() == batchSize) {
          batchInsertCollateralScriptsDetailRecords(collateralScripsDetailRecords);
          collateralScripsDetailRecords.clear();
        }
      }
    }
    controlRecordRepository.saveAll(controlRecords);
    summaryRecordRepository.saveAll(summaryRecords);
    batchInsertDetailRecords(detailRecords);
    collateralSummaryRecordRepository.saveAll(collateralSummaryRecords);
    batchInsertCollateralScriptsDetailRecords(collateralScripsDetailRecords);
    log.info("finish processing file lines");

    SubmitMTRFileResponse resp = new SubmitMTRFileResponse();
    resp.setStatus(200);
    resp.setMessage("MTR Submission completely successfully for "
            .concat(validation.getReportingDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
    resp.setReportingDate(validation.getReportingDate());
    resp.setSubmittedDate(mtrDailyFile.getDailyFileSubmissionDate());

    return resp;

  }

  private void batchInsertCollateralScriptsDetailRecords(
      final List<MTRCollateralsScripsDetailRecord> collateralScripsDetailRecords) {
      jdbcTemplate.batchUpdate(
          MTRCollateralsScripsDetailRecord.INSERT_SQL,
          new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int recordIndex) throws SQLException {
              MTRCollateralsScripsDetailRecord.fillPreparedStatement(ps, collateralScripsDetailRecords.get(recordIndex));
            }

            @Override
            public int getBatchSize() {
              return collateralScripsDetailRecords.size();
            }
          });
  }

  public void batchInsertDetailRecords(final List<MTRDetailRecord> detailRecords) {
      jdbcTemplate.batchUpdate(
          MTRDetailRecord.INSERT_SQL,
          new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int recordIndex) throws SQLException {
              MTRDetailRecord.fillPreparedStatement(ps, detailRecords.get(recordIndex));
            }
            @Override
            public int getBatchSize() {
              return detailRecords.size();
            }
          });
  }

    /**
     * Validate file.
   *
   * @param memberMaster the member
   * @return true if the file is valida
   */
  private MTRFileValidationResult validate(
      final MemberMaster memberMaster,
      final String fileName,
      final InputStream fileInputStream,
      final boolean isSubmitting) throws BaseServiceException, IOException {

    int streamBatchSize = 10000;
    List<MTRDailyFileErrorDto> dailyFileErrors = new ArrayList<>();
    List<LocalDate> missedDates = new ArrayList<>();
    
    Map<String, String> messageTemplates = mtrErrorCodeMasterRepository.findAll().stream().collect(toMap(e -> e.getErrorCode(), e -> e.getErrorDesc()));

    MTRFileValidationResult fileResult = new MTRFileValidationResult();
    fileResult.setDailyFileErrors(dailyFileErrors);
    fileResult.setMissedDates(missedDates);

    // file with IF00X error should be rejected above
    // response should be generated for the file with other errors
    if (fileName.length() < 3 || !"mtr".equalsIgnoreCase(fileName.substring(0, 3))) {
      rejectInvalidFile("IF002", of(), messageTemplates);
    }

    if (fileName.length() < 12) {
      rejectInvalidFile("IF003", of("date", fileName.length() < 5 ? "" : fileName.substring(4)), messageTemplates);
    }

    LocalDate reportDate = getReportDate(fileName);
    if (reportDate == null) {
      rejectInvalidFile("IF003", of("date", fileName.substring(4, 12)), messageTemplates);
    }

    MemberUploadStatus memberUploadStatus = mtrReportService.getMemberUploadStatus(memberMaster.getMemId());
    if (memberUploadStatus.getMissedDates().size() == 0 || !memberUploadStatus.getMissedDates().get(0).equals(reportDate)) {
      log.info("validate file failed with reportDate={}, missedDates={}", reportDate, memberUploadStatus.getMissedDates());
      rejectInvalidFile("IF003", of("date", fileName.substring(4, 12)), messageTemplates);
    }

    if (!"t".equalsIgnoreCase(fileName.substring(13, 14))) {
      rejectInvalidFile("IF004", of(), messageTemplates);
    }
    if (getBatchNo(fileName) == 0) {
      rejectInvalidFile("IF005", of("batchNo", fileName.substring(14)), messageTemplates);
    }

    int maxBatchNo = 0;

    // validating, get the maxBatchNo from validation file table
    Optional<MTRDailyFileValidation> validation = dailyFileValidationRepository.findFirstByMember_MemIdAndReportingDateOrderByBatchNoDesc(memberMaster.getMemId(), reportDate.atStartOfDay());
    if (validation.isPresent()) {
      maxBatchNo = validation.get().getBatchNo();
    }

    int batchNo = getBatchNo(fileName);
    if (!isSubmitting && batchNo - maxBatchNo != 1) {
      rejectInvalidFile("IF006", of("batchNo", batchNo, "maxBatchNo", maxBatchNo), messageTemplates);
    }
    fileResult.setMaxBatchNo(maxBatchNo);

    log.info("start to read file lines.");
    List<LineValidationResult> batchRecords = new ArrayList<>();
    int totalRecords = 0;
    String lastType = "00";
    Set<String> recordTypes = new HashSet<>();
    boolean hasInvalidOrder = false;
    int declarationLineNo = -1;
    boolean hasMissingRecordType = false;
    int collateralScripCount = 0;
    boolean duplicatePan = false;
    Set<String> panSymbolSet = new HashSet<>();

    Set<String> validSymbolCodes = symbolNameRepository.findAll().stream()
      .map(s -> s.getSymbolCode()).collect(Collectors.toSet());
    DroolsEnv env = new DroolsEnv();
    env.setValidSymbolCodes(validSymbolCodes);
    int summaryCount = 0;
    int detailCount = 0;
    double summaryAmount = 0.0;
    double detailsAmount = 0.0;
    env.setBatchNum(getBatchNo(fileName));
    env.setMemberCode(memberMaster.getMemCd());
    env.setBatchDate(fileName.substring(4, 12));
    // get the previous business day
    LocalDate lastBusinessDay = CommonUtils.getLastBusinessDay(reportDate);
    String lastBusinessDayStr = CommonUtils.getDatabaseDateStr(lastBusinessDay);
    // get the mtrDaily file of previous business day
    log.info("lastBusinessDay is {}", lastBusinessDay);
    Optional<MTRDailyFile> dailyFile = dailyFileRepository.findByMemberIdAndReportingDate(memberMaster.getMemId(), lastBusinessDayStr);
    env.setPreviousDate(lastBusinessDay);
    env.setCurrentDate(reportDate);

    double scriptAmount = 0.0;

    List<LineValidationResult> controlRecords = new ArrayList<>();
    List<LineValidationResult> summaryRecords = new ArrayList<>();
    List<LineValidationResult> collateralSummaryRecords = new ArrayList<>();

    String detailRecordResponseFilePath = CommonUtils.getFilePath(resFileDirectory, UUID.randomUUID().toString() + ".csv");
    PrintWriter detailRecordResponsePrintWriter = new PrintWriter(detailRecordResponseFilePath);
    String detailRecordErrorCodeFilePath = CommonUtils.getFilePath(resFileDirectory, UUID.randomUUID().toString() + ".csv");
    PrintWriter detailRecordErrorCodePrintWriter = new PrintWriter(detailRecordErrorCodeFilePath);
    CSVPrinter detailRecordErrorCodeCsvPrinter = new CSVPrinter(detailRecordErrorCodePrintWriter, CSVFormat.DEFAULT);

    String collateralScripsDetailRecordResponseFilePath = CommonUtils.getFilePath(resFileDirectory, UUID.randomUUID().toString() + ".csv");
    PrintWriter collateralScripsDetailRecordResponsePrintWriter = new PrintWriter(collateralScripsDetailRecordResponseFilePath);
    String collateralScripsDetailRecordErrorCodeFilePath = CommonUtils.getFilePath(resFileDirectory, UUID.randomUUID().toString() + ".csv");
    PrintWriter collateralScripsDetailRecordErrorCodePrintWriter = new PrintWriter(collateralScripsDetailRecordErrorCodeFilePath);
    CSVPrinter collateralScripsDetailRecordErrorCodeCsvPrinter = new CSVPrinter(collateralScripsDetailRecordErrorCodePrintWriter, CSVFormat.DEFAULT);

    List<PrintWriter> printWriterList = Arrays.asList(
      detailRecordResponsePrintWriter,
      detailRecordErrorCodePrintWriter,
      collateralScripsDetailRecordResponsePrintWriter,
      collateralScripsDetailRecordErrorCodePrintWriter
    );
    List<CSVPrinter> csvPrinterList = Arrays.asList(
      detailRecordErrorCodeCsvPrinter,
      collateralScripsDetailRecordErrorCodeCsvPrinter
    );

    LineValidationResult declarationLine = null;

    LineIterator it = IOUtils.lineIterator(fileInputStream, "UTF-8");
    int detailRecordsErrorCount = 0;
    int collateralScripsDetailRecordsErrorCount = 0;
    List<Integer> keys = new ArrayList<>();
    while (it.hasNext()) {
      String line = StringUtils.trim(it.nextLine());
      if (StringUtils.isBlank(line)) {
        continue;
      }
      List<String> fields = asList(line.split(",")).stream().map(f -> StringUtils.trim(f)).collect(toList());
      LineValidationResult record = new LineValidationResult(line, fields.get(0), fields);
      totalRecords++;

      // validateContentOrder
      String type = record.getType();
      recordTypes.add(type);
      recordTypes.add(type);
      if (!ORDER_HEADER.getOrDefault(lastType, newArrayList()).contains(type)) {
        if (ORDER_HEADER.get(type) != null) {
          // invalid order
          if (!hasInvalidOrder) {
            hasInvalidOrder = true;
          }
        }
      }
      if (DECLARATION.equals(record.getLine())) {
        declarationLineNo = totalRecords;
        declarationLine = new LineValidationResult(line, "declaration", null);
      }

      if ("10".equals(record.getType())) {
        controlRecords.add(record);
      } else if ("30".equals(record.getType())) {
        summaryRecords.add(record);
        summaryCount++;
        summaryAmount += ValidationUtils.getDouble(record, 3);
      } else if ("20".equals(record.getType())) {
        detailsAmount += ValidationUtils.getDouble(record, 12);
        detailCount++;
        if (!duplicatePan && record.getFields().size() >= 4) {
          String panSymbol = record.getFields().get(2) + record.getFields().get(3);
          if (panSymbolSet.contains(panSymbol)) {
            duplicatePan = true;
          } else {
            panSymbolSet.add(panSymbol);
          }
        }
        batchRecords.add(record);
        keys.add(MTRDetailRecord.createHashCodeClientNamePanSymbol(
          record.getFields().get(1),
          record.getFields().get(2),
          record.getFields().get(3)));
      } else if ("40".equals(record.getType())) {
        collateralSummaryRecords.add(record);
        if (record.getFields().size() > 3 && ValidationUtils.isPositive(record.getFields().get(3))) {
          collateralScripCount++;
        }
      } else if ("50".equals(record.getType())) {
        scriptAmount += ValidationUtils.getDouble(record, 6);
        batchRecords.add(record);
      }

      if (batchRecords.size() == streamBatchSize) {
        log.info("start batch validate records, size={}, processed={}", batchRecords.size(), totalRecords);
        if (keys.size() > 0) {
          Map<String, MTRDetailRecordDtoInterface> records = fetchRecords(dailyFile, keys);
          env.setRecords(records);
        }
        MutableInt newDetailRecordsErrorCount = new MutableInt(0);
        MutableInt newCollateralScriptsDetailRecordsErrorCount = new MutableInt(0);
        batchValidate(
          batchRecords,
          detailRecordResponsePrintWriter,
          detailRecordErrorCodeCsvPrinter,
          collateralScripsDetailRecordResponsePrintWriter,
          collateralScripsDetailRecordErrorCodeCsvPrinter,
          newDetailRecordsErrorCount,
          newCollateralScriptsDetailRecordsErrorCount,
          env,
          messageTemplates);
        detailRecordsErrorCount += newDetailRecordsErrorCount.intValue();
        collateralScripsDetailRecordsErrorCount += newCollateralScriptsDetailRecordsErrorCount.intValue();
        batchRecords.clear();
        env.setRecords(null);
        keys.clear();
        log.info("finish batch validate records");
      }
      lastType = type;
    }
    it.close();

    if (batchRecords.size() > 0) {
      log.info("start batch validate records, size={}, processed={}", batchRecords.size(), totalRecords);
      if (keys.size() > 0) {
        Map<String, MTRDetailRecordDtoInterface> records = fetchRecords(dailyFile, keys);
        env.setRecords(records);
      }
      MutableInt newDetailRecordsErrorCount = new MutableInt(0);
      MutableInt newCollateralScriptsDetailRecordsErrorCount = new MutableInt(0);
      batchValidate(
        batchRecords,
        detailRecordResponsePrintWriter,
        detailRecordErrorCodeCsvPrinter,
        collateralScripsDetailRecordResponsePrintWriter,
        collateralScripsDetailRecordErrorCodeCsvPrinter,
        newDetailRecordsErrorCount,
        newCollateralScriptsDetailRecordsErrorCount,
        env,
        messageTemplates);
      detailRecordsErrorCount += newDetailRecordsErrorCount.intValue();
      collateralScripsDetailRecordsErrorCount += newCollateralScriptsDetailRecordsErrorCount.intValue();
      batchRecords.clear();
      env.setRecords(null);
      keys.clear();
      log.info("finish batch validate records");
    }
    panSymbolSet.clear();

    for (CSVPrinter csvPrinter : csvPrinterList) {
      csvPrinter.flush();
      csvPrinter.close();
    }
    for (PrintWriter printWriter : printWriterList) {
      printWriter.flush();
      printWriter.close();
    }

    env.setSummaryCount(summaryCount);
    env.setDetailCount(detailCount);
    env.setSummaryAmount(summaryAmount);
    env.setScriptAmount(scriptAmount);
    KieSession kieSession = kieContainer.newKieSession();
    kieSession.setGlobal("env", env);
    controlRecords.forEach(l -> kieSession.insert(l));
    summaryRecords.forEach(l -> kieSession.insert(l));
    collateralSummaryRecords.forEach(l -> kieSession.insert(l));
    kieSession.fireAllRules();
    kieSession.dispose();

    if (declarationLineNo < 0) {
      dailyFileErrors.add(createErrorDailyFile("FS009", of()));
      hasMissingRecordType = true;
    } else if (declarationLineNo != totalRecords) {
      dailyFileErrors.add(createErrorDailyFile("FS008", of()));
    }

    // validate missing recordTypes
    if (!recordTypes.contains("10")) {
      dailyFileErrors.add(createErrorDailyFile("FS003", of()));
      hasMissingRecordType = true;
    }

    if (!recordTypes.contains("30")) {
      dailyFileErrors.add(createErrorDailyFile("FS004", of()));
      hasMissingRecordType = true;
    }

    if (!recordTypes.contains("20")) {
      dailyFileErrors.add(createErrorDailyFile("FS005", of()));
      hasMissingRecordType = true;
    }

    if (!recordTypes.contains("40")) {
      dailyFileErrors.add(createErrorDailyFile("FS006", of()));
      hasMissingRecordType = true;
    }

    // missing 50
    if (collateralScripCount > 0 && !recordTypes.contains("50")) {
      hasMissingRecordType = true;
      dailyFileErrors.add(createErrorDailyFile("FS007", of()));
    }

    if (!hasMissingRecordType && hasInvalidOrder) {
      dailyFileErrors.add(createErrorDailyFile("FS002", of()));
    }

    if (totalRecords == 0) {
    	rejectInvalidFile("IF001", of(), messageTemplates);
    }

    if (totalRecords < 5) {
    	dailyFileErrors.add(createErrorDailyFile("FS001", of()));
    }

    if (duplicatePan) {
    	dailyFileErrors.add(createErrorDailyFile("FS013", of()));
    }

    if (!ValidationUtils.isSame(summaryAmount, detailsAmount)) {
    	dailyFileErrors.add(createErrorDailyFile("IFD015", of("summaryAmount", summaryAmount, "detailsAmount", detailsAmount)));
    }

    dailyFileErrors.forEach(errorDto -> {
      String template = messageTemplates.getOrDefault(errorDto.getErrorCode(), errorDto.getErrorCode());
      errorDto.setErrorMessage(new StringSubstitutor(errorDto.getParameters(), "<%", ">").replace(template));
    });

    // generate response file
    String responseFileName = join("_", "TM", memberMaster.getMemCd(), fileName.replace(".T", ".R"));
    String responseFilePath = CommonUtils.getFilePath(resFileDirectory, UUID.randomUUID().toString() + ".csv");
    PrintWriter responsePrintWriter = new PrintWriter(responseFilePath);
    CSVPrinter responseCsvPrinter = new CSVPrinter(detailRecordResponsePrintWriter, CSVFormat.DEFAULT);
    printResponseRecords(controlRecords, responsePrintWriter);
    printResponseRecords(summaryRecords, responsePrintWriter);
    responseCsvPrinter.flush();
    responsePrintWriter.flush();
    printCachedRecordsAndDeleteCacheFile(detailRecordResponseFilePath, responsePrintWriter);
    printResponseRecords(collateralSummaryRecords, responsePrintWriter);
    responseCsvPrinter.flush();
    responsePrintWriter.flush();
    printCachedRecordsAndDeleteCacheFile(collateralScripsDetailRecordResponseFilePath, responsePrintWriter);
    if (declarationLine != null) {
      printResponseRecords(Arrays.asList(declarationLine), responsePrintWriter);
    }
    responseCsvPrinter.close();
    responsePrintWriter.close();
    fileResult.setResponseFileName(responseFileName);
    fileResult.setResponseFilePath(responseFilePath);

    boolean isFailed = (missedDates != null && !missedDates.isEmpty()) ||
      (dailyFileErrors != null && !dailyFileErrors.isEmpty()) ||
      (controlRecords != null && controlRecords.stream().filter(s -> !s.getErrorCodes().isEmpty()).count() > 0) ||
      (summaryRecords != null && summaryRecords.stream().filter(s -> !s.getErrorCodes().isEmpty()).count() > 0) ||
      (detailRecordsErrorCount > 0) ||
      (collateralSummaryRecords != null && collateralSummaryRecords.stream().filter(s -> !s.getErrorCodes().isEmpty()).count() > 0) ||
      (collateralScripsDetailRecordsErrorCount > 0);
    fileResult.setFailed(isFailed);
    if (isFailed) {
      // generate error code file
      String errorCodeFileName = join("_", "TM", reportDate.format(DateTimeFormatter.ofPattern("DDMMYYY")), (fileResult.getMaxBatchNo() + 1) + "ErrorMsg.csv");
      String errorCodeFilePath = CommonUtils.getFilePath(resFileDirectory, UUID.randomUUID().toString() + ".csv");
      PrintWriter errorCodePrintWriter = new PrintWriter(errorCodeFilePath);
      CSVPrinter errorCodeCsvPrinter = new CSVPrinter(errorCodePrintWriter, CSVFormat.DEFAULT);
      for (MTRDailyFileErrorDto error : dailyFileErrors) {
        errorCodeCsvPrinter.printRecord(error.getErrorCode(), error.getErrorMessage());
      }
      csvPrintErrorCodeRecords(controlRecords, errorCodeCsvPrinter, messageTemplates);
      csvPrintErrorCodeRecords(summaryRecords, errorCodeCsvPrinter, messageTemplates);
      errorCodeCsvPrinter.flush();
      errorCodePrintWriter.flush();
      printCachedRecordsAndDeleteCacheFile(detailRecordErrorCodeFilePath, errorCodePrintWriter);
      csvPrintErrorCodeRecords(collateralSummaryRecords, errorCodeCsvPrinter, messageTemplates);
      errorCodeCsvPrinter.flush();
      errorCodePrintWriter.flush();
      printCachedRecordsAndDeleteCacheFile(collateralScripsDetailRecordErrorCodeFilePath, errorCodePrintWriter);
      errorCodeCsvPrinter.close();
      errorCodePrintWriter.close();

      fileResult.setErrorCodeFileName(errorCodeFileName);
      fileResult.setErrorCodeFilePath(errorCodeFilePath);
    }
    List<MTRValidateRecordSummary> recordSummaryList = new ArrayList<>();
    MTRValidateRecordSummary controlRecordSummary = createRecordSummary(controlRecords, "10");

    // add file level count into control record
    int controlErrorCount = controlRecordSummary.getErrorCount() + fileResult.getDailyFileErrors().size();
    controlRecordSummary.setErrorCount(controlErrorCount);
    if (controlErrorCount > 0) {
      controlRecordSummary.setResult(Reject);
    }

    recordSummaryList.add(controlRecordSummary);
    recordSummaryList.add(createRecordSummary(summaryRecords, "30"));
    recordSummaryList.add(new MTRValidateRecordSummary(
      "20",
      detailRecordsErrorCount > 0 ? Reject : Success,
      detailRecordsErrorCount));
    recordSummaryList.add(createRecordSummary(collateralSummaryRecords, "40"));
    recordSummaryList.add(new MTRValidateRecordSummary(
      "50",
      collateralScripsDetailRecordsErrorCount > 0 ? Reject : Success,
      collateralScripsDetailRecordsErrorCount));
    fileResult.setRecordSummaryList(recordSummaryList);
    return fileResult;
  }

  /**
   * Add audit fields.
   *
   * @param entity the db entity
   * @param memberId the member id
   */
  private void addAudit(final AuditableEntity entity, final Long memberId) {
    entity.setCreatedBy(memberId.toString());
    entity.setCreatedDate(LocalDateTime.now());
  }

  /**
   * Fill base record fields.
   *
   * @param entity the record entity
   * @param memberId the member id
   * @param dailyFile the file
   */
  private void fillRecord(
      final BaseRecord entity, final Long memberId, final MTRDailyFile dailyFile) {
    addAudit(entity, memberId);
    entity.setSubmissionDate(LocalDate.now());
    entity.setMtrFile(dailyFile);
  }

  private void fillRecord(
    final MTRDetailRecord entity, final Long memberId, final MTRDailyFile dailyFile) {
    entity.setCreatedBy(memberId.toString());
    entity.setCreatedDate(LocalDateTime.now());
    entity.setSubmissionDate(LocalDate.now());
    entity.setMtrFile(dailyFile);
  }

  private void fillRecord(
    final MTRCollateralsScripsDetailRecord entity, final Long memberId, final MTRDailyFile dailyFile) {
    entity.setCreatedBy(memberId.toString());
    entity.setCreatedDate(LocalDateTime.now());
    entity.setSubmissionDate(LocalDate.now());
    entity.setMtrFile(dailyFile);
  }

  private void fillRecord(
    final MTRSummaryRecord entity, final Long memberId, final MTRDailyFile dailyFile) {
    entity.setCreatedBy(memberId.toString());
    entity.setCreatedDate(LocalDateTime.now());
    entity.setSubmissionDate(LocalDate.now());
    entity.setMtrFile(dailyFile);
  }

  /**
   * Create record summary.
   *
   * @param records the list of records
   * @param type the record type
   * @return MTRValidateRecordSummary
   */
  private MTRValidateRecordSummary createRecordSummary(
      final List<LineValidationResult> records, final String type) {
    int errors = (int) records.stream().filter(r -> !r.getErrorCodes().isEmpty()).count();
    if (errors > 0) {
      return new MTRValidateRecordSummary(type, Reject, errors);
    } else {
      return new MTRValidateRecordSummary(type, Success, 0);
    }
  }

  /**
   * Parse line to MTRControlRecord.
   *
   * @param fields List of String
   * @return MTRControlRecord
   */
  private MTRControlRecord parseControlRecord(final List<String> fields) {
    MTRControlRecord record = new MTRControlRecord();
    record.setFileType(fields.get(1));
    record.setMemberCode(fields.get(2));
    record.setBatchDate(LocalDate.parse(fields.get(3), DATE_FORMAT));
    record.setBatchNumber(parseInt(fields.get(4)));
    record.setTotalSummaryRecords(parseInt(fields.get(5)));
    record.setTotalDetailRecords(parseInt(fields.get(6)));
    record.setTotalAmountFunded(parseDouble(fields.get(7)));

    return record;
  }

  /**
   * Parse string to MTRSummaryRecord.
   *
   * @param fields List of String
   * @return the MTRSummaryRecord
   */
  private MTRSummaryRecord parseSummaryRecord(final List<String> fields) {
    MTRSummaryRecord record = new MTRSummaryRecord();
    record.setLenderName(fields.get(1));
    record.setLenderCategory(parseInt(fields.get(2)));
    record.setAmountFunded(parseDouble(fields.get(3)));
    return record;
  }

  public Map<String, MTRDetailRecordDtoInterface> fetchRecords(Optional<MTRDailyFile> dailyFile, List<Integer> keys) {
    Map<String, MTRDetailRecordDtoInterface> records = new HashMap<>();
    if (dailyFile.isPresent()) {
      int sqlQueryBatchSize = 1000;
      for (int i = 0; i < keys.size(); i += sqlQueryBatchSize) {
        log.info("start query previous day detailRecord for keys.size={}, index={}", keys.size(), i);
        int end = Math.min(keys.size(), i + sqlQueryBatchSize);
        List<Integer> sublist = keys.subList(i, end);
        detailRecordRepository.findByMtrFile_IdAndKeys(dailyFile.get().getId(), sublist).forEach(record -> {
          records.put(record.getMapKey(), record);
        });
      }
      log.info("finish query previous day detailRecord with result size={}", records.size());
    }
    return records;
  }


  public static int getPreviousDayEndQuantity(List<String> fields, Map<String, MTRDetailRecordDtoInterface> records) {
    if (fields.size() < 13) {
      return 0;
    }
    String clientName = fields.get(1);
    String pan = fields.get(2);
    String symbolCode = fields.get(3);
    String mapKey = MTRDetailRecord.createClientNamePanSymbol(clientName, pan, symbolCode);
    MTRDetailRecordDtoInterface record = records.get(mapKey);
    if (record != null) {
      return Integer.valueOf(record.getFundedQuantityEndDay());
    } else {
      return 0;
    }
  }

  public static double getPreviousDayEndAmount(List<String> fields, Map<String, MTRDetailRecordDtoInterface> records) {
    if (fields.size() < 13) {
      return 0.0;
    }
    String clientName = fields.get(1);
    String pan = fields.get(2);
    String symbolCode = fields.get(3);
    String mapKey = MTRDetailRecord.createClientNamePanSymbol(clientName, pan, symbolCode);
    MTRDetailRecordDtoInterface record = records.get(mapKey);
    if (record != null) {
      return Double.valueOf(record.getFundedAmountEndDay());
    } else {
      return 0.0;
    }
  }

  /**
   * Parse string to MTRDetailRecord.
   *
   * @param fields List of String
   * @return the MTRDetailRecord
   */
  private MTRDetailRecord parseDetailRecord(final List<String> fields) {
    MTRDetailRecord record = new MTRDetailRecord();
    record.setClientName(fields.get(1));
    record.setPan(fields.get(2));
    MTRSymbolName symbol = new MTRSymbolName();
    symbol.setSymbolCode(fields.get(3));
    record.setSymbol(symbol);
    record.setSeries(fields.get(4));
    record.setFundedQuantityBeginDay(parseInt(fields.get(5)));
    record.setFundedAmountBeginDay(parseDouble(fields.get(6)));
    record.setFundedQuantityDuringDay(parseInt(fields.get(7)));
    record.setFundedAmountDuringDay(parseDouble(fields.get(8)));
    record.setFundedQuantityLiquidatedDuringDay(parseInt(fields.get(9)));
    record.setFundedAmountLiquidatedDuringDay(parseDouble(fields.get(10)));
    record.setFundedQuantityEndDay(parseInt(fields.get(11)));
    record.setFundedAmountEndDay(parseDouble(fields.get(12)));
    record.setCategoryOfHolding("Y".equalsIgnoreCase(fields.get(13)));
    record.setStockExchange(fields.get(14));

    return record;
  }

  /**
   * Parse string to MTRCollateralSummaryRecord.
   *
   * @param fields List of String
   * @return the MTRCollateralSummaryRecord
   */
  private MTRCollateralsSummaryRecord parseCollateralRecord(final List<String> fields) {
    MTRCollateralsSummaryRecord record = new MTRCollateralsSummaryRecord();
    record.setCollateralCash(parseDouble(fields.get(1)));
    record.setCollateralCashEquivalent(parseDouble(fields.get(2)));
    record.setCollateralScrips(parseDouble(fields.get(3)));

    return record;
  }

  /**
   * Parse collateral script record.
   *
   * @param fields List of String
   * @return the MTRCollateralsScripsDetailRecord
   */
  private MTRCollateralsScripsDetailRecord parseCollateralScriptRecord(final List<String> fields) {
    MTRCollateralsScripsDetailRecord record = new MTRCollateralsScripsDetailRecord();
    record.setClientName(fields.get(1));
    record.setPan(fields.get(2));
    record.setSymbol(fields.get(3));
    record.setSeries(fields.get(4));
    record.setQuantityEndDay(parseInt(fields.get(5)));
    record.setAmountEndDay(parseDouble(fields.get(6)));
    record.setCategoryOfHolding("Y".equalsIgnoreCase(fields.get(7)));
    record.setStockExchange(fields.get(8));

    return record;
  }

  /**
   * Get batch no.
   *
   * @param fileName the file name
   * @return batchNo
   */
  private int getBatchNo(final String fileName) {
    try {
      return parseInt(fileName.substring(14));
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  /**
   * Create error daily file.
   *
   * @param errorCode the error code
   * @return the MTRErrorDailyFile
   */
  private MTRDailyFileErrorDto createErrorDailyFile(final String errorCode, final Map<String, Object> messageParams) {
	  MTRDailyFileErrorDto mtrErrorDailyFile = new MTRDailyFileErrorDto();
	  mtrErrorDailyFile.setErrorCode(errorCode);
	  mtrErrorDailyFile.setParameters(messageParams);
    return mtrErrorDailyFile;
  }
  
  private void rejectInvalidFile(String errorCode, final Map<String, Object> messageParams, Map<String, String> messageTemplates) {
	  MTRDailyFileErrorDto  dailyFileError = createErrorDailyFile(errorCode, messageParams);
	  
	  String errorMessage = MTRFileValidationResult.generateErrorMessage(dailyFileError, messageTemplates);
	  throw new BaseServiceException(errorCode + ": " + errorMessage, HttpStatus.BAD_REQUEST);
  }

  /**
   * Get report date.
   *
   * @param fileName the file name
   * @return the report date
   */
  private LocalDate getReportDate(final String fileName) {
    try {
      return LocalDate.parse(fileName.substring(4, 12), DATE_FORMAT);
    } catch (DateTimeParseException e) {
      return null;
    }
  }
}
