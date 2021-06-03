package com.nseindia.mc.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Entity
@Getter
@NoArgsConstructor
@Setter
@Table(name = "TBL_KMP_APPLI_DTLS")
@Data
public class KMPApplicationDetails extends AuditableEntity {
	
	@Id
	@Column(name = "APP_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "MEMBER_NAME")
	private String memberName;
	
	@Column(name = "MEMBER_CODE")
	private Long memberCode;
	
	@Column(name = "MEMBER_TYPE")
	private String memberType;
	
	@Column(name = "REQUEST_TYPE")
	private String requestType;
	
	@Column(name = "STATUS")
	private String status;
	
	@Column(name = "APPLICATION_STARTED_ON")
	private LocalDateTime applicationStartedOn;
	
	@Column(name = "APPLICATION_SUBMITTED_ON")
	private LocalDateTime applicationSubmittedOn;
	
	@Column(name = "REMARKS")
	private String remarks;
	
	@Column(name = "DELETED_BY")
	private String deletedBy;
	
	@Column(name = "DELETED_DATE")
	private LocalDateTime deletedDate;
}
