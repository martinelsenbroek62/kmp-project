package scripts;

import com.github.javafaker.Faker;
import com.nseindia.mc.model.ComplianceOfficer;
import com.nseindia.mc.model.Director;
import com.nseindia.mc.model.KMPAction;
import com.nseindia.mc.model.KeyManagementPersonnel;
import com.nseindia.mc.model.MTRControlRecord;
import com.nseindia.mc.model.MTRDailyFile;
import com.nseindia.mc.model.MTRDetailRecord;
import com.nseindia.mc.model.MTRMrgTradingReport;
import com.nseindia.mc.model.MTRSummaryRecord;
import com.nseindia.mc.model.MTRSymbolName;
import com.nseindia.mc.model.MemberMaster;
import com.nseindia.mc.model.Partner;
import com.nseindia.mc.model.Proprietor;
import com.nseindia.mc.model.Shareholder;
import com.nseindia.mc.model.UserMemCom;
import com.nseindia.mc.repository.ComplianceOfficerRepository;
import com.nseindia.mc.repository.DirectorRepository;
import com.nseindia.mc.repository.KeyManagementPersonnelRepository;
import com.nseindia.mc.repository.MTRCollateralScripsDetailRecordRepository;
import com.nseindia.mc.repository.MTRCollateralSummaryRecordRepository;
import com.nseindia.mc.repository.MTRControlRecordRepository;
import com.nseindia.mc.repository.MTRDailyFileRepository;
import com.nseindia.mc.repository.MTRDetailRecordRepository;
import com.nseindia.mc.repository.MTRMrgTradingReportRepository;
import com.nseindia.mc.repository.PenaltyMemberRepository;
import com.nseindia.mc.repository.PenaltyRepository;
import com.nseindia.mc.repository.PenaltyReviewRepository;
import com.nseindia.mc.repository.MTRSummaryRecordRepository;
import com.nseindia.mc.repository.MTRSymbolNameRepository;
import com.nseindia.mc.repository.MemberMasterRepository;
import com.nseindia.mc.repository.PartnerRepository;
import com.nseindia.mc.repository.ProprietorRepository;
import com.nseindia.mc.repository.ShareholderRepository;
import com.nseindia.mc.repository.UserMemComRepository;
import com.nseindia.mc.service.mtrReport.MtrFileValidateService;
import com.nseindia.mc.service.mtrReport.MtrReportService;
import com.nseindia.mc.service.penalty.PenaltyService;
import com.nseindia.mc.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.io.File;
import java.io.FileInputStream;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.nseindia.mc.util.CommonUtils.getLastBusinessDay;
import static com.nseindia.mc.util.CommonUtils.getReportFileDateStr;
import static java.lang.String.join;

/**
 * This data generator generates MTRDaily file records for all members in the database for the last 6 months
 */
@Slf4j
@SpringBootApplication(scanBasePackages = {"com.nseindia.mc"})
public class DataGenerator implements CommandLineRunner {

  private static final String sampleSubmissionFileDirectory = "docker-db/sample-submission-files";

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");

  private static final String dummyPanDocFileName = "pan_doc_dummy.pdf";

  private static final String notValidatedStatus = "Not Validated";

  private static final String validatedStatus = "Validated";

  /**
   * Represents the number of months for which to generate the test data
   * For example if it is set to 6,
   * Then the mtr daily file test data will be generated for all members in the TBL_MEMBER_MASTER for the last 6 months
   */
  private static final long MTR_DAILY_FILE_TEST_DATA_MONTHS = 6;

  /**
   * The batch number to set when creating MTR daily files
   */
  private static final int DEFAULT_MTR_DAILY_FILES_BATCH_NUMBER = 1;

  /**
   * The name of the user who manages (creates and updates the mtr daily file records)
   */
  private static final String MTR_DAILY_FILE_MANAGER = "ADMIN";

  /**
   * The Faker instance to use to generate randon file names
   */
  private static final Faker faker = new Faker();

  /**
   * The repository for MTRDailyFile management
   */
  @Autowired
  MTRDailyFileRepository mtrDailyFileRepository;

  /**
   * The repository for MTRControlRecord management
   */
  @Autowired
  MTRControlRecordRepository mtrControlRecordRepository;

  /**
   * The repository for MTRSummaryRecord management
   */
  @Autowired
  MTRSummaryRecordRepository mtrSummaryRecordRepository;
  
  @Autowired
  MTRCollateralSummaryRecordRepository mtrCollateralSummaryRecordRepository;
  
  
  @Autowired
  MTRCollateralScripsDetailRecordRepository mtrCollateralScripsDetailRecordRepository;
  
  @Autowired
  MTRSymbolNameRepository mtrSymbolNameRepository;

  /**
   * The repository for MTRDetailRecord management
   */
  @Autowired
  MTRDetailRecordRepository mtrDetailRecordRepository;

  /**
   * The repository for MemberMaster management
   */
  @Autowired
  MemberMasterRepository memberMasterRepository;

  @Autowired
  MtrReportService mtrReportService;

  @Autowired
  MtrFileValidateService mtrFileValidateService;

  @Autowired
  MTRMrgTradingReportRepository mtrMrgTradingReportRepository;

  @Value("${dms.response.path}")
  private String resFileDirectory;

  @Autowired
  PenaltyService penaltyService;

  @Autowired
  UserMemComRepository userMemComRepository;

  @Autowired private PenaltyRepository penaltyRepository;
  @Autowired private PenaltyMemberRepository penaltyMemberRepository;
  @Autowired private PenaltyReviewRepository penaltyReviewRepository;

  /**
   * The number of users to generate for each role: Director, shareholder, partner, proprietor and compliance officer
   */
  private static final int NUMBER_OF_USERS_WITH_ROLES = 100;

  /**
   * The number of KMPs to generate in the database, it should be less than or equal to the number of directors
   */
  private static final int NUMBER_OF_KMPS = 50;

  /**
   * The Admin constant
   */
  private static final String ADMIN = "ADMIN";

  /**
   * The director repository to use for director managament
   */
  @Autowired
  DirectorRepository directorRepository;

  /**
   * The KMP repository to use for creating KMP records
   */
  @Autowired
  KeyManagementPersonnelRepository kmpRepository;

  /**
   * Share holder repository
   */
  @Autowired
  ShareholderRepository shareholderRepository;

  /**
   * Partner repository
   */
  @Autowired
  PartnerRepository partnerRepository;

  /**
   * Proprietor repository
   */
  @Autowired
  ProprietorRepository proprietorRepository;

  /**
   * Compliance officer repository
   */
  @Autowired
  ComplianceOfficerRepository complianceOfficerRepository;

  /**
   * The main entry point of the data generator.
   * It generates test data for MTR Daily file records in the last X months (X = MTR_DAILY_FILE_TEST_DATA_MONTHS)
   */
  @Override
  public void run(final String... args) throws Exception {
    boolean insertMTRFile = false;
    
    LocalDate endDate = LocalDate.now().minusDays(10);
    if (args.length > 0) {
      for (String arg : args) {
        if ("insertMTRFile".equals(arg)) {
          insertMTRFile = true;
          endDate = getMTRFileDate();
          break;
        }
      }
    }

    log.info("end date of test data generation is {}", endDate);
    List<MemberMaster> members = memberMasterRepository.findAll();
    defaultDataGenerator(endDate, members);
    kmpDataGenerator(members);

    if (insertMTRFile) {
      insertMTRFile();
    }
  }


  private LocalDate getMTRFileDate() throws Exception {
    File folder = new File(System.getProperty("user.dir") + File.separator + sampleSubmissionFileDirectory);
    Set<String> memCodes = new HashSet<>();
    for (int fi = 0; fi < folder.listFiles().length; fi++) {
      final File fileEntry = folder.listFiles()[fi];
      String originalFileName = fileEntry.getName();
      String[] fields = originalFileName.split("_");
      if (fields.length != 3) continue;
      String memCode = fields[0];
      memCodes.add(memCode);

      String dateStr = fields[2].split("\\.")[0];
      LocalDate reportingDate = CommonUtils.getLastBusinessDay(CommonUtils.getLastBusinessDay(LocalDate.parse(dateStr, DATE_FORMAT)));
      return reportingDate;
    }
    return LocalDate.now().minusDays(10);
  }

  private void insertMTRFile() throws Exception{
    log.info("processing insertMTRFile");
    File folder = new File(System.getProperty("user.dir") + File.separator + sampleSubmissionFileDirectory);
    Set<String> memCodes = new HashSet<>();
    Map<String, MTRSymbolName> symbolMap = mtrSymbolNameRepository.findAll().stream()
      .collect(Collectors.toMap(s -> s.getSymbolCode(), s -> s));

    for (int fi = 0; fi < folder.listFiles().length; fi++) {
      final File fileEntry = folder.listFiles()[fi];
      String originalFileName = fileEntry.getName();
      log.info(String.format("processing %s [%5d / %5d]", originalFileName, fi, folder.listFiles().length));
      String[] fields = originalFileName.split("_");
      if (fields.length != 3) continue;
      String memCode = fields[0];
      memCodes.add(memCode);

      String dateStr = fields[2].split("\\.")[0];
      LocalDate reportingDate = CommonUtils.getLastBusinessDay(LocalDate.parse(dateStr, DATE_FORMAT));
      LocalDate submissionDate = reportingDate;
      LocalDateTime submissionTime = submissionDate.atStartOfDay();
      MTRDailyFile mtrDailyFile = new MTRDailyFile();

      MemberMaster targetMember = memberMasterRepository.findFirstByMemCdOrMemName(memCode, null).get();

      mtrDailyFile.setMember(targetMember);
      mtrDailyFile.setReportingDate(reportingDate.atStartOfDay());
      mtrDailyFile.setBatchNo(DEFAULT_MTR_DAILY_FILES_BATCH_NUMBER);

      String fileName = String.format("MTR_%s.T%d", getReportFileDateStr(reportingDate), DEFAULT_MTR_DAILY_FILES_BATCH_NUMBER);
      String responseFileName = join("_", "TM", targetMember.getMemCd(), fileName.replace(".T", ".R"));
      // TODO: generate actual files for above two.
      mtrDailyFile.setDailyFileSubmissionDate(submissionTime);
      mtrDailyFile.setDailyFileName(fileName);
      mtrDailyFile.setDmsDocIndex(fileName);
      mtrDailyFile.setResponseFileName(responseFileName);
      mtrDailyFile.setDmsResIndex(responseFileName);
      mtrDailyFile.setNilSubmissionStatus(false);
      mtrDailyFile.setDailyFileStatus(true);
      mtrDailyFile.setCreatedBy(MTR_DAILY_FILE_MANAGER);
      mtrDailyFile.setCreatedDate(submissionTime);
      mtrDailyFile.setUpdatedBy(MTR_DAILY_FILE_MANAGER);
      mtrDailyFile.setUpdatedDate(submissionTime);
      mtrDailyFileRepository.save(mtrDailyFile);

      List<MTRDetailRecord> detailRecords = new ArrayList<>();
      List<MTRSummaryRecord> summaryRecords = new ArrayList<>();
      FileInputStream fileInputStream = new FileInputStream(fileEntry);

      MTRControlRecord controlRecord = new MTRControlRecord();
      controlRecord.setMtrFile(mtrDailyFile);
      double totalAmountFunded = 0.0;
      int totalDetailRecords = 0;
      int totalSummaryRecords = 0;
      List<String> lines = IOUtils.readLines(fileInputStream, "utf8");
      log.info("lines.size={}", lines.size());
      for (int i = 0; i < lines.size(); i++) {
        String l = lines.get(i);
        System.out.print(String.format("Processing lines [%5d / %5d]\r", i, lines.size()));
        String[] f = l.split(",");
        String type = StringUtils.trim(f[0]);
        if (type.equals("20") && f.length >= 15 && "EQ".equals(StringUtils.trim(f[4]))) {
          int fundedQuantity = Integer.parseInt(f[5]);
          double fundedAmount = Double.parseDouble(f[6]);
          String symbol = StringUtils.trim(f[3]);
          String pan = StringUtils.trim(f[2]);
          String clientName = StringUtils.trim(f[1]);

          totalAmountFunded += fundedAmount;
          totalDetailRecords += 1;

          MTRDetailRecord detailRecord = new MTRDetailRecord();
          detailRecord.setMtrFile(mtrDailyFile);
          detailRecord.setFundedAmountBeginDay(0.0);
          detailRecord.setFundedQuantityBeginDay(0);

          detailRecord.setFundedAmountDuringDay(fundedAmount);
          detailRecord.setFundedQuantityDuringDay(fundedQuantity);
          detailRecord.setFundedAmountLiquidatedDuringDay(fundedAmount);
          detailRecord.setFundedQuantityLiquidatedDuringDay(fundedQuantity);

          detailRecord.setFundedAmountEndDay(fundedAmount);
          detailRecord.setFundedQuantityEndDay(fundedQuantity);
          detailRecord.setSymbol(symbolMap.get(symbol));

          detailRecord.setPan(pan);
          detailRecord.setClientName(clientName);

          detailRecords.add(detailRecord);
        } else if (type.equals("30") && f.length >= 4) {
          double amountFunded = Double.parseDouble(f[3]);
          int lenderCategory = Integer.parseInt(f[2]);
          String lenderName = StringUtils.trim(f[1]);
          totalSummaryRecords += 1;

          MTRSummaryRecord summaryRecord = new MTRSummaryRecord();
          summaryRecord.setMtrFile(mtrDailyFile);
          summaryRecord.setAmountFunded(amountFunded);
          summaryRecord.setLenderCategory(lenderCategory);
          summaryRecord.setLenderName(lenderName);

          summaryRecords.add(summaryRecord);
        }
      }
      controlRecord.setTotalAmountFunded(totalAmountFunded);
      controlRecord.setTotalDetailRecords(totalDetailRecords);
      controlRecord.setTotalSummaryRecords(totalSummaryRecords);

      log.info("mtrControlRecordRepository.save(controlRecord)");
      mtrControlRecordRepository.save(controlRecord);
      mtrControlRecordRepository.flush();
      log.info("mtrDetailRecordRepository.saveAll(detailRecords) for size={}", detailRecords.size());
//      int batchSize = 1000;
//      for (int i = 0; i < detailRecords.size(); i += batchSize) {
//        System.out.print(String.format("Batch saving detailRecords [%5d / %5d]\r", i, detailRecords.size()));
//        mtrDetailRecordRepository.saveAll(detailRecords.subList(i, Math.min(i + batchSize, detailRecords.size())));
//        mtrDetailRecordRepository.flush();
//      }
      mtrFileValidateService.batchInsertDetailRecords(detailRecords);
      log.info("mtrSummaryRecordRepository.saveAll(summaryRecords) for size={}", summaryRecords.size());
      mtrSummaryRecordRepository.saveAll(summaryRecords);
      mtrSummaryRecordRepository.flush();
    }
  }

