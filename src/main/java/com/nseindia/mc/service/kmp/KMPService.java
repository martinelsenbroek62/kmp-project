package com.nseindia.mc.service.kmp;

import com.nseindia.mc.config.KmpConfigData;
import com.nseindia.mc.constants.ResponseMessages;
import com.nseindia.mc.constants.ServiceConstants;
import com.nseindia.mc.controller.dto.AddKMPDetails;
import com.nseindia.mc.controller.dto.CommonMessageDto;
import com.nseindia.mc.controller.dto.CreateKMPRequestBody;
import com.nseindia.mc.controller.dto.DeleteKMPDetails;
import com.nseindia.mc.controller.dto.DeleteKMPDetailsRequestBody;
import com.nseindia.mc.controller.dto.EditKMPDetails;
import com.nseindia.mc.controller.dto.KMPApplicationsDetailDto;
import com.nseindia.mc.controller.dto.KeyManagementPersonnelDto;
import com.nseindia.mc.controller.dto.KmpConfigDataDto;
import com.nseindia.mc.controller.dto.OtpSendResponse;
import com.nseindia.mc.controller.dto.PanDataRequest;
import com.nseindia.mc.controller.dto.PanInvalidityType;
import com.nseindia.mc.controller.dto.PanPayload;
import com.nseindia.mc.controller.dto.PanValidationResponse;
import com.nseindia.mc.controller.dto.PanValidationStatus;
import com.nseindia.mc.controller.dto.PanVerificationResponse;
import com.nseindia.mc.controller.dto.RoleBasedKMPDataDto;
import com.nseindia.mc.controller.dto.UpdateKMPsRequestBody;
import com.nseindia.mc.controller.dto.UpdateKmpData;
import com.nseindia.mc.exception.BaseServiceException;
import com.nseindia.mc.model.ComplianceOfficer;
import com.nseindia.mc.model.Director;
import com.nseindia.mc.model.KMPAction;
import com.nseindia.mc.model.KMPApplicationDetails;
import com.nseindia.mc.model.KeyManagementPersonnel;
import com.nseindia.mc.model.MemberAimlDtl;
import com.nseindia.mc.model.MemberMaster;
import com.nseindia.mc.model.Partner;
import com.nseindia.mc.model.Proprietor;
import com.nseindia.mc.model.Shareholder;
import com.nseindia.mc.model.VerificationDetails;
import com.nseindia.mc.proxy.FintechNotificationServiceProxy;
import com.nseindia.mc.proxy.VerificationServiceProxy;
import com.nseindia.mc.repository.ComplianceOfficerRepository;
import com.nseindia.mc.repository.DirectorRepository;
import com.nseindia.mc.repository.KMPApplicationDetailsRepository;
import com.nseindia.mc.repository.KeyManagementPersonnelRepository;
import com.nseindia.mc.repository.PartnerRepository;
import com.nseindia.mc.repository.ProprietorRepository;
import com.nseindia.mc.repository.ShareholderRepository;
import com.nseindia.mc.repository.VerificationDetailsRepository;
import com.nseindia.mc.service.member.MemberService;
import com.nseindia.mc.service.namematching.NameMatchingService;
import com.nseindia.mc.service.panvalidation.PanValidationService;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.criteria.Predicate;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import static com.google.common.collect.Lists.newArrayList;
import static com.nseindia.mc.constants.ServiceConstants.LIST_AUDIT_KMP_ACTION;
import static java.lang.String.join;

/** This service defines methods for KMP management */
@Service
public class KMPService {

  /** The director role name */
  private static final String DIRECTOR = "Director";

  /** The shareholder role name */
  private static final String SHAREHOLDER = "Shareholder";

  /** The partner role name */
  private static final String PARTNER = "Partner";

  /** The proprietor role name */
  private static final String PROPRIETOR = "Proprietor";

  /** The compliance officer role name */
  private static final String COMPLIANCE_OFFICER = "ComplianceOfficer";

  /**
   * The Regular expression to use to validate the PAN number: The PAN has to be of valid
   * 10-character format (AAAPA1234A): 1. First 5 characters alphabets 2. Next 4 characters numbers
   * 3. Last character alphabet 4. 4th character has to be 'P'
   */
  private static final String PAN_VALIDATION_REGEX = "^[A-Z]{3}[P]{1}[A-Z]{1}[0-9]{4}[A-Z]{1}";

  /** The message to return when PAN holder is already a KMP of the current member */
  private static final String PAN_EXISTS_FOR_MEMBER_MESSAGE =
      "The person you are trying to assign as KMP is already your existing %s. Are you sure you want to continue?";

  /** The message to return when the PAN holder is a KMP of another member */
  private static final String PAN_EXISTS_FOR_OTHER_MEMBER_MESSAGE =
      "The person you are trying to assign as KMP is already a KMP in %d. Please ensure the PAN entered is correct. Are you sure you want to continue?";

  /** The formatter to use for formatting the date time string value */
  DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd h:m a").withLocale(Locale.US);

