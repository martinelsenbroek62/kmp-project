package com.nseindia.mc.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class PenaltyLetterDetailsResponse {
  private List<PenaltyLetterDetailsDto> penaltyLetterDetails;
}
