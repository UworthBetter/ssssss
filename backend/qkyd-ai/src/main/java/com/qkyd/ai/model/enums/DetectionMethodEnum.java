package com.qkyd.ai.model.enums;

/**
 * Detection Method Enum
 */
public enum DetectionMethodEnum {
    THRESHOLD("threshold", "Threshold Rule"),
    STATISTICAL("statistical", "Statistical Analysis"),
    LLM("llm", "Large Language Model");

    private final String code;
    private final String desc;

    DetectionMethodEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static DetectionMethodEnum getByCode(String code) {
        for (DetectionMethodEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
