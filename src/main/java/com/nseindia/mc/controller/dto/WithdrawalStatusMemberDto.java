package com.nseindia.mc.controller.dto;

import lombok.Data;

@Data
public class WithdrawalStatusMemberDto {
    String memberName;
    String memberCode;
    String forMonth;
    String coolingPeriodStartDate;
    String coolingPeriodEndDate;
}
