package com.qkyd.ai.model.enums;

/**
 * Trend Direction Enum
 */
public enum TrendDirectionEnum {
    UP("up", "Up"),
    DOWN("down", "Down"),
    STABLE("stable", "Stable");

    private final String code;
    private final String desc;

    TrendDirectionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static TrendDirectionEnum getByCode(String code) {
        for (TrendDirectionEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
