package com.nseindia.mc.model;

import java.util.stream.Stream;

public enum PenaltyApplicationStatus {
    APPLICATION_COMPLETED(1, "Application Completed"),
    APPLICATION_UNDER_REVIEW(2, "Application Under Review");

    private int code;
    String value;

    PenaltyApplicationStatus(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public static PenaltyApplicationStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Stream.of(PenaltyApplicationStatus.values())
            .filter(c -> c.getCode() == code)
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    public static PenaltyApplicationStatus fromName(String name) {
        if (name == null) {
            return null;
        }
        return Stream.of(PenaltyApplicationStatus.values())
            .filter(c -> c.name().equals(name))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    public static PenaltyApplicationStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Stream.of(PenaltyApplicationStatus.values())
            .filter(c -> c.getValue().equals(value))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}