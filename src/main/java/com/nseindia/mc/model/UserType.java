package com.nseindia.mc.model;

import java.util.stream.Stream;

public enum UserType {
    MAKER(1),
    CHECKER(2);

    private int code;

    UserType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static UserType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Stream.of(UserType.values())
            .filter(c -> c.getCode() == code)
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    public static UserType fromName(String name) {
        if (name == null) {
            return null;
        }
        return Stream.of(UserType.values())
            .filter(c -> c.name().equals(name))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}