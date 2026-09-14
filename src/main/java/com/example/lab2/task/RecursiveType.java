package com.example.lab2.task;

public enum RecursiveType {
    DAY(1),
    WEEK(2),
    NONE(-1);

    private final int value;

    RecursiveType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static RecursiveType fromValue(int value) {
        for (RecursiveType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown RecursiveType value: " + value);
    }
}
