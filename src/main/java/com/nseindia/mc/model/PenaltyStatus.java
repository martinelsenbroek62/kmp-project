package com.nseindia.mc.model;

import java.util.stream.Stream;

public enum PenaltyStatus {
    NOT_SENT_TO_CHECKER(1, "Not Sent to Checker"),
    FOR_CHECKER_REVIEW(2, "For Checker Review"),
    FOR_MAKER_REVIEW(3, "For Maker Review"),
    APPROVED(4, "Approved");

    private int code;
    String value;

    PenaltyStatus(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public static PenaltyStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Stream.of(PenaltyStatus.values())
            .filter(c -> c.getCode() == code)
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    public static PenaltyStatus fromName(String name) {
        if (name == null) {
            return null;
        }
        return Stream.of(PenaltyStatus.values())
            .filter(c -> c.name().equals(name))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    public static PenaltyStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Stream.of(PenaltyStatus.values())
            .filter(c -> c.getValue().equals(value))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}