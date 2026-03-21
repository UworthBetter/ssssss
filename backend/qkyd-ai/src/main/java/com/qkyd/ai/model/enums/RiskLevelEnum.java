package com.qkyd.ai.model.enums;

/**
 * Risk Level Enum
 */
public enum RiskLevelEnum {
    DANGER("danger", "Danger", 3),
    WARNING("warning", "Warning", 2),
    NORMAL("normal", "Normal", 1);

    private final String code;
    private final String desc;
    private final int level;

    RiskLevelEnum(String code, String desc, int level) {
        this.code = code;
        this.desc = desc;
        this.level = level;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public int getLevel() {
        return level;
    }

    public static RiskLevelEnum getByCode(String code) {
        for (RiskLevelEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
