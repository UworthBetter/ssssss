package com.qkyd.ai.model.enums;

/**
 * Data Quality Level Enum
 */
public enum DataQualityLevelEnum {
    HIGH("high", "High Quality"),
    MEDIUM("medium", "Medium Quality"),
    LOW("low", "Low Quality");

    private final String code;
    private final String desc;

    DataQualityLevelEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static DataQualityLevelEnum getByCode(String code) {
        for (DataQualityLevelEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
