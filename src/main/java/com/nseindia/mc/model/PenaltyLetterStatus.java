package com.nseindia.mc.model;

import java.util.stream.Stream;

public enum PenaltyLetterStatus {
    NOT_GENERATED(1),
    NOT_SENT(2),
    SENT_TO_MEMBER(3);

    private int code;

    PenaltyLetterStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static PenaltyLetterStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Stream.of(PenaltyLetterStatus.values())
            .filter(c -> c.getCode() == code)
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    public static PenaltyLetterStatus fromName(String name) {
        if (name == null) {
            return null;
        }
        return Stream.of(PenaltyLetterStatus.values())
            .filter(c -> c.name().equals(name))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}