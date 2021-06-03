package com.nseindia.mc.model;

import java.util.stream.Stream;

public enum PenaltyApplicationSubStatus {
    REVIEW_PENDING(1, "Review pending"),
    IN_PROGRESS(2, "In progress"),
    POST_CHECKS(3, "Post Checks");

    private int code;
    String value;

    PenaltyApplicationSubStatus(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public static PenaltyApplicationSubStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Stream.of(PenaltyApplicationSubStatus.values())
            .filter(c -> c.getCode() == code)
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    public static PenaltyApplicationSubStatus fromName(String name) {
        if (name == null) {
            return null;
        }
        return Stream.of(PenaltyApplicationSubStatus.values())
            .filter(c -> c.name().equals(name))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    public static PenaltyApplicationSubStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Stream.of(PenaltyApplicationSubStatus.values())
            .filter(c -> c.getValue().equals(value))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}