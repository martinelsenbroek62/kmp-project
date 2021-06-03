package com.nseindia.mc.model;

import java.util.stream.Stream;

public enum PenaltyLetterType {
    PENALTY_LETTER(1),
    PENALTY_REVERSAL_LETTER(2);

    private int code;

    PenaltyLetterType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static PenaltyLetterType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Stream.of(PenaltyLetterType.values())
            .filter(c -> c.getCode() == code)
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    public static PenaltyLetterType fromName(String name) {
        if (name == null) {
            return null;
        }
        return Stream.of(PenaltyLetterType.values())
            .filter(c -> c.name().equals(name))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}