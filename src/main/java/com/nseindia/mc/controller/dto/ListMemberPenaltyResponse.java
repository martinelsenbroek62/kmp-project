package com.nseindia.mc.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class ListMemberPenaltyResponse {
  private List<MemberPenaltyDetailsDto> memberPenaltyDetails;
}
