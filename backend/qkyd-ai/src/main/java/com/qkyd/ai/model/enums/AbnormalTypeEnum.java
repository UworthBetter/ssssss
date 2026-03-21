package com.qkyd.ai.model.enums;

/**
 * Abnormal Type Enum
 */
public enum AbnormalTypeEnum {
    TOO_HIGH("too_high", "Too High"),
    TOO_LOW("too_low", "Too Low"),
    IRREGULAR("irregular", "Irregular");

    private final String code;
    private final String desc;

    AbnormalTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static AbnormalTypeEnum getByCode(String code) {
        for (AbnormalTypeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
