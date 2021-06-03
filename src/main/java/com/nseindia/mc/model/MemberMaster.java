package com.nseindia.mc.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Entity
@Table(name = "TBL_MEMBER_MASTER")
public class MemberMaster {
	@Id
	@Column(name = "MEM_ID")
	private Long memId;

	@Column(name = "MEM_CD")
	private String memCd;

	@Column(name = "MEM_NAME")
	private String memName;

	@Column(name = "MEM_CENTRE")
	private String memCentre;

	@Column(name = "TYPE")
	private String memType;

	@Column(name = "SEBI_REGISTRATION_NUM")
	private String sebiRegistrationNum;

	@Column(name = "MEM_PAN_NUM")
	private String memPanNum;

	@Column(name = "MEM_ENTITY_TYPE")
	private String memEntityType;

	@Column(name = "EMAIL_ID")
	private String email;

	/*
	 * The member constitution type
	 */
	@Column(name = "CONSTITUTION_TYPE")
	private String constitutionType;

	@Column(name = "CIN")
	private String cin;

	@Column(name = "FULL_STATUS")
	private String fullStatus;

	@Column(name = "FULL_STATUS_UPDATE_TIME")
	private Date fullStatusUpdateTime;

	@Column(name = "MEM_CREATED_BY_USER")
	private String memCreatedByUser;

	@Column(name = "MEM_CREATED_DATE")
	private Date memCreatedDate;

	@Column(name = "CLEARING_MEM_ID")
	private String clearingMemId;

	@Column(name = "MEM_CONTACT_NUM")
	private String memContactNum;

	@Column(name = "MEM_APPLICATION_DATE")
	private Date memApplicationDate;

	@Column(name = "MEM_APPROVAL_DATE")
	private Date memApprovalDate;

	@Column(name = "SEBI_REGISTRATION_NUM_PART_1")
	private String sebiRegistrationNumPart1;

	@Column(name = "SEBI_REGISTRATION_NUM_PART_2_MEM")
	private String sebiRegistrationNumPart2Mem;

	@Column(name = "SEBI_REGISTRATION_NUM_PART_3")
	private String sebiRegistrationNumPart3;

	@Column(name = "SEBI_REGISTRATION_NUM_PART_4")
	private String sebiRegistrationNumPart4;

	@Column(name = "MEM_ADDRESS_PART1")
	private String memAddressPart1;

	@Column(name = "MEM_ADDRESS_PART2")
	private String memAddressPart2;

	@Column(name = "MEM_ADDRESS_PART3")
	private String memAddressPart3;

	@Column(name = "SYSTEM_COMPLY_SEBI")
	private String systemComplySebi;

	@Column(name = "SYS_AUDIT_ADVERSE")
	private String sysAuditAdverse;

	@Column(name = "SYS_ENTITY_INSPECT")
	private String sysEntityInspect;

	@Column(name = "SYS_IRREGULARITIES")
	private String sysIrregularities;
}