  private void defaultDataGenerator(LocalDate endDate, List<MemberMaster> members) throws Exception {
    log.info("processing defaultDataGenerator");
    // Delete the existing test data
    deleteAll();

    List<MTRSymbolName> symbols = mtrSymbolNameRepository.findAll();

    // Compute the date from which to start generating the MTR daily files
    LocalDate date = endDate.minusMonths(MTR_DAILY_FILE_TEST_DATA_MONTHS).withDayOfMonth(1);


    LocalDate lastMonthOfEndDate = endDate.minusMonths(1);
    List<LocalDate> businessDatesLastMonth = CommonUtils.getBusinessDaysForYearMonth(
      lastMonthOfEndDate.getYear(), lastMonthOfEndDate.getMonthValue());

    MemberMaster targetMember = members.get(4);
    LocalDate targetDate = businessDatesLastMonth.get(businessDatesLastMonth.size() - 7);
    List<Pair<List<MemberMaster>, List<LocalDate>>> notUploadedSubmissionLastMonth = Arrays.asList(
      new ImmutablePair<>( // missing 2 days
        members.subList(0, 3),
        new ArrayList(businessDatesLastMonth.subList(
          businessDatesLastMonth.size() - 2,
          businessDatesLastMonth.size()
        ))
      ),
      new ImmutablePair<>( // missing 1 days
        members.subList(3, 4),
        new ArrayList(businessDatesLastMonth.subList(
          businessDatesLastMonth.size() - 1,
          businessDatesLastMonth.size()
        ))
      ),
      new ImmutablePair<>( // missing 7 days
        Arrays.asList(targetMember),
        new ArrayList(businessDatesLastMonth.subList(
          businessDatesLastMonth.size() - 7,
          businessDatesLastMonth.size()
        ))
      )
    );

    for (int i = 2; i <= MTR_DAILY_FILE_TEST_DATA_MONTHS; i++) {
      LocalDate lastIMonthOfEndDate = endDate.minusMonths(i);
      List<LocalDate> businessDatesLastIMonth = CommonUtils.getBusinessDaysForYearMonth(
        lastIMonthOfEndDate.getYear(), lastIMonthOfEndDate.getMonthValue());
      for (int j = 0; j < 2; j++) {
        // only adding penalty record for the first two members,
        // and the third member is planed to be withdrawn in the last month dur to more than 5 records missing within one month
        notUploadedSubmissionLastMonth.get(j).getRight().addAll(
          businessDatesLastIMonth.subList(
            businessDatesLastIMonth.size() - (j + 1),
            businessDatesLastIMonth.size()
          )
        );
      }
    }

    log.info("notUploadedSubmissionLastMonth={}", notUploadedSubmissionLastMonth);

    Random random = new Random();

    File filePath = new File(System.getProperty("user.dir") + File.separator + resFileDirectory);
    if (!filePath.isDirectory()) filePath.mkdirs();

    List<UserMemCom> userMemComs = userMemComRepository.findAll();
    // iterate over the last X months day by day to generate the test data for members
    while (date.isBefore(endDate)) {
      // Check if the date is a business day
      if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) { // skip weekends
        LocalDate submissionDate = date.plusDays(1);
        LocalDateTime submissionTime = submissionDate.atStartOfDay();
        List<MTRDailyFile> dailyFiles = new ArrayList<>();
        List<MTRControlRecord> controlRecords = new ArrayList<>();
        List<MTRDetailRecord> detailRecords = new ArrayList<>();
        List<MTRSummaryRecord> summaryRecords = new ArrayList<>();

        // iterate over the members and create a daily file record for each member
        for (MemberMaster member : members) {

          boolean notUploaded = false;

          for (Pair<List<MemberMaster>, List<LocalDate>> pair : notUploadedSubmissionLastMonth) {
            if (pair.getLeft().contains(member) && pair.getRight().contains(date)) {
              notUploaded = true;
              break;
            }
          }

          if(notUploaded) {
            continue;
          }

          MTRDailyFile mtrDailyFile = new MTRDailyFile();

          boolean generateDataFile = random.nextBoolean();

          mtrDailyFile.setMember(member);
          mtrDailyFile.setReportingDate(date.atStartOfDay());
          mtrDailyFile.setBatchNo(DEFAULT_MTR_DAILY_FILES_BATCH_NUMBER);

          if (generateDataFile) {
            String fileName = String.format("MTR_%s.T%d", getReportFileDateStr(date), DEFAULT_MTR_DAILY_FILES_BATCH_NUMBER);
            String responseFileName = join("_", "TM", member.getMemCd(), fileName.replace(".T", ".R"));
            // TODO: generate actual files for above two.
            mtrDailyFile.setDailyFileSubmissionDate(submissionDate.atStartOfDay());
            mtrDailyFile.setDailyFileName(fileName);
            mtrDailyFile.setDmsDocIndex(fileName);
            mtrDailyFile.setResponseFileName(responseFileName);
            mtrDailyFile.setDmsResIndex(responseFileName);
            mtrDailyFile.setNilSubmissionStatus(false);

            MTRControlRecord controlRecord = new MTRControlRecord();

            controlRecord.setMtrFile(mtrDailyFile);
            controlRecord.setTotalAmountFunded(getRandomDouble(random, 50.0, 200.0));
            controlRecords.add(controlRecord);

            MTRDetailRecord detailRecord = new MTRDetailRecord();
            detailRecord.setMtrFile(mtrDailyFile);
            detailRecord.setFundedAmountBeginDay(getRandomDouble(random, 50.0, 200.0));
            detailRecord.setFundedQuantityBeginDay(100);

            detailRecord.setFundedAmountDuringDay(getRandomDouble(random, 50.0, 200.0));
            detailRecord.setFundedQuantityDuringDay(50);
            detailRecord.setFundedAmountLiquidatedDuringDay(getRandomDouble(random, 50.0, 200.0));
            detailRecord.setFundedQuantityLiquidatedDuringDay(50);

            detailRecord.setFundedAmountEndDay(getRandomDouble(random, 50.0, 200.0));
            detailRecord.setFundedQuantityEndDay(100);
            detailRecord.setSymbol(symbols.get(random.nextInt(symbols.size())));

            int rand = RandomUtils.nextInt(1, 5);

            detailRecord.setPan("PAN" + rand);
            detailRecord.setClientName("Client" + rand);

            detailRecords.add(detailRecord);


            // summary
            MTRSummaryRecord summaryRecord = new MTRSummaryRecord();
            summaryRecord.setMtrFile(mtrDailyFile);
            summaryRecord.setAmountFunded(getRandomDouble(random, 50.0, 200.0));
            summaryRecord.setLenderCategory(rand);
            summaryRecord.setLenderName("NSE" + rand);

            summaryRecords.add(summaryRecord);


          } else {
            String resFileName = mtrReportService.generateNilSubmission(filePath, member.getMemCd(), date, true);
            String submissionFileName = mtrReportService.generateNilSubmission(filePath, member.getMemCd(), date, false);
            mtrDailyFile.setNilSubmissionDate(date.atStartOfDay());
            mtrDailyFile.setNilSubmissionStatus(true);
            mtrDailyFile.setResponseFileName(mtrReportService.getFileName(member.getMemCd(), date, DEFAULT_MTR_DAILY_FILES_BATCH_NUMBER, true));
            mtrDailyFile.setDmsResIndex(resFileName);
            mtrDailyFile.setDailyFileName(mtrReportService.getFileName(null, date, DEFAULT_MTR_DAILY_FILES_BATCH_NUMBER, false));
            mtrDailyFile.setDmsDocIndex(submissionFileName);
          }
          mtrDailyFile.setDailyFileStatus(true);
          mtrDailyFile.setCreatedBy(MTR_DAILY_FILE_MANAGER);
          mtrDailyFile.setCreatedDate(submissionTime);
          mtrDailyFile.setUpdatedBy(MTR_DAILY_FILE_MANAGER);
          mtrDailyFile.setUpdatedDate(submissionDate.atStartOfDay());

          dailyFiles.add(mtrDailyFile);
          log.info("generating data for member[{}], date[{}]", mtrDailyFile.getMember().getMemId(), mtrDailyFile.getReportingDate());
        }
        // Save The daily files for the current day
        mtrDailyFileRepository.saveAll(dailyFiles);

        mtrControlRecordRepository.saveAll(controlRecords);
        mtrDetailRecordRepository.saveAll(detailRecords);
        mtrSummaryRecordRepository.saveAll(summaryRecords);

        MTRMrgTradingReport report = new MTRMrgTradingReport();
        report.setCreatedDate(LocalDateTime.now());
        report.setReportingDate(date);
        report.setMtrCounterDate(LocalDate.now());
        report.setMtrCounterTotal(members.size());
        report.setMtrCounterLatest(members.size() - dailyFiles.size());
        report.setMrgTradingFinalStatus(report.getMtrCounterLatest() == 0);
        String outputFile =
          mtrReportService.writeToMarginTradingReportFile(date, detailRecords, report.getMrgTradingFinalStatus(), null);
        if (report.getMrgTradingFinalStatus()) {
          report.setFinalReportDmsDocIndex(outputFile);
        } else {
          report.setProvisionalReportDmsDocIndex(outputFile);
          report.setMrgTradingProvisionalStatus(true);
        }
        report.setNotifyDailySignoffStatus(true);

        mtrMrgTradingReportRepository.save(report);
      }

      if (CommonUtils.getLastBusinessDayOfMonth(date).equals(date)) {
        UserMemCom randomMaker = userMemComs.get(random.nextInt(userMemComs.size()));
        mtrReportService.assignHoMaker(randomMaker.getId());
        penaltyService.computePenaltyMTR(date.getYear(), date.getMonthValue());
      }

      // Increment the date by one day to process the next day
      date = date.plusDays(1);
    }

    LocalDate lastBusinessDayOfEndDate = getLastBusinessDay(endDate);
    mtrReportService.processDailyNonSubmissionRecords(lastBusinessDayOfEndDate);

    // generate final margin trade reporting file
    LocalDate submissionDate = targetDate.plusDays(1);
    addDailyFile(targetMember, targetDate, submissionDate, symbols.get(random.nextInt(symbols.size())));
    mtrReportService.generateMarginTradingReport(submissionDate, submissionDate);
  }

  private void addDailyFile(MemberMaster targetMember, LocalDate reportingDate, LocalDate submissionDate, MTRSymbolName symbol) {
    log.info("addDailyFile for member[{}] date[{}]", targetMember.getMemId(), reportingDate);
    Random random = new Random();

    LocalDateTime submissionTime = submissionDate.atStartOfDay();
    MTRDailyFile mtrDailyFile = mtrDailyFileRepository.findByMemberIdAndReportingDateIncludingAutoGeneratedFile(
      targetMember.getMemId(),
      CommonUtils.getDatabaseDateStr(reportingDate)).orElse(new MTRDailyFile());

    mtrDailyFile.setMember(targetMember);
    mtrDailyFile.setReportingDate(reportingDate.atStartOfDay());
    mtrDailyFile.setBatchNo(DEFAULT_MTR_DAILY_FILES_BATCH_NUMBER);

    String fileName = String.format("MTR_%s.T%d", getReportFileDateStr(reportingDate), DEFAULT_MTR_DAILY_FILES_BATCH_NUMBER);
    String responseFileName = join("_", "TM", targetMember.getMemCd(), fileName.replace(".T", ".R"));
    // TODO: generate actual files for above two.
    mtrDailyFile.setDailyFileSubmissionDate(submissionTime);
    mtrDailyFile.setDailyFileName(fileName);
    mtrDailyFile.setDmsDocIndex(fileName);
    mtrDailyFile.setResponseFileName(responseFileName);
    mtrDailyFile.setDmsResIndex(responseFileName);
    mtrDailyFile.setNilSubmissionStatus(false);
    mtrDailyFile.setDailyFileStatus(true);
    mtrDailyFile.setCreatedBy(MTR_DAILY_FILE_MANAGER);
    mtrDailyFile.setCreatedDate(submissionTime);
    mtrDailyFile.setUpdatedBy(MTR_DAILY_FILE_MANAGER);
    mtrDailyFile.setUpdatedDate(submissionTime);
    mtrDailyFileRepository.save(mtrDailyFile);

    MTRControlRecord controlRecord = new MTRControlRecord();

    controlRecord.setMtrFile(mtrDailyFile);
    controlRecord.setTotalAmountFunded(getRandomDouble(random, 50.0, 200.0));
    mtrControlRecordRepository.save(controlRecord);

    MTRDetailRecord detailRecord = new MTRDetailRecord();
    detailRecord.setMtrFile(mtrDailyFile);
    detailRecord.setFundedAmountBeginDay(getRandomDouble(random, 50.0, 200.0));
    detailRecord.setFundedQuantityBeginDay(100);

    detailRecord.setFundedAmountDuringDay(getRandomDouble(random, 50.0, 200.0));
    detailRecord.setFundedQuantityDuringDay(50);
    detailRecord.setFundedAmountLiquidatedDuringDay(getRandomDouble(random, 50.0, 200.0));
    detailRecord.setFundedQuantityLiquidatedDuringDay(50);

    detailRecord.setFundedAmountEndDay(getRandomDouble(random, 50.0, 200.0));
    detailRecord.setFundedQuantityEndDay(100);
    detailRecord.setSymbol(symbol);

    int rand = RandomUtils.nextInt(1, 5);

    detailRecord.setPan("PAN" + rand);
    detailRecord.setClientName("Client" + rand);

    mtrDetailRecordRepository.save(detailRecord);

    // summary
    MTRSummaryRecord summaryRecord = new MTRSummaryRecord();
    summaryRecord.setMtrFile(mtrDailyFile);
    summaryRecord.setAmountFunded(getRandomDouble(random, 50.0, 200.0));
    summaryRecord.setLenderCategory(rand);
    summaryRecord.setLenderName("NSE" + rand);

    mtrSummaryRecordRepository.save(summaryRecord);
  }

  private double getRandomDouble(Random random, double start, double end){
    double result = start + (random.nextDouble() * (end - start));
    return Double.parseDouble(String.format("%.6f", result));
  }

  /**
   * This private method cleans up the existing test data before generating new test data.
   * It removes the data from the following tables:
   * 1. tbl_mtr_control_record
   * 2. TBL_MTR_SUMMARY_RECORD
   * 3. tbl_mtr_detail_record
   * 4. TBL_MTR_DAILY_FILE
   */
  private void deleteAll() {
    log.info("delete tables");
    mtrControlRecordRepository.deleteAllInBatch();
    mtrSummaryRecordRepository.deleteAllInBatch();
    mtrDetailRecordRepository.deleteAllInBatch();
    mtrCollateralSummaryRecordRepository.deleteAllInBatch();
    mtrCollateralScripsDetailRecordRepository.deleteAllInBatch();

    mtrDailyFileRepository.deleteAllInBatch();
    mtrMrgTradingReportRepository.deleteAllInBatch();

    penaltyReviewRepository.deleteAllInBatch();
    penaltyMemberRepository.deleteAllInBatch();
    penaltyRepository.deleteAllInBatch();
  }

  /**
   * Generates a random string to use as a file name
   * @return The random file name
   */
  private static String randomFilename() {
    String name = faker.name().name();

    return name.substring(0, Math.min(name.length(), 10)).replaceAll("\\s+", "");
  }

   /**
    * The main entry point of the data generator command line
    * @param args
    */
  public static void main(String[] args) {
    log.info("STARTING THE DATA GENERATOR");

    SpringApplication application = new SpringApplication(DataGenerator.class);
    application.setWebApplicationType(WebApplicationType.NONE);
    application.run(args);

    log.info("APPLICATION FINISHED");
    System.exit(0);
  }

  @Bean(name = "mvcHandlerMappingIntrospector")
  public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
    return new HandlerMappingIntrospector();
  }


  private void kmpDataGenerator(List<MemberMaster> members) throws Exception {
    log.info("processing kmpDataGenerator");
    // Clean the database data
    cleanUp();

    List<Director> directors = new ArrayList<>();
    List<Shareholder> sharholders = new ArrayList<>();
    List<Partner> partners = new ArrayList<>();
    List<Proprietor> proprietors = new ArrayList<>();
    List<ComplianceOfficer> complianceOfficers = new ArrayList<>();

    List<KeyManagementPersonnel> kmps = new ArrayList<>();

    for(int i = 0 ; i < members.size(); ++i) {
      MemberMaster memberMaster = members.get(i);
      Director director = generateDirectorData(i, memberMaster);
      Shareholder shareholder = generateShareholderData(i, memberMaster);
      Partner partner = generatePartnerData(i, memberMaster);
      Proprietor proprietor = generateProprietorData(i, memberMaster);
      ComplianceOfficer complianceOfficer = generateComplianceOfficerData(i, memberMaster);

      if(i < NUMBER_OF_KMPS) {
        switch (i%5) {
          case 0 : {
            kmps.add(createKmpWithDirectorRole(director, i));
            break;
          }
          case 1 : {
            kmps.add(createKmpWithShareholderRole(shareholder, i));
            break;
          }
          case 2 : {
            kmps.add(createKmpWithPartnerRole(partner, i));
            break;
          }
          case 3 : {
            kmps.add(createKmpWithProprietorRole(proprietor, i));
            break;
          }
          case 4 : {
            kmps.add( createKmpWithComplianceOfficerRole(complianceOfficer,i));
            break;
          }
        }
      }

      directors.add(director);
      sharholders.add(shareholder);
      partners.add(partner);
      proprietors.add(proprietor);
      complianceOfficers.add(complianceOfficer);
    }

    directorRepository.saveAll(directors);
    shareholderRepository.saveAll(sharholders);
    partnerRepository.saveAll(partners);
    proprietorRepository.saveAll(proprietors);
    complianceOfficerRepository.saveAll(complianceOfficers);
    kmpRepository.saveAll(kmps);
  }

  /**
   * Creates a KMP instance with Director role
   */
  private KeyManagementPersonnel createKmpWithDirectorRole (Director director, int i) {
    KeyManagementPersonnel kmp = new KeyManagementPersonnel();
    kmp.setMemId(director.getMember().getMemId());
    kmp.setAppId(RandomUtils.nextLong(1, 40000));
    kmp.setRole("Director");
    kmp.setAdditionalDesignation(faker.name().title());
    kmp.setSalutation(faker.name().prefix());
    kmp.setName(director.getName());
    kmp.setPan(director.getPan());
    kmp.setPanStatus(director.getPanStatus());
    kmp.setDin(director.getDin());
    kmp.setEmail(director.getEmail());
    kmp.setMobileNumber(director.getMobileNumber());
    kmp.setPhoneNumber(director.getPhoneNumber());
    kmp.setDeclarationDate(randomPastDate().atStartOfDay());
    kmp.setSubmissionDate(kmp.getDeclarationDate());
    kmp.setAction(KMPAction.ADDITION);
    kmp.setActionDate(kmp.getDeclarationDate());
    kmp.setFromNse(i%3 == 0);
    kmp.setCreatedBy(ADMIN);
    kmp.setCreatedDate(kmp.getDeclarationDate());
    kmp.setUpdatedDate(kmp.getDeclarationDate());
    if(kmp.getPanStatus().equals(notValidatedStatus)) {
      kmp.setPanDocFileName(dummyPanDocFileName);
      kmp.setPanDocDmsIndex(String.format("%d", i));
    }

    return kmp;
  }


  /**
   * Creates a KMP instance with Partner role
   */
  private KeyManagementPersonnel createKmpWithPartnerRole (Partner entity, int i) {
    KeyManagementPersonnel kmp = new KeyManagementPersonnel();
    kmp.setMemId(entity.getMemberId());
    kmp.setAppId(RandomUtils.nextLong(1, 40000));
    kmp.setRole("Partner");
    kmp.setAdditionalDesignation(faker.name().title());
    kmp.setSalutation(faker.name().prefix());
    kmp.setName(entity.getName());
    kmp.setPan(entity.getPan());
    kmp.setPanStatus(entity.getPanStatus());
    kmp.setDin(entity.getDin());
    kmp.setEmail(entity.getEmail());
    kmp.setMobileNumber(entity.getMobileNumber());
    kmp.setPhoneNumber(entity.getPhoneNumber());
    kmp.setDeclarationDate(randomPastDate().atStartOfDay());
    kmp.setSubmissionDate(kmp.getDeclarationDate());
    kmp.setAction(KMPAction.ADDITION);
    kmp.setActionDate(kmp.getDeclarationDate());
    kmp.setFromNse(i%3 == 0);
    kmp.setCreatedBy(ADMIN);
    kmp.setCreatedDate(kmp.getDeclarationDate());
    kmp.setUpdatedDate(kmp.getDeclarationDate());
    if(kmp.getPanStatus().equals(notValidatedStatus)) {
      kmp.setPanDocFileName(dummyPanDocFileName);
      kmp.setPanDocDmsIndex(String.format("%d", i));
    }

    return kmp;
  }

  /**
   * Creates a KMP instance with Sharholder role
   */
  private KeyManagementPersonnel createKmpWithShareholderRole (Shareholder entity, int i) {
    KeyManagementPersonnel kmp = new KeyManagementPersonnel();
    kmp.setMemId(entity.getMemberId());
    kmp.setAppId(RandomUtils.nextLong(1, 40000));
    kmp.setRole("Shareholder");
    kmp.setAdditionalDesignation(faker.name().title());
    kmp.setSalutation(faker.name().prefix());
    kmp.setName(entity.getName());
    kmp.setPan(entity.getPan());
    kmp.setPanStatus(entity.getPanStatus());
    kmp.setDin(entity.getDin());
    kmp.setEmail(entity.getEmail());
    kmp.setMobileNumber(entity.getMobileNumber());
    kmp.setPhoneNumber(entity.getPhoneNumber());
    kmp.setDeclarationDate(randomPastDate().atStartOfDay());
    kmp.setSubmissionDate(kmp.getDeclarationDate());
    kmp.setAction(KMPAction.ADDITION);
    kmp.setActionDate(kmp.getDeclarationDate());
    kmp.setFromNse(i%3 == 0);
    kmp.setCreatedBy(ADMIN);
    kmp.setCreatedDate(kmp.getDeclarationDate());
    kmp.setUpdatedDate(kmp.getDeclarationDate());
    if(kmp.getPanStatus().equals(notValidatedStatus)) {
      kmp.setPanDocFileName(dummyPanDocFileName);
      kmp.setPanDocDmsIndex(String.format("%d", i));
    }

    return kmp;
  }

  /**
   * Creates a KMP instance with Compliance Officer role
   */
  private KeyManagementPersonnel createKmpWithComplianceOfficerRole (ComplianceOfficer entity, int i) {
    KeyManagementPersonnel kmp = new KeyManagementPersonnel();
    kmp.setMemId(entity.getMember().getMemId());
    kmp.setAppId(RandomUtils.nextLong(1, 40000));
    kmp.setRole("Compliance Officer");
    kmp.setAdditionalDesignation(faker.name().title());
    kmp.setSalutation(faker.name().prefix());
    kmp.setName(entity.getName());
    kmp.setPan(entity.getPan());
    kmp.setPanStatus(entity.getPanStatus());
    kmp.setDin(entity.getDin());
    kmp.setEmail(entity.getEmail());
    kmp.setMobileNumber(entity.getMobileNumber());
    kmp.setPhoneNumber(entity.getPhoneNumber());
    kmp.setDeclarationDate(randomPastDate().atStartOfDay());
    kmp.setSubmissionDate(kmp.getDeclarationDate());
    kmp.setAction(KMPAction.ADDITION);
    kmp.setActionDate(kmp.getDeclarationDate());
    kmp.setFromNse(i%3 == 0);
    kmp.setCreatedBy(ADMIN);
    kmp.setCreatedDate(kmp.getDeclarationDate());
    kmp.setUpdatedDate(kmp.getDeclarationDate());
    if(kmp.getPanStatus().equals(notValidatedStatus)) {
      kmp.setPanDocFileName(dummyPanDocFileName);
      kmp.setPanDocDmsIndex(String.format("%d", i));
    }

    return kmp;
  }

  /**
   * Creates a KMP instance with Proprietor role
   */
  private KeyManagementPersonnel createKmpWithProprietorRole (Proprietor entity, int i) {
    KeyManagementPersonnel kmp = new KeyManagementPersonnel();
    kmp.setMemId(entity.getMemberId());
    kmp.setAppId(RandomUtils.nextLong(1, 40000));
    kmp.setRole("Proprietor");
    kmp.setAdditionalDesignation(faker.name().title());
    kmp.setSalutation(faker.name().prefix());
    kmp.setName(entity.getName());
    kmp.setPan(entity.getPan());
    kmp.setPanStatus(entity.getPanStatus());
    kmp.setDin(entity.getDin());
    kmp.setEmail(entity.getEmail());
    kmp.setMobileNumber(entity.getMobileNumber());
    kmp.setPhoneNumber(entity.getPhoneNumber());
    kmp.setDeclarationDate(randomPastDate().atStartOfDay());
    kmp.setSubmissionDate(kmp.getDeclarationDate());
    kmp.setAction(KMPAction.ADDITION);
    kmp.setActionDate(kmp.getDeclarationDate());
    kmp.setFromNse(i%3 == 0);
    kmp.setCreatedBy(ADMIN);
    kmp.setCreatedDate(kmp.getDeclarationDate());
    kmp.setUpdatedDate(kmp.getDeclarationDate());
    if(kmp.getPanStatus().equals(notValidatedStatus)) {
      kmp.setPanDocFileName(dummyPanDocFileName);
      kmp.setPanDocDmsIndex(String.format("%d", i));
    }

    return kmp;
  }

  /**
   * Generates the random data for the i-th director
   * @param i The index of the director
   * @return The director entity
   */
  private Director generateDirectorData(int i, MemberMaster memberMaster) {
    Director director = new Director();

    director.setTitle(faker.name().title());
    director.setName(memberMaster.getMemName());
    director.setDirectorId(RandomUtils.nextInt());
    director.setPan(generatePan());
    director.setMobileNumber(RandomStringUtils.random(10, false, true));
    director.setPhoneNumber(RandomStringUtils.random(8, false, true));
    director.setEmail(generateEmail(director.getName()));
    director.setDob(getRandomBirthDate());
    director.setTotalExperience(RandomUtils.nextLong(1, 25));
    director.setMember(memberMaster);
    director.setDin(RandomUtils.nextLong(1, 40000));
    director.setType(String.format("DirType%d", i));
    director.setESignature(RandomStringUtils.randomAlphanumeric(1));
    director.setStatus(i%5 == 0 ? "Inactive" : "Active" );
    director.setPostFacto(RandomStringUtils.randomAlphabetic(1));
    String nationality = faker.nation().nationality();
    director.setNationality(nationality.substring(0, Math.min(nationality.length(), 10)));
    director.setStartDate(randomPastDate());
    director.setEndDate(randomFutureDate());
    director.setPanValidated(i%2 == 0 ? "N" : "Y");
    director.setPanStatus(i%2 == 0 ? notValidatedStatus : validatedStatus);
    director.setCreatedBy(ADMIN);
    director.setCreatedDate(LocalDateTime.now());
    director.setJoiningDate(LocalDate.now());

    return director;
  }

  /**
   * Generates the random data for the i-th shareholder
   * @param i The index of the shareholder
   * @return The shareholder entity
   */
  private Shareholder generateShareholderData(int i, MemberMaster memberMaster) {
    Shareholder entity = new Shareholder();
    entity.setTitle(faker.name().title());
    entity.setName(memberMaster.getMemName());
    entity.setPan(generatePan());
    entity.setMobileNumber(RandomStringUtils.random(10, false, true));
    entity.setPhoneNumber(RandomStringUtils.random(8, false, true));
    entity.setEmail(generateEmail(entity.getName()));
    entity.setDob(getRandomBirthDate());
    entity.setMemberId(memberMaster.getMemId());
    entity.setDin(RandomUtils.nextLong(1, 40000));
    entity.setType(String.format("shType%d", i));
    entity.setStatus(i%5 == 0 ? "Inactive" : "Active" );
    entity.setStartDate(randomPastDate());
    entity.setEndDate(randomFutureDate());
    entity.setPanValidated(i%2 == 0 ? "N" : "Y");
    entity.setPanStatus(i%2 == 0 ? notValidatedStatus : validatedStatus);
    entity.setCreatedBy(ADMIN);
    entity.setCreatedDate(LocalDateTime.now());

    return entity;
  }

  /**
   * Generates the random data for the i-th compliance officer
   * @param i The index of the compliance officer
   * @return The compliance officer entity
   */
  private ComplianceOfficer generateComplianceOfficerData(int i, MemberMaster memberMaster) {
    ComplianceOfficer entity = new ComplianceOfficer();
    entity.setTitle(faker.name().title());
    entity.setName(memberMaster.getMemName());
    entity.setPan(generatePan());
    entity.setMobileNumber(RandomStringUtils.random(10, false, true));
    entity.setPhoneNumber(RandomStringUtils.random(8, false, true));
    entity.setEmail(generateEmail(entity.getName()));
    entity.setDob(getRandomBirthDate());
    entity.setMember(memberMaster);
    entity.setDin(RandomUtils.nextLong(1, 40000));
    entity.setStartDate(randomPastDate());
    entity.setEndDate(randomFutureDate());
    entity.setPanValidated(i%2 == 0 ? "No" : "Yes");
    entity.setPanStatus(i%2 == 0 ? notValidatedStatus : validatedStatus);
    // entity.setCreatedBy(ADMIN);
    // entity.setCreatedDate(LocalDateTime.now());
    return entity;
  }

  /**
   * Generates the random data for the i-th partner
   * @param i The index of the partner
   * @return The partner entity
   */
  private Partner generatePartnerData(int i, MemberMaster memberMaster) {
    Partner entity = new Partner();
    entity.setTitle(faker.name().title());
    entity.setName(memberMaster.getMemName());
    entity.setPan(generatePan());
    entity.setMobileNumber(RandomStringUtils.random(10, false, true));
    entity.setPhoneNumber(RandomStringUtils.random(8, false, true));
    entity.setEmail(generateEmail(entity.getName()));
    entity.setDob(getRandomBirthDate());
    entity.setMemberId(memberMaster.getMemId());
    entity.setDin(RandomUtils.nextLong(1, 40000));
    entity.setType(String.format("paType%d", i));
    entity.setStatus(i%5 == 0 ? "Inactive" : "Active" );
    entity.setStartDate(randomPastDate());
    entity.setEndDate(randomFutureDate());
    entity.setPanValidated(i%2 == 0 ? "N" : "Y");
    entity.setPanStatus(i%2 == 0 ? notValidatedStatus : validatedStatus);
    entity.setCreatedBy(ADMIN);
    entity.setCreatedDate(LocalDateTime.now());
    return entity;
  }

  /**
   * Generates the random data for the i-th partner
   * @param i The index of the partner
   * @return The partner entity
   */
  private Proprietor generateProprietorData(int i, MemberMaster memberMaster) {
    Proprietor entity = new Proprietor();
    entity.setTitle(faker.name().title());
    entity.setName(memberMaster.getMemName());
    entity.setPan(generatePan());
    entity.setMobileNumber(RandomStringUtils.random(10, false, true));
    entity.setPhoneNumber(RandomStringUtils.random(8, false, true));
    entity.setEmail(generateEmail(entity.getName()));
    entity.setDob(getRandomBirthDate());
    entity.setMemberId(memberMaster.getMemId());
    entity.setDin(RandomUtils.nextLong(1, 40000));
    entity.setType(String.format("poType%d", i%2));
    entity.setStatus(i%5 == 0 ? "Inactive" : "Active" );
    entity.setStartDate(randomPastDate());
    entity.setEndDate(randomFutureDate());
    entity.setPanValidated(i%2 == 0 ? "N" : "Y");
    entity.setPanStatus(i%2 == 0 ? notValidatedStatus : validatedStatus);
    entity.setCreatedBy(ADMIN);
    entity.setCreatedDate(LocalDateTime.now());
    return entity;
  }

  /**
   * Removes the existing data from the database
   */
  private void cleanUp() {
    //directorRepository.deleteAll();
    shareholderRepository.deleteAll();
    partnerRepository.deleteAll();
    proprietorRepository.deleteAll();
    //complianceOfficerRepository.deleteAll();
    kmpRepository.deleteAll();
  }

  /**
   * Generates a random date in the past
   * @return The generated date
   */
  private LocalDate randomPastDate() {
    return Instant.ofEpochMilli(faker.date().past(1000, TimeUnit.DAYS).getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
  }

  /**
   * Generates a random date in the future
   *
   * @return The generated date
   */
  private LocalDate randomFutureDate() {
    return Instant.ofEpochMilli(faker.date().future(365, TimeUnit.DAYS).getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
  }

  /**
   * Gets t a random birth date for age between 18 to 65
   *
   * @return The generated birth date
   */
  private LocalDate getRandomBirthDate() {
    return Instant.ofEpochMilli(faker.date().birthday().getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
  }

  /**
   * Generates an email from the user name
   *
   * @param name The user name for whom to generate the email
   * @return the generated email
   */
  private static String generateEmail(String name) {
    return name.replaceAll("\\s+", "\\.") + "@nse.co.in";
  }

  /**
   * Generates a random PAN number
   *
   * @return The generated PAN
   */
  private static String generatePan() {
    return RandomStringUtils
      .randomAlphabetic(3).toUpperCase()
      .concat("P")
      .concat(RandomStringUtils.randomAlphabetic(1).toUpperCase())
      .concat(RandomStringUtils.randomNumeric(4))
      .concat(RandomStringUtils.randomAlphabetic(1).toUpperCase());
  }
}