  /** The formatter to use for formattin the date string values */
  DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /** The KMP repository to use for managing KMPs in the persistence */
  @Autowired private KeyManagementPersonnelRepository kmpRepository;

  /** The KMP static configuration data */
  @Autowired private KmpConfigData kmpConfigData;

  /** The repository to use for managing director instances in the persistence */
  @Autowired private DirectorRepository directorRepository;

  /**
   * Shareholder repository
   */
  @Autowired
  private ShareholderRepository shareholderRepository;

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
   * The Verification details repository
   */
  @Autowired
  VerificationDetailsRepository verificationDetailsRepository;
  
  /**
   * The KmpApplicationDetails details repository
   */
  @Autowired
  KMPApplicationDetailsRepository kmpApplicationDetailsRepository;

  /**
   * The service to use to perform name matching
   */
  @Autowired
  private NameMatchingService nameMatchingService;

  /**
   * The service to use to perform pan validation
   */
  @Autowired
  private PanValidationService panValidationService;

  /**
   * The service to use to access member data
   */
  @Autowired
  private MemberService memberService;

  /**
   * The service to send opt to member
   */
  @Autowired
  private FintechNotificationServiceProxy fintechNotificationServiceProxy;

  /**
   * Searches KMPs based on the specified search criteria Can search all KMPs without filters Or
   * search KMPs as on selected date Or search KMPs audit trail with date range
   *
   * @param tradingMemberId The trading member id
   * @param selectedDateString The string representation of the selected date
   * @param fromDateString The date lower bound to search audit trail from
   * @param toDateString The date upper bound to search audit trail to
   * @param pan the pan by which to filter the KMP members
   * @param includeDeleted The flag indicating whether to include deleted KMPs in the response
   * @return The List of KMPs matching the search criteria
   */
  public List<KeyManagementPersonnelDto> listKmpMembers(
      Long tradingMemberId, String selectedDateString, String fromDateString, String toDateString, String pan, Boolean includeDeleted) {

    // Validate the filter parameters
    // Either the pan, selectedDate or the date range should be provided
    // If none of pan, selectedDate nor the date range (fromDate and toDate) are provided, then no
    // filtering is applied
    if(pan == null) { // pan filter is not provided, we validate the date filters
      if (selectedDateString != null && fromDateString != null && toDateString != null) {
        throw new BaseServiceException(
            "Either selectedDate or date range should be provided, not both", HttpStatus.BAD_REQUEST);
      } else if (fromDateString != null && toDateString == null
          || (fromDateString == null && toDateString != null)) {
        // Filter by range requires the provision of both from and to dates
        throw new BaseServiceException(
            "View Audit trail requires the provision of both from and to dates",
            HttpStatus.BAD_REQUEST);
      }
    } else { // The pan filter is provided, then no date filter should be provided
      if(selectedDateString != null || fromDateString != null || toDateString != null) {
        throw new BaseServiceException("'pan' filter should not be used with date filters", HttpStatus.BAD_REQUEST);
      }
    }

    // Initialize the kmps result list
    List<KeyManagementPersonnel> kmps;

    if(pan == null) {
      if (selectedDateString != null) {
        LocalDateTime selectedDate = LocalDateTime.parse(selectedDateString, dateTimeFormatter);
        if (selectedDate.isAfter(LocalDateTime.now())) {
          // selectedDate should not be in the future
          throw new BaseServiceException(
              "Selected Date should not be in the future", HttpStatus.BAD_REQUEST);
        } else {
          // Get the KMPs as on selectedDate
          if(includeDeleted) {
            kmps = kmpRepository.findByMemIdAndActionDateLessThanEqualOrderByActionDateDesc(
              tradingMemberId, selectedDate);
          } else {
            kmps = kmpRepository.findByMemIdAndActionDateLessThanEqualAndDeletedDateNullOrderByActionDateDesc(
              tradingMemberId, selectedDate);
          }
        }
      } else if (fromDateString != null && toDateString != null) {
        LocalDateTime fromDate = LocalDateTime.parse(fromDateString, dateTimeFormatter);
        LocalDateTime toDate = LocalDateTime.parse(toDateString, dateTimeFormatter);
        if (fromDate.isAfter(toDate)) {
          throw new BaseServiceException("From date should be before toDate", HttpStatus.BAD_REQUEST);
        } else if (toDate.isAfter(LocalDateTime.now())) {
          throw new BaseServiceException(
              "fromDate and toDate should not be in the future", HttpStatus.BAD_REQUEST);
        } else {
          if(includeDeleted) {
            kmps =
              kmpRepository.findByMemIdAndActionDateBetweenOrderByActionDateDesc(
                  tradingMemberId,
                  LocalDateTime.parse(fromDateString, dateTimeFormatter),
                  LocalDateTime.parse(toDateString, dateTimeFormatter));
          } else {
            kmps =
              kmpRepository.findByMemIdAndDeletedDateNullAndActionDateBetweenOrderByActionDateDesc(
                  tradingMemberId,
                  LocalDateTime.parse(fromDateString, dateTimeFormatter),
                  LocalDateTime.parse(toDateString, dateTimeFormatter));
          }
        }
      } else {
        // no filtering is applied, we get all KMPs for the member id
        if(includeDeleted) {
          kmps = kmpRepository.findByMemIdOrderByActionDateDesc(tradingMemberId);
        } else {
          kmps = kmpRepository.findByMemIdAndDeletedDateNullOrderByActionDateDesc(tradingMemberId);
        }
      }
    } else {
        kmps = Arrays.asList(kmpRepository
                           .findByMemIdAndPanAndDeletedDateNull(tradingMemberId, pan)
                           .orElseThrow(() -> new BaseServiceException(String.format("KMP with pan %s does not exist", pan) , HttpStatus.NOT_FOUND)));
    }

    return kmps.stream()
        .map(kmp -> new KeyManagementPersonnelDto(kmp))
        .collect(Collectors.toList());
  }

