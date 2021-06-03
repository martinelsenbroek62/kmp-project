package com.nseindia.mc.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "TBL_USER_MEM_COM")
public class UserMemCom extends IdentifiableEntity {

  @Column(name = "FIRST_NAME")
  private String firstName;

  @Column(name = "LAST_NAME")
  private String lastName;

  @Column(name = "EMAIL_ID")
  private String emailId;

  @Column(name = "USER_TYPE")
  private String userType;

  @Column(name = "RO")
  private String ro;

  @Column(name = "HO")
  private String ho;
}
