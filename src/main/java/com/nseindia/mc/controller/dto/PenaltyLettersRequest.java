package com.nseindia.mc.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class PenaltyLettersRequest {

    int submissionYear;
    int submissionMonth;
    List<PenaltyLetterDetailsDto> memberPenaltyDetails;
}
