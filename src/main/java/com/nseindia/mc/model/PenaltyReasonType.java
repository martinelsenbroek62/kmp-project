package com.nseindia.mc.model;

import java.util.stream.Stream;

public enum PenaltyReasonType {
    OTHERS(1);

    private int code;

    PenaltyReasonType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static PenaltyReasonType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Stream.of(PenaltyReasonType.values())
            .filter(c -> c.getCode() == code)
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    public static PenaltyReasonType fromName(String name) {
        if (name == null) {
            return null;
        }
        return Stream.of(PenaltyReasonType.values())
            .filter(c -> c.name().equals(name))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}