package com.nseindia.mc.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Verification Details Entity.
 */

@Entity
@Getter
@NoArgsConstructor
@Setter
@Table(name = "TBL_VERIFICATION_DETAILS")
public class VerificationDetails extends AuditableEntity {

    @Id
    @Column(name = "verify_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long verifyId;

    @Column(name = "verify_type")
    private String verifyType;

    @Column(name = "verify_entity")
    private String verifyEntity;

    @Column(name = "verified_flag")
    private Boolean verifiedFlag;

    @Column(name = "verified_status")
    private String verifiedStatus;

    @Column(name = "reason")
    private String reason;

    @Column(name = "name")
    private String name;
}