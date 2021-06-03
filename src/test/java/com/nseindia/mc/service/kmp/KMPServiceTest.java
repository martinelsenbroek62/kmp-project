package com.nseindia.mc.service.kmp;

import com.nseindia.mc.config.KmpConfigData;
import com.nseindia.mc.constants.ResponseMessages;
import com.nseindia.mc.constants.ServiceConstants;
import com.nseindia.mc.controller.dto.*;
import com.nseindia.mc.exception.BaseServiceException;
import com.nseindia.mc.model.*;
import com.nseindia.mc.proxy.FintechNotificationServiceProxy;
import com.nseindia.mc.proxy.VerificationServiceProxy;
import com.nseindia.mc.repository.*;
import com.nseindia.mc.service.member.MemberService;
import com.nseindia.mc.service.namematching.NameMatchingService;
import com.nseindia.mc.service.panvalidation.PanValidationService;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.matchers.CapturesArguments;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KMPServiceTest {

  @InjectMocks
  private KMPService kmpService;

  @Mock
  private KeyManagementPersonnelRepository kmpRepository;

  @Mock
  private KmpConfigData kmpConfigData;

  @Mock
  private DirectorRepository directorRepository;

  @Mock
  private ShareholderRepository shareholderRepository;

  @Mock
  PartnerRepository partnerRepository;

  @Mock
  ProprietorRepository proprietorRepository;

  @Mock
  ComplianceOfficerRepository complianceOfficerRepository;

  @Mock
  private VerificationServiceProxy verificationServiceProxy;

  @Mock
  private NameMatchingService nameMatchingService;

  @Mock
  private PanValidationService panValidationService;

  @Mock
  private MemberService memberService;

  @Mock
  private FintechNotificationServiceProxy fintechNotificationServiceProxy;

  @Mock
  private VerificationDetailsRepository verificationDetailsRepository;


  @Test
  void listKmpMembers() {
    try {
      kmpService.listKmpMembers(1L, "selectedDate", "fromDate", "toDate", null, true);
    } catch (BaseServiceException ex) {
      assertEquals("Either selectedDate or date range should be provided, not both", ex.getMessage());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }
    try {
      kmpService.listKmpMembers(1L, "selectedDate", "fromDate", null, null, true);
    } catch (BaseServiceException ex) {
      assertEquals("View Audit trail requires the provision of both from and to dates", ex.getMessage());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }

    try {
      kmpService.listKmpMembers(1L, "selectedDate", "fromDate", "toDate", "pan", true);
    } catch (BaseServiceException ex) {
      assertEquals("'pan' filter should not be used with date filters", ex.getMessage());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }

    try {
      kmpService.listKmpMembers(1L, null, null, null, "pan", true);
    } catch (BaseServiceException ex) {
      assertEquals("KMP with pan pan does not exist", ex.getMessage());
      assertEquals(HttpStatus.NOT_FOUND, ex.getHttpStatus());
    }

    KeyManagementPersonnel kmp = new KeyManagementPersonnel();
    kmp.setId(1L);
    kmp.setAction(KMPAction.ADDITION);
    kmp.setEmail("email");
    kmp.setMobileNumber("mobile");

    when(kmpRepository.findByMemIdAndPanAndDeletedDateNull(eq(1L), eq("pan"))).thenReturn(Optional.of(kmp));
    List<KeyManagementPersonnelDto> actual = kmpService.listKmpMembers(1L, null, null, null, "pan", true);
    List<KeyManagementPersonnel> kmps = Collections.singletonList(kmp);
    List<KeyManagementPersonnelDto> expected = kmps.stream()
            .map(KeyManagementPersonnelDto::new)
            .collect(Collectors.toList());
    assertEquals(expected, actual);

    try {
      kmpService.listKmpMembers(1L, LocalDate.now().plusDays(1).toString() + " 5:0 PM", null, null, null, true);
    } catch (BaseServiceException ex) {
      assertEquals("Selected Date should not be in the future", ex.getMessage());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }

    when(kmpRepository.findByMemIdAndActionDateLessThanEqualOrderByActionDateDesc(eq(1L), any())).thenReturn(kmps);
    actual = kmpService.listKmpMembers(1L, "2020-01-01 5:0 PM", null, null, null, true);
    assertEquals(expected, actual);

    try {
      kmpService.listKmpMembers(1L, null, "2020-02-01 5:0 PM", "2020-01-01 5:0 PM", null, true);
    } catch (BaseServiceException ex) {
      assertEquals("From date should be before toDate", ex.getMessage());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }

    try {
      kmpService.listKmpMembers(1L, null, "2020-01-01 5:0 PM", LocalDate.now().plusDays(1).toString() + " 5:0 PM", null, true);
    } catch (BaseServiceException ex) {
      assertEquals("fromDate and toDate should not be in the future", ex.getMessage());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }

    when(kmpRepository.findByMemIdAndActionDateBetweenOrderByActionDateDesc(eq(1L), any(), any())).thenReturn(kmps);
    actual = kmpService.listKmpMembers(1L, null, "2020-01-01 5:0 PM", "2020-01-02 5:0 PM", null, true);
    assertEquals(expected, actual);
  }

  @Test
  void getConfigData() {
    when(kmpConfigData.getGuidelines()).thenReturn("guidelines");
    when(kmpConfigData.getCorporateBankExistingRoleInfoTip()).thenReturn("infoTip");
    when(kmpConfigData.getAdditionalDesignationInfoTip()).thenReturn("additionalTip");
    when(kmpConfigData.getDateOfDeclarationInfoTip()).thenReturn("declarationTip");
    when(kmpConfigData.getEmailIdInfoTip()).thenReturn("emailInfoTip");
    when(kmpConfigData.getLlpPartnershipIndividualInfoTip()).thenReturn("partnership");
    when(kmpConfigData.getMobileNumberInfoTip()).thenReturn("mobile");
    when(kmpConfigData.getNameOfKmpInfoTip()).thenReturn("name");
    when(kmpConfigData.getExchangeText()).thenReturn("exchange");

    KmpConfigDataDto actual = kmpService.getConfigData();
    KmpConfigDataDto configData = new KmpConfigDataDto();
    configData.setGuidelines("guidelines");
    configData.setCorporateBankExistingRoleInfoTip("infoTip");
    configData.setAdditionalDesignationInfoTip("additionalTip");
    configData.setDateOfDeclarationInfoTip("declarationTip");
    configData.setEmailIdInfoTip("emailInfoTip");
    configData.setLlpPartnershipIndividualInfoTip("partnership");
    configData.setMobileNumberInfoTip("mobile");
    configData.setNameOfKmpInfoTip("name");
    configData.setExchangeText("exchange");

    assertEquals(configData, actual);
  }

  @Test
  void getMembersByRoleForDirector() {
    Director director = new Director();
    director.setId(1L);
    director.setName("name");
    director.setPan("pan");
    director.setMobileNumber("mobile");
    director.setPhoneNumber("phone");
    director.setEmail("email");
    List<Director> directors = Collections.singletonList(director);

    when(directorRepository.getNonKmpMemberDirectors(1L)).thenReturn(directors);
    List<RoleBasedKMPDataDto> actual = kmpService.getMembersByRole(1L, "Director");
    List<RoleBasedKMPDataDto> expected = directors.stream()
            .map(
                    d ->
                            new RoleBasedKMPDataDto(
                                    d.getId(),
                                    d.getName(),
                                    d.getPan(),
                                    d.getDin(),
                                    d.getMobileNumber(),
                                    d.getPhoneNumber(),
                                    d.getEmail()))
            .collect(Collectors.toList());
    assertEquals(expected, actual);
  }

  @Test
  void getMembersByRoleForShareholder() {
    Shareholder shareholder = new Shareholder();
    shareholder.setId(1L);
    shareholder.setName("name");
    shareholder.setPan("pan");
    shareholder.setMobileNumber("mobile");
    shareholder.setPhoneNumber("phone");
    shareholder.setEmail("email");
    List<Shareholder> shareholders = Collections.singletonList(shareholder);

    when(shareholderRepository.getNonKmpMemberShareholders(1L)).thenReturn(shareholders);
    List<RoleBasedKMPDataDto> actual = kmpService.getMembersByRole(1L, "Shareholder");
    List<RoleBasedKMPDataDto> expected = shareholders.stream()
            .map(
                    d ->
                            new RoleBasedKMPDataDto(
                                    d.getId(),
                                    d.getName(),
                                    d.getPan(),
                                    d.getDin(),
                                    d.getMobileNumber(),
                                    d.getPhoneNumber(),
                                    d.getEmail()))
            .collect(Collectors.toList());
    assertEquals(expected, actual);
  }

  @Test
  void getMembersByRoleForPartner() {
    Partner partner = new Partner();
    partner.setId(1L);
    partner.setName("name");
    partner.setPan("pan");
    partner.setMobileNumber("mobile");
    partner.setPhoneNumber("phone");
    partner.setEmail("email");
    List<Partner> partners = Collections.singletonList(partner);

    when(partnerRepository.getNonKmpMemberPartners(1L)).thenReturn(partners);
    List<RoleBasedKMPDataDto> actual = kmpService.getMembersByRole(1L, "Partner");
    List<RoleBasedKMPDataDto> expected = partners.stream()
            .map(
                    d ->
                            new RoleBasedKMPDataDto(
                                    d.getId(),
                                    d.getName(),
                                    d.getPan(),
                                    d.getDin(),
                                    d.getMobileNumber(),
                                    d.getPhoneNumber(),
                                    d.getEmail()))
            .collect(Collectors.toList());
    assertEquals(expected, actual);
  }

  @Test
  void getMembersByRoleForProprietor() {
    Proprietor proprietor = new Proprietor();
    proprietor.setId(1L);
    proprietor.setName("name");
    proprietor.setPan("pan");
    proprietor.setMobileNumber("mobile");
    proprietor.setPhoneNumber("phone");
    proprietor.setEmail("email");
    List<Proprietor> proprietors = Collections.singletonList(proprietor);

    when(proprietorRepository.getNonKmpMemberProprietors(1L)).thenReturn(proprietors);
    List<RoleBasedKMPDataDto> actual = kmpService.getMembersByRole(1L, "Proprietor");
    List<RoleBasedKMPDataDto> expected = proprietors.stream()
            .map(
                    d ->
                            new RoleBasedKMPDataDto(
                                    d.getId(),
                                    d.getName(),
                                    d.getPan(),
                                    d.getDin(),
                                    d.getMobileNumber(),
                                    d.getPhoneNumber(),
                                    d.getEmail()))
            .collect(Collectors.toList());
    assertEquals(expected, actual);
  }

  @Test
  void getMembersByRoleForCompliance() {
    ComplianceOfficer complianceOfficer = new ComplianceOfficer();
    complianceOfficer.setId(1L);
    complianceOfficer.setName("name");
    complianceOfficer.setPan("pan");
    complianceOfficer.setMobileNumber("mobile");
    complianceOfficer.setPhoneNumber("phone");
    complianceOfficer.setEmail("email");
    List<ComplianceOfficer> complianceOfficers = Collections.singletonList(complianceOfficer);

    when(complianceOfficerRepository.getNonKmpMemberComplianceOfficers(1L)).thenReturn(complianceOfficers);
    List<RoleBasedKMPDataDto> actual = kmpService.getMembersByRole(1L, "ComplianceOfficer");
    List<RoleBasedKMPDataDto> expected = complianceOfficers.stream()
            .map(
                    d ->
                            new RoleBasedKMPDataDto(
                                    d.getId(),
                                    d.getName(),
                                    d.getPan(),
                                    d.getDin(),
                                    d.getMobileNumber(),
                                    d.getPhoneNumber(),
                                    d.getEmail()))
            .collect(Collectors.toList());
    assertEquals(expected, actual);
  }

  @Test
  void createKMP() {
    CreateKMPRequestBody request = new CreateKMPRequestBody();
    request.setDateOfDeclaration("2020-01-01");
    kmpService.createKMP(1L, request);
    verify(kmpRepository).save(any());
  }

  @Test
  void updateKMPs() {
    UpdateKMPsRequestBody request = new UpdateKMPsRequestBody();
    request.setKmps(new ArrayList<>());
    request.getKmps().add(new UpdateKmpData());
    request.getKmps().get(0).setKmpId(1L);

    KeyManagementPersonnel kmp = new KeyManagementPersonnel();
    kmp.setFromNse(true);
    when(kmpRepository.findByIdAndMemId(eq(1L), eq(1L))).thenReturn(Optional.of(kmp));
    kmpService.updateKMPs(1L, request);
    verify(kmpRepository).save(any());

    try {
      kmpService.updateKMPs(2L, request);
    } catch (BaseServiceException ex) {
      assertEquals("KMP with id 1 does not exist for member id : 2", ex.getMessage());
      assertEquals(HttpStatus.NOT_FOUND, ex.getHttpStatus());
    }
  }

  @Test
  void validatePanInvalidPan() {
    PanValidationResponse actual = kmpService.validatePan(1L, "pan", "kmpName", false);
    PanValidationResponse expected = PanValidationResponse.builder()
            .message("The provided pan does not match the required format pattern: AAAPA1234A")
            .panStatus(PanValidationStatus.INVALID)
            .invalidityType(PanInvalidityType.DOES_NOT_MATCH_REQUIRED_FORMAT).build();
    assertEquals(expected, actual);
  }

  @Test
  void validatePanAlreadyExisting() {

    String pan = "AAAPA1234A";
    doReturn(Optional.of(new KeyManagementPersonnel())).when(kmpRepository).findByMemIdAndPan(1L, pan);
    PanValidationResponse expected = PanValidationResponse.builder()
            .message(ResponseMessages.PERSON_IS_ALREADY_OWN_KMP)
            .panStatus(PanValidationStatus.INVALID)
            .invalidityType(PanInvalidityType.PAN_HOLDER_IS_ALREADY_MEMBER_KMP).build();
    PanValidationResponse actual = kmpService.validatePan(1L, pan, "kmpName", false);
    assertEquals(expected, actual);
  }

  @Test
  void validatePanInvalidPanForCorporate() {
    String pan = "AAAPA1334A";
    MemberMaster member = new MemberMaster();
    member.setConstitutionType("Corporate");
    doReturn(member).when(memberService).getByMemberId(1L);

    doReturn(Optional.of(new Director())).when(directorRepository).findByMemberMemIdAndPan(1L, pan);
    doReturn(Optional.of(new Shareholder())).when(shareholderRepository).findByMemberIdAndPan(1L, pan);
    doReturn(Optional.of(new ComplianceOfficer())).when(complianceOfficerRepository).findByMemberMemIdAndPan(1L, pan);

    PanValidationResponse expected = PanValidationResponse.builder()
            .message("The person you are trying to assign as KMP is already your existing Director/Shareholder/Compliance Officer. Are you sure you want to continue?")
            .pan(pan)
            .role("Director")
            .panStatus(PanValidationStatus.INVALID)
            .invalidityType(PanInvalidityType.ALREADY_EXISTS_FOR_CURRENT_MEMBER).build();
    PanValidationResponse actual = kmpService.validatePan(1L, pan, "kmpName", false);
    assertEquals(expected, actual);

    doReturn(Optional.empty()).when(directorRepository).findByMemberMemIdAndPan(1L, pan);
    doReturn(Optional.empty()).when(shareholderRepository).findByMemberIdAndPan(1L, pan);
    doReturn(Optional.empty()).when(complianceOfficerRepository).findByMemberMemIdAndPan(1L, pan);
    List<KeyManagementPersonnel> otherKmps = new ArrayList<>();
    otherKmps.add(new KeyManagementPersonnel());
    otherKmps.get(0).setRole("Director");
    otherKmps.get(0).setMemId(1L);
    doReturn(otherKmps).when(kmpRepository).findByPan(pan);

    expected = PanValidationResponse.builder()
            .message("The person you are trying to assign as KMP is already a KMP in 1. Please ensure the PAN entered is correct. Are you sure you want to continue?")
            .pan(pan)
            .role("Director")
            .panStatus(PanValidationStatus.INVALID)
            .invalidityType(PanInvalidityType.ALREADY_EXISTS_FOR_OTHER_MEMBER).build();
    actual = kmpService.validatePan(1L, pan, "kmpName", false);
    assertEquals(expected, actual);
  }

  @Test
  void validatePanInvalidPanForLLP() {
    String pan = "AAAPA1334A";
    MemberMaster member = new MemberMaster();
    member.setConstitutionType("LLP");
    doReturn(member).when(memberService).getByMemberId(1L);

    doReturn(Optional.of(new Partner())).when(partnerRepository).findByMemberIdAndPan(1L, pan);
    doReturn(Optional.of(new ComplianceOfficer())).when(complianceOfficerRepository).findByMemberMemIdAndPan(1L, pan);

    PanValidationResponse expected = PanValidationResponse.builder()
            .message("The person you are trying to assign as KMP is already your existing Partner/Compliance Officer. Are you sure you want to continue?")
            .pan(pan)
            .role("Partner")
            .panStatus(PanValidationStatus.INVALID)
            .invalidityType(PanInvalidityType.ALREADY_EXISTS_FOR_CURRENT_MEMBER).build();
    PanValidationResponse actual = kmpService.validatePan(1L, pan, "kmpName", false);
    assertEquals(expected, actual);

    doReturn(Optional.empty()).when(partnerRepository).findByMemberIdAndPan(1L, pan);
    doReturn(Optional.empty()).when(complianceOfficerRepository).findByMemberMemIdAndPan(1L, pan);

    List<KeyManagementPersonnel> otherKmps = new ArrayList<>();
    otherKmps.add(new KeyManagementPersonnel());
    otherKmps.get(0).setRole("Partner");
    otherKmps.get(0).setMemId(1L);
    doReturn(otherKmps).when(kmpRepository).findByPan(pan);

    expected = PanValidationResponse.builder()
            .message("The person you are trying to assign as KMP is already a KMP in 1. Please ensure the PAN entered is correct. Are you sure you want to continue?")
            .pan(pan)
            .role("Partner")
            .panStatus(PanValidationStatus.INVALID)
            .invalidityType(PanInvalidityType.ALREADY_EXISTS_FOR_OTHER_MEMBER).build();
    actual = kmpService.validatePan(1L, pan, "kmpName", false);
    assertEquals(expected, actual);
  }

  @Test
  void validatePanInvalidPanForIndividual() {
    String pan = "AAAPA1334A";
    MemberMaster member = new MemberMaster();
    member.setConstitutionType("Individual");
    doReturn(member).when(memberService).getByMemberId(1L);

    doReturn(Optional.of(new Proprietor())).when(proprietorRepository).findByMemberIdAndPan(1L, pan);
    doReturn(Optional.of(new ComplianceOfficer())).when(complianceOfficerRepository).findByMemberMemIdAndPan(1L, pan);

    PanValidationResponse expected = PanValidationResponse.builder()
            .message("The person you are trying to assign as KMP is already your existing Proprietor/Compliance Officer. Are you sure you want to continue?")
            .pan(pan)
            .role("Proprietor")
            .panStatus(PanValidationStatus.INVALID)
            .invalidityType(PanInvalidityType.ALREADY_EXISTS_FOR_CURRENT_MEMBER).build();
    PanValidationResponse actual = kmpService.validatePan(1L, pan, "kmpName", false);
    assertEquals(expected, actual);

    doReturn(Optional.empty()).when(proprietorRepository).findByMemberIdAndPan(1L, pan);
    doReturn(Optional.empty()).when(complianceOfficerRepository).findByMemberMemIdAndPan(1L, pan);

    List<KeyManagementPersonnel> otherKmps = new ArrayList<>();
    otherKmps.add(new KeyManagementPersonnel());
    otherKmps.get(0).setRole("Proprietor");
    otherKmps.get(0).setMemId(1L);
    doReturn(otherKmps).when(kmpRepository).findByPan(pan);

    expected = PanValidationResponse.builder()
            .message("The person you are trying to assign as KMP is already a KMP in 1. Please ensure the PAN entered is correct. Are you sure you want to continue?")
            .pan(pan)
            .role("Proprietor")
            .panStatus(PanValidationStatus.INVALID)
            .invalidityType(PanInvalidityType.ALREADY_EXISTS_FOR_OTHER_MEMBER).build();
    actual = kmpService.validatePan(1L, pan, "kmpName", false);
    assertEquals(expected, actual);
  }

  @Test
  void validatePanInvalidPanForOthers() {
    MemberMaster member = new MemberMaster();
    member.setConstitutionType("");
    doReturn(member).when(memberService).getByMemberId(1L);

    String pan = "AAAPA1334A";
    PanResponse panResponse = new PanResponse();
    panResponse.setFirstName("Dave");
    panResponse.setLastName("Silman");
    PanVerificationResponse panVerificationResponse = PanVerificationResponse.builder().panResponse(panResponse).statusCode(String.format("%d", HttpStatus.OK.value())).build();

    doReturn(true).when(nameMatchingService).verifyNameMatching(eq(ServiceConstants.NAME_MATCHING_CUSTOMER_ID), eq(pan), eq("Dave Silman"), eq("kmpName"));
    doReturn(panVerificationResponse).when(panValidationService).validatePan(eq(ServiceConstants.PAN_VALIDATION_CUSTOMER_ID ), eq(pan));
    PanValidationResponse expected = PanValidationResponse.builder()
            .message(ResponseMessages.PAN_IS_VALID)
            .panStatus(PanValidationStatus.EXISTING_AND_VALID)
            .panHolderName("Dave Silman").build();
    PanValidationResponse actual = kmpService.validatePan(1L, pan, "kmpName", false);
    assertEquals(expected, actual);

    panResponse.setFirstName("Preeti");
    panResponse.setLastName("Shenoy");
    panVerificationResponse.setStatusCode("BAD_REQUEST");

    expected = PanValidationResponse.builder()
            .message(ResponseMessages.PAN_VALIDATION_FAILED)
            .panStatus(PanValidationStatus.INVALID)
            .invalidityType(PanInvalidityType.PAN_VALIDATION_FAILED).build();
    actual = kmpService.validatePan(1L, pan, "kmpName", false);
    assertEquals(expected, actual);
  }

  @Test
  void deleteKMPDetails() {
    DeleteKMPDetailsRequestBody request = new DeleteKMPDetailsRequestBody();
    request.setDeleteKMPDetails(new ArrayList<>());
    request.getDeleteKMPDetails().add(new DeleteKMPDetails());
    request.getDeleteKMPDetails().get(0).setKmpId(1L);
    request.getDeleteKMPDetails().get(0).setAppId(1L);

    KeyManagementPersonnel kmp = new KeyManagementPersonnel();
    when(kmpRepository.findByIdAndAppIdAndDeletedDateNull(eq(1L), eq(1L))).thenReturn(Optional.of(kmp));
    kmpService.deleteKMPDetails(1L, request);
    verify(kmpRepository).saveAndFlush(any());

    try {
      request.getDeleteKMPDetails().get(0).setKmpId(2L);
      kmpService.deleteKMPDetails(2L, request);
    } catch (BaseServiceException ex) {
      assertEquals("KMP with id = 2 and appId = 1 does not exist", ex.getMessage());
      assertEquals(HttpStatus.NOT_FOUND, ex.getHttpStatus());
    }
  }

  @Test
  void sendOtp() {
    when(fintechNotificationServiceProxy.sendOTPViaSms("543")).thenReturn("129765");
    kmpService.sendOtp(1L, "v", "vn", "543");
    verify(verificationDetailsRepository).saveAndFlush(any());
  }

  @Test
  void validateOtp() {
    VerificationDetails vd = new VerificationDetails();
    vd.setVerifiedFlag(false);
    vd.setName("vn");
    vd.setVerifyEntity("135791,v,543");
    when(verificationDetailsRepository.findById(1L)).thenReturn(Optional.of(vd));
    CommonMessageDto commonMessageDto = kmpService.validateOtp(1L, 1L, "v", "vn", "543", "135791");
    assertEquals(new CommonMessageDto(HttpStatus.OK.value(), "ÓTP validated successfully."), commonMessageDto);
    ArgumentCaptor<VerificationDetails> captor = ArgumentCaptor.forClass(VerificationDetails.class);
    verify(verificationDetailsRepository).saveAndFlush(captor.capture());
    assertEquals(captor.getValue().getVerifiedFlag(), true);

    CommonMessageDto failMessage = kmpService.validateOtp(1L, 1L, "v", "vn", "543", "975319");
    assertEquals(new CommonMessageDto(HttpStatus.BAD_REQUEST.value(), "ÓTP validated unsuccessfully."), failMessage);
  }
}