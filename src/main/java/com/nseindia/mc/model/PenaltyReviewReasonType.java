package com.nseindia.mc.model;

import java.util.stream.Stream;

public enum PenaltyReviewReasonType {

  WAIVER_FULL(1), WAIVER_PARTIAL(2), LEVY(3);

  private int code;

  PenaltyReviewReasonType(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public static PenaltyReviewReasonType fromCode(Integer code) {
    if (code == null) {
      return null;
    }
    return Stream.of(PenaltyReviewReasonType.values())
        .filter(c -> c.getCode() == code)
        .findFirst()
        .orElseThrow(IllegalArgumentException::new);
  }

  public static PenaltyReviewReasonType fromName(String name) {
    if (name == null) {
      return null;
    }
    return Stream.of(PenaltyReviewReasonType.values())
        .filter(c -> c.name().equals(name))
        .findFirst()
        .orElseThrow(IllegalArgumentException::new);
  }
}