  /**
   * Searches KMPs based on the specified search criteria Can search all KMPs without filters Or
   * search KMPs as on selected date Or search KMPs audit trail with date range
   *
   * @param tradingMemberId The trading member id
   * @param selectedDateString The string representation of the selected date
   * @param fromDateString The date lower bound to search audit trail from
   * @param toDateString The date upper bound to search audit trail to
   * @param action The action to search
   * @param memIds The particular members id
   * @return The List of KMPs matching the search criteria
   */
  public String listKmpMembers(Long tradingMemberId, String selectedDateString, String fromDateString, String toDateString, String action, String memIds) {
    List<KeyManagementPersonnel> kmps = newArrayList();
    if (action.equals(LIST_AUDIT_KMP_ACTION)) {
      if (toDateString == null || fromDateString == null) {
        throw new BaseServiceException("View Audit trail requires the provision of both from and to dates", HttpStatus.BAD_REQUEST);
      }
      kmps.addAll(kmpRepository.findAll(getExample(null, null, memIds, fromDateString, toDateString),
              Sort.by(Sort.Direction.DESC, "actionDate")));
    } else {
        if (selectedDateString == null) {
          throw new BaseServiceException("View KMP list requires the provision of selectedDate", HttpStatus.BAD_REQUEST);
        }
      kmps.addAll(kmpRepository.findAll(getExample(memIds, selectedDateString, null, null, null),
              Sort.by(Sort.Direction.DESC, "actionDate")));
    }
    String header = "kmpId,action,actionDate,appId,role,addlDesignation,salutation,name,pan,panStatus,din,mobileNumber,phoneNumber,email,fromNse,declarationDate,submissionDate,lastUpdateDate,panDocFileName";
    String body = kmps.stream()
            .map(kmp -> join(",",
                    kmp.getId().toString(),
                    kmp.getAction().value,
                    kmp.getActionDate().format(dateTimeFormatter),
                    kmp.getAppId().toString(),
                    kmp.getRole(),
                    kmp.getAdditionalDesignation(),
                    kmp.getSalutation(),
                    kmp.getName(),
                    kmp.getPan(),
                    kmp.getPanStatus(),
                    kmp.getDin().toString(),
                    kmp.getMobileNumber(),
                    kmp.getPhoneNumber(),
                    kmp.getEmail(),
                    kmp.getFromNse().toString(),
                    kmp.getDeclarationDate().format(dateTimeFormatter),
                    kmp.getSubmissionDate().format(dateTimeFormatter),
                    kmp.getUpdatedDate().format(dateTimeFormatter),
                    kmp.getPanDocFileName()))
            .collect(Collectors.joining("\n"));
    return join("\n", header, body);
  }

  /**
   * Generate the Specification.
   *
   * @param memberIds The particular members id
   * @param selectedDateString The string representation of the selected date
   * @param auditIds The audits id to search
   * @param fromDateString The date lower bound to search audit trail from
   * @param toDateString The date upper bound to search audit trail to
   * @return The Specification
   */
  private Specification<KeyManagementPersonnel> getExample(final String memberIds,
                                                           final String selectedDateString,
                                                           final String auditIds,
                                                           final String fromDateString,
                                                           final String toDateString) {
    return (root, query, builder) -> {
      Predicate result = builder.isNotNull(root.get("id"));
      if (StringUtils.isNotEmpty(memberIds)) {
        result = builder.and(result, root.get("memId").in(memberIds.split(",")));
      }
      if (StringUtils.isNotEmpty(selectedDateString)) {
        LocalDate ld = LocalDateTime.parse(selectedDateString, dateTimeFormatter).toLocalDate();
        result = builder.and(result, builder.between(root.get("actionDate"), ld.atStartOfDay(), ld.plusDays(1).atStartOfDay()));
      }
      if (StringUtils.isNotEmpty(fromDateString)) {
        result = builder.and(result, builder.between(root.get("updatedDate"), LocalDateTime.parse(fromDateString, dateTimeFormatter), LocalDateTime.parse(toDateString, dateTimeFormatter)));
      }
      if (StringUtils.isNotEmpty(auditIds)) {
        result = builder.and(result, root.get("updatedBy").in(auditIds.split(",")));
      }
      return result;
    };
  }

