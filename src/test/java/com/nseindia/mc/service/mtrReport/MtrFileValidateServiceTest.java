/*
package com.nseindia.mc.service.mtrReport;

import com.nseindia.mc.controller.dto.*;
import com.nseindia.mc.exception.BaseServiceException;
import com.nseindia.mc.model.*;
import com.nseindia.mc.repository.*;
import com.nseindia.mc.util.CommonUtils;
import org.apache.commons.csv.CSVPrinter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import static org.mockito.ArgumentMatchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CommonUtils.class,CSVPrinter.class,MtrFileValidateService.class,
        PrintWriter.class,File.class,FileOutputStream.class, FileInputStream.class})
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
public class MtrFileValidateServiceTest {

    @InjectMocks
    private MtrFileValidateService mtrFileValidateService;

    @Mock
    private MemberMasterRepository memberMasterRepository;

    @Mock
    private MTRErrorCodeMasterRepository mtrErrorCodeMasterRepository;

    @Mock
    private MtrReportService mtrReportService;

    @Mock
    private MTRDailyFileValidationRepository dailyFileValidationRepository;

    @Mock
    private MTRSymbolNameRepository symbolNameRepository;

    @Mock
    private MTRDailyFileRepository dailyFileRepository;

    @Mock
    private MTRDetailRecordRepository detailRecordRepository;

    @Mock
    private MTRControlRecordRepository controlRecordRepository;

    @Mock
    private MTRSummaryRecordRepository summaryRecordRepository;

    @Mock
    private MTRCollateralScripsDetailRecordRepository collateralScripsDetailRecordRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private MTRCollateralSummaryRecordRepository collateralSummaryRecordRepository;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private CSVPrinter csvPrinter;

    @Mock
    private PrintWriter printWriter;

    @Mock
    private File newFile;

    @Mock
    private FileOutputStream fileOutputStream;

    @Mock
    private FileInputStream fileInputStream;

    private PodamFactory factory = new PodamFactoryImpl();

    private Resource mtrValidFile = new ClassPathResource("mtr_27012021.t01");

    private Resource mtrInValidDataFile = new ClassPathResource("mtr_28012021.t01");

    private Resource mtrInvalidOrderFile = new ClassPathResource("mtr_29012021.t01");

    private Resource mtrMissingRecord50OrderFile = new ClassPathResource("mtr_30012021.t01");

    private Resource mtrFSMultiValidationFile = new ClassPathResource("mtr_31012021.t01");

    private Resource mtrIFDMultiValidationFile = new ClassPathResource("mtr_01022021.t01");

    private Resource mtrPDMultiValidationFile = new ClassPathResource("mtr_02022021.t01");

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(mtrFileValidateService,"batchSize", 10);
        ReflectionTestUtils.invokeMethod(mtrFileValidateService,"initializeKieContainer");
    }

    @Test
    public void validateMtrFile_validFilePassed_success() throws Exception {
        // Given
        MultipartFile file = new MockMultipartFile(mtrValidFile.getFilename(),mtrValidFile.getFilename(),"text/csv", mtrValidFile.getInputStream().readAllBytes());
        MemberMaster memberMaster = factory.manufacturePojo(MemberMaster.class);
        memberMaster.setMemCd("10002");
        Mockito.when(memberMasterRepository.findById(1l)).thenReturn(Optional.of(memberMaster));
        List<MTRErrorCodeMaster> mtrErrorCodeMasters = factory.manufacturePojo(List.class, MTRErrorCodeMaster.class);
        Mockito.when(mtrErrorCodeMasterRepository.findAll()).thenReturn(mtrErrorCodeMasters);
        MemberUploadStatus memberUploadStatus = factory.manufacturePojo(MemberUploadStatus.class);
        LocalDate reportDate = getReportDate(mtrValidFile.getFilename().substring(4, 12));
        memberUploadStatus.setMissedDates(Arrays.asList(reportDate));
        Mockito.when(mtrReportService.getMemberUploadStatus(memberMaster.getMemId())).thenReturn(memberUploadStatus);
        MTRDailyFileValidation mtrDailyFileValidation = factory.manufacturePojo(MTRDailyFileValidation.class);
        mtrDailyFileValidation.setMember(memberMaster);
        mtrDailyFileValidation.setBatchNo(0);
        Mockito.when(dailyFileValidationRepository
                .findFirstByMember_MemIdAndReportingDateOrderByBatchNoDesc(memberMaster.getMemId(), reportDate.atStartOfDay()))
                .thenReturn(Optional.of(mtrDailyFileValidation));
        List<MTRSymbolName> mtrSymbolNames = new ArrayList<>();
        MTRSymbolName mtrSymbolName = factory.manufacturePojo(MTRSymbolName.class);
        mtrSymbolName.setSymbolCode("SBIN");
        mtrSymbolNames.add(mtrSymbolName);
        mtrSymbolName = factory.manufacturePojo(MTRSymbolName.class);
        mtrSymbolName.setSymbolCode("RELIANCE");
        mtrSymbolNames.add(mtrSymbolName);

        Mockito.when(symbolNameRepository.findAll()).thenReturn(mtrSymbolNames);
        MTRDailyFile mtrDailyFile = factory.manufacturePojo(MTRDailyFile.class);
        Mockito.when(dailyFileRepository
                .findByMemberIdAndReportingDate(memberMaster.getMemId(), CommonUtils.getDatabaseDateStr(CommonUtils.getLastBusinessDay(reportDate))))
                .thenReturn(Optional.of(mtrDailyFile));
        MTRDetailRecordDtoInterface mtrDetailRecordDtoInterface = new MTRDetailRecordDtoInterface() {
            @Override
            public String getMapKey() {
                return "Refund";
            }

            @Override
            public Integer getFundedQuantityEndDay() {
                return 1;
            }

            @Override
            public Double getFundedAmountEndDay() {
                return 100.00;
            }
        };
        Mockito.when(detailRecordRepository.findByMtrFile_Id(mtrDailyFile.getId())).thenReturn(Arrays.asList(mtrDetailRecordDtoInterface).stream());
        Answer<MTRDailyFileValidation> answer = new Answer<>() {
            public MTRDailyFileValidation answer(InvocationOnMock invocation) {
                return invocation.getArgument(0);
            }
        };
        Mockito.when(dailyFileValidationRepository.saveAndFlush(any(MTRDailyFileValidation.class))).thenAnswer(answer);
        PowerMockito.whenNew(CSVPrinter.class).withAnyArguments().thenReturn(csvPrinter);
        PowerMockito.whenNew(PrintWriter.class).withAnyArguments().thenReturn(printWriter);
        PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(newFile);
        PowerMockito.whenNew(FileOutputStream.class).withAnyArguments().thenReturn(fileOutputStream);

        // When
        MTRValidateFileDto mtrValidateFileDto = mtrFileValidateService.validateMtrFile(file, 1l);

        // Then
        Assert.assertNotNull(mtrValidateFileDto);
        Assert.assertEquals(file.getOriginalFilename(), mtrValidateFileDto.getUploadedFileName());
        Assert.assertEquals("Y", mtrValidateFileDto.getValidMtrFile());

        ArgumentCaptor<MTRDailyFileValidation> argumentCaptor = ArgumentCaptor.forClass(MTRDailyFileValidation.class);
        Mockito.verify(dailyFileValidationRepository).saveAndFlush(argumentCaptor.capture());
        MTRDailyFileValidation savedMtrDailyFileValidation = argumentCaptor.getValue();
        Assert.assertNotNull(savedMtrDailyFileValidation);
        Assert.assertEquals(Integer.valueOf(1), savedMtrDailyFileValidation.getBatchNo());
        Assert.assertEquals(memberMaster, savedMtrDailyFileValidation.getMember());
    }

    @Test
    public void validateMtrFile_emptyFile_shouldGetException() {
        // Given
        MultipartFile file = new MockMultipartFile(mtrValidFile.getFilename(),mtrValidFile.getFilename(),"text/csv", "".getBytes());
        MemberMaster memberMaster = factory.manufacturePojo(MemberMaster.class);
        memberMaster.setMemCd("10003");
        Mockito.when(memberMasterRepository.findById(1l)).thenReturn(Optional.of(memberMaster));
        List<MTRErrorCodeMaster> mtrErrorCodeMasters = factory.manufacturePojo(List.class, MTRErrorCodeMaster.class);
        Mockito.when(mtrErrorCodeMasterRepository.findAll()).thenReturn(mtrErrorCodeMasters);

        // When/Then
        try {
            mtrFileValidateService.validateMtrFile(file, 1l);
            Assert.fail();
        } catch (BaseServiceException ex) {
            Assert.assertEquals("IF001: IF001", ex.getMessage());
        }
    }

    @Test
    public void validateMtrFile_fileNameNotValid_shouldGetException() {
        // Given
        MultipartFile file = new MockMultipartFile("invalid_file","invalid_file","text/csv", "hello".getBytes());
        MemberMaster memberMaster = factory.manufacturePojo(MemberMaster.class);
        memberMaster.setMemCd("10003");
        Mockito.when(memberMasterRepository.findById(1l)).thenReturn(Optional.of(memberMaster));
        List<MTRErrorCodeMaster> mtrErrorCodeMasters = factory.manufacturePojo(List.class, MTRErrorCodeMaster.class);
        Mockito.when(mtrErrorCodeMasterRepository.findAll()).thenReturn(mtrErrorCodeMasters);

        // When/Then
        try {
            mtrFileValidateService.validateMtrFile(file, 1l);
            Assert.fail();
        } catch (BaseServiceException ex) {
            Assert.assertEquals("IF002: IF002", ex.getMessage());
        }
    }

    @Test
    public void validateMtrFile_fileNameLengthNotValid_shouldGetException() {
        // Given
        MultipartFile file = new MockMultipartFile("mtr_270121","mtr_270121","text/csv", "hello".getBytes());
        MemberMaster memberMaster = factory.manufacturePojo(MemberMaster.class);
        memberMaster.setMemCd("10003");
        Mockito.when(memberMasterRepository.findById(1l)).thenReturn(Optional.of(memberMaster));
        List<MTRErrorCodeMaster> mtrErrorCodeMasters = factory.manufacturePojo(List.class, MTRErrorCodeMaster.class);
        Mockito.when(mtrErrorCodeMasterRepository.findAll()).thenReturn(mtrErrorCodeMasters);

        // When/Then
        try {
            mtrFileValidateService.validateMtrFile(file, 1l);
            Assert.fail();
        } catch (BaseServiceException ex) {
            Assert.assertEquals("IF003: IF003", ex.getMessage());
        }
    }

    @Test
    public void validateMtrFile_fileNameDateNotValid_shouldGetException() {
        // Given
        MultipartFile file = new MockMultipartFile("mtr_invaliDt","mtr_invaliDt","text/csv", "hello".getBytes());
        MemberMaster memberMaster = factory.manufacturePojo(MemberMaster.class);
        memberMaster.setMemCd("10003");
        Mockito.when(memberMasterRepository.findById(1l)).thenReturn(Optional.of(memberMaster));
        List<MTRErrorCodeMaster> mtrErrorCodeMasters = factory.manufacturePojo(List.class, MTRErrorCodeMaster.class);
        Mockito.when(mtrErrorCodeMasterRepository.findAll()).thenReturn(mtrErrorCodeMasters);

        // When/Then
        try {
            mtrFileValidateService.validateMtrFile(file, 1l);
            Assert.fail();
        } catch (BaseServiceException ex) {
            Assert.assertEquals("IF003: IF003", ex.getMessage());
        }
    }

    @Test
    public void validateMtrFile_missedDates_shouldGetException() {
        // Given
        MultipartFile file = new MockMultipartFile(mtrValidFile.getFilename(),mtrValidFile.getFilename(),"text/csv", "hello".getBytes());
        MemberMaster memberMaster = factory.manufacturePojo(MemberMaster.class);
        memberMaster.setMemCd("10003");
        Mockito.when(memberMasterRepository.findById(1l)).thenReturn(Optional.of(memberMaster));
        List<MTRErrorCodeMaster> mtrErrorCodeMasters = factory.manufacturePojo(List.class, MTRErrorCodeMaster.class);
        Mockito.when(mtrErrorCodeMasterRepository.findAll()).thenReturn(mtrErrorCodeMasters);
        MemberUploadStatus memberUploadStatus = factory.manufacturePojo(MemberUploadStatus.class);
        LocalDate reportDate = getReportDate("28012021");
        memberUploadStatus.setMissedDates(Arrays.asList(reportDate));
        Mockito.when(mtrReportService.getMemberUploadStatus(memberMaster.getMemId())).thenReturn(memberUploadStatus);

        // When/Then
        try {
            mtrFileValidateService.validateMtrFile(file, 1l);
            Assert.fail();
        } catch (BaseServiceException ex) {
            Assert.assertEquals("IF003: IF003", ex.getMessage());
        }
    }

    @Test
    public void validateMtrFile_fileNameDoesNotContainT_shouldGetException() {
        // Given
        MultipartFile file = new MockMultipartFile("mtr_27012021.b01","mtr_27012021.b01","text/csv", "hello".getBytes());
        MemberMaster memberMaster = factory.manufacturePojo(MemberMaster.class);
        memberMaster.setMemCd("10003");
        Mockito.when(memberMasterRepository.findById(1l)).thenReturn(Optional.of(memberMaster));
        List<MTRErrorCodeMaster> mtrErrorCodeMasters = factory.manufacturePojo(List.class, MTRErrorCodeMaster.class);
        Mockito.when(mtrErrorCodeMasterRepository.findAll()).thenReturn(mtrErrorCodeMasters);
        MemberUploadStatus memberUploadStatus = factory.manufacturePojo(MemberUploadStatus.class);
        LocalDate reportDate = getReportDate(mtrValidFile.getFilename().substring(4, 12));
        memberUploadStatus.setMissedDates(Arrays.asList(reportDate));
        Mockito.when(mtrReportService.getMemberUploadStatus(memberMaster.getMemId())).thenReturn(memberUploadStatus);

        // When/Then
        try {
            mtrFileValidateService.validateMtrFile(file, 1l);
            Assert.fail();
        } catch (BaseServiceException ex) {
            Assert.assertEquals("IF004: IF004", ex.getMessage());
        }
    }

    @Test
    public void validateMtrFile_invalidBatchNoCase1_shouldGetException() {
        // Given
        MultipartFile file = new MockMultipartFile("mtr_27012021.t0a","mtr_27012021.t0a","text/csv", "hello".getBytes());
        MemberMaster memberMaster = factory.manufacturePojo(MemberMaster.class);
        memberMaster.setMemCd("10003");
        Mockito.when(memberMasterRepository.findById(1l)).thenReturn(Optional.of(memberMaster));
        List<MTRErrorCodeMaster> mtrErrorCodeMasters = factory.manufacturePojo(List.class, MTRErrorCodeMaster.class);
        Mockito.when(mtrErrorCodeMasterRepository.findAll()).thenReturn(mtrErrorCodeMasters);
        MemberUploadStatus memberUploadStatus = factory.manufacturePojo(MemberUploadStatus.class);
        LocalDate reportDate = getReportDate(mtrValidFile.getFilename().substring(4, 12));
        memberUploadStatus.setMissedDates(Arrays.asList(reportDate));
        Mockito.when(mtrReportService.getMemberUploadStatus(memberMaster.getMemId())).thenReturn(memberUploadStatus);

        // When/Then
        try {
            mtrFileValidateService.validateMtrFile(file, 1l);
            Assert.fail();
        } catch (BaseServiceException ex) {
            Assert.assertEquals("IF005: IF005", ex.getMessage());
        }
    }

    @Test
    public void validateMtrFile_invalidBatchNoCase2_shouldGetException() {
        // Given
        MultipartFile file = new MockMultipartFile("mtr_27012021.t01","mtr_27012021.t01","text/csv", "hello".getBytes());
        MemberMaster memberMaster = factory.manufacturePojo(MemberMaster.class);
        memberMaster.setMemCd("10003");
        Mockito.when(memberMasterRepository.findById(1l)).thenReturn(Optional.of(memberMaster));
        List<MTRErrorCodeMaster> mtrErrorCodeMasters = factory.manufacturePojo(List.class, MTRErrorCodeMaster.class);
        Mockito.when(mtrErrorCodeMasterRepository.findAll()).thenReturn(mtrErrorCodeMasters);
        MemberUploadStatus memberUploadStatus = factory.manufacturePojo(MemberUploadStatus.class);
        LocalDate reportDate = getReportDate(mtrValidFile.getFilename().substring(4, 12));
        memberUploadStatus.setMissedDates(Arrays.asList(reportDate));
        Mockito.when(mtrReportService.getMemberUploadStatus(memberMaster.getMemId())).thenReturn(memberUploadStatus);
        MTRDailyFileValidation mtrDailyFileValidation = factory.manufacturePojo(MTRDailyFileValidation.class);
        mtrDailyFileValidation.setMember(memberMaster);
        mtrDailyFileValidation.setBatchNo(1);
        Mockito.when(dailyFileValidationRepository
                .findFirstByMember_MemIdAndReportingDateOrderByBatchNoDesc(memberMaster.getMemId(), reportDate.atStartOfDay()))
                .thenReturn(Optional.of(mtrDailyFileValidation));

        // When/Then
        try {
            mtrFileValidateService.validateMtrFile(file, 1l);
            Assert.fail();
        } catch (BaseServiceException ex) {
            Assert.assertEquals("IF006: IF006", ex.getMessage());
        }
    }

    @Test
    public void validateMtrFile_invalidDataMultipleValidations_shouldGetValidationError() throws Exception {
        // Given
        MultipartFile file = new MockMultipartFile(mtrInValidDataFile.getFilename(), mtrInValidDataFile.getFilename(),"text/csv", mtrInValidDataFile.getInputStream().readAllBytes());
        MemberMaster memberMaster = factory.manufacturePojo(MemberMaster.class);
        memberMaster.setMemCd("10003");
        Mockito.when(memberMasterRepository.findById(1l)).thenReturn(Optional.of(memberMaster));
        List<MTRErrorCodeMaster> mtrErrorCodeMasters = factory.manufacturePojo(List.class, MTRErrorCodeMaster.class);
        Mockito.when(mtrErrorCodeMasterRepository.findAll()).thenReturn(mtrErrorCodeMasters);
        MemberUploadStatus memberUploadStatus = factory.manufacturePojo(MemberUploadStatus.class);
        LocalDate reportDate = getReportDate(mtrInValidDataFile.getFilename().substring(4, 12));
        memberUploadStatus.setMissedDates(Arrays.asList(reportDate));
        Mockito.when(mtrReportService.getMemberUploadStatus(memberMaster.getMemId())).thenReturn(memberUploadStatus);
        MTRDailyFileValidation mtrDailyFileValidation = factory.manufacturePojo(MTRDailyFileValidation.class);
        mtrDailyFileValidation.setMember(memberMaster);
        mtrDailyFileValidation.setBatchNo(0);
        Mockito.when(dailyFileValidationRepository
                .findFirstByMember_MemIdAndReportingDateOrderByBatchNoDesc(memberMaster.getMemId(), reportDate.atStartOfDay()))
                .thenReturn(Optional.of(mtrDailyFileValidation));
        Answer<MTRDailyFileValidation> answer = new Answer<>() {
            public MTRDailyFileValidation answer(InvocationOnMock invocation) {
                return invocation.getArgument(0);
            }
        };
        Mockito.when(dailyFileValidationRepository.saveAndFlush(any(MTRDailyFileValidation.class))).thenAnswer(answer);
        PowerMockito.whenNew(CSVPrinter.class).withAnyArguments().thenReturn(csvPrinter);
        PowerMockito.whenNew(PrintWriter.class).withAnyArguments().thenReturn(printWriter);
        PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(newFile);
        PowerMockito.whenNew(FileOutputStream.class).withAnyArguments().thenReturn(fileOutputStream);

        // When
        MTRValidateFileDto mtrValidateFileDto = mtrFileValidateService.validateMtrFile(file, 1l);

        // Then
        Assert.assertNotNull(mtrValidateFileDto);
        Assert.assertEquals("N", mtrValidateFileDto.getValidMtrFile());
        Assert.assertEquals(6, mtrValidateFileDto.getDailyFileErrors().size());
        List<String> validationErrorCode = mtrValidateFileDto.getDailyFileErrors().stream().map(MTRDailyFileErrorDto::getErrorCode).collect(Collectors.toList());
        Assert.assertTrue(validationErrorCode.contains("FS001"));
        Assert.assertTrue(validationErrorCode.contains("FS003"));
        Assert.assertTrue(validationErrorCode.contains("FS004"));
        Assert.assertTrue(validationErrorCode.contains("FS005"));
        Assert.assertTrue(validationErrorCode.contains("FS006"));
        Assert.assertTrue(validationErrorCode.contains("FS009"));

        ArgumentCaptor<MTRDailyFileValidation> argumentCaptor = ArgumentCaptor.forClass(MTRDailyFileValidation.class);
        Mockito.verify(dailyFileValidationRepository).saveAndFlush(argumentCaptor.capture());
        MTRDailyFileValidation savedMtrDailyFileValidation = argumentCaptor.getValue();
        Assert.assertNotNull(savedMtrDailyFileValidation);
        Assert.assertEquals(Integer.valueOf(1), savedMtrDailyFileValidation.getBatchNo());
        Assert.assertEquals(memberMaster, savedMtrDailyFileValidation.getMember());
        Assert.assertNotNull(savedMtrDailyFileValidation.getErrorCodeFileName());
        Assert.assertTrue(savedMtrDailyFileValidation.getErrorCodeFileName().endsWith(".csv"));

    }

    @Test
    public void validateMtrFile_invalidOrderMultipleValidations_shouldGetValidationError() throws Exception {
        // Given
        MultipartFile file = new MockMultipartFile(mtrInvalidOrderFile.getFilename(), mtrInvalidOrderFile.getFilename(),
                "text/csv", mtrInvalidOrderFile.getInputStream().readAllBytes());
        MemberMaster memberMaster = factory.manufacturePojo(MemberMaster.class);
        memberMaster.setMemCd("10003");
        Mockito.when(memberMasterRepository.findById(1l)).thenReturn(Optional.of(memberMaster));
        List<MTRErrorCodeMaster> mtrErrorCodeMasters = factory.manufacturePojo(List.class, MTRErrorCodeMaster.class);
        Mockito.when(mtrErrorCodeMasterRepository.findAll()).thenReturn(mtrErrorCodeMasters);
        MemberUploadStatus memberUploadStatus = factory.manufacturePojo(MemberUploadStatus.class);
        LocalDate reportDate = getReportDate(mtrInvalidOrderFile.getFilename().substring(4, 12));
        memberUploadStatus.setMissedDates(Arrays.asList(reportDate));
        Mockito.when(mtrReportService.getMemberUploadStatus(memberMaster.getMemId())).thenReturn(memberUploadStatus);
        MTRDailyFileValidation mtrDailyFileValidation = factory.manufacturePojo(MTRDailyFileValidation.class);
        mtrDailyFileValidation.setMember(memberMaster);
        mtrDailyFileValidation.setBatchNo(0);
        Mockito.when(dailyFileValidationRepository
                .findFirstByMember_MemIdAndReportingDateOrderByBatchNoDesc(memberMaster.getMemId(), reportDate.atStartOfDay()))
                .thenReturn(Optional.of(mtrDailyFileValidation));
        Answer<MTRDailyFileValidation> answer = new Answer<>() {
            public MTRDailyFileValidation answer(InvocationOnMock invocation) {
                return invocation.getArgument(0);
            }
        };
        Mockito.when(dailyFileValidationRepository.saveAndFlush(any(MTRDailyFileValidation.class))).thenAnswer(answer);
        PowerMockito.whenNew(CSVPrinter.class).withAnyArguments().thenReturn(csvPrinter);
        PowerMockito.whenNew(PrintWriter.class).withAnyArguments().thenReturn(printWriter);
        PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(newFile);
        PowerMockito.whenNew(FileOutputStream.class).withAnyArguments().thenReturn(fileOutputStream);

        // When
        MTRValidateFileDto mtrValidateFileDto = mtrFileValidateService.validateMtrFile(file, 1l);

        // Then
        Assert.assertNotNull(mtrValidateFileDto);
        Assert.assertEquals("N", mtrValidateFileDto.getValidMtrFile());
        Assert.assertEquals(4, mtrValidateFileDto.getDailyFileErrors().size());
        List<String> validationErrorCode = mtrValidateFileDto.getDailyFileErrors().stream().map(MTRDailyFileErrorDto::getErrorCode).collect(Collectors.toList());
        Assert.assertTrue(validationErrorCode.contains("FS008"));
        Assert.assertTrue(validationErrorCode.contains("FS002"));
        Assert.assertTrue(validationErrorCode.contains("FS013"));
        Assert.assertTrue(validationErrorCode.contains("IFD015"));

        ArgumentCaptor<MTRDailyFileValidation> argumentCaptor = ArgumentCaptor.forClass(MTRDailyFileValidation.class);
        Mockito.verify(dailyFileValidationRepository).saveAndFlush(argumentCaptor.capture());
        MTRDailyFileValidation savedMtrDailyFileValidation = argumentCaptor.getValue();
        Assert.assertNotNull(savedMtrDailyFileValidation);
        Assert.assertEquals(Integer.valueOf(1), savedMtrDailyFileValidation.getBatchNo());
        Assert.assertEquals(memberMaster, savedMtrDailyFileValidation.getMember());
        Assert.assertNotNull(savedMtrDailyFileValidation.getErrorCodeFileName());
        Assert.assertTrue(savedMtrDailyFileValidation.getErrorCodeFileName().endsWith(".csv"));

    }

    @Test
    public void validateMtrFile_missingRecord50MultipleValidations_shouldGetValidationError() throws Exception {
        // Given
        MultipartFile file = new MockMultipartFile(mtrMissingRecord50OrderFile.getFilename(), mtrMissingRecord50OrderFile.getFilename(),
                "text/csv", mtrMissingRecord50OrderFile.getInputStream().readAllBytes());
        MemberMaster memberMaster = factory.manufacturePojo(MemberMaster.class);
        memberMaster.setMemCd("10003");
        Mockito.when(memberMasterRepository.findById(1l)).thenReturn(Optional.of(memberMaster));
        List<MTRErrorCodeMaster> mtrErrorCodeMasters = factory.manufacturePojo(List.class, MTRErrorCodeMaster.class);
        Mockito.when(mtrErrorCodeMasterRepository.findAll()).thenReturn(mtrErrorCodeMasters);
        MemberUploadStatus memberUploadStatus = factory.manufacturePojo(MemberUploadStatus.class);
        LocalDate reportDate = getReportDate(mtrMissingRecord50OrderFile.getFilename().substring(4, 12));
        memberUploadStatus.setMissedDates(Arrays.asList(reportDate));
        Mockito.when(mtrReportService.getMemberUploadStatus(memberMaster.getMemId())).thenReturn(memberUploadStatus);
        MTRDailyFileValidation mtrDailyFileValidation = factory.manufacturePojo(MTRDailyFileValidation.class);
        mtrDailyFileValidation.setMember(memberMaster);
        mtrDailyFileValidation.setBatchNo(0);
        Mockito.when(dailyFileValidationRepository
                .findFirstByMember_MemIdAndReportingDateOrderByBatchNoDesc(memberMaster.getMemId(), reportDate.atStartOfDay()))
                .thenReturn(Optional.of(mtrDailyFileValidation));
        Answer<MTRDailyFileValidation> answer = new Answer<>() {
            public MTRDailyFileValidation answer(InvocationOnMock invocation) {
                return invocation.getArgument(0);
            }
        };
        Mockito.when(dailyFileValidationRepository.saveAndFlush(any(MTRDailyFileValidation.class))).thenAnswer(answer);
        PowerMockito.whenNew(CSVPrinter.class).withAnyArguments().thenReturn(csvPrinter);
        PowerMockito.whenNew(PrintWriter.class).withAnyArguments().thenReturn(printWriter);
        PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(newFile);
        PowerMockito.whenNew(FileOutputStream.class).withAnyArguments().thenReturn(fileOutputStream);

        // When
        MTRValidateFileDto mtrValidateFileDto = mtrFileValidateService.validateMtrFile(file, 1l);

        // Then
        Assert.assertNotNull(mtrValidateFileDto);
        Assert.assertEquals("N", mtrValidateFileDto.getValidMtrFile());
        Assert.assertEquals(2, mtrValidateFileDto.getDailyFileErrors().size());
        List<String> validationErrorCode = mtrValidateFileDto.getDailyFileErrors().stream().map(MTRDailyFileErrorDto::getErrorCode).collect(Collectors.toList());
        Assert.assertTrue(validationErrorCode.contains("FS007"));
        Assert.assertTrue(validationErrorCode.contains("IFD015"));


        ArgumentCaptor<MTRDailyFileValidation> argumentCaptor = ArgumentCaptor.forClass(MTRDailyFileValidation.class);
        Mockito.verify(dailyFileValidationRepository).saveAndFlush(argumentCaptor.capture());
        MTRDailyFileValidation savedMtrDailyFileValidation = argumentCaptor.getValue();
        Assert.assertNotNull(savedMtrDailyFileValidation);
        Assert.assertEquals(Integer.valueOf(1), savedMtrDailyFileValidation.getBatchNo());
        Assert.assertEquals(memberMaster, savedMtrDailyFileValidation.getMember());
        Assert.assertNotNull(savedMtrDailyFileValidation.getErrorCodeFileName());
        Assert.assertTrue(savedMtrDailyFileValidation.getErrorCodeFileName().endsWith(".csv"));

    }

    @Test
    public void validateMtrFile_multipleValidationsForFS10To15_shouldGetValidationError() throws Exception {
        // Given
        MultipartFile file = new MockMultipartFile(mtrFSMultiValidationFile.getFilename(), mtrFSMultiValidationFile.getFilename(),
                "text/csv", mtrFSMultiValidationFile.getInputStream().readAllBytes());
        MemberMaster memberMaster = factory.manufacturePojo(MemberMaster.class);
        memberMaster.setMemCd("10003");
        Mockito.when(memberMasterRepository.findById(1l)).thenReturn(Optional.of(memberMaster));
        List<MTRErrorCodeMaster> mtrErrorCodeMasters = factory.manufacturePojo(List.class, MTRErrorCodeMaster.class);
        Mockito.when(mtrErrorCodeMasterRepository.findAll()).thenReturn(mtrErrorCodeMasters);
        MemberUploadStatus memberUploadStatus = factory.manufacturePojo(MemberUploadStatus.class);
        LocalDate reportDate = getReportDate(mtrFSMultiValidationFile.getFilename().substring(4, 12));
        memberUploadStatus.setMissedDates(Arrays.asList(reportDate));
        Mockito.when(mtrReportService.getMemberUploadStatus(memberMaster.getMemId())).thenReturn(memberUploadStatus);
        MTRDailyFileValidation mtrDailyFileValidation = factory.manufacturePojo(MTRDailyFileValidation.class);
        mtrDailyFileValidation.setMember(memberMaster);
        mtrDailyFileValidation.setBatchNo(0);
        Mockito.when(dailyFileValidationRepository
                .findFirstByMember_MemIdAndReportingDateOrderByBatchNoDesc(memberMaster.getMemId(), reportDate.atStartOfDay()))
                .thenReturn(Optional.of(mtrDailyFileValidation));
        Answer<MTRDailyFileValidation> answer = new Answer<>() {
            public MTRDailyFileValidation answer(InvocationOnMock invocation) {
                return invocation.getArgument(0);
            }
        };
        Mockito.when(dailyFileValidationRepository.saveAndFlush(any(MTRDailyFileValidation.class))).thenAnswer(answer);
        PowerMockito.whenNew(CSVPrinter.class).withAnyArguments().thenReturn(csvPrinter);
        PowerMockito.whenNew(PrintWriter.class).withAnyArguments().thenReturn(printWriter);
        PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(newFile);
        PowerMockito.whenNew(FileOutputStream.class).withAnyArguments().thenReturn(fileOutputStream);

        // When
        MTRValidateFileDto mtrValidateFileDto = mtrFileValidateService.validateMtrFile(file, 1l);

        // Then
        Assert.assertNotNull(mtrValidateFileDto);
        Assert.assertEquals("N", mtrValidateFileDto.getValidMtrFile());
        ArgumentCaptor<String> errorCodeArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> errorMsgArgumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(csvPrinter, Mockito.times(6)).printRecord(errorCodeArgumentCaptor.capture(), errorMsgArgumentCaptor.capture());
        List<String> validationErrorCode = errorCodeArgumentCaptor.getAllValues();
        Assert.assertEquals("N", mtrValidateFileDto.getValidMtrFile());
        Assert.assertEquals(6, validationErrorCode.size());
        Assert.assertTrue(validationErrorCode.contains("FS010"));
        Assert.assertTrue(validationErrorCode.contains("FS011"));
        Assert.assertTrue(validationErrorCode.contains("FS012"));
        Assert.assertTrue(validationErrorCode.contains("FS014"));
        Assert.assertTrue(validationErrorCode.contains("FS015"));


        ArgumentCaptor<MTRDailyFileValidation> argumentCaptor = ArgumentCaptor.forClass(MTRDailyFileValidation.class);
        Mockito.verify(dailyFileValidationRepository).saveAndFlush(argumentCaptor.capture());
        MTRDailyFileValidation savedMtrDailyFileValidation = argumentCaptor.getValue();
        Assert.assertNotNull(savedMtrDailyFileValidation);
        Assert.assertEquals(Integer.valueOf(1), savedMtrDailyFileValidation.getBatchNo());
        Assert.assertEquals(memberMaster, savedMtrDailyFileValidation.getMember());
        Assert.assertNotNull(savedMtrDailyFileValidation.getErrorCodeFileName());
        Assert.assertTrue(savedMtrDailyFileValidation.getErrorCodeFileName().endsWith(".csv"));
    }

    @Test
    public void validateMtrFile_multipleValidationsForIFD1To28_shouldGetValidationError() throws Exception {
        // Given
        MultipartFile file = new MockMultipartFile(mtrIFDMultiValidationFile.getFilename(), mtrIFDMultiValidationFile.getFilename(),
                "text/csv", mtrIFDMultiValidationFile.getInputStream().readAllBytes());
        MemberMaster memberMaster = factory.manufacturePojo(MemberMaster.class);
        memberMaster.setMemCd("10001");
        Mockito.when(memberMasterRepository.findById(1l)).thenReturn(Optional.of(memberMaster));
        List<MTRErrorCodeMaster> mtrErrorCodeMasters = factory.manufacturePojo(List.class, MTRErrorCodeMaster.class);
        Mockito.when(mtrErrorCodeMasterRepository.findAll()).thenReturn(mtrErrorCodeMasters);
        MemberUploadStatus memberUploadStatus = factory.manufacturePojo(MemberUploadStatus.class);
        LocalDate reportDate = getReportDate(mtrIFDMultiValidationFile.getFilename().substring(4, 12));
        memberUploadStatus.setMissedDates(Arrays.asList(reportDate));
        Mockito.when(mtrReportService.getMemberUploadStatus(memberMaster.getMemId())).thenReturn(memberUploadStatus);
        MTRDailyFileValidation mtrDailyFileValidation = factory.manufacturePojo(MTRDailyFileValidation.class);
        mtrDailyFileValidation.setMember(memberMaster);
        mtrDailyFileValidation.setBatchNo(0);
        Mockito.when(dailyFileValidationRepository
                .findFirstByMember_MemIdAndReportingDateOrderByBatchNoDesc(memberMaster.getMemId(), reportDate.atStartOfDay()))
                .thenReturn(Optional.of(mtrDailyFileValidation));
        Answer<MTRDailyFileValidation> answer = new Answer<>() {
            public MTRDailyFileValidation answer(InvocationOnMock invocation) {
                return invocation.getArgument(0);
            }
        };
        Mockito.when(dailyFileValidationRepository.saveAndFlush(any(MTRDailyFileValidation.class))).thenAnswer(answer);
        PowerMockito.whenNew(CSVPrinter.class).withAnyArguments().thenReturn(csvPrinter);
        PowerMockito.whenNew(PrintWriter.class).withAnyArguments().thenReturn(printWriter);
        PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(newFile);
        PowerMockito.whenNew(FileOutputStream.class).withAnyArguments().thenReturn(fileOutputStream);

        // When
        MTRValidateFileDto mtrValidateFileDto = mtrFileValidateService.validateMtrFile(file, 1l);

        // Then
        Assert.assertNotNull(mtrValidateFileDto);
        ArgumentCaptor<String> errorCodeArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> errorMsgArgumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(csvPrinter, Mockito.times(26)).printRecord(errorCodeArgumentCaptor.capture(), errorMsgArgumentCaptor.capture());
        List<String> validationErrorCode = errorCodeArgumentCaptor.getAllValues();
        Assert.assertEquals("N", mtrValidateFileDto.getValidMtrFile());
        Assert.assertEquals(26, validationErrorCode.size());
        Assert.assertTrue(validationErrorCode.contains("IFD001"));
        Assert.assertTrue(validationErrorCode.contains("IFD002"));
        Assert.assertTrue(validationErrorCode.contains("IFD003"));
        Assert.assertTrue(validationErrorCode.contains("IFD004"));
        Assert.assertTrue(validationErrorCode.contains("IFD006"));
        Assert.assertTrue(validationErrorCode.contains("IFD007"));
        Assert.assertTrue(validationErrorCode.contains("IFD008"));
        Assert.assertTrue(validationErrorCode.contains("IFD009"));
        Assert.assertTrue(validationErrorCode.contains("IFD010"));
        Assert.assertTrue(validationErrorCode.contains("IFD011"));
        Assert.assertTrue(validationErrorCode.contains("IFD012"));
        Assert.assertTrue(validationErrorCode.contains("IFD014"));
        Assert.assertTrue(validationErrorCode.contains("IFD017"));
        Assert.assertTrue(validationErrorCode.contains("IFD019"));
        Assert.assertTrue(validationErrorCode.contains("IFD021"));
        Assert.assertTrue(validationErrorCode.contains("IFD022"));
        Assert.assertTrue(validationErrorCode.contains("IFD023"));
        Assert.assertTrue(validationErrorCode.contains("IFD025"));
        Assert.assertTrue(validationErrorCode.contains("IFD026"));
        Assert.assertTrue(validationErrorCode.contains("IFD027"));
        Assert.assertTrue(validationErrorCode.contains("IFD028"));

        ArgumentCaptor<MTRDailyFileValidation> argumentCaptor = ArgumentCaptor.forClass(MTRDailyFileValidation.class);
        Mockito.verify(dailyFileValidationRepository).saveAndFlush(argumentCaptor.capture());
        MTRDailyFileValidation savedMtrDailyFileValidation = argumentCaptor.getValue();
        Assert.assertNotNull(savedMtrDailyFileValidation);
        Assert.assertEquals(Integer.valueOf(1), savedMtrDailyFileValidation.getBatchNo());
        Assert.assertEquals(memberMaster, savedMtrDailyFileValidation.getMember());
        Assert.assertNotNull(savedMtrDailyFileValidation.getErrorCodeFileName());
        Assert.assertTrue(savedMtrDailyFileValidation.getErrorCodeFileName().endsWith(".csv"));
    }

    @Test
    public void validateMtrFile_multipleValidationsForPD1To6_shouldGetValidationError() throws Exception {
        // Given
        MultipartFile file = new MockMultipartFile(mtrPDMultiValidationFile.getFilename(), mtrPDMultiValidationFile.getFilename(),
                "text/csv", mtrPDMultiValidationFile.getInputStream().readAllBytes());
        MemberMaster memberMaster = factory.manufacturePojo(MemberMaster.class);
        memberMaster.setMemCd("10001");
        Mockito.when(memberMasterRepository.findById(1l)).thenReturn(Optional.of(memberMaster));
        List<MTRErrorCodeMaster> mtrErrorCodeMasters = factory.manufacturePojo(List.class, MTRErrorCodeMaster.class);
        Mockito.when(mtrErrorCodeMasterRepository.findAll()).thenReturn(mtrErrorCodeMasters);
        MemberUploadStatus memberUploadStatus = factory.manufacturePojo(MemberUploadStatus.class);
        LocalDate reportDate = getReportDate(mtrPDMultiValidationFile.getFilename().substring(4, 12));
        memberUploadStatus.setMissedDates(Arrays.asList(reportDate));
        Mockito.when(mtrReportService.getMemberUploadStatus(memberMaster.getMemId())).thenReturn(memberUploadStatus);
        MTRDailyFileValidation mtrDailyFileValidation = factory.manufacturePojo(MTRDailyFileValidation.class);
        mtrDailyFileValidation.setMember(memberMaster);
        mtrDailyFileValidation.setBatchNo(0);
        Mockito.when(dailyFileValidationRepository
                .findFirstByMember_MemIdAndReportingDateOrderByBatchNoDesc(memberMaster.getMemId(), reportDate.atStartOfDay()))
                .thenReturn(Optional.of(mtrDailyFileValidation));
        Answer<MTRDailyFileValidation> answer = new Answer<>() {
            public MTRDailyFileValidation answer(InvocationOnMock invocation) {
                return invocation.getArgument(0);
            }
        };
        Mockito.when(dailyFileValidationRepository.saveAndFlush(any(MTRDailyFileValidation.class))).thenAnswer(answer);
        PowerMockito.whenNew(CSVPrinter.class).withAnyArguments().thenReturn(csvPrinter);
        PowerMockito.whenNew(PrintWriter.class).withAnyArguments().thenReturn(printWriter);
        PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(newFile);
        PowerMockito.whenNew(FileOutputStream.class).withAnyArguments().thenReturn(fileOutputStream);

        // When
        MTRValidateFileDto mtrValidateFileDto = mtrFileValidateService.validateMtrFile(file, 1l);

        // Then
        Assert.assertNotNull(mtrValidateFileDto);
        ArgumentCaptor<String> errorCodeArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> errorMsgArgumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(csvPrinter, Mockito.times(10)).printRecord(errorCodeArgumentCaptor.capture(), errorMsgArgumentCaptor.capture());
        List<String> validationErrorCode = errorCodeArgumentCaptor.getAllValues();
        Assert.assertEquals("N", mtrValidateFileDto.getValidMtrFile());
        Assert.assertEquals(10, validationErrorCode.size());
        Assert.assertTrue(validationErrorCode.contains("PD001"));
        Assert.assertTrue(validationErrorCode.contains("PD002"));
        Assert.assertTrue(validationErrorCode.contains("PD003"));
        Assert.assertTrue(validationErrorCode.contains("PD004"));
        Assert.assertTrue(validationErrorCode.contains("PD005"));
        Assert.assertTrue(validationErrorCode.contains("PD006"));

        ArgumentCaptor<MTRDailyFileValidation> argumentCaptor = ArgumentCaptor.forClass(MTRDailyFileValidation.class);
        Mockito.verify(dailyFileValidationRepository).saveAndFlush(argumentCaptor.capture());
        MTRDailyFileValidation savedMtrDailyFileValidation = argumentCaptor.getValue();
        Assert.assertNotNull(savedMtrDailyFileValidation);
        Assert.assertEquals(Integer.valueOf(1), savedMtrDailyFileValidation.getBatchNo());
        Assert.assertEquals(memberMaster, savedMtrDailyFileValidation.getMember());
        Assert.assertNotNull(savedMtrDailyFileValidation.getErrorCodeFileName());
        Assert.assertTrue(savedMtrDailyFileValidation.getErrorCodeFileName().endsWith(".csv"));

    }

    @Test
    public void downloadMtrFileValidationResponse_validData_success() {
        // Given
        MemberMaster memberMaster = factory.manufacturePojo(MemberMaster.class);
        MTRDailyFileValidation mtrDailyFileValidation = factory.manufacturePojo(MTRDailyFileValidation.class);
        mtrDailyFileValidation.setMember(memberMaster);
        Mockito.when(dailyFileValidationRepository.findById(1l)).thenReturn(Optional.of(mtrDailyFileValidation));

        // When
        ByteArrayOutputStream byteArrayOutputStream = mtrFileValidateService
                .downloadMtrFileValidationResponse(memberMaster.getMemId(), 1l);

        // Then
        Assert.assertNotNull(byteArrayOutputStream);
    }

    @Test
    public void downloadMtrFileValidationResponse_fileIsNotValidated_shouldGetException() {
        // When/Then
        try {
            mtrFileValidateService.downloadMtrFileValidationResponse(1l, 1l);
            Assert.fail();
        } catch (BaseServiceException ex) {
            Assert.assertEquals("The file hasn't been validated yet.", ex.getMessage());
        }
    }

    @Test
    public void downloadMtrFileValidationResponse_memberIdMismatch_shouldGetException() {
        // Given
        MemberMaster memberMaster = factory.manufacturePojo(MemberMaster.class);
        MTRDailyFileValidation mtrDailyFileValidation = factory.manufacturePojo(MTRDailyFileValidation.class);
        Mockito.when(dailyFileValidationRepository.findById(1l)).thenReturn(Optional.of(mtrDailyFileValidation));

        // When/Then
        try {
            mtrFileValidateService.downloadMtrFileValidationResponse(memberMaster.getMemId(), 1l);
            Assert.fail();
        } catch (BaseServiceException ex) {
            Assert.assertEquals("the validation["+1+"] does not belong to member["+memberMaster.getMemId()+"]", ex.getMessage());
        }
    }

    @Test
    public void downloadMtrFileValidationResponse_runtimeException_shouldGetException() throws IOException {
        // Given
        MemberMaster memberMaster = factory.manufacturePojo(MemberMaster.class);
        MTRDailyFileValidation mtrDailyFileValidation = factory.manufacturePojo(MTRDailyFileValidation.class);
        mtrDailyFileValidation.setResponseFileName(null);
        mtrDailyFileValidation.setMember(memberMaster);
        Mockito.when(dailyFileValidationRepository.findById(1l)).thenReturn(Optional.of(mtrDailyFileValidation));
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.addFileToZip(ArgumentMatchers.anyString(),ArgumentMatchers.anyString(), any(ZipOutputStream.class)))
                .thenThrow(new IOException());

        // When/Then
        try {
            mtrFileValidateService.downloadMtrFileValidationResponse(memberMaster.getMemId(), 1l);
            Assert.fail();
        } catch (BaseServiceException ex) {
            Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getHttpStatus());
        }

    }

    @Test
    public void submitMtrFile_validData_success() throws SQLException {
        // Given
        MemberMaster memberMaster = factory.manufacturePojo(MemberMaster.class);
        Mockito.when(memberMasterRepository.findById(memberMaster.getMemId())).thenReturn(Optional.of(memberMaster));
        MTRDailyFileValidation mtrDailyFileValidation = factory.manufacturePojo(MTRDailyFileValidation.class);
        mtrDailyFileValidation.setDmsDocIndex("./src/test/resources/mtr_27012021.t01");
        mtrDailyFileValidation.setMember(memberMaster);
        mtrDailyFileValidation.setDailyFileStatus(true);
        Mockito.when(dailyFileValidationRepository.findById(1l)).thenReturn(Optional.of(mtrDailyFileValidation));
        MTRDailyFile mtrDailyFile = factory.manufacturePojo(MTRDailyFile.class);
        Mockito.when(dailyFileRepository
                .findByMemberIdAndReportingDateIncludingAutoGeneratedFiles(memberMaster.getMemId(),
                        CommonUtils.getDatabaseDateStr(mtrDailyFileValidation.getReportingDate())))
                .thenReturn(Optional.of(mtrDailyFile));

        // When
        SubmitMTRFileResponse submitMTRFileResponse = mtrFileValidateService
                .submitMtrFile(memberMaster.getMemId(), 1l);

        // Then
        Assert.assertNotNull(submitMTRFileResponse);
        Assert.assertEquals(200, submitMTRFileResponse.getStatus());
        Assert.assertEquals("MTR Submission completely successfully for "
                .concat(mtrDailyFileValidation.getReportingDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))), submitMTRFileResponse.getMessage());
        Assert.assertEquals(mtrDailyFileValidation.getReportingDate(), submitMTRFileResponse.getReportingDate());

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<BatchPreparedStatementSetter> batchPreparedStatementSetterArgumentCaptor
                = ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);
        Mockito.verify(jdbcTemplate, Mockito.times(2)).batchUpdate(queryCaptor.capture(), batchPreparedStatementSetterArgumentCaptor.capture());
        Assert.assertEquals(MTRDetailRecord.INSERT_SQL, queryCaptor.getAllValues().get(0));
        Assert.assertEquals(MTRCollateralsScripsDetailRecord.INSERT_SQL, queryCaptor.getAllValues().get(1));
        BatchPreparedStatementSetter batchPreparedStatementSetter = batchPreparedStatementSetterArgumentCaptor.getAllValues().get(0);
        Assert.assertEquals(1,batchPreparedStatementSetter.getBatchSize());
        batchPreparedStatementSetter.setValues(preparedStatement, 0);

        batchPreparedStatementSetter = batchPreparedStatementSetterArgumentCaptor.getAllValues().get(1);
        Assert.assertEquals(1,batchPreparedStatementSetter.getBatchSize());
        batchPreparedStatementSetter.setValues(preparedStatement, 0);

    }

    @Test
    public void submitMtrFile_notPassedValidation_shouldGetException() {
        // Given
        MemberMaster memberMaster = factory.manufacturePojo(MemberMaster.class);
        Mockito.when(memberMasterRepository.findById(memberMaster.getMemId())).thenReturn(Optional.of(memberMaster));
        MTRDailyFileValidation mtrDailyFileValidation = factory.manufacturePojo(MTRDailyFileValidation.class);
        mtrDailyFileValidation.setDmsDocIndex("./src/test/resources/mtr_27012021.t01");
        mtrDailyFileValidation.setMember(memberMaster);
        mtrDailyFileValidation.setDailyFileStatus(false);
        Mockito.when(dailyFileValidationRepository.findById(1l)).thenReturn(Optional.of(mtrDailyFileValidation));

        // When/Then
        try {
            mtrFileValidateService.submitMtrFile(memberMaster.getMemId(), 1l);
            Assert.fail();
        } catch (BaseServiceException ex) {
            Assert.assertEquals("The submitted file doesn't pass the validation.", ex.getMessage());
            Assert.assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());

        }
    }

    @Test
    public void submitMtrFile_memberIdNotMatching_shouldGetException() {
        // Given
        MemberMaster memberMaster = factory.manufacturePojo(MemberMaster.class);
        Mockito.when(memberMasterRepository.findById(memberMaster.getMemId())).thenReturn(Optional.of(memberMaster));
        MTRDailyFileValidation mtrDailyFileValidation = factory.manufacturePojo(MTRDailyFileValidation.class);
        mtrDailyFileValidation.setDmsDocIndex("./src/test/resources/mtr_27012021.t01");
        mtrDailyFileValidation.setDailyFileStatus(true);
        Mockito.when(dailyFileValidationRepository.findById(1l)).thenReturn(Optional.of(mtrDailyFileValidation));

        // When/Then
        try {
            mtrFileValidateService.submitMtrFile(memberMaster.getMemId(), 1l);
            Assert.fail();
        } catch (BaseServiceException ex) {
            Assert.assertEquals("The submitted file doesn't belong to current user.", ex.getMessage());
            Assert.assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());

        }
    }

    @Test
    public void submitMtrFile_invalidFile_shouldGetException() {
        // Given
        MemberMaster memberMaster = factory.manufacturePojo(MemberMaster.class);
        Mockito.when(memberMasterRepository.findById(memberMaster.getMemId())).thenReturn(Optional.of(memberMaster));
        MTRDailyFileValidation mtrDailyFileValidation = factory.manufacturePojo(MTRDailyFileValidation.class);
        mtrDailyFileValidation.setDmsDocIndex("INVALID_FILE");
        mtrDailyFileValidation.setDailyFileStatus(true);
        mtrDailyFileValidation.setMember(memberMaster);
        Mockito.when(dailyFileValidationRepository.findById(1l)).thenReturn(Optional.of(mtrDailyFileValidation));

        // When/Then
        try {
            mtrFileValidateService.submitMtrFile(memberMaster.getMemId(), 1l);
            Assert.fail();
        } catch (BaseServiceException ex) {
            Assert.assertEquals("The submitted file is corrupted.", ex.getMessage());
            Assert.assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());

        }
    }

    private LocalDate getReportDate(final String date) {
        try {
            return LocalDate.parse(date, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

}*/
