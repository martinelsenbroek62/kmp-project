/*

package com.nseindia.mc.service.mtrReport;

import com.nseindia.mc.controller.dto.*;
import com.nseindia.mc.exception.BaseServiceException;
import com.nseindia.mc.model.*;
import com.nseindia.mc.repository.*;
import com.nseindia.mc.service.MailService;
import com.nseindia.mc.service.penalty.PenaltyService;
import com.nseindia.mc.util.CommonUtils;
import lombok.Getter;
import lombok.Setter;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.nseindia.mc.TestConstants.MEMBER_CODE;
import static com.nseindia.mc.TestConstants.MAKER_ID;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@RunWith(PowerMockRunner.class)
@PrepareForTest({CommonUtils.class})
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
public class MtrReportServiceTest {

  @InjectMocks
  private MtrReportService mtrReportService;

  @Mock
  private MemberMasterRepository memberMasterRepository;

  @Mock private MemberListRepository memberListRepository;

  @Mock private MTRDailyFileRepository dailyFileRepository;

  @Mock
  private UserMemComRepository userMemComRepository;

  @Mock
  private MTRHoMakerRepository mtrHoMakerRepository;

  @Mock
  private PenaltyDocRepository penaltyDocRepository;

  @Mock
  private MTRControlRecordRepository controlRecordRepository;

  @Mock
  private MTRDetailRecordRepository detailRecordRepository;

  @Mock
  private MTRSummaryRecordRepository summaryRecordRepository;

  @Mock
  private MailService mailService;

  @Mock
  private PenaltyService penaltyService;

  @Mock
  private MTRMrgTradingReportRepository mrgTradingReportRepository;

  @Mock
  private PenaltyRepository penaltyRepository;


  @Test
  public void getMainRequestOptions() {
    ReflectionTestUtils.setField(mtrReportService, "submissionTypes", new ArrayList<>(){{
      add("hello");
      add("world");
    }});
    List<MainRequestOption> actual = mtrReportService.getMainRequestOptions();
    assertEquals("hello", actual.get(0).getSubmissionType());
    assertEquals("world", actual.get(1).getSubmissionType());
  }

  @Test
  public void getUploadCutOffPeriod() {
    ReflectionTestUtils.setField(mtrReportService, "uploadCutOfffStartTime", "startTime");
    ReflectionTestUtils.setField(mtrReportService, "uploadCutOffEndTime", "endTime");
    UploadCutOffPeriod actual = mtrReportService.getUploadCutOffPeriod();
    assertEquals("startTime", actual.getCutoffStartTime());
    assertEquals("endTime", actual.getCutoffEndTime());
  }

  @Test
  public void getMemberUploadStatus() {
    MemberMaster member = mockMemberMaster();
    PowerMockito.mockStatic(CommonUtils.class);
    LocalDate date = LocalDate.now();
    PowerMockito.when(CommonUtils.getLastBusinessDay(any())).thenReturn(date);

    MTRMemberList memberList = new MTRMemberList();
    memberList.setEligibleMemberMtrStatus(false);
    memberList.setEligibleMemberMtrFrom(date);
    memberList.setMemberStatus("memberStatus");
    memberList.setMember(member);
    doReturn(Optional.of(memberList)).when(memberListRepository).findTopByMember_MemId(MEMBER_CODE);

    MTRDailyFile file = new MTRDailyFile();
    file.setId(1L);
    doReturn(Optional.of(file)).when(dailyFileRepository).findTopByDailyFileStatusTrueAndMember_MemIdOrderByReportingDateDesc(MEMBER_CODE);

    MTRControlRecord controlRecord = new MTRControlRecord();
    controlRecord.setTotalAmountFunded(2.3);
    doReturn(Optional.of(controlRecord)).when(controlRecordRepository).findTopByMtrFile_IdOrderByMtrFile_ReportingDateDesc(file.getId());

    MemberUploadStatus actual = mtrReportService.getMemberUploadStatus(MEMBER_CODE);
    MemberUploadStatus expected = new MemberUploadStatus();
    expected.setApprovedStatus(member.getFullStatus());
    expected.setEligibilityFlag(memberList.getEligibleMemberMtrStatus() ? "Y" : "N");
    expected.setEligibilityDate(memberList.getEligibleMemberMtrFrom());
    expected.setApprovedDate(member.getMemApprovalDate() == null ? null : member.getMemApprovalDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    expected.setMemberStatus(memberList.getMemberStatus());
    expected.setMemberCode(memberList.getMember().getMemCd());
    expected.setMemberName(memberList.getMember().getMemName());
    expected.setMemberType(memberList.getMember().getMemType());
    expected.setLastMTRSubmittedDate(file.getDailyFileSubmissionDate());
    expected.setLastMTRReportingDate(file.getReportingDate());
    expected.setLastSubmissionIsNil(file.getNilSubmissionStatus() != null && file.getNilSubmissionStatus());
    expected.setLastTotalAmountFunded(controlRecord.getTotalAmountFunded());
    expected.setMissedDates(Collections.singletonList(date));

    assertEquals(expected, actual);
  }

  @Test
  public void getMemberUploadStatus1() {
    MemberMaster member = mockMemberMaster();
    member.setMemApprovalDate(new Date());
    PowerMockito.mockStatic(CommonUtils.class);
    LocalDate date = LocalDate.now();
    PowerMockito.when(CommonUtils.getLastBusinessDay(any())).thenReturn(date);

    MTRMemberList memberList = new MTRMemberList();
    memberList.setEligibleMemberMtrStatus(false);
    memberList.setEligibleMemberMtrFrom(date);
    memberList.setMemberStatus("memberStatus");
    memberList.setMember(member);
    doReturn(Optional.of(memberList)).when(memberListRepository).findTopByMember_MemId(MEMBER_CODE);

    MTRDailyFile file = new MTRDailyFile();
    file.setId(1L);
    file.setNilSubmissionStatus(false);
    file.setReportingDate(LocalDateTime.now());
    doReturn(Optional.of(file)).when(dailyFileRepository).findTopByDailyFileStatusTrueAndMember_MemIdOrderByReportingDateDesc(MEMBER_CODE);
    doReturn(Optional.of(file)).when(dailyFileRepository).findFirstByMember_MemIdAndDailyFileStatusOrderByReportingDateDesc(MEMBER_CODE, true);

    MTRControlRecord controlRecord = new MTRControlRecord();
    controlRecord.setTotalAmountFunded(2.3);
    doReturn(Optional.of(controlRecord)).when(controlRecordRepository).findTopByMtrFile_IdOrderByMtrFile_ReportingDateDesc(file.getId());

    List<LocalDate> missingDates = Arrays.asList(new LocalDate[] {date});
    PowerMockito.when(CommonUtils.getBusinessDaysInclusive(any(), any())).thenReturn(missingDates);

    MemberUploadStatus actual = mtrReportService.getMemberUploadStatus(MEMBER_CODE);
    MemberUploadStatus expected = new MemberUploadStatus();
    expected.setApprovedStatus(member.getFullStatus());
    expected.setEligibilityFlag(memberList.getEligibleMemberMtrStatus() ? "Y" : "N");
    expected.setEligibilityDate(memberList.getEligibleMemberMtrFrom());
    expected.setApprovedDate(member.getMemApprovalDate() == null ? null : member.getMemApprovalDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    expected.setMemberStatus(memberList.getMemberStatus());
    expected.setMemberCode(memberList.getMember().getMemCd());
    expected.setMemberName(memberList.getMember().getMemName());
    expected.setMemberType(memberList.getMember().getMemType());
    expected.setLastMTRSubmittedDate(file.getDailyFileSubmissionDate());
    expected.setLastMTRReportingDate(file.getReportingDate());
    expected.setLastSubmissionIsNil(file.getNilSubmissionStatus() != null && file.getNilSubmissionStatus());
    expected.setLastTotalAmountFunded(controlRecord.getTotalAmountFunded());
    expected.setMissedDates(Collections.singletonList(date));

    assertEquals(expected, actual);
  }

  @Test
  public void getMemberUploadStatus2() {
    MemberMaster member = mockMemberMaster();
    PowerMockito.mockStatic(CommonUtils.class);
    LocalDate date = LocalDate.now();
    PowerMockito.when(CommonUtils.getLastBusinessDay(any())).thenReturn(date);

    MTRMemberList memberList = new MTRMemberList();
    memberList.setEligibleMemberMtrStatus(false);
    memberList.setEligibleMemberMtrFrom(date);
    memberList.setMemberStatus("memberStatus");
    memberList.setMember(member);
    doReturn(Optional.of(memberList)).when(memberListRepository).findTopByMember_MemId(MEMBER_CODE);

    MTRDailyFile file = new MTRDailyFile();
    file.setId(1L);
    file.setNilSubmissionStatus(true);
    doReturn(Optional.of(file)).when(dailyFileRepository).findTopByDailyFileStatusTrueAndMember_MemIdOrderByReportingDateDesc(MEMBER_CODE);

    MTRControlRecord controlRecord = new MTRControlRecord();
    controlRecord.setTotalAmountFunded(2.3);
    doReturn(Optional.of(controlRecord)).when(controlRecordRepository).findTopByMtrFile_IdOrderByMtrFile_ReportingDateDesc(file.getId());

    MemberUploadStatus actual = mtrReportService.getMemberUploadStatus(MEMBER_CODE);
    MemberUploadStatus expected = new MemberUploadStatus();
    expected.setApprovedStatus(member.getFullStatus());
    expected.setEligibilityFlag(memberList.getEligibleMemberMtrStatus() ? "Y" : "N");
    expected.setEligibilityDate(memberList.getEligibleMemberMtrFrom());
    expected.setApprovedDate(member.getMemApprovalDate() == null ? null : member.getMemApprovalDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    expected.setMemberStatus(memberList.getMemberStatus());
    expected.setMemberCode(memberList.getMember().getMemCd());
    expected.setMemberName(memberList.getMember().getMemName());
    expected.setMemberType(memberList.getMember().getMemType());
    expected.setLastMTRSubmittedDate(file.getDailyFileSubmissionDate());
    expected.setLastMTRReportingDate(file.getReportingDate());
    expected.setLastSubmissionIsNil(file.getNilSubmissionStatus() != null && file.getNilSubmissionStatus());
    expected.setLastTotalAmountFunded(controlRecord.getTotalAmountFunded());
    expected.setMissedDates(Collections.singletonList(date));

    assertEquals(expected, actual);
  }

  @Test(expected = BaseServiceException.class)
  public void getMemberUploadStatus_fail() {
    doReturn(Optional.empty()).when(memberListRepository).findTopByMember_MemId(1L);

    mtrReportService.getMemberUploadStatus(1L);
  }

  private MemberMaster mockMemberMaster() {
    MemberMaster member = new MemberMaster();
    member.setMemCd("memCd");
    member.setMemType("memType");
    member.setMemName("memName");
    member.setMemId(MEMBER_CODE);
    doReturn(Optional.of(member)).when(memberMasterRepository).findById(MEMBER_CODE);
    return member;
  }

  @Test
  public void listMtrDailyFilesNse() {
    MemberMaster member1 = mockMemberMaster();
    LocalDate reportingDate = LocalDate.now();
    List<MTRDailyFile> mtrDailyFileList = new ArrayList<>(){{
      add(new MTRDailyFile());
      add(new MTRDailyFile());
    }};
    mtrDailyFileList.get(0).setId(1L);
    mtrDailyFileList.get(0).setDailyFileStatus(true);
    mtrDailyFileList.get(0).setMember(member1);
    MemberMaster member2 = new MemberMaster();
    member2.setMemCd("memCd");
    member2.setMemType("memType");
    member2.setMemName("memName");
    member2.setMemId(2L);
    mtrDailyFileList.get(1).setId(2L);
    mtrDailyFileList.get(1).setDailyFileStatus(true);
    mtrDailyFileList.get(1).setMember(member2);

    List<Long> memberIds = new ArrayList<>(){{
      add(member1.getMemId());
    }};
    doReturn(mtrDailyFileList).when(dailyFileRepository).findByMemberIdsAndReportingDate(eq(memberIds), eq(CommonUtils.getDatabaseDateStr(reportingDate)));

    MTRDailyFile item = mtrDailyFileList.get(0);
    MTRDailyFileDetailsDto dto1 = new MTRDailyFileDetailsDto();
    if (item.getMember() != null) {
      dto1.setMemberId(item.getMember().getMemId());
      dto1.setMemberName(item.getMember().getMemName());
      dto1.setMemberCode(item.getMember().getMemCd());
    }

    dto1.setResponseFilename(item.getResponseFileName());
    dto1.setSubmittedFilename(item.getDailyFileName());
    dto1.setFileSubmissionStatus(item.getDailyFileStatus() ? "Y" : "N");
    dto1.setSubmissionDate(item.getDailyFileSubmissionDate());
    dto1.setReferenceNumber("1");

    List<MTRDailyFileDetailsDto> actual = mtrReportService.listMtrDailyFilesNse(MEMBER_CODE, reportingDate);
    List<MTRDailyFileDetailsDto> expected = new ArrayList<>(){{
      add(dto1);
    }};
    assertEquals(expected, actual);

    List<MTRMemberList> memberList = new ArrayList<>();
    memberList.add(new MTRMemberList());
    memberList.add(new MTRMemberList());
    memberList.get(0).setMember(member1);
    memberList.get(1).setMember(member2);

    MTRDailyFile dailyFile = new MTRDailyFile();
    dailyFile.setNilSubmissionStatus(false);

    doReturn(memberList).when(memberListRepository).findByEligibleMemberMtrStatus(eq(true));

    actual = mtrReportService.listMtrDailyFilesNse(null, reportingDate);
    MTRDailyFileDetailsDto dto2 = new MTRDailyFileDetailsDto();
    dto1.setFileSubmissionStatus("N");
    dto1.setReferenceNumber("null");

    item = mtrDailyFileList.get(1);
    if (item.getMember() != null) {
      dto2.setMemberId(item.getMember().getMemId());
      dto2.setMemberName(item.getMember().getMemName());
      dto2.setMemberCode(item.getMember().getMemCd());
    }

    dto2.setResponseFilename(item.getResponseFileName());
    dto2.setSubmittedFilename(item.getDailyFileName());
    dto2.setFileSubmissionStatus("N");
    dto2.setSubmissionDate(item.getDailyFileSubmissionDate());
    dto2.setReferenceNumber("null");

    expected = new ArrayList<>(){{
      add(dto1);
      add(dto2);
    }};
    assertEquals(expected, actual);
  }

  @Test(expected = BaseServiceException.class)
  public void listMtrDailyFilesNse_fail() {
    doReturn(Optional.empty()).when(memberMasterRepository).findById(any());
    mtrReportService.listMtrDailyFilesNse(1L, LocalDate.now());
  }

  @Test
  public void listHoMakerMtr() {
    List<UserMemCom> userMemComs = new ArrayList<>();
    userMemComs.add(new UserMemCom());
    userMemComs.get(0).setId(1L);
    userMemComs.get(0).setHo("ho1");

    doReturn(userMemComs).when(userMemComRepository).findAll();
    List<HoMakerDto> actual = mtrReportService.listHoMakerMtr();
    List<HoMakerDto> expected = userMemComs.stream().map(item -> {
      HoMakerDto entity = new HoMakerDto();
      entity.setHoMakerId(item.getId());
      entity.setHoMakerName(item.getHo());
      return entity;
    }).collect(Collectors.toList());
    assertEquals(expected, actual);
  }

  @Test
  public void assignHoMaker() {
    UserMemCom maker = new UserMemCom();
    doReturn(Optional.of(maker)).when(userMemComRepository).findById(MAKER_ID);
    MTRHoMaker hoMaker = new MTRHoMaker();
    doReturn(Optional.of(hoMaker)).when(mtrHoMakerRepository).findFirstByMakerStatus(eq(true));

    mtrReportService.assignHoMaker(MAKER_ID);
    verify(mtrHoMakerRepository, times(2)).save(any(MTRHoMaker.class));
    verify(penaltyRepository, times(1)).updateUnassignedPenaltyWithMaker(MAKER_ID);
  }

  @Test(expected = BaseServiceException.class)
  public void assignHoMaker_fail() {
    doReturn(Optional.empty()).when(userMemComRepository).findById(MAKER_ID);

    mtrReportService.assignHoMaker(MAKER_ID);
  }
  @Test
  public void listMtrPenaltyFileTypes() {
    List<MTRPenaltyDocMaster> userMemComs = new ArrayList<>();
    userMemComs.add(new MTRPenaltyDocMaster());
    userMemComs.get(0).setId(1L);
    userMemComs.get(0).setPenaltyDocTypeName("type");

    doReturn(userMemComs).when(penaltyDocRepository).findAll();
    List<PenaltyDocDto> actual = mtrReportService.listMtrPenaltyFileTypes();
    List<PenaltyDocDto> expected = userMemComs.stream().map(item -> {
      PenaltyDocDto dto = new PenaltyDocDto();
      dto.setDocId(item.getId());
      dto.setDocName(item.getPenaltyDocTypeName());
      return dto;
    }).collect(Collectors.toList());
    assertEquals(expected, actual);
  }

  @Test(expected = BaseServiceException.class)
  public void listMtrDailyFilesMember_fail() {
    doReturn(Optional.empty()).when(memberListRepository).findTopByMember_MemId(MEMBER_CODE);

    mtrReportService.listMtrDailyFilesMember(MEMBER_CODE, "memberName", LocalDateTime.now().minusDays(1), LocalDateTime.now());
  }

  @Test
  public void listMtrDailyFilesMember() {
    String memberName = "memberName";
    LocalDateTime reportDateFrom = LocalDateTime.now();
    LocalDateTime reportDateTo = LocalDateTime.now();

    MemberMaster member = mockMemberMaster();

    MTRMemberList memberList = new MTRMemberList();
    memberList.setMember(member);
    memberList.setEligibleMemberMtrFrom(reportDateFrom.toLocalDate());
    doReturn(Optional.of(memberList)).when(memberListRepository).findTopByMember_MemId(MEMBER_CODE);

    MTRDailyFile dailyFile = new MTRDailyFile();
    dailyFile.setReportingDate(reportDateFrom);
    dailyFile.setDailyFileStatus(true);
    MTRDailyFile dailyFile1 = new MTRDailyFile();
    dailyFile1.setReportingDate(reportDateFrom);
    dailyFile1.setDailyFileStatus(true);
    dailyFile1.setNilSubmissionStatus(true);
    MTRDailyFile dailyFile2 = new MTRDailyFile();
    dailyFile2.setReportingDate(reportDateFrom);
    dailyFile2.setDailyFileStatus(true);
    dailyFile2.setNilSubmissionStatus(false);
    List<MTRDailyFile> dailyFiles = Arrays.asList(new MTRDailyFile[] { dailyFile, dailyFile1, dailyFile2 });
    doReturn(dailyFiles).when(dailyFileRepository).findByMember_MemIdAndReportingDateBetween(MEMBER_CODE, reportDateFrom, reportDateTo);

    List<MTRMemberDailyFileDetailsDto> actual = mtrReportService.listMtrDailyFilesMember(MEMBER_CODE, memberName, reportDateFrom, reportDateTo);
    List<MTRMemberDailyFileDetailsDto> expected = dailyFiles
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

    assertEquals(expected, actual);
  }
  @Test
  public void getMemberDetails() {
    MemberMaster member = mockMemberMaster();
    MTRMemberList memberList = new MTRMemberList();
    memberList.setMember(member);
    memberList.setEligibleMemberMtrStatus(false);
    memberList.setEligibleMemberMtrFrom(LocalDate.now());
    doReturn(Optional.of(memberList)).when(memberListRepository).findTopByMember_MemNameOrMember_MemCd(eq("member"), eq("memberCode"));

    MemberDetailsDto actual = mtrReportService.getMemberDetails(MEMBER_CODE, "member", "memberCode");
    MemberDetailsDto expected = new MemberDetailsDto();
    expected.setMemberId(memberList.getMember().getMemId());
    expected.setMemberName(memberList.getMember().getMemName());
    expected.setMtrActiveFromDate(memberList.getEligibleMemberMtrFrom());
    expected.setMtrActiveStatus(memberList.getEligibleMemberMtrStatus() ? "Active" : "Inactive");
    assertEquals(expected, actual);

    memberList.setEligibleMemberMtrStatus(true);
    actual = mtrReportService.getMemberDetails(MEMBER_CODE, "member", "memberCode");
    expected.setMtrActiveStatus("Active");
    assertEquals(expected, actual);
  }


  @Test(expected = BaseServiceException.class)
  public void getMemberDetails_fail1() {
    doReturn(Optional.empty()).when(memberListRepository).findById(MEMBER_CODE);

    mtrReportService.getMemberDetails(MEMBER_CODE, "member", "memberCode");
  }

  @Test(expected = BaseServiceException.class)
  public void getMemberDetails_fail2() {
    MemberMaster member = mockMemberMaster();
    doReturn(Optional.empty()).when(memberListRepository).findTopByMember_MemNameOrMember_MemCd(eq("member"), eq("memberCode"));

    mtrReportService.getMemberDetails(MEMBER_CODE, "member", "memberCode");
  }
  @Test
  public void newMembersOnboard() {
    ReflectionTestUtils.setField(mtrReportService, "uploadCutOffEndTime", "cutOff");
    MemberMaster member = mockMemberMaster();
    MTRMemberList memberList = new MTRMemberList();
    memberList.setMember(member);
    memberList.setEligibleMemberMtrStatus(false);
    memberList.setEligibleMemberMtrFrom(LocalDate.now());
    doReturn(Optional.of(memberList)).when(memberListRepository).findTopByMember_MemId(eq(MEMBER_CODE));

    mtrReportService.newMembersOnboard(MEMBER_CODE, true);

    verify(memberListRepository, times(1)).save(any(MTRMemberList.class));
    verify(mailService, times(1)).sendCutOffWindowsAutoEmail(
        eq(memberList.getMember().getEmail()), eq("cutOff"), eq(false));
  }

  @Test(expected = BaseServiceException.class)
  public void newMembersOnboard_fail() {
    doReturn(Optional.empty()).when(memberListRepository).findTopByMember_MemId(eq(MEMBER_CODE));

    mtrReportService.newMembersOnboard(MEMBER_CODE, true);
  }

  @Test
  public void getMtrFileByReportingDate() {
    mockMemberMaster();
    MTRDailyFile dailyFile = new MTRDailyFile();
    LocalDate reportingBusinessDate = LocalDate.now();

    doReturn(Optional.of(dailyFile)).when(dailyFileRepository).findTopByDailyFileStatusTrueAndMember_MemIdAndReportingDateBetweenOrderByDailyFileSubmissionDateDesc(eq(MEMBER_CODE), any(LocalDateTime.class), any(LocalDateTime.class));

    MTRDailyFile actual = mtrReportService.getMtrFileByReportingDate(MEMBER_CODE, reportingBusinessDate);
    assertEquals(dailyFile, actual);
  }

  @Test(expected = BaseServiceException.class)
  public void getMtrFileByReportingDate_fail1() {
    mtrReportService.getMtrFileByReportingDate(MEMBER_CODE, LocalDate.now());
  }

  @Test(expected = BaseServiceException.class)
  public void getMtrFileByReportingDate_fail2() {
    mockMemberMaster();
    doReturn(Optional.empty()).when(dailyFileRepository).findTopByDailyFileStatusTrueAndMember_MemIdAndReportingDateBetweenOrderByDailyFileSubmissionDateDesc(eq(MEMBER_CODE), any(LocalDateTime.class), any(LocalDateTime.class));
    mtrReportService.getMtrFileByReportingDate(MEMBER_CODE, LocalDate.now());
  }

  @Test
  public void getMtrFileById() {
    long fileId = 1L;
    MTRDailyFile expected = new MTRDailyFile();
    doReturn(Optional.of(expected)).when(dailyFileRepository).findById(fileId);
    MTRDailyFile actual = mtrReportService.getMtrFileById(fileId);
    assertEquals(expected, actual);
  }

  @Test(expected = BaseServiceException.class)
  public void getMtrFileById_fail() {
    long fileId = 1L;
    doReturn(Optional.empty()).when(dailyFileRepository).findById(fileId);
    mtrReportService.getMtrFileById(fileId);
  }

  @Test
  public void downloadMtrFiles() throws IOException {
    MemberMaster member = mockMemberMaster();
    MTRDailyFile dailyFile = new MTRDailyFile();
    dailyFile.setDailyFileName("dailyFile1");
    dailyFile.setResponseFileName("response");
    dailyFile.setMember(member);

    LocalDate submissionFromDate = LocalDate.now();
    LocalDate submissionToDate = LocalDate.now();

    List<MTRDailyFile> dailyFiles = new ArrayList<>() {{
      add(dailyFile);
    }};

    doReturn(dailyFiles).when(dailyFileRepository).findByDailyFileStatusTrueAndMember_MemIdAndDailyFileSubmissionDateBetween(eq(MEMBER_CODE), any(LocalDateTime.class), any(LocalDateTime.class));

    PowerMockito.mockStatic(CommonUtils.class);
    PowerMockito.when(CommonUtils.addFileToZip(any(), any(), any())).thenReturn(true);

    ByteArrayOutputStream actual = mtrReportService.downloadMtrFiles(MEMBER_CODE, submissionFromDate, submissionToDate, Arrays.asList(new String[] { "All" }));
    assertNotNull(actual);

    actual = mtrReportService.downloadMtrFiles(MEMBER_CODE, submissionFromDate, submissionToDate, Arrays.asList(new String[] { "Not existed" }));
    assertNotNull(actual);

    PowerMockito.when(CommonUtils.addFileToZip(any(), any(), any())).thenReturn(false);
    actual = mtrReportService.downloadMtrFiles(MEMBER_CODE, submissionFromDate, submissionToDate, Arrays.asList(new String[] { "Member", "Response" }));
    assertNotNull(actual);
  }

  @Test(expected = BaseServiceException.class)
  public void downloadMtrFiles_fail() throws IOException {
    LocalDate submissionFromDate = LocalDate.now();
    LocalDate submissionToDate = LocalDate.now();

    mtrReportService.downloadMtrFiles(MEMBER_CODE, submissionFromDate, submissionToDate, Arrays.asList(new String[] { "All" }));
  }

  @Test(expected = BaseServiceException.class)
  public void downloadMtrFiles_fail1() throws IOException {
    MemberMaster member = mockMemberMaster();
    MTRDailyFile dailyFile = new MTRDailyFile();
    dailyFile.setDailyFileName("dailyFile1");
    dailyFile.setResponseFileName("response");
    dailyFile.setMember(member);

    LocalDate submissionFromDate = LocalDate.now();
    LocalDate submissionToDate = LocalDate.now();

    List<MTRDailyFile> dailyFiles = new ArrayList<>() {{
      add(dailyFile);
    }};

    doReturn(dailyFiles).when(dailyFileRepository).findByDailyFileStatusTrueAndMember_MemIdAndDailyFileSubmissionDateBetween(eq(MEMBER_CODE), any(LocalDateTime.class), any(LocalDateTime.class));

    PowerMockito.mockStatic(CommonUtils.class);
    PowerMockito.when(CommonUtils.addFileToZip(any(), any(), any())).thenThrow(new IOException());

    mtrReportService.downloadMtrFiles(MEMBER_CODE, submissionFromDate, submissionToDate, Arrays.asList(new String[] { "All" }));
  }

  

  @Test
  public void downloadMtrPenaltyFiles() {
    List<MTRPenaltyDocMaster> docs = new ArrayList<>();
    docs.add(new MTRPenaltyDocMaster());
    docs.get(0).setPenaltyDocIndex("0");

    doReturn(docs).when(penaltyDocRepository).findByPenalty_PenaltyYearAndPenalty_penaltyMonthAndPenaltyDocTypeName(eq(2020), eq(5), anyString());
    ByteArrayOutputStream actual = mtrReportService.downloadMtrPenaltyFiles("2020 5", "fileType");
    assertNotNull(actual);
  }

  @Test(expected = BaseServiceException.class)
  public void downloadMtrPenaltyFiles_fail() throws IOException {
    List<MTRPenaltyDocMaster> docs = new ArrayList<>();
    docs.add(new MTRPenaltyDocMaster());
    docs.get(0).setPenaltyDocIndex("0");

    doReturn(docs).when(penaltyDocRepository).findByPenalty_PenaltyYearAndPenalty_penaltyMonthAndPenaltyDocTypeName(eq(2020), eq(5), anyString());
    PowerMockito.mockStatic(CommonUtils.class);
    PowerMockito.when(CommonUtils.addFileToZip(any(), any(), any())).thenThrow(new IOException());

    mtrReportService.downloadMtrPenaltyFiles("2020 5", "fileType");
  }

  @Test
  public void validateNilSubmission() {
    mockMemberMaster();
    MTRDailyFile dailyFile = new MTRDailyFile();
    dailyFile.setId(1L);
    doReturn(Optional.of(dailyFile)).when(dailyFileRepository).findTopByDailyFileStatusTrueAndMember_MemIdOrderByReportingDateDesc(eq(MEMBER_CODE));
    MTRControlRecord record = new MTRControlRecord();
    record.setTotalAmountFunded(24.3);
    doReturn(Optional.of(record)).when(controlRecordRepository).findTopByMtrFile_IdOrderByMtrFile_ReportingDateDesc(eq(dailyFile.getId()));

    boolean actual = mtrReportService.validateNilSubmission(MEMBER_CODE, null);
    assertFalse(actual);

    doReturn(Optional.empty()).when(dailyFileRepository).findTopByDailyFileStatusTrueAndMember_MemIdOrderByReportingDateDesc(eq(MEMBER_CODE));
    actual = mtrReportService.validateNilSubmission(MEMBER_CODE, null);
    assertTrue(actual);
  }

  @Test(expected = BaseServiceException.class)
  public void validateNilSubmission_fail() {
    mtrReportService.validateNilSubmission(MEMBER_CODE, null);
  }

  @Test
  public void submitNilSubmission() {
    Resource responseTemplate = new ClassPathResource("Nil_Submission_Response_Template.csv");
    Resource submissionTemplate = new ClassPathResource("Nil_Submission_Template.csv");

    ReflectionTestUtils.setField(mtrReportService, "responseTemplate", responseTemplate);
    ReflectionTestUtils.setField(mtrReportService, "submissionTemplate", submissionTemplate);
    LocalDate reportingBusinessDate = LocalDate.now();
    MemberMaster member = mockMemberMaster();
    MTRMemberList memberList = new MTRMemberList();
    memberList.setMember(member);
    memberList.setEligibleMemberMtrStatus(true);
    memberList.setEligibleMemberMtrFrom(LocalDate.now());
    memberList.setMemberStatus("memberStatus");

    doReturn(Optional.of(memberList)).when(memberListRepository).findTopByMember_MemId(MEMBER_CODE);

    doReturn(Optional.empty()).when(dailyFileRepository).findByMemberIdAndReportingDate(eq(MEMBER_CODE), eq(reportingBusinessDate.format(DateTimeFormatter.ISO_DATE)));
    mtrReportService.submitNilSubmission(MEMBER_CODE);

    verify(dailyFileRepository, times(1)).save(any(MTRDailyFile.class));
  }

  @Test(expected = BaseServiceException.class)
  public void submitNilSubmission_fail() {
    Resource responseTemplate = new ClassPathResource("Nil_Submission_Response_Template.csv");
    Resource submissionTemplate = new ClassPathResource("Nil_Submission_Template.csv");
    ReflectionTestUtils.setField(mtrReportService, "responseTemplate", responseTemplate);
    ReflectionTestUtils.setField(mtrReportService, "submissionTemplate", submissionTemplate);
    MemberMaster member = mockMemberMaster();
    PowerMockito.mockStatic(CommonUtils.class);
    LocalDate date = LocalDate.now();
    PowerMockito.when(CommonUtils.getLastBusinessDay(any())).thenReturn(date);

    MTRMemberList memberList = new MTRMemberList();
    memberList.setEligibleMemberMtrStatus(true);
    memberList.setEligibleMemberMtrFrom(date);
    memberList.setMemberStatus("memberStatus");
    memberList.setMember(member);
    doReturn(Optional.of(memberList)).when(memberListRepository).findTopByMember_MemId(MEMBER_CODE);

    MTRDailyFile file = new MTRDailyFile();
    file.setId(1L);
    doReturn(Optional.of(file)).when(dailyFileRepository).findTopByDailyFileStatusTrueAndMember_MemIdOrderByReportingDateDesc(MEMBER_CODE);

    MTRControlRecord controlRecord = new MTRControlRecord();
    controlRecord.setTotalAmountFunded(2.3);
    doReturn(Optional.of(controlRecord)).when(controlRecordRepository).findTopByMtrFile_IdOrderByMtrFile_ReportingDateDesc(file.getId());

    // doReturn(Optional.empty()).when(dailyFileRepository).findByMemberIdAndReportingDate(eq(MEMBER_CODE), eq(reportingBusinessDate.format(DateTimeFormatter.ISO_DATE)));
    mtrReportService.submitNilSubmission(MEMBER_CODE);
  }

  @Test(expected = BaseServiceException.class)
  public void submitNilSubmission_fail1() {
    Resource responseTemplate = new ClassPathResource("Nil_Submission_Response_Template.csv");
    Resource submissionTemplate = new ClassPathResource("Nil_Submission_Template.csv");
    ReflectionTestUtils.setField(mtrReportService, "responseTemplate", responseTemplate);
    ReflectionTestUtils.setField(mtrReportService, "submissionTemplate", submissionTemplate);
    MemberMaster member = mockMemberMaster();
    PowerMockito.mockStatic(CommonUtils.class);
    LocalDate date = LocalDate.now();
    PowerMockito.when(CommonUtils.getLastBusinessDay(any())).thenReturn(date);

    MTRMemberList memberList = new MTRMemberList();
    memberList.setEligibleMemberMtrStatus(true);
    memberList.setEligibleMemberMtrFrom(date);
    memberList.setMemberStatus("memberStatus");
    memberList.setMember(member);
    doReturn(Optional.of(memberList)).when(memberListRepository).findTopByMember_MemId(MEMBER_CODE);

    MTRDailyFile file = new MTRDailyFile();
    file.setId(1L);
    file.setNilSubmissionStatus(true);
    doReturn(Optional.of(file)).when(dailyFileRepository).findTopByDailyFileStatusTrueAndMember_MemIdOrderByReportingDateDesc(MEMBER_CODE);

    MTRControlRecord controlRecord = new MTRControlRecord();
    controlRecord.setTotalAmountFunded(0.0);
    doReturn(Optional.of(controlRecord)).when(controlRecordRepository).findTopByMtrFile_IdOrderByMtrFile_ReportingDateDesc(file.getId());

    // doReturn(Optional.empty()).when(dailyFileRepository).findByMemberIdAndReportingDate(eq(MEMBER_CODE), eq(reportingBusinessDate.format(DateTimeFormatter.ISO_DATE)));
    mtrReportService.submitNilSubmission(MEMBER_CODE);
  }

  @Test(expected = BaseServiceException.class)
  public void submitNilSubmissionInternally_fail() {
    mtrReportService.submitNilSubmissionInternally(10000L, LocalDate.now());
  }

  @Test
  public void listCumulativeDtls() {
    PowerMockito.mockStatic(CommonUtils.class);
    PowerMockito.when(CommonUtils.getFirstBusinessDayOfMonthISOStr(any())).thenReturn("date");
    LocalDate fromDate = LocalDate.now().minusDays(1);
    LocalDate toDate = LocalDate.now();

    PowerMockito.when(CommonUtils.getDatabaseDateStr(eq(fromDate))).thenReturn("fromDate");
    PowerMockito.when(CommonUtils.getDatabaseDateStr(eq(toDate))).thenReturn("toDate");

    List<CumulativeDetailsDto> dtoInterfaces = new ArrayList<>();
    CumulativeDetailsDto dtoInterface = new CumulativeDetailsDto();
    dtoInterface.setExposureLiquidatedForMonth(2.2);
    dtoInterface.setFreshExposureForMonth(2.3);
    dtoInterface.setMemberCode("memberCode");
    dtoInterface.setMemberName("memberName");
    dtoInterface.setMonth("month");
    dtoInterface.setYear("year");

    dtoInterfaces.add(dtoInterface);
    doReturn(dtoInterfaces).when(detailRecordRepository).listCumulativeDtlsMemberWise(eq("fromDate"), eq("toDate"), eq(Collections.singletonList("date")), eq(Collections.singletonList(null)));

    ListCumulativeDetailsResponse actual = mtrReportService.listCumulativeDtls(fromDate, toDate, true);

    ListCumulativeDetailsResponse response = new ListCumulativeDetailsResponse();
    response.setCumulativeMarginTradingDetailsList(new ArrayList<>());
    for (CumulativeDetailsDto in : dtoInterfaces) {
      com.nseindia.mc.controller.dto.CumulativeDetailsDto dto = new com.nseindia.mc.controller.dto.CumulativeDetailsDto();
      dto.setFreshExposureForMonth(2.3);
      dto.setExposureLiquidatedForMonth(2.2);
      dto.setMemberName(in.getMemberName());
      dto.setMemberCode(in.getMemberCode());
      dto.setMonth(in.getMonth());
      dto.setYear(in.getYear());
      dto.setNumberOfBrokers(in.getNumberOfBrokers());
      dto.setNumberOfScripts(in.getNumberOfScripts());
      dto.setTotalOutstandingForMonth(0.0D);
      dto.setNetOutstandingExposures(0.0D);
      response.getCumulativeMarginTradingDetailsList().add(dto);
    }
    response.setCumulativeSqlQuery("SELECT a.memberName as memberName, a.memberCode as memberCode, a.freshExposureForMonth, a.exposureLiquidatedForMonth, a.numberOfBrokers, a.numberOfScripts, a.year, a.month, b.totalOutstandingForMonth as totalOutstandingForMonth, c.netOutstandingExposures as netOutstandingExposures FROM (SELECT  m.MEM_NAME as memberName, m.MEM_CD as memberCode, SUM(FUNDED_AMOUNT_DURING_DAY) as freshExposureForMonth,  SUM(FUNDED_AMOUNT_LIQUIDATED_DURING_DAY) as exposureLiquidatedForMonth,  COUNT(DISTINCT m.MEM_CD) as numberOfBrokers,  COUNT(DISTINCT r.SYMBOL) as numberOfScripts,  to_char(f.REPORTING_DATE, 'YYYY') AS year, to_char(f.REPORTING_DATE, 'MM') AS month  FROM     TBL_MTR_DETAIL_RECORD r INNER JOIN TBL_MTR_DAILY_FILE f ON r.MTR_FILE_ID = f.id     INNER JOIN TBL_MEMBER_MASTER m ON m.MEM_ID = f.MEMBER_ID WHERE f.REPORTING_DATE >= TO_DATE('fromDate', 'DD-MM-YYYY' ) and f.REPORTING_DATE < TO_DATE('toDate', 'DD-MM-YYYY' )  GROUP BY to_char(f.REPORTING_DATE, 'YYYY'), to_char(f.REPORTING_DATE, 'MM'), m.MEM_NAME, m.MEM_CD) a LEFT JOIN (SELECT  m.MEM_NAME as memberName, m.MEM_CD as memberCode, SUM(FUNDED_AMOUNT_BEGIN_DAY) as totalOutstandingForMonth, SUM(FUNDED_AMOUNT_END_DAY) as netOutstandingExposures,  to_char(f.REPORTING_DATE, 'YYYY') AS year, to_char(f.REPORTING_DATE, 'MM') AS month FROM     TBL_MTR_DETAIL_RECORD r INNER JOIN TBL_MTR_DAILY_FILE f ON r.MTR_FILE_ID = f.id     INNER JOIN TBL_MEMBER_MASTER m ON m.MEM_ID = f.MEMBER_ID WHERE to_char(f.REPORTING_DATE, 'DD-MM-YYYY') IN ( 'date' ) GROUP BY to_char(f.REPORTING_DATE, 'YYYY'), to_char(f.REPORTING_DATE, 'MM'), m.MEM_NAME, m.MEM_CD) b ON a.year=b.year and a.month=b.month and a.memberName=b.memberName and a.memberCode=b.memberCode LEFT JOIN (SELECT  m.MEM_NAME as memberName, m.MEM_CD as memberCode, SUM(FUNDED_AMOUNT_BEGIN_DAY) as totalOutstandingForMonth, SUM(FUNDED_AMOUNT_END_DAY) as netOutstandingExposures,  to_char(f.REPORTING_DATE, 'YYYY') AS year, to_char(f.REPORTING_DATE, 'MM') AS month FROM     TBL_MTR_DETAIL_RECORD r INNER JOIN TBL_MTR_DAILY_FILE f ON r.MTR_FILE_ID = f.id     INNER JOIN TBL_MEMBER_MASTER m ON m.MEM_ID = f.MEMBER_ID WHERE to_char(f.REPORTING_DATE, 'DD-MM-YYYY') IN ( 'null' ) GROUP BY to_char(f.REPORTING_DATE, 'YYYY'), to_char(f.REPORTING_DATE, 'MM'), m.MEM_NAME, m.MEM_CD) c ON a.year=c.year and a.month=c.month and a.memberName=c.memberName and a.memberCode=c.memberCode");
    assertEquals(response.getCumulativeMarginTradingDetailsList(), actual.getCumulativeMarginTradingDetailsList());

    doReturn(new ArrayList<>()).when(detailRecordRepository).listCumulativeDtls(anyString(), anyString(), anyList(), anyList());
    actual = mtrReportService.listCumulativeDtls(fromDate, toDate, false);
    response.setCumulativeMarginTradingDetailsList(new ArrayList<>());
    response.setCumulativeSqlQuery("SELECT a.freshExposureForMonth, a.exposureLiquidatedForMonth, a.numberOfBrokers, a.numberOfScripts, a.year, a.month, b.totalOutstandingForMonth as totalOutstandingForMonth, c.netOutstandingExposures as outstandingExposures FROM (SELECT  SUM(FUNDED_AMOUNT_DURING_DAY) as freshExposureForMonth,  SUM(FUNDED_AMOUNT_LIQUIDATED_DURING_DAY) as exposureLiquidatedForMonth,  COUNT(DISTINCT m.MEM_CD) as numberOfBrokers,  COUNT(DISTINCT r.SYMBOL) as numberOfScripts,  to_char(f.REPORTING_DATE, 'YYYY') AS year, to_char(f.REPORTING_DATE, 'MM') AS month  FROM     TBL_MTR_DETAIL_RECORD r INNER JOIN TBL_MTR_DAILY_FILE f ON r.MTR_FILE_ID = f.id     INNER JOIN TBL_MEMBER_MASTER m ON m.MEM_ID = f.MEMBER_ID WHERE f.REPORTING_DATE >= TO_DATE('fromDate', 'DD-MM-YYYY' ) and f.REPORTING_DATE < TO_DATE('toDate', 'DD-MM-YYYY' )  GROUP BY to_char(f.REPORTING_DATE, 'YYYY'), to_char(f.REPORTING_DATE, 'MM')) a LEFT JOIN (SELECT  SUM(FUNDED_AMOUNT_BEGIN_DAY) as totalOutstandingForMonth, SUM(FUNDED_AMOUNT_END_DAY) as netOutstandingExposures,  to_char(f.REPORTING_DATE, 'YYYY') AS year, to_char(f.REPORTING_DATE, 'MM') AS month FROM     TBL_MTR_DETAIL_RECORD r INNER JOIN TBL_MTR_DAILY_FILE f ON r.MTR_FILE_ID = f.id     INNER JOIN TBL_MEMBER_MASTER m ON m.MEM_ID = f.MEMBER_ID WHERE to_char(f.REPORTING_DATE, 'DD-MM-YYYY') IN ( 'date' ) GROUP BY to_char(f.REPORTING_DATE, 'YYYY'), to_char(f.REPORTING_DATE, 'MM')) b ON a.year=b.year and a.month=b.month LEFT JOIN (SELECT  SUM(FUNDED_AMOUNT_BEGIN_DAY) as totalOutstandingForMonth, SUM(FUNDED_AMOUNT_END_DAY) as netOutstandingExposures,  to_char(f.REPORTING_DATE, 'YYYY') AS year, to_char(f.REPORTING_DATE, 'MM') AS month FROM     TBL_MTR_DETAIL_RECORD r INNER JOIN TBL_MTR_DAILY_FILE f ON r.MTR_FILE_ID = f.id     INNER JOIN TBL_MEMBER_MASTER m ON m.MEM_ID = f.MEMBER_ID WHERE to_char(f.REPORTING_DATE, 'DD-MM-YYYY') IN ( 'null' ) GROUP BY to_char(f.REPORTING_DATE, 'YYYY'), to_char(f.REPORTING_DATE, 'MM')) c ON a.year=c.year and a.month=c.month");
    assertEquals(response.getCumulativeMarginTradingDetailsList(), actual.getCumulativeMarginTradingDetailsList());
  }

  @Getter
  @Setter
  class Dto implements TotalIndeptnessDtoInterface, LenderWiseExposureDtoInterface, MaxClientAllowableExposureDtoInterface, NonSubmissionMbrDtlsDtoInterface, MaxAllowableExposureDtoInterface {
    private Long dailyFileId;
    private String memberCode;
    private String memberName;
    private LocalDateTime submissionDate;
    private Double netWorth;
    private Double totalBorrowedFunds;
    private Double totalBorrowedFundsOnPreviousDay;
    private Boolean limitExceeded;
    private Long lenderCategory;
    private String clientName;
    private String clientPan;
    private Double exposureToClient;
    private Long memberId;
    private Integer nonSubmissionCount;
    private Integer year;
    private Integer month;

    @Override
    public Double getMaxAllowableExposure() {
      return 0.0D;
    }
  }

  @Getter
  @Setter
  public class CumulativeDetailsDto implements CumulativeDetailsDtoInterface {
    String memberCode;
    String memberName;
    Double freshExposureForMonth;
    Double totalOutstandingForMonth;
    Double exposureLiquidatedForMonth;
    Double netOutstandingExposures;
    Integer numberOfBrokers;
    Integer numberOfScripts;
    String year;
    String month;
  }

  @Test
  public void leverageTotalIndeptnessReport() {
    LocalDate fromDate = LocalDate.now();
    LocalDate toDate = LocalDate.now();

    List<Dto> dtoInterfaces = new ArrayList<>();
    Dto dtoInterface = new Dto();
    dtoInterface.setDailyFileId(1L);
    dtoInterface.setMemberCode("member1");
    dtoInterface.setMemberName("member1");
    dtoInterface.setSubmissionDate(LocalDateTime.now());
    dtoInterface.setNetWorth(2.4D);
    dtoInterface.setTotalBorrowedFunds(2.4D);
    dtoInterface.setLimitExceeded(false);
    dtoInterfaces.add(dtoInterface);

    doReturn(dtoInterfaces).when(summaryRecordRepository).aggregateByReportDate(any(String.class), any(String.class));

    LeverageReportResponse actual = mtrReportService.leverageTotalIndebtednessReport(fromDate, toDate);
    LeverageReportResponse expected = new LeverageReportResponse();
    List<TotalIndebtednessDto> list = new ArrayList<>();
    TotalIndebtednessDto dto = new TotalIndebtednessDto();
    dto.setMemberCode(dtoInterface.getMemberCode());
    dto.setMemberName(dtoInterface.getMemberName());
    dto.setNetWorth(0.0);
    dto.setTotalBorrowedFunds(dtoInterface.getTotalBorrowedFunds());
    dto.setSubmissionDate(dtoInterface.getSubmissionDate());
    dto.setLimitExceeded(dto.getTotalBorrowedFunds() > dto.getNetWorth() * 5);
    list.add(dto);
    expected.setTotalIndebtedness(list);
    assertEquals(expected.getTotalIndebtedness(), actual.getTotalIndebtedness());
  }

  @Test
  public void leverageLenderWiseExposureReport() {
    LocalDate fromDate = LocalDate.now();
    LocalDate toDate = LocalDate.now();

    LocalDateTime fromDateTime = fromDate.withDayOfMonth(1).atStartOfDay();
    LocalDateTime toDateTime = toDate.withDayOfMonth(toDate.lengthOfMonth()).plusDays(1).atStartOfDay();

    List<LenderWiseExposureDtoInterface> dtoInterfaces = new ArrayList<>();
    Dto dtoInterface = new Dto();
    dtoInterfaces.add(dtoInterface);
    dtoInterface.setDailyFileId(1L);
    dtoInterface.setMemberCode("member1");
    dtoInterface.setMemberName("member1");
    dtoInterface.setSubmissionDate(LocalDateTime.now());
    dtoInterface.setNetWorth(2.4D);
    dtoInterface.setTotalBorrowedFunds(2.4D);
    dtoInterface.setLimitExceeded(false);
    dtoInterface.setLenderCategory(1L);
   doReturn(dtoInterfaces).when(summaryRecordRepository).aggregateByReportDateGroupByLenderCategory(any(String.class), any(String.class), any(String.class));

    Map<String, LenderWiseExposureDtoInterface> dtoInterfacesPreviousDayMap = new HashMap<>();
    dtoInterfacesPreviousDayMap.put("1_1", dtoInterfaces.get(0));
  //  doReturn(new ArrayList<>()).when(summaryRecordRepository).aggregateByReportDateGroupByLenderCategory(eq(fromDateTime.minusDays(1)), eq(toDateTime.minusDays(1)));

    LeverageReportResponse actual = mtrReportService.leverageLenderWiseExposureReport(fromDate, toDate);
    List<LenderWiseExposureDto> dtos = new ArrayList<>();
    LenderWiseExposureDto dto = new LenderWiseExposureDto();
    dto.setMemberCode(dtoInterface.getMemberCode());
    dto.setMemberName(dtoInterface.getMemberName());
    dto.setLenderCategory(dtoInterface.getLenderCategory());
    dto.setTotalBorrowedFunds(dtoInterface.getTotalBorrowedFunds());
    String key = String.format("%d_%d", dtoInterface.getDailyFileId(), dtoInterface.getLenderCategory());
    dto.setTotalBorrowedFundsOnPreviousDay(0.0);
    dto.setSubmissionDate(dtoInterface.getSubmissionDate());
    dtos.add(dto);
    assertEquals(dtos, actual.getLenderWiseExposure());
  }

  @Test
  public void leverageMaxAllowableExposureReport() {
    LocalDate fromDate = LocalDate.now();
    LocalDate toDate = LocalDate.now();

    List<Dto> dtoInterfaces = new ArrayList<>();
    Dto dtoInterface = new Dto();
    dtoInterface.setDailyFileId(1L);
    dtoInterface.setMemberCode("member1");
    dtoInterface.setMemberName("member1");
    dtoInterface.setSubmissionDate(LocalDateTime.now());
    dtoInterface.setNetWorth(2.4D);
    dtoInterface.setTotalBorrowedFunds(2.4D);
    dtoInterface.setLimitExceeded(false);
    dtoInterfaces.add(dtoInterface);

    doReturn(dtoInterfaces).when(detailRecordRepository).aggregateByReportDateGroupByDailyFileId(any(String.class), any(String.class));

    LeverageReportResponse actual = mtrReportService.leverageMaxAllowableExposureReport(fromDate, toDate);
    LeverageReportResponse expected = new LeverageReportResponse();
    MaxAllowableExposureDto dto = new MaxAllowableExposureDto();
    dto.setMemberCode(dtoInterface.getMemberCode());
    dto.setMemberName(dtoInterface.getMemberName());
    dto.setMaxAllowableExposure(0.0);
    dto.setNetWorth(0.0);
    dto.setTotalBorrowedFunds(dtoInterface.getTotalBorrowedFunds());
    dto.setSubmissionDate(dtoInterface.getSubmissionDate());
    dto.setLimitExceeded(false);
    expected.setMaxAllowableExposure(Arrays.asList(dto));
    assertEquals(expected.getMaxAllowableExposure(), actual.getMaxAllowableExposure());
  }

  @Test
  public void leverageMaxClientAllowableExposureReport() {
    LocalDate fromDate = LocalDate.now();
    LocalDate toDate = LocalDate.now();

    List<Dto> dtoInterfaces = new ArrayList<>();
    Dto dtoInterface = new Dto();
    dtoInterface.setDailyFileId(1L);
    dtoInterface.setMemberCode("member1");
    dtoInterface.setMemberName("member1");
    dtoInterface.setSubmissionDate(LocalDateTime.now());
    dtoInterface.setNetWorth(2.4D);
    dtoInterface.setTotalBorrowedFunds(2.4D);
    dtoInterface.setLimitExceeded(false);
    dtoInterfaces.add(dtoInterface);

    doReturn(dtoInterfaces).when(summaryRecordRepository).aggregateByReportDate(any(String.class), any(String.class));

    doReturn(dtoInterfaces).when(detailRecordRepository).aggregateByReportDateGroupByDailyFileIdAndClient(any(), any());

    LeverageReportResponse actual = mtrReportService.leverageMaxClientAllowableExposureReport(fromDate, toDate);
    List<MaxClientAllowableExposureDto> expected = new ArrayList<>();
    MaxClientAllowableExposureDto dto = new MaxClientAllowableExposureDto();
    dto.setMemberCode(dtoInterface.getMemberCode());
    dto.setMemberName(dtoInterface.getMemberName());
    dto.setNetWorth(0.0);
    dto.setExposureToClient(0.0);
    dto.setTotalBorrowedFunds(dtoInterface.getTotalBorrowedFunds());
    dto.setSubmissionDate(dtoInterface.getSubmissionDate());
    dto.setLimitExceeded(false);
    expected.add(dto);
    assertEquals(expected, actual.getMaxClientAllowableExposure());
  }

  @Test
  public void sendNotificationWhenCutOffWindowOpen() {
    String uploadCutOffEndTime = "cutOff";
    ReflectionTestUtils.setField(mtrReportService, "uploadCutOffEndTime", uploadCutOffEndTime);
    List<MTRMemberList> memberList = new ArrayList<>();
    memberList.add(new MTRMemberList());
    memberList.get(0).setMember(new MemberMaster());
    memberList.get(0).getMember().setEmail("emailAddress");

    MTRDailyFile dailyFile = new MTRDailyFile();
    dailyFile.setNilSubmissionStatus(false);

    doReturn(memberList).when(memberListRepository).findByEligibleMemberMtrStatus(eq(true));
    doReturn(Optional.of(dailyFile)).when(dailyFileRepository).findTopByDailyFileStatusTrueAndMember_MemIdOrderByReportingDateDesc(anyLong());

    mtrReportService.sendNotificationWhenCutOffWindowOpen();

    verify(mailService, times(1)).sendCutOffWindowsAutoEmail(eq(memberList.get(0).getMember().getEmail()), eq(uploadCutOffEndTime), eq(dailyFile.getNilSubmissionStatus()));
  }

  @Test
  public void sendNotificationWhenCutOffWindowOpenWhenDailyFileExists() {
    String uploadCutOffEndTime = "cutOff";
    ReflectionTestUtils.setField(mtrReportService, "uploadCutOffEndTime", uploadCutOffEndTime);
    List<MTRMemberList> memberList = new ArrayList<>();
    memberList.add(new MTRMemberList());
    memberList.get(0).setMember(new MemberMaster());
    memberList.get(0).getMember().setEmail("emailAddress");
    memberList.get(0).getMember().setMemId(1L);

    MTRDailyFile dailyFile = new MTRDailyFile();
    dailyFile.setNilSubmissionStatus(false);

    doReturn(memberList).when(memberListRepository).findByEligibleMemberMtrStatus(eq(true));
    doReturn(Optional.of(dailyFile)).when(dailyFileRepository).findTopByDailyFileStatusTrueAndMember_MemIdOrderByReportingDateDesc(anyLong());

    mtrReportService.sendNotificationWhenCutOffWindowOpen();

    verify(mailService, times(1)).sendCutOffWindowsAutoEmail(eq(memberList.get(0).getMember().getEmail()), eq(uploadCutOffEndTime), eq(dailyFile.getNilSubmissionStatus()));
  }

  @Test
  public void sendNotificationWhenFirstWeekDayOfMonth() {
    PowerMockito.mockStatic(CommonUtils.class);
    PowerMockito.when(CommonUtils.isFirstWeekDayOfMonth()).thenReturn(true);

    mtrReportService.sendNotificationWhenFirstWeekDayOfMonth();
    verify(mailService, times(1)).sendFirstWeekDayOfMonthAutoEmail();
  }

  @Test
  public void generateMarginTradingReport() throws IOException {
    ReflectionTestUtils.setField(mtrReportService, "tradingFinalReportTemplate", new ClassPathResource("MRG_Trading_Final.csv"));

    ReflectionTestUtils.setField(mtrReportService, "tradingProvisionalReportTemplate", new ClassPathResource("MRG_Trading_Provisional.csv"));

    PowerMockito.mockStatic(CommonUtils.class);
    LocalDate today = LocalDate.now();
    PowerMockito.when(CommonUtils.getLastBusinessDay(any())).thenReturn(today);

    File tmp = new File("tmp.txt");
    tmp.createNewFile();
    PowerMockito.when(CommonUtils.getFilePath(any(), any())).thenReturn("/tmp/tmp.txt");

    MTRDailyFile dailyFile = new MTRDailyFile();
    dailyFile.setDailyFileName("dailyFile1");
    dailyFile.setResponseFileName("response");
    dailyFile.setReportingDate(LocalDateTime.now());
    dailyFile.setMember(new MemberMaster());
    dailyFile.getMember().setMemId(MEMBER_CODE);

    List<MTRDailyFile> dailyFiles = new ArrayList<>() {{
      add(dailyFile);
    }};

    doReturn(dailyFiles).when(dailyFileRepository).findByDailyFileStatusTrueAndDailyFileSubmissionDateBetweenAndReportingDateBefore(any(), any(), any());

    MTRMrgTradingReport report = new MTRMrgTradingReport();
    doReturn(Optional.of(report)).when(mrgTradingReportRepository).findByReportingDate(any());

    MTRSymbolName symbol = new MTRSymbolName();
    symbol.setSymbolName("symbol name");
    symbol.setSymbolCode("symbol code");
    List<MTRDetailRecord> records = new ArrayList<>();
    records.add(new MTRDetailRecord());
    records.get(0).setFundedAmountBeginDay(2.3);
    records.get(0).setFundedAmountDuringDay(2.3);
    records.get(0).setFundedAmountLiquidatedDuringDay(2.3);
    records.get(0).setSymbol(new MTRSymbolName());
    records.get(0).setFundedQuantityEndDay(2);
    records.get(0).setFundedAmountEndDay(2.3);
    records.get(0).setSymbol(symbol);
    doReturn(records).when(detailRecordRepository).findByMtrFile_IdIn(anySet());

    mtrReportService.generateMarginTradingReport();
    verify(mailService, times(1)).sendDailySignOffEmail();
    verify(mrgTradingReportRepository, times(1)).saveAndFlush(any());

    report.setReportingDate(today);
    report.setMtrCounterLatest(1);
    report.setMtrCounterLatest(2);
    report.setMtrCounterTotal(2);
    report.setProvisionalReportDmsDocIndex("test_files" + File.separator + "MTR_Trading_Provisional.csv");
    PowerMockito.when(CommonUtils.getLastBusinessDay(any())).thenReturn(today.plusDays(2));
    mtrReportService.generateMarginTradingReport();
    verify(mrgTradingReportRepository, times(2)).saveAndFlush(any());

    report.setReportingDate(today.minusDays(3));
    doReturn(Optional.empty()).when(mrgTradingReportRepository).findByReportingDate(any());
    mtrReportService.generateMarginTradingReport();
    verify(mrgTradingReportRepository, times(2)).saveAndFlush(any());

    tmp.deleteOnExit();
  }

  @Test
  public void publishNSEWeb() {
    PowerMockito.mockStatic(CommonUtils.class);
    LocalDate date = LocalDate.now();
    PowerMockito.when(CommonUtils.getLastBusinessDay(any())).thenReturn(date);
    doReturn(true).when(mailService).sendPublishToNSEWebEmail(any());
    List<MTRMrgTradingReport> reports = new ArrayList<>();
    reports.add(new MTRMrgTradingReport());
    reports.get(0).setMrgTradingFinalStatus(true);
    reports.get(0).setFinalReportDmsDocIndex("reportIndex");
    reports.get(0).setReportingDate(date);
    reports.get(0).setMrgTradingProvisionalStatus(true);
    reports.get(0).setProvisionalReportDmsDocIndex("dmsIndex");

    doReturn(reports).when(mrgTradingReportRepository).findByMtrCounterDate(any());

    mtrReportService.publishNSEWeb();

    verify(mailService, times(2)).sendPublishToNSEWebEmail(any());
    verify(mrgTradingReportRepository, times(1)).saveAndFlush(any());
  }

  @Test
  public void getNonSubmissionMbrDtls() {
    int year = 2020;
    int month = 2;
    String memberCode = "memberCode";
    String memberName = "memberName";
    PowerMockito.mockStatic(CommonUtils.class);
    List<LocalDate> dates = new ArrayList<>();
    LocalDate today = LocalDate.now();
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    dates.add(today);
    dates.add(tomorrow);
    PowerMockito.when(CommonUtils.getBusinessDaysForYearMonth(eq(year), eq(month))).thenReturn(new ArrayList<>(){{
      add(today);
      add(tomorrow);
    }});
    PowerMockito.when(CommonUtils.getDatabaseDateStr(eq(today))).thenReturn("startDate");
    PowerMockito.when(CommonUtils.getDatabaseDateStr(eq(tomorrow))).thenReturn("endDate");

    List<Dto> dtoInterfaces = new ArrayList<>();
    Dto dtoInterface = new Dto();
    dtoInterfaces.add(dtoInterface);
    dtoInterface.setMemberId(MEMBER_CODE);
    dtoInterface.setMemberName("memberName");
    dtoInterface.setMemberCode("memberCode");
    dtoInterface.setNonSubmissionCount(1);
    dtoInterface.setYear(year);
    dtoInterface.setMonth(month);
    doReturn(dtoInterfaces).when(dailyFileRepository).getNonSubmissionMbrDtlsByMemberCodeOrMemberName(eq(year), eq(month), eq(2), eq("startDate"), eq("endDate"), eq("memberCode"), eq("memberName"));
    NonSubmissionMbrDtlsResponse actual = mtrReportService.getNonSubmissionMbrDtls(year, month, memberCode, memberName);

    NonSubmissionMbrDtlsResponse response = new NonSubmissionMbrDtlsResponse();
    List<NonSubmissionMbrDtlsDto> dtos = new ArrayList<>();
    response.setNonSubmissionMemberList(dtos);
    response.setNonSubmissionSqlQuery("select f.memberId as memberId, f.nonSubmissionCount as nonSubmissionCount,  e.MEM_CD as memberCode, e.MEM_NAME as memberName,  2020 as year, 2 as month  from (select b.MEMBER_ID as memberId,  2 - count(to_char(d.REPORTING_DATE, 'DD-MM-YYYY')) as nonSubmissionCount  from  (select a.MEMBER_ID from TBL_MTR_MEMBER_LIST a where a.ELIGIBLE_MEMBER_MTR_STATUS = 1 ) b left join  (select MEMBER_ID, REPORTING_DATE from TBL_MTR_DAILY_FILE c  where c.REPORTING_DATE >= TO_DATE('startDate', 'DD-MM-YYYY')  and c.DAILY_FILE_STATUS = 1 and c.REPORTING_DATE <= TO_DATE('endDate', 'DD-MM-YYYY')) d  on b.MEMBER_ID = d.MEMBER_ID  group by b.MEMBER_ID  ) f  inner join TBL_MEMBER_MASTER e  on f.memberId = e.MEM_ID  and (e.MEM_CD= 'memberCode' or e.MEM_NAME= 'memberName' ) ");
    NonSubmissionMbrDtlsDto dto = new NonSubmissionMbrDtlsDto();
    dto.setMemberName(dtoInterface.getMemberName());
    dto.setMemberCode(dtoInterface.getMemberCode());
    dto.setMemberId(dtoInterface.getMemberId());
    dto.setNonSubmissionCount(dtoInterface.getNonSubmissionCount());
    dto.setYear(year);
    dto.setMonth(month);
    dtos.add(dto);
    assertEquals(response, actual);

    memberCode = "";
    memberName = "";
    doReturn(dtoInterfaces).when(dailyFileRepository).getNonSubmissionMbrDtlsByMemberCodeOrMemberName(eq(year), eq(month), eq(2), eq("startDate"), eq("endDate"), eq(""), eq(""));
    actual = mtrReportService.getNonSubmissionMbrDtls(year, month, memberCode, memberName);
    response.setNonSubmissionMemberList(new ArrayList<>());
    response.setNonSubmissionSqlQuery("select f.memberId as memberId, f.nonSubmissionCount as nonSubmissionCount,  e.MEM_CD as memberCode, e.MEM_NAME as memberName,  2020 as year, 2 as month  from (select b.MEMBER_ID as memberId,  2 - count(to_char(d.REPORTING_DATE, 'DD-MM-YYYY')) as nonSubmissionCount  from  (select a.MEMBER_ID from TBL_MTR_MEMBER_LIST a where a.ELIGIBLE_MEMBER_MTR_STATUS = 1 ) b left join  (select MEMBER_ID, REPORTING_DATE from TBL_MTR_DAILY_FILE c  where c.REPORTING_DATE >= TO_DATE('startDate', 'DD-MM-YYYY')  and c.DAILY_FILE_STATUS = 1 and c.REPORTING_DATE <= TO_DATE('endDate', 'DD-MM-YYYY')) d  on b.MEMBER_ID = d.MEMBER_ID  group by b.MEMBER_ID  ) f  inner join TBL_MEMBER_MASTER e  on f.memberId = e.MEM_ID");
    assertEquals(response, actual);
  }

  @Test
  public void processMissedDates() {
    Resource responseTemplate = new ClassPathResource("Nil_Submission_Response_Template.csv");
    Resource submissionTemplate = new ClassPathResource("Nil_Submission_Template.csv");

    ReflectionTestUtils.setField(mtrReportService, "responseTemplate", responseTemplate);
    ReflectionTestUtils.setField(mtrReportService, "submissionTemplate", submissionTemplate);

    PowerMockito.mockStatic(CommonUtils.class);
    LocalDate today = LocalDate.now();
    List<LocalDate> businessDays = new ArrayList<>();
    businessDays.add(today);
    businessDays.add(today.plusDays(1));
    PowerMockito.when(CommonUtils.getBusinessDaysInclusive(any(), any())).thenReturn(businessDays);
    MemberMaster memberMaster = mockMemberMaster();
    LocalDate reportDate = LocalDate.now();
    boolean autoFillMissingRecord = true;
    MTRDailyFile dailyFile = new MTRDailyFile();
    dailyFile.setReportingDate(LocalDateTime.now());
    dailyFile.setNilSubmissionStatus(false);

    doReturn(Optional.of(dailyFile)).when(dailyFileRepository).findFirstByMember_MemIdAndDailyFileStatusOrderByReportingDateDesc(eq(memberMaster.getMemId()), eq(true));

    MTRMemberList memberList = new MTRMemberList();
    memberList.setEligibleMemberMtrFrom(today.minusDays(5));
    memberList.setMember(new MemberMaster());
    memberList.getMember().setMemId(1L);
    doReturn(Optional.of(memberList)).when(memberListRepository).findTopByMember_MemId(eq(memberMaster.getMemId()));

    List<MTRDailyFile> existedFiles = new ArrayList<>();
    MTRDailyFile existingFile = new MTRDailyFile();
    existedFiles.add(existingFile);
    existingFile.setReportingDate(LocalDateTime.now());
    doReturn(existedFiles).when(dailyFileRepository).findByMemberIdAndReportingDatesIncludingAutoGeneratedFiles(eq(memberMaster.getMemId()), anyList());

    List<LocalDate> actual = mtrReportService.processMissedDates(memberList, reportDate, autoFillMissingRecord);
    assertEquals(new ArrayList<>(){{
      add(today);
      add(today.plusDays(1));
    }}, actual);
    verify(dailyFileRepository).save(any());

    dailyFile.setNilSubmissionStatus(true);
    actual = mtrReportService.processMissedDates(memberList, reportDate, autoFillMissingRecord);
    assertEquals(new ArrayList<>(), actual);
  }

  @Test
  public void processDailyNonSubmissionRecords_nonBusinessDay() {
    PowerMockito.mockStatic(CommonUtils.class);
    PowerMockito.when(CommonUtils.isBusinessDay(any())).thenReturn(false);
    mtrReportService.processDailyNonSubmissionRecords();
  }

  @Test
  public void processDailyNonSubmissionRecords() {
    LocalDate today = LocalDate.now();
    PowerMockito.mockStatic(CommonUtils.class);
    PowerMockito.when(CommonUtils.isBusinessDay(any())).thenReturn(true);

    List<MTRMemberList> mtrMembers = new ArrayList<>();
    mtrMembers.add(new MTRMemberList());
    mtrMembers.get(0).setMember(new MemberMaster());
    mtrMembers.get(0).getMember().setMemId(1L);
    mtrMembers.get(0).setEligibleMemberMtrFrom(today.minusDays(5));
    doReturn(mtrMembers).when(memberListRepository).findByEligibleMemberMtrStatus(eq(true));

    Resource responseTemplate = new ClassPathResource("Nil_Submission_Response_Template.csv");
    Resource submissionTemplate = new ClassPathResource("Nil_Submission_Template.csv");

    ReflectionTestUtils.setField(mtrReportService, "responseTemplate", responseTemplate);
    ReflectionTestUtils.setField(mtrReportService, "submissionTemplate", submissionTemplate);

    List<LocalDate> businessDays = new ArrayList<>();
    businessDays.add(today);
    businessDays.add(today.plusDays(1));
    PowerMockito.when(CommonUtils.getBusinessDaysInclusive(any(), any())).thenReturn(businessDays);
    MemberMaster memberMaster = mockMemberMaster();
    LocalDate reportDate = LocalDate.now();
    boolean autoFillMissingRecord = true;
    MTRDailyFile dailyFile = new MTRDailyFile();
    dailyFile.setReportingDate(LocalDateTime.now());
    dailyFile.setNilSubmissionStatus(false);

    doReturn(Optional.of(dailyFile)).when(dailyFileRepository).findFirstByMember_MemIdAndDailyFileStatusOrderByReportingDateDesc(eq(memberMaster.getMemId()), eq(true));

    MTRMemberList memberList = new MTRMemberList();
    doReturn(Optional.of(memberList)).when(memberListRepository).findTopByMember_MemId(eq(memberMaster.getMemId()));

    List<MTRDailyFile> existedFiles = new ArrayList<>();
    MTRDailyFile existingFile = new MTRDailyFile();
    existedFiles.add(existingFile);
    existingFile.setReportingDate(LocalDateTime.now());
    doReturn(existedFiles).when(dailyFileRepository).findByMemberIdAndReportingDatesIncludingAutoGeneratedFiles(eq(memberMaster.getMemId()), anyList());

    mtrReportService.processDailyNonSubmissionRecords();
    verify(dailyFileRepository).save(any());
  }
}
*/
