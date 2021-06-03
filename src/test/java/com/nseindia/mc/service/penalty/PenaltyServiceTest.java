/*

package com.nseindia.mc.service.penalty;

import com.nseindia.mc.controller.dto.*;
import com.nseindia.mc.model.*;
import com.nseindia.mc.repository.*;
import com.nseindia.mc.service.MailService;
import com.nseindia.mc.util.CommonUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.nseindia.mc.TestConstants.MEMBER_CODE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PenaltyServiceTest {

  @InjectMocks
  private PenaltyService penaltyService;

  @Mock
  private MemberListRepository memberListRepository;
  @Mock private MTRDailyFileRepository dailyFileRepository;
  @Mock private PenaltyRepository penaltyRepository;
  @Mock private PenaltyMemberRepository penaltyMemberRepository;
  @Mock private PenaltyReviewRepository penaltyReviewRepository;
  @Mock private UserMemComRepository userMemComRepository;
  @Mock private MailService mailService;
  @Mock private MTRHoMakerRepository hoMakerRepository;
  @Mock private MemComApplicationRepository memComApplicationRepository;

  @Test
  void scheduledPenaltyCalculation() {
    doReturn(Optional.empty()).when(penaltyRepository).findFirstByPenaltyYearAndPenaltyMonth(anyInt(), anyInt());
    MTRHoMaker hoMaker = new MTRHoMaker();
    doReturn(Optional.of(hoMaker)).when(hoMakerRepository).findFirstByMakerStatus(eq(true));
    List<MTRMemberList> members = new ArrayList<>();
    members.add(new MTRMemberList());
    members.get(0).setMember(new MemberMaster());
    doReturn(members).when(memberListRepository).findByEligibleMemberMtrStatus(eq(true));

    penaltyService.penaltyCalculationMonthlyJob();
    verify(penaltyRepository).save(any());
  }

  @Test
  void computePenaltyMTR() {
    doReturn(Optional.empty()).when(penaltyRepository).findFirstByPenaltyYearAndPenaltyMonth(eq(2020), eq(1));
    MTRHoMaker hoMaker = new MTRHoMaker();
    doReturn(Optional.of(hoMaker)).when(hoMakerRepository).findFirstByMakerStatus(eq(true));
    List<MTRMemberList> members = new ArrayList<>();
    members.add(new MTRMemberList());
    members.get(0).setMember(new MemberMaster());
    doReturn(members).when(memberListRepository).findByEligibleMemberMtrStatus(eq(true));

    CommonMessageDto actual = penaltyService.computePenaltyMTR(2020, 1);
    assertEquals(new CommonMessageDto(
        200,
        "MTR penalty calculation successfully completed, Ok"
    ), actual);
    verify(penaltyRepository).save(any());
  }

  @Test
  void listMemberPenalty() {
    List<PenaltyMember> penaltyMembers = new ArrayList<>();
    penaltyMembers.add(new PenaltyMember());
    penaltyMembers.get(0).setMember(new MemberMaster());
    doReturn(penaltyMembers).when(penaltyMemberRepository).findByPenalty_PenaltyYearAndPenalty_PenaltyMonth(2020, 1);
    ListMemberPenaltyResponse actual = penaltyService.listMemberPenalty(2020, 1);
    List<MemberPenaltyDetailsDto> memberPenaltyDetailsList = penaltyMembers.stream()
        .map(mtrPenaltyMember -> {
          MemberPenaltyDetailsDto dto = new MemberPenaltyDetailsDto();
          dto.setMemberId(mtrPenaltyMember.getMember().getMemId());
          dto.setMemberCode(mtrPenaltyMember.getMember().getMemCd());
          dto.setMemberName(mtrPenaltyMember.getMember().getMemName());
          dto.setOriginalPenalty(mtrPenaltyMember.getOriginalPenaltyAmt());
          dto.setNonSubmissionCount(mtrPenaltyMember.getNonSubmissionCount());
          dto.setNewPenalty(mtrPenaltyMember.getNewPenaltyAmt());
          dto.setReviewStatus(mtrPenaltyMember.getReviewStatus());
          dto.setReviewReason(
              mtrPenaltyMember.getReviewReasonType() != null ?
                  mtrPenaltyMember.getReviewReasonType().name() : null);
          dto.setRevisedAmount(mtrPenaltyMember.getRevisedAmt());
          dto.setRemark(mtrPenaltyMember.getRemark());
          dto.setAgendaMinutes(mtrPenaltyMember.getAgendaMinutesFileName());
          return dto;
        }).collect(Collectors.toList());
    ListMemberPenaltyResponse expected = new ListMemberPenaltyResponse();
    expected.setMemberPenaltyDetails(memberPenaltyDetailsList);
    assertEquals(expected, actual);
  }

  @Test
  void generateApprovalNoteAnnexure() {
    ReflectionTestUtils.setField(penaltyService, "annexureTemplate", new ClassPathResource("Annexure_MTR_TEMPLATE.xlsx"));
    ReflectionTestUtils.setField(penaltyService, "approvalNoteTemplate", new ClassPathResource("Approval_Note_MTR_TEMPLATE.docx"));
    ReflectionTestUtils.setField(penaltyService, "resFileDirectory", "test_files");
    GenerateApprovalNoteAnnexureRequest request = new GenerateApprovalNoteAnnexureRequest();
    request.setSubmissionYear(2020);
    request.setSubmissionMonth(1);
    request.setMemberPenaltyDetails(new ArrayList<>());
    request.getMemberPenaltyDetails().add(new MemberPenaltyDetailsDto());
    request.getMemberPenaltyDetails().get(0).setMemberId(1L);

    List<PenaltyMember> penaltyMemberList = new ArrayList<>();
    penaltyMemberList.add(new PenaltyMember());
    penaltyMemberList.get(0).setMember(new MemberMaster());
    penaltyMemberList.get(0).getMember().setMemId(1L);
    penaltyMemberList.get(0).setOriginalPenaltyAmt(2.3);
    penaltyMemberList.get(0).setPenalty(new MTRPenalty());
    penaltyMemberList.get(0).setNonSubmissionCount(2);

    doReturn(penaltyMemberList).when(penaltyMemberRepository).findByPenaltyPenaltyYearAndPenaltyPenaltyMonth(2020, 1);
    UserMemCom maker = new UserMemCom();
    doReturn(Optional.of(maker)).when(userMemComRepository).findById(1L);

    CommonMessageDto actual = penaltyService.generateApprovalNoteAnnexure(request, 1L);
    assertEquals(new CommonMessageDto(
        200,
        "Approval Note and Annexure files have been generated successfully"
    ), actual);
    verify(penaltyRepository, times(2)).save(any());
  }

  @Test
  void downloadMemberNonSubmissionFile() {
    PenaltyMember penaltyMember = new PenaltyMember();
    penaltyMember.setMember(new MemberMaster());
    penaltyMember.getMember().setMemId(1L);
    doReturn(Optional.of(penaltyMember)).when(penaltyMemberRepository).findByMember_MemIdAndPenalty_PenaltyYearAndPenalty_PenaltyMonth(eq(MEMBER_CODE), eq(2020), eq(1));
    NonSubmissionCountRequest request = new NonSubmissionCountRequest();
    request.setMemberId(MEMBER_CODE);
    request.setSubmissionYear(2020);
    request.setSubmissionMonth(1);
    NonSubmissionCountResponse actual = penaltyService.downloadMemberNonSubmissionFile(request);
    assertEquals(200, actual.getStatus());
  }

  @Test
  void getReviewCommentsMTR() {

  }

  @Test
  void postReviewCommentsMTR() {
  }

  @Test
  void getWithdrawalMbrDtls() {
  }

  @Test
  void calculateNonSubmissionDates() {
    List<MTRDailyFile> submissions = new ArrayList<>();
    LocalDateTime date = LocalDateTime.now();
    submissions.add(new MTRDailyFile());
    submissions.get(0).setMember(new MemberMaster());
    submissions.get(0).getMember().setMemId(1L);
    submissions.get(0).setReportingDate(date);

    doReturn(submissions).when(dailyFileRepository).findByDailyFileStatusTrueAndReportingDateBetween(any(), any());

    Map<MemberMaster, List<LocalDate>> actual = penaltyService.calculateNonSubmissionDates(new ArrayList<>(){{
      add(LocalDate.now());
    }});
    assertEquals(new ArrayList<>(), actual.get(submissions.get(0).getMember()));
  }

  @Test
  void testCalculateNonSubmissionDates() {
  }

  @Test
  void testCalculateNonSubmissionDates1() {
  }

  @Test
  void postPenaltyApproval() {
    PenaltyApprovalRequest request = new PenaltyApprovalRequest();
    long nseOfficialId = 1;
    request.setSubmissionMonth(1);
    request.setSubmissionYear(2020);

    MTRPenalty penalty = new MTRPenalty();
    penalty.setChecker(new UserMemCom());
    penalty.getChecker().setId(nseOfficialId);
    doReturn(Optional.of(penalty)).when(penaltyRepository).findFirstByPenaltyYearAndPenaltyMonth(eq(request.getSubmissionYear()), eq(request.getSubmissionMonth()));
    MTRPenaltyReview review = new MTRPenaltyReview();
    doReturn(Optional.of(review)).when(penaltyReviewRepository).findFirstByPenalty_PenaltyYearAndPenalty_PenaltyMonth(eq(request.getSubmissionYear()), eq(request.getSubmissionMonth()));

    List<PenaltyMember> penaltyMemberList = new ArrayList<>();
    penaltyMemberList.add(new PenaltyMember());
    doReturn(penaltyMemberList).when(penaltyMemberRepository).findByPenaltyPenaltyYearAndPenaltyPenaltyMonth(eq(request.getSubmissionYear()), eq(request.getSubmissionMonth()));

    CommonMessageDto actual = penaltyService.postPenaltyApproval(request, nseOfficialId);
    assertEquals(new CommonMessageDto(200, "MTR penalty calculation successfully completed, Ok"), actual);
  }

  @Test
  void putPenaltyDispute() {
    ReflectionTestUtils.setField(penaltyService, "annexureTemplate", new ClassPathResource("Annexure_MTR_TEMPLATE.xlsx"));
    ReflectionTestUtils.setField(penaltyService, "approvalNoteTemplate", new ClassPathResource("Approval_Note_MTR_TEMPLATE.docx"));
    ReflectionTestUtils.setField(penaltyService, "resFileDirectory", "test_files");

    PenaltyDisputeRequest request = new PenaltyDisputeRequest();
    long nseOfficialId = 1L;
    request.setSubmissionMonth(1);
    request.setSubmissionYear(2020);
    request.setMemberPenaltyDetails(new ArrayList<>());
    request.getMemberPenaltyDetails().add(new MemberPenaltyDetailsDto());
    request.getMemberPenaltyDetails().get(0).setMemberId(1L);

    MTRPenalty penalty = new MTRPenalty();
    penalty.setChecker(new UserMemCom());
    penalty.getChecker().setId(nseOfficialId);
    doReturn(Optional.of(penalty)).when(penaltyRepository).findFirstByPenaltyYearAndPenaltyMonth(eq(request.getSubmissionYear()), eq(request.getSubmissionMonth()));

    List<PenaltyMember> penaltyMemberList = new ArrayList<>();
    penaltyMemberList.add(new PenaltyMember());
    penaltyMemberList.get(0).setMember(new MemberMaster());
    penaltyMemberList.get(0).getMember().setMemId(1L);
    penaltyMemberList.get(0).setOriginalPenaltyAmt(2.3);
    penaltyMemberList.get(0).setPenalty(penalty);
    penaltyMemberList.get(0).setNonSubmissionCount(1);

    doReturn(penaltyMemberList).when(penaltyMemberRepository).findByPenaltyPenaltyYearAndPenaltyPenaltyMonth(eq(request.getSubmissionYear()), eq(request.getSubmissionMonth()));

    CommonMessageDto actual = penaltyService.putPenaltyDispute(request, nseOfficialId);
    assertEquals(new CommonMessageDto(
        200,
        "Approval Note and Annexure files have been generated successfully"
    ), actual);
  }

  @Test
  void getPenaltyLetterDetails() {
    List<PenaltyMember> members = new ArrayList<>();
    members.add(new PenaltyMember());
    members.get(0).setMember(new MemberMaster());
    members.get(0).getMember().setMemId(1L);
    members.get(0).setGenerateLetterStatus(PenaltyLetterStatus.NOT_SENT);
    doReturn(members).when(penaltyMemberRepository).findByPenaltyPenaltyYearAndPenaltyPenaltyMonth(eq(2020), eq(1));

    PenaltyLetterDetailsResponse actual = penaltyService.getPenaltyLetterDetails(2020, 1, "PENALTY_LETTER");
    PenaltyLetterDetailsDto dto = new PenaltyLetterDetailsDto();
    PenaltyLetterDetailsResponse expected = new PenaltyLetterDetailsResponse();
    dto.setMemberId(members.get(0).getMember().getMemId());
    dto.setMemberCode(members.get(0).getMember().getMemCd());
    dto.setMemberName(members.get(0).getMember().getMemName());
    dto.setPenaltyAmount(null);
    dto.setPenaltyLetterStatus("NOT_SENT");
    dto.setPenaltySubmissionCycle(CommonUtils.getAbbrMonthAndYear(2020, 1));
    dto.setPenaltyLetterType(PenaltyLetterType.fromName("PENALTY_LETTER").name());
    expected.setPenaltyLetterDetails(Collections.singletonList(dto));
    assertEquals(expected, actual);

    actual = penaltyService.getPenaltyLetterDetails(2020, 1, "PENALTY_REVERSAL_LETTER");
    dto.setPenaltyLetterStatus("NOT_GENERATED");
    dto.setPenaltyLetterType(PenaltyLetterType.fromName("PENALTY_REVERSAL_LETTER").name());
    assertEquals(expected, actual);
  }

  @Test
  void generatePenaltyLetters() {
    ReflectionTestUtils.setField(penaltyService, "penaltyLetterTemplate", new ClassPathResource("Penalty_letter_template.docx"));
    ReflectionTestUtils.setField(penaltyService, "penaltyReversalLetterTemplate", new ClassPathResource("Penalty_Reversal_letter_template.docx"));
    ReflectionTestUtils.setField(penaltyService, "tollFreePhoneNumber", "888");
    ReflectionTestUtils.setField(penaltyService, "resFileDirectory", "test_files");

    PenaltyLettersRequest request = new PenaltyLettersRequest();
    request.setSubmissionYear(2020);
    request.setSubmissionMonth(1);
    request.setMemberPenaltyDetails(new ArrayList<>());
    request.getMemberPenaltyDetails().add(new PenaltyLetterDetailsDto());
    request.getMemberPenaltyDetails().get(0).setMemberId(1L);
    request.getMemberPenaltyDetails().get(0).setMemberName("member");
    request.getMemberPenaltyDetails().get(0).setPenaltyLetterType(PenaltyLetterType.PENALTY_LETTER.name());

    List<PenaltyMember> penaltyMembers = new ArrayList<>();
    penaltyMembers.add(new PenaltyMember());
    penaltyMembers.get(0).setMember(new MemberMaster());
    penaltyMembers.get(0).getMember().setMemId(1L);
    penaltyMembers.get(0).getMember().setMemName("member");
    penaltyMembers.get(0).setPenalty(new MTRPenalty());
    doReturn(penaltyMembers).when(penaltyMemberRepository).findByPenaltyPenaltyYearAndPenaltyPenaltyMonth(2020, 1);

    PenaltyLetterDetailsResponse actual = penaltyService.generatePenaltyLetters(request, 0);
    assertNotNull(actual);

    request.getMemberPenaltyDetails().get(0).setPenaltyLetterType(PenaltyLetterType.PENALTY_REVERSAL_LETTER.name());
    actual = penaltyService.generatePenaltyLetters(request, 0);
    assertNotNull(actual);
  }

  @Test
  void downloadInspectionPenaltyFile() {
  }

  @Test
  void sendPenaltyLetterEmail() {
  }

  @Test
  void downloadMtrPenaltyFiles() {
  }

  @Test
  void downloadPenaltyLetters() {
  }
}
*/