  /**
   * Get the static configuration data for the KMP module
   *
   * @return The configuration data DTO
   */
  public KmpConfigDataDto getConfigData() {
    KmpConfigDataDto configData = new KmpConfigDataDto();

    configData.setGuidelines(kmpConfigData.getGuidelines());
    configData.setCorporateBankExistingRoleInfoTip(
        kmpConfigData.getCorporateBankExistingRoleInfoTip());
    configData.setAdditionalDesignationInfoTip(kmpConfigData.getAdditionalDesignationInfoTip());
    configData.setDateOfDeclarationInfoTip(kmpConfigData.getDateOfDeclarationInfoTip());
    configData.setEmailIdInfoTip(kmpConfigData.getEmailIdInfoTip());
    configData.setLlpPartnershipIndividualInfoTip(
        kmpConfigData.getLlpPartnershipIndividualInfoTip());
    configData.setMobileNumberInfoTip(kmpConfigData.getMobileNumberInfoTip());
    configData.setNameOfKmpInfoTip(kmpConfigData.getNameOfKmpInfoTip());
    configData.setExchangeText(kmpConfigData.getExchangeText());
    return configData;
  }

  /**
   * Gets the members by the specified role (potential KMPs)
   *
   * @param tradingMemberId The trading member id
   * @param role The role by which to search
   * @return The List of KMP data macthing the search criteria
   */
  public List<RoleBasedKMPDataDto> getMembersByRole(Long tradingMemberId, String role) {
    List<RoleBasedKMPDataDto> result = new ArrayList<RoleBasedKMPDataDto>();

    switch (role) {
      case DIRECTOR:
        {
          List<Director> directors = directorRepository.getNonKmpMemberDirectors(tradingMemberId);
          result =
              directors.stream()
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
          break;
        }
      case SHAREHOLDER: {
        List<Shareholder> shareholders = shareholderRepository.getNonKmpMemberShareholders(tradingMemberId);
        result =
        shareholders.stream()
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
          break;
      }

      case PARTNER: {
        List<Partner> partners = partnerRepository.getNonKmpMemberPartners(tradingMemberId);
        result =
        partners.stream()
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
          break;
      }
      case PROPRIETOR: {
        List<Proprietor> proprietors = proprietorRepository.getNonKmpMemberProprietors(tradingMemberId);
        result =
        proprietors.stream()
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
          break;
      }
      case COMPLIANCE_OFFICER: {
        // Check if the user already has a inProgress Compliance Officer
        if(complianceOfficerRepository.existsByMemberMemIdAndCompStatus(tradingMemberId, ServiceConstants.IN_PROGRESS_COMPLIANCE_OFFICER)) {
          throw new BaseServiceException(ResponseMessages.IN_PROGRESS_COMPLIANCE_OFFICER_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        } else {
          List<ComplianceOfficer> complianceOfficers = complianceOfficerRepository.getNonKmpMemberComplianceOfficers(tradingMemberId);
          result =
          complianceOfficers.stream()
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
        }
        break;
      }
    }
    return result;
  }

  /**
   * Creates a KMP using the specified input data
   *
   * @param tradingMemberId The trading member id
   * @param request The request for creating the KMP
   */
  public void createKMP(Long tradingMemberId, CreateKMPRequestBody request) {
    KeyManagementPersonnel kmp = new KeyManagementPersonnel();

    // Set the KMP data from the request
    kmp.setSalutation(request.getSalutation());
    kmp.setName(request.getKmpName());
    kmp.setDeclarationDate(
        LocalDate.parse(request.getDateOfDeclaration(), dateFormatter).atStartOfDay());
    kmp.setPan(request.getPan());
    kmp.setRole(request.getRole());
    kmp.setMobileNumber(request.getMobileNumber());
    kmp.setPhoneNumber(request.getPhoneNumber());
    kmp.setEmail(request.getEmailId());
    kmp.setFromNse(request.getFromNse());

    // set the member Id
    kmp.setMemId(tradingMemberId);

    // Get the current date/time
    LocalDateTime now = LocalDateTime.now();

    // Update the action and action date
    kmp.setAction(KMPAction.ADDITION);
    kmp.setActionDate(now);

    // Set the submission date
    kmp.setSubmissionDate(now);

    // Set auditing fields
    kmp.setCreatedDate(now);
    kmp.setCreatedBy(tradingMemberId.toString());
    kmp.setUpdatedDate(now);
    kmp.setUpdatedBy(tradingMemberId.toString());


    // Save the KMP record
    kmpRepository.save(kmp);
  }

  /**
   * Updates a KMP using the specified input data
   *
   * @param tradingMemberId The trading member id
   * @param request The request for updating the KMP
   */
  @Transactional
  public void updateKMPs(Long tradingMemberId, UpdateKMPsRequestBody request) {
    for(UpdateKmpData kmpData : request.getKmps()) {
      // Check if the KMP to update exists
      Optional<KeyManagementPersonnel> optionalKmp =
        kmpRepository.findByIdAndMemId(kmpData.getKmpId(), tradingMemberId);

        if (optionalKmp.isEmpty()) {
          throw new BaseServiceException(
              String.format(
                  "KMP with id %d does not exist for member id : %d",
                  kmpData.getKmpId(), tradingMemberId),
              HttpStatus.NOT_FOUND);
        } else {
          KeyManagementPersonnel kmp = optionalKmp.get();

          // Check if the KMP is from nse
          if(kmp.getFromNse()) {
            // only additional designation is editable
            kmp.setAdditionalDesignation(kmpData.getAddlDesignation());
          } else {
            kmp.setRole(kmpData.getRole());
            kmp.setMobileNumber(kmpData.getMobileNumber());
            kmp.setPhoneNumber(kmpData.getPhoneNumber());
            kmp.setEmail(kmpData.getEmailId());
          }

          LocalDateTime now = LocalDateTime.now();

          // Set the action and action date
          kmp.setAction(KMPAction.EDITED);
          kmp.setActionDate(now);

          // Set the updated date
          kmp.setUpdatedDate(now);
          kmp.setUpdatedBy(tradingMemberId.toString());

          // Save the updated kmp
          kmpRepository.save(kmp);
        }
    }
  }

  /**
   * Validates the specified pan
   *
   * @param tradingMemberId The trading member id
   * @param pan The pan to validate
   * @param kmpName The entered KMP name
   * @param kmpOfOtherMember the flag indicating whether to only check if the pan holder is KMP of other member
   * @return The pan validation response
   */
  public PanValidationResponse validatePan(Long tradingMemberId, String pan, String kmpName, boolean kmpOfOtherMember) {
    Pattern panPattern = Pattern.compile(PAN_VALIDATION_REGEX);

    if (!panPattern.matcher(pan).matches()) {
      return PanValidationResponse.builder()
               .message("The provided pan does not match the required format pattern: AAAPA1234A")
               .panStatus(PanValidationStatus.INVALID)
               .invalidityType(PanInvalidityType.DOES_NOT_MATCH_REQUIRED_FORMAT).build();
    }

    // In this case we only check if the pan holder is KMP for another member
    if(kmpOfOtherMember) {
      // Check if the pan holder is a KMP for other member
      List<KeyManagementPersonnel> otherMemberKmp = kmpRepository.findByPan(pan);
      if(!otherMemberKmp.isEmpty()) {
        return buildKmpOfOtherMemberResponse(otherMemberKmp.get(0), pan);
      } else {
        return buildExistingAndValidPanResponse(kmpName);
      }
    }

    // Check if the pan holder is already a KMP for the current member
    Optional<KeyManagementPersonnel> optionalMemberKmp = kmpRepository.findByMemIdAndPan(tradingMemberId, pan);
    if(optionalMemberKmp.isPresent()) {
      return PanValidationResponse.builder()
              .message(ResponseMessages.PERSON_IS_ALREADY_OWN_KMP)
              .panStatus(PanValidationStatus.INVALID)
              .invalidityType(PanInvalidityType.PAN_HOLDER_IS_ALREADY_MEMBER_KMP).build();
    }

    // Get the member info
    MemberMaster member = memberService.getByMemberId(tradingMemberId);

    // Check member constitution type
    switch(member.getConstitutionType()) {
      case ServiceConstants.CORPORATE_CONSTITUTION:
      case ServiceConstants.BANK_CONSTITUTION : {
        List<String> roles = new ArrayList<>();
        List<String> panHoldersNames = new ArrayList<>();

        Optional<Director> memberDirector = directorRepository.findByMemberMemIdAndPan(tradingMemberId, pan);
        if(memberDirector.isPresent()) {
          roles.add(DIRECTOR);
          panHoldersNames.add(memberDirector.get().getName());
        }

        Optional<Shareholder> memberShareholder = shareholderRepository.findByMemberIdAndPan(tradingMemberId, pan);
        if(memberShareholder.isPresent()) {
          roles.add(SHAREHOLDER);
          panHoldersNames.add(memberShareholder.get().getName());
        }

        Optional<ComplianceOfficer> memberCo = complianceOfficerRepository.findByMemberMemIdAndPan(tradingMemberId, pan);
        if(memberCo.isPresent()) {
          roles.add(ServiceConstants.COMPLIANCE_OFFICER_SPACED_NAME);
          panHoldersNames.add(memberCo.get().getName());
        }

        if(!roles.isEmpty()) {
          return PanValidationResponse.builder()
          .message(String.format(PAN_EXISTS_FOR_MEMBER_MESSAGE, roles.stream().collect(Collectors.joining("/"))))
          .panStatus(PanValidationStatus.INVALID)
          .pan(pan)
          .role(roles.get(0))
          .panHolderName(panHoldersNames.get(0))
          .invalidityType(PanInvalidityType.ALREADY_EXISTS_FOR_CURRENT_MEMBER).build();
        } else {
          // Check if the pan holder is a KMP for other member
          List<KeyManagementPersonnel> otherMemberKmp = kmpRepository.findByPan(pan);
          if(!otherMemberKmp.isEmpty()) {
            return buildKmpOfOtherMemberResponse(otherMemberKmp.get(0), pan);
          }
        }
        break;
      }
      case ServiceConstants.LLP_CONSTITUTION:
      case ServiceConstants.PARTNERSHIP_CONSTITUTION: {
        List<String> roles = new ArrayList<>();
        List<String> panHoldersNames = new ArrayList<>();

        Optional<Partner> memberPartner = partnerRepository.findByMemberIdAndPan(tradingMemberId, pan);
        if(memberPartner.isPresent()) {
          roles.add(PARTNER);
          panHoldersNames.add(memberPartner.get().getName());
        }

        Optional<ComplianceOfficer> memberCo = complianceOfficerRepository.findByMemberMemIdAndPan(tradingMemberId, pan);
        if(memberCo.isPresent()) {
          roles.add(ServiceConstants.COMPLIANCE_OFFICER_SPACED_NAME);
          panHoldersNames.add(memberCo.get().getName());
        }
        if(!roles.isEmpty()) {
          return PanValidationResponse.builder()
          .message(String.format(PAN_EXISTS_FOR_MEMBER_MESSAGE, roles.stream().collect(Collectors.joining("/"))))
          .panStatus(PanValidationStatus.INVALID)
          .pan(pan)
          .panHolderName(panHoldersNames.get(0))
          .role(roles.get(0))
          .invalidityType(PanInvalidityType.ALREADY_EXISTS_FOR_CURRENT_MEMBER).build();
        } else {
          // Check if the pan holder is a KMP for other member
          List<KeyManagementPersonnel> otherMemberKmp = kmpRepository.findByPan(pan);
          if(!otherMemberKmp.isEmpty()) {
            return buildKmpOfOtherMemberResponse(otherMemberKmp.get(0), pan);
          }
        }
        break;
      }
      case ServiceConstants.INDIVIDUAL_CONSTITUTION: {
        List<String> roles = new ArrayList<>();
        List<String> panHoldersNames = new ArrayList<>();

        Optional<Proprietor> memberProprietor = proprietorRepository.findByMemberIdAndPan(tradingMemberId, pan);
        if(memberProprietor.isPresent()) {
          roles.add(PROPRIETOR);
          panHoldersNames.add(memberProprietor.get().getName());
        }

        Optional<ComplianceOfficer> memberCo = complianceOfficerRepository.findByMemberMemIdAndPan(tradingMemberId, pan);
        if(memberCo.isPresent()) {
          roles.add(ServiceConstants.COMPLIANCE_OFFICER_SPACED_NAME);
          panHoldersNames.add(memberCo.get().getName());
        }
        if(!roles.isEmpty()) {
          return PanValidationResponse.builder()
          .message(String.format(PAN_EXISTS_FOR_MEMBER_MESSAGE, roles.stream().collect(Collectors.joining("/"))))
          .panStatus(PanValidationStatus.INVALID)
          .pan(pan)
          .role(roles.get(0))
          .panHolderName(panHoldersNames.get(0))
          .invalidityType(PanInvalidityType.ALREADY_EXISTS_FOR_CURRENT_MEMBER).build();
        } else {
          // Check if the pan holder is a KMP for other member
          List<KeyManagementPersonnel> otherMemberKmp = kmpRepository.findByPan(pan);
          if(!otherMemberKmp.isEmpty()) {
            return buildKmpOfOtherMemberResponse(otherMemberKmp.get(0), pan);
          }
        }
        break;
      }
    }

    // Verify pan externally
    PanVerificationResponse panVerificationResponse = panValidationService.validatePan(ServiceConstants.PAN_VALIDATION_CUSTOMER_ID, pan);
    String panHolderName = "";
    if(!panVerificationResponse.getStatusCode().equals(String.format("%d", HttpStatus.OK.value()))) {
      // The response from the verification service generated an error
      // TODO  uncomment the following line of code when a valid PAN to validate is available for testing
        return PanValidationResponse.builder()
                  .message(ResponseMessages.PAN_VALIDATION_FAILED)
                  .panStatus(PanValidationStatus.INVALID)
                  .invalidityType(PanInvalidityType.PAN_VALIDATION_FAILED).build();

      // TODO - remove the following line of code when a valid pan is available for testing
      //panHolderName = "Preeti Shenoy";
    } else {
      // The pan validation responds with a valid pan holder name
      panHolderName = String.format("%s %s",
      panVerificationResponse.getPanResponse().getFirstName(),
      panVerificationResponse.getPanResponse().getLastName());
    }

    // Perform the name matching check
    if(!nameMatchingService.verifyNameMatching(ServiceConstants.NAME_MATCHING_CUSTOMER_ID, pan, panHolderName, kmpName)) {
      return PanValidationResponse.builder()
             .message(ResponseMessages.PAN_NAME_DO_NOT_MATCH)
             .panStatus(PanValidationStatus.INVALID)
             .invalidityType(PanInvalidityType.PAN_NAME_DO_NOT_MATCH).build();
    } else {
      return buildExistingAndValidPanResponse(panHolderName);
    }
  }

  /**
   * This private method builds the PanValidationResponse for the case when the pan exists
   * @param panHolderName
   * @return The PanValidationResponse
   */
  private PanValidationResponse buildExistingAndValidPanResponse(String panHolderName) {
    return PanValidationResponse.builder()
            .message(ResponseMessages.PAN_IS_VALID)
            .panStatus(PanValidationStatus.EXISTING_AND_VALID)
            .panHolderName(panHolderName).build();
  }

  /**
   * This private method builds the PanValidationResponse for the case when the pan holder is KMP of other member
   *
   * @param kmp The KMP entity
   * @param pan The pan value
   * @return The PanValidationResponse
   */
  private PanValidationResponse buildKmpOfOtherMemberResponse(KeyManagementPersonnel kmp, String pan) {
    return PanValidationResponse.builder()
    .message(String.format(PAN_EXISTS_FOR_OTHER_MEMBER_MESSAGE, kmp.getMemId()))
    .pan(pan)
    .role(kmp.getRole())
    .panStatus(PanValidationStatus.INVALID)
    .invalidityType(PanInvalidityType.ALREADY_EXISTS_FOR_OTHER_MEMBER).build();
  }

  /**
   * Deletes multiple KMPs identified in the input request
   *
   * @param tradingMemberId The trading member id
   * @param request The request for deleting the KMP records
   */
  @Transactional
  public void deleteKMPDetails(Long tradingMemberId, DeleteKMPDetailsRequestBody request) {

    for (DeleteKMPDetails details : request.getDeleteKMPDetails()) {
      Optional<KeyManagementPersonnel> optionalKmp =
          kmpRepository.findByIdAndAppIdAndDeletedDateNull(details.getKmpId(), details.getAppId());

      if (optionalKmp.isEmpty()) { // The KMP does not exist
        throw new BaseServiceException(
            String.format(
                "KMP with id = %d and appId = %d does not exist",
                details.getKmpId(), details.getAppId()),
            HttpStatus.NOT_FOUND);
      } else {
        // Set the KMP as deleted
        KeyManagementPersonnel kmp = optionalKmp.get();

        LocalDateTime now = LocalDateTime.now();

        kmp.setDeletedDate(now);
        kmp.setDeletedBy(tradingMemberId.toString());
        kmp.setUpdatedBy(tradingMemberId.toString());
        kmp.setUpdatedDate(now);

        // Set the action and action date
        kmp.setAction(KMPAction.DELETION);
        kmp.setActionDate(now);

        // Save the kmp
        kmpRepository.saveAndFlush(kmp);
      }
    }
  }

  /**
   * Send a otp to the mobile number for Verifier
   *
   * @param tradingMemberId The trading member id
   * @param verifierId The verifierId
   * @param verifierName The verifierName
   * @param mobileNo The mobile number to send
   * @return The OtpSendResponse
   */
  @Transactional
  public OtpSendResponse sendOtp(Long tradingMemberId, String verifierId, String verifierName, String mobileNo) {
    String otp = fintechNotificationServiceProxy.sendOTPViaSms(mobileNo);
    VerificationDetails verificationDetails = new VerificationDetails();
    verificationDetails.setName(verifierName);
    verificationDetails.setVerifyType(ServiceConstants.OTP);
    verificationDetails.setVerifyEntity(join(",", otp, verifierId, mobileNo));
    verificationDetails.setVerifiedFlag(false);
    verificationDetails.setVerifiedStatus(ServiceConstants.SUCCESS);
    verificationDetails.setReason(ServiceConstants.SUCCESS);
    verificationDetails = verificationDetailsRepository.save(verificationDetails);
    return new OtpSendResponse(HttpStatus.OK.value(), "ÓTP sent successfully.", verificationDetails.getVerifyId());
  }

  /**
   * Validate the OTP entered by the Verifier
   *
   * @param tradingMemberId The trading member id
   * @param verifyId The verifyId
   * @param verifierId The verifierId
   * @param verifierName The verifierName
   * @param mobileNo The mobile number to send
   * @param otp The otp
   * @return The CommonMessageDto
   */
  @Transactional
  public CommonMessageDto validateOtp(Long tradingMemberId, Long verifyId, String verifierId, String verifierName, String mobileNo, String otp) {
    Optional<VerificationDetails> ov = verificationDetailsRepository.findById(verifyId)
            .filter(v -> !v.getVerifiedFlag()
                && v.getName().equals(verifierName)
                && v.getVerifyEntity().equals(join(",", otp, verifierId, mobileNo)));
    if (ov.isPresent()) {
      VerificationDetails vd = ov.get();
      vd.setVerifiedFlag(true);
      verificationDetailsRepository.saveAndFlush(vd);
      return new CommonMessageDto(HttpStatus.OK.value(), "ÓTP validated successfully.");
    } else {
      return new CommonMessageDto(HttpStatus.BAD_REQUEST.value(), "ÓTP validated unsuccessfully.");
    }
  }
  
  public List<KMPApplicationsDetailDto> kmpRequestlist(Long tradingMemberId, String memberName, String memberCode, String memberType) {
	  List<KMPApplicationsDetailDto> list = new ArrayList<KMPApplicationsDetailDto>();
	  kmpApplicationDetailsRepository.findByMemberNameMemberCodeAndMemberType(memberName, memberCode, memberType).forEach(rec -> {
		  list.add(new KMPApplicationsDetailDto(
				  rec.getId(), 
				  rec.getMemberName(), 
				  rec.getMemberCode(), 
				  rec.getMemberType(), 
				  rec.getRequestType(), 
				  rec.getStatus(), 
				  rec.getApplicationStartedOn(), 
				  rec.getApplicationSubmittedOn(), 
				  rec.getRemarks()));
	  });
	  return list;
  }
  
  @Transactional
  public CommonMessageDto createKMPApplication(Long tradingMemberId, AddKMPDetails request) {
	  
	  	KMPApplicationDetails kmp = new KMPApplicationDetails();
	  	
	  	// Get the current date/time
	    LocalDateTime now = LocalDateTime.now();
	    
	    // Set the KMP data from the request
	    kmp.setApplicationStartedOn(now);
	    kmp.setApplicationSubmittedOn(now);
	    kmp.setRemarks(request.getRemarks());
	    kmp.setRequestType(request.getRequestType());
	    kmp.setStatus(request.getStatus());
	    
	    kmp.setMemberCode(request.getMemberCode());
	    kmp.setMemberName(request.getMemberName());
	    kmp.setMemberType(request.getMemberType());

	    // Set auditing fields
	    kmp.setCreatedDate(now);
	    kmp.setCreatedBy(tradingMemberId.toString());
	    kmp.setUpdatedDate(now);
	    kmp.setUpdatedBy(tradingMemberId.toString());
	
	
		    // Save the KMP record
	    try {
	    	kmpApplicationDetailsRepository.save(kmp);
	    } catch(Exception ex) {
	    	throw new BaseServiceException(
	                String.format(
	                    "Some error occured in the processing, please try again after sometime."),
	                HttpStatus.BAD_REQUEST);
	    }
	    return new CommonMessageDto(HttpStatus.OK.value(), "");
	  }
  
  @Transactional
  public void updateKMPApplication(Long tradingMemberId, EditKMPDetails request) {
	  
	  Optional<KMPApplicationDetails> optionalKmp =
        kmpApplicationDetailsRepository.findById(request.getAppId());

        if (optionalKmp.isEmpty()) {
          throw new BaseServiceException(
              String.format(
                  "KMP Request with id %d does not exist",
                  request.getAppId()),
              HttpStatus.NOT_FOUND);
        } else {
        	KMPApplicationDetails kmp = optionalKmp.get();
        	// Get the current date/time
    	    LocalDateTime now = LocalDateTime.now();
    	    
    	    // Set the KMP data from the request
    	    kmp.setApplicationStartedOn(null != request.getApplicationStartedOn() ? request.getApplicationStartedOn() : kmp.getApplicationStartedOn());
    	    kmp.setApplicationSubmittedOn(null != request.getApplicationSubmittedOn() ? request.getApplicationSubmittedOn() : kmp.getApplicationSubmittedOn());
    	    kmp.setRemarks(null != request.getRemarks() ? request.getRemarks() : kmp.getRemarks());
    	    kmp.setRequestType(null != request.getRequestType() ? request.getRequestType() : kmp.getRequestType());
    	    kmp.setStatus(null != request.getStatus() ? request.getStatus() : kmp.getStatus());
    	    
    	    kmp.setMemberCode(null != request.getMemberCode() ? request.getMemberCode() : kmp.getMemberCode());
    	    kmp.setMemberName(null != request.getMemberName() ? request.getMemberName() : kmp.getMemberName());
    	    kmp.setMemberType(null != request.getMemberType() ? request.getMemberType() : kmp.getMemberType());

    	    // Set auditing fields
    	    kmp.setUpdatedDate(now);
    	    kmp.setUpdatedBy(tradingMemberId.toString());
    	
    	
    	    // Save the KMP record
        	kmpApplicationDetailsRepository.saveAndFlush(kmp);
        }
	  }
  
  @Transactional
  public void deleteKMPApplication(Long tradingMemberId, DeleteKMPDetailsRequestBody request) {

    for (DeleteKMPDetails details : request.getDeleteKMPDetails()) {
      Optional<KMPApplicationDetails> optionalKmp = kmpApplicationDetailsRepository.findById(details.getAppId());

      if (optionalKmp.isEmpty()) { // The KMP does not exist
        throw new BaseServiceException(
            String.format(
                "KMP Application with id = %d does not exist",
                details.getAppId()),
            HttpStatus.NOT_FOUND);
      } else {
        // Set the KMP as deleted
    	  KMPApplicationDetails kmp = optionalKmp.get();

        LocalDateTime now = LocalDateTime.now();

        kmp.setDeletedBy(tradingMemberId.toString());
        kmp.setDeletedDate(now);
        kmp.setUpdatedBy(tradingMemberId.toString());
        kmp.setUpdatedDate(now);

        // Save the kmp
        kmpApplicationDetailsRepository.saveAndFlush(kmp);
      }
    }
  }
}
