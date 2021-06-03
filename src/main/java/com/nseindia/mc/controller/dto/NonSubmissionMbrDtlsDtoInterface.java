package com.nseindia.mc.controller.dto;

public interface NonSubmissionMbrDtlsDtoInterface {
  Long getMemberId();
  String getMemberCode();
  String getMemberName();
  Integer getNonSubmissionCount();
  Integer getYear();
  Integer getMonth();
}
