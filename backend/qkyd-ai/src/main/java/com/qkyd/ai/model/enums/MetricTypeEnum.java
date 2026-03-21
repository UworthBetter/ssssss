package com.qkyd.ai.model.enums;

/**
 * Metric Type Enum
 */
public enum MetricTypeEnum {
    HEART_RATE("heart_rate", "Heart Rate"),
    BLOOD_PRESSURE("blood_pressure", "Blood Pressure"),
    TEMP("temp", "Body Temperature"),
    SPO2("spo2", "Blood Oxygen");

    private final String code;
    private final String desc;

    MetricTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static MetricTypeEnum getByCode(String code) {
        for (MetricTypeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
