package com.nseindia.mc.service.aiml;

import static org.springframework.beans.BeanUtils.copyProperties;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.persistence.criteria.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nseindia.mc.config.NotificationConfig;
import com.nseindia.mc.controller.dto.EligibleMemberInfoDto;
import com.nseindia.mc.controller.dto.EligibleSubmissionDto;
import com.nseindia.mc.controller.dto.ForEligibleMemberDto;
import com.nseindia.mc.controller.dto.MemberInfoDto;
import com.nseindia.mc.controller.dto.SubmissionDto;
import com.nseindia.mc.controller.dto.TblAimlSystemDetailDto;
import com.nseindia.mc.controller.dto.TblAimlSystemDetailNewDto;
import com.nseindia.mc.exception.BaseServiceException;
import com.nseindia.mc.model.MemberAimlDtl;
import com.nseindia.mc.model.MemberMaster;
import com.nseindia.mc.model.TblAimlSystemDetail;
import com.nseindia.mc.repository.MemberAimlDtlRepository;
import com.nseindia.mc.repository.MemberMasterRepository;
import com.nseindia.mc.repository.TblAimlSystemDetailRepository;
import com.nseindia.mc.service.MailService;
import com.nseindia.mc.util.CommonUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AimlService {
	private static final int EXPIRED_DATE = 15;
	private final MemberAimlDtlRepository memberAimlDtlRepository;
	private final MemberMasterRepository memberMasterRepository;
	private final TblAimlSystemDetailRepository tblAimlSystemDetailRepository;
	private final MailService mailService;
	private final Resource reportTemplate;
	protected final NotificationConfig notificationConfig;

	public AimlService(final MemberAimlDtlRepository memberAimlDtlRepository,
			final MemberMasterRepository memberMasterRepository,
			@Value("classpath:SEBI_REPORT_TEMPLATE.xlsx") final Resource reportTemplate,
			final TblAimlSystemDetailRepository tblAimlSystemDetailRepository, final MailService mailService,
			final NotificationConfig notificationConfig) {
		this.memberAimlDtlRepository = memberAimlDtlRepository;
		this.memberMasterRepository = memberMasterRepository;
		this.tblAimlSystemDetailRepository = tblAimlSystemDetailRepository;
		this.mailService = mailService;
		this.reportTemplate = reportTemplate;
		this.notificationConfig = notificationConfig;
	}

	/**
	 * Get for eligible member.
	 *
	 * @param memberId
	 * @return the for eligible member
	 */
	public ForEligibleMemberDto getForEligibleMember(final long memberId) {
		ForEligibleMemberDto result = new ForEligibleMemberDto();
		String quarter = CommonUtils.previousQuarterName();
		result.setQuarter(quarter);

		// TODO: always not expired for now
		result.setExpired(false);
		// result.setExpired(LocalDate.now().get(IsoFields.DAY_OF_QUARTER) >
		// EXPIRED_DATE);
		Optional<MemberAimlDtl> optionalMemberAimlDtl = memberAimlDtlRepository.findByMemIdAndQuarterName(memberId,
				quarter);
		if (optionalMemberAimlDtl.isPresent()) {
			MemberAimlDtl memberAimlDtl = optionalMemberAimlDtl.get();
			result.setEligible(CommonUtils.convertFlag(memberAimlDtl.getEligibleFlag()));
			result.setSubmission(new EligibleSubmissionDto(memberAimlDtl.getUpdateDt(),
					CommonUtils.convertFlag(memberAimlDtl.getNilFlag())));
			memberMasterRepository.findById(memberAimlDtl.getMemId())
					.ifPresent(memberMaster -> result.setMemberInfo(new EligibleMemberInfoDto(
							memberMaster.getSebiRegistrationNum(), memberMaster.getMemEntityType(),
							memberMaster.getMemName(), memberMaster.getMemPanNum())));
		} else {
			result.setEligible(false);
		}
		return result;
	}

	/**
	 * Get list of member info.
	 *
	 * @return the list of member info
	 */
	public List<MemberInfoDto> getMemberInfo() {
		List<Long> memberIds = memberAimlDtlRepository.findBySubmissionDone("Y").stream().map(MemberAimlDtl::getMemId)
				.distinct().collect(Collectors.toList());
		return memberMasterRepository.findAllById(memberIds).stream()
				.map(m -> new MemberInfoDto(m.getMemId(), m.getMemCd(), m.getMemName())).collect(Collectors.toList());
	}

	/**
	 * Get list of quarter.
	 *
	 * @param memberId
	 * @return the list of quarter
	 */
	public List<String> getQuarters(final Long memberId) {
		if (memberId != null) {
			return convert(memberAimlDtlRepository.findByMemId(memberId));
		}
		return convert(memberAimlDtlRepository.findAll());
	}

	/**
	 * Convert list of MemberAimlDtl to list of quarterName.
	 *
	 * @param memberAimlDtlList
	 * @return the list of quarterName
	 */
	private List<String> convert(final List<MemberAimlDtl> memberAimlDtlList) {
		return memberAimlDtlList.stream().map(MemberAimlDtl::getQuarterName).distinct().collect(Collectors.toList());
	}

	/**
	 * Generate report.
	 *
	 * @param quarter
	 * @return report content
	 */
	public ByteArrayResource generateReport(final String quarter, final Long tradingMemberId) {
		List<MemberAimlDtl> eligibleMember = memberAimlDtlRepository
				.findByEligibleFlagAndQuarterName(CommonUtils.convertFlag(true), quarter);
		List<MemberAimlDtl> previousEligibleMember = memberAimlDtlRepository
				.findByEligibleFlagAndQuarterName(CommonUtils.convertFlag(true), CommonUtils.previousQuarter(quarter));
		List<Long> memberIds =

				eligibleMember.stream()
						.filter(m -> CommonUtils.convertFlag(m.getSubmissionDone()) && m.getNilFlag().equals("N"))
						.map(m -> m.getMemId()).filter(mId -> tradingMemberId == null || tradingMemberId.equals(mId))
						.distinct().collect(Collectors.toList());
		if (memberIds.size() == 0) {
			throw new BaseServiceException("No SEBI report generated for download in the selected quarter " + quarter,
					HttpStatus.NOT_FOUND);
		}
		List<MemberMaster> masters = memberMasterRepository.findAllById(memberIds);
		List<TblAimlSystemDetail> applications = tblAimlSystemDetailRepository.findByMemberIdIn(memberIds).stream()
				.filter(apps -> apps.getQuarterName().equals(quarter)).collect(Collectors.toList());
		try {
			Workbook workbook = new XSSFWorkbook(reportTemplate.getInputStream());
			Sheet sheet = workbook.getSheetAt(0);
			int rowNum = 3;
			for (MemberMaster mm : masters) {
				TblAimlSystemDetail app = applications.stream().filter(a -> a.getMemberId().equals(mm.getMemId()))
						.findFirst().get();
				Row row = sheet.createRow(rowNum);
				row.createCell(0).setCellValue(mm.getSebiRegistrationNum());
				row.createCell(1).setCellValue(mm.getMemName());
				row.createCell(2).setCellValue(mm.getMemPanNum());
				row.createCell(3).setCellValue(app.getAppSystemName());
				row.createCell(4).setCellValue(app.getAppSystemUsedDate().toString());
				row.createCell(5).setCellValue(app.getAreaTypeAimlUsed());
				// row.createCell(6).setCellValue(app.getCicularSebiSecurityControl());
				row.createCell(6).setCellValue(mm.getSystemComplySebi());
				row.createCell(7).setCellValue(mm.getSysAuditAdverse());
				row.createCell(8).setCellValue(mm.getSysEntityInspect());
				row.createCell(9).setCellValue(mm.getSysIrregularities());
				rowNum++;
			}
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			workbook.write(bos);
			return new ByteArrayResource(bos.toByteArray());
		} catch (Exception e) {
			log.error("generate report fail", e);
			throw new BaseServiceException("generate report error", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Send a generated report email to ibes.
	 *
	 * @param quarter
	 */
	public void sendReport(final String quarter, final Long tradingMemberId) {
		mailService.sendIBESReportEmail(quarter, generateReport(quarter, tradingMemberId));
	}

	/**
	 * Update nil submission.
	 *
	 * @param memberId
	 */
	@Transactional
	public void updateNilSubmission(final long memberId) {
		MemberAimlDtl mad = memberAimlDtlRepository
				.findByMemIdAndQuarterName(memberId, CommonUtils.previousQuarterName())
				.orElseThrow(() -> new BaseServiceException("the tradingMemberId is not found", BAD_REQUEST));
		mad.setNilFlag(CommonUtils.convertFlag(true));
		mad.setSubmissionDone(CommonUtils.convertFlag(true));
		mad.setUpdateDt(new Date());
		memberAimlDtlRepository.save(mad);
		tblAimlSystemDetailRepository.deleteByMemberIdAndQuarterName(memberId, CommonUtils.previousQuarterName());
		memberMasterRepository.findEmail(mad.getMemId()).ifPresent(email -> mailService
				.sendEmailAfterSuccessfulNilSubmission(email, mad.getQuarterStartDate(), mad.getQuarterEndDate()));
	}

	@Scheduled(cron = "0 30 8 20 */3 *")
	public void sendNotificationDataWhenFirstWeekDayOfMonth() {
		List<MemberAimlDtl> memList = memberAimlDtlRepository.findByNilFlagAndQuarterName("N",
				CommonUtils.previousQuarterName());
		Date quarterEnd = memList.get(0).getQuarterEndDate();
		List<Long> memberIds = memList.stream().map(MemberAimlDtl::getMemId).distinct().collect(Collectors.toList());
		memberMasterRepository.findAllEmail(memberIds).get().stream()
				.forEach(email -> mailService.sendFirstWeekDayOfMonthAIMLAutoEmail(email, quarterEnd));

	}
	
	@Scheduled(cron = "0 30 8 20 */3 *")
	public void sendNotificationDataWhenSenenthDayOfQuarter() {
		List<MemberAimlDtl> memList = memberAimlDtlRepository.findByNilFlagAndSubmissionDoneAndQuarterName("N","N",
				CommonUtils.previousQuarterName());
		Date quarterEnd = memList.get(0).getQuarterEndDate();
		List<Long> memberIds = memList.stream().map(MemberAimlDtl::getMemId).distinct().collect(Collectors.toList());
		memberMasterRepository.findAllEmail(memberIds).get().stream()
				.forEach(email -> mailService.sendSeventhDayOfMonthAIMLAutoEmail(email, quarterEnd));

	}
	
	@Scheduled(cron = "0 30 8 20 */3 *")
	public void sendNotificationDataWhenFifteenthDayOfQuarter() {
		List<MemberAimlDtl> memList = memberAimlDtlRepository.findByNilFlagAndSubmissionDoneAndQuarterName("N","N",
				CommonUtils.previousQuarterName());
		Date quarterEnd = memList.get(0).getQuarterEndDate();
		List<Long> memberIds = memList.stream().map(MemberAimlDtl::getMemId).distinct().collect(Collectors.toList());
		memberMasterRepository.findAllEmail(memberIds).get().stream()
				.forEach(email -> mailService.sendFifteenthDayOfMonthAIMLAutoEmail(email, quarterEnd));

	}

	@Scheduled(cron = "0 30 8 20 */3 *")
	public void sendNotificationNILWhenFirstWeekDayOfMonth() {

		List<MemberAimlDtl> memList = memberAimlDtlRepository.findByNilFlagAndQuarterName("Y",
				CommonUtils.previousQuarterName());
		Date quarterEnd = memList.get(0).getQuarterEndDate();
		List<Long> memberIds = memList.stream().map(MemberAimlDtl::getMemId).distinct().collect(Collectors.toList());
		memberMasterRepository.findAllEmail(memberIds).get().stream()
				.forEach(email -> mailService.sendFirstNilWeekDayOfMonthAIMLAutoEmail(email, quarterEnd));

	}

	/**
	 * Get list of data submission.
	 *
	 * @param memberId
	 * @return the list of data submission
	 */
	public List<TblAimlSystemDetailNewDto> getDataSubmissions(final Long memberId, final String quarter) {

		List<MemberAimlDtl> data = new ArrayList<>();
		List<MemberMaster> masters = new ArrayList<>();
		List<MemberMaster> masters1 = new ArrayList<>();
		if (quarter.equals("datasubmissionpage")) {
			data = memberAimlDtlRepository.findAll(getExample(memberId, CommonUtils.previousQuarterName(), null, null));
			List<Long> memIds = new ArrayList<>();
			memIds.add(memberId);

			masters1 = memberMasterRepository.findAllById(memIds);

		} else {
			data = memberAimlDtlRepository.findAll(getExample(memberId, quarter, null, null));
			masters1 = memberMasterRepository
					.findAllById(data.stream().map(d -> d.getMemId()).collect(Collectors.toList()));
		}

		masters.addAll(masters1);

		/*
		 * List<MemberMaster> masters = memberMasterRepository
		 * .findAllById(data.stream().map(d ->
		 * d.getMemId()).collect(Collectors.toList()));
		 */
		List<TblAimlSystemDetail> systems = new ArrayList<>();
		if (quarter.equals("datasubmissionpage")) {
			systems = tblAimlSystemDetailRepository.findAll(getExample1(memberId, CommonUtils.previousQuarterName()));
		} else {
			systems = tblAimlSystemDetailRepository.findAll(getExample1(memberId, quarter));
		}
		if (!data.isEmpty() && data.get(0).getNilFlag().equals("N")) {
			return systems.stream().map(model -> {
				TblAimlSystemDetailNewDto dto = new TblAimlSystemDetailNewDto();
				for (MemberMaster mm : masters) {
					dto.setSebiRegistrationNum(mm.getSebiRegistrationNum());
					dto.setMemName(mm.getMemName());
					dto.setMemPanNum(mm.getMemPanNum());
					dto.setMemEntityType(mm.getMemEntityType());
				}
				copyProperties(model, dto);
				return dto;
			}).collect(Collectors.toList());
		}
		List<TblAimlSystemDetailNewDto> nilList = new ArrayList<>();
		TblAimlSystemDetailNewDto dto = new TblAimlSystemDetailNewDto();
		for (MemberMaster mm : masters) {
			dto.setSebiRegistrationNum(mm.getSebiRegistrationNum());
			dto.setMemName(mm.getMemName());
			dto.setMemPanNum(mm.getMemPanNum());
			dto.setMemEntityType(mm.getMemEntityType());
		}

		nilList.add(dto);
		return nilList;
	}

	/**
	 * Update data submission.
	 *
	 * @param memberId
	 * @param dtoList
	 */
	@Transactional
	public void updateDataSubmission(final long memberId, final List<TblAimlSystemDetailDto> dtoList) {
		MemberAimlDtl mad = memberAimlDtlRepository
				.findByMemIdAndQuarterName(memberId, CommonUtils.previousQuarterName())
				.orElseThrow(() -> new BaseServiceException("the tradingMemberId is not found", BAD_REQUEST));
		mad.setNilFlag(CommonUtils.convertFlag(false));
		mad.setUpdateDt(new Date());
		mad.setSubmissionDone(CommonUtils.convertFlag(true));
		memberAimlDtlRepository.save(mad);

		// tblAimlSystemDetailRepository.deleteByMemberId(memberId);
		tblAimlSystemDetailRepository.deleteByMemberIdAndQuarterName(memberId, CommonUtils.previousQuarterName());
		AtomicInteger index = new AtomicInteger(1);
		tblAimlSystemDetailRepository.saveAll(dtoList.stream().map(d -> {
			TblAimlSystemDetail m = new TblAimlSystemDetail();
			copyProperties(d, m);
			if (m.getCreatedBy() == null) {
				m.setCreatedBy(String.valueOf(memberId));
			}
			if (m.getCreatedDt() == null) {
				m.setCreatedDt(new Date());
			}
			if (m.getAppSystemId() == null) {
				m.setAppSystemId(UUID.randomUUID().toString());
			}
			m.setMemberId(memberId);
			m.setSysType("sys" + index.getAndIncrement());
			m.setQuarterName(CommonUtils.previousQuarterName());
			return m;
		}).collect(Collectors.toList()));

		memberMasterRepository.findEmail(mad.getMemId()).ifPresent(email -> mailService
				.sendEmailAfterSuccessfulDataSubmission(email, mad.getQuarterStartDate(), mad.getQuarterEndDate()));
	}

	/**
	 * Get list of submission.
	 *
	 * @param memberId
	 * @param quarter
	 * @param startDate
	 * @param endDate
	 * @return the list of submission
	 */
	public List<SubmissionDto> getSubmissions(final Long memberId, final String quarter, final Date startDate,
			final Date endDate) {
		List<MemberAimlDtl> data = memberAimlDtlRepository.findAll(getExample(memberId, quarter, startDate, endDate));
		List<MemberMaster> masters = memberMasterRepository
				.findAllById(data.stream().map(d -> d.getMemId()).collect(Collectors.toList()));
		AtomicInteger counter = new AtomicInteger(0);
		return data.stream().map(d -> {
			int i = counter.incrementAndGet();
			MemberMaster m = masters.stream().filter(ma -> ma.getMemId().equals(d.getMemId())).findFirst().get();
			return new SubmissionDto(i, d.getQuarterName(), d.getQuarterStartDate(), d.getQuarterEndDate(),
					d.getUpdateDt(), m.getMemId(), m.getMemCd(), m.getMemName(),
					CommonUtils.convertFlag(d.getSubmissionDone()), CommonUtils.convertFlag(d.getNilFlag()));
		}).collect(Collectors.toList());
	}

	
	/**
	 * Get query example.
	 *
	 * @param memberId
	 * @param quarter
	 * @param startDate
	 * @param endDate
	 * @return the MemberAimlDtl query example
	 */
	private Specification<MemberAimlDtl> getExample(final Long memberId, final String quarter, final Date startDate,
			final Date endDate) {
		return (root, query, builder) -> {
			Predicate result = builder.equal(root.get("submissionDone"), "Y");
			if (memberId != null) {
				result = builder.and(result, builder.equal(root.get("memId"), memberId));
			}
			if (StringUtils.isNotEmpty(quarter)) {
				result = builder.and(result, builder.equal(root.get("quarterName"), quarter));
			}
			if (startDate != null) {
				result = builder.and(result, builder.greaterThanOrEqualTo(root.get("updateDt"), startDate));
			}
			if (endDate != null) {
				result = builder.and(result, builder.lessThanOrEqualTo(root.get("updateDt"), endDate));
			}
			return result;
		};
	}

	private Specification<TblAimlSystemDetail> getExample1(final Long memberId, final String quarter) {
		return (root, query, builder) -> {
			Predicate result = builder.equal(root.get("memberId"), memberId);
			/*
			 * if (memberId != null) { result = builder.and(result,
			 * builder.equal(root.get("memberId"), memberId)); }
			 */
			if (StringUtils.isNotEmpty(quarter)) {
				result = builder.and(result, builder.equal(root.get("quarterName"), quarter));
			}
			return result;
		};
	}

	
}