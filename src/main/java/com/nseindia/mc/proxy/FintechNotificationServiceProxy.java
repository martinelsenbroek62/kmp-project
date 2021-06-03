package com.nseindia.mc.proxy;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

/** Client to call fintech notification service. */
@Service
public class FintechNotificationServiceProxy {

  public String sendOTPViaSms(String mobile) {
    return RandomStringUtils.randomNumeric(6);
  }
}
