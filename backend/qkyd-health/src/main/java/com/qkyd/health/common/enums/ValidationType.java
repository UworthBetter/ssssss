package com.qkyd.health.common.enums;

/**
 * 验证类型枚举
 *
 * @author OpenClaw
 * @since 2026-02-06
 */
public enum ValidationType {
    /**
     * 必填项检查
     */
    REQUIRED,

    /**
     * 手机号验证
     */
    MOBILE,

    /**
     * 邮箱验证
     */
    EMAIL,

    /**
     * 身份证验证
     */
    ID_CARD,

    /**
     * 日期验证
     */
    DATE,

    /**
     * 数字验证
     */
    NUMBER,

    /**
     * 整数验证
     */
    INTEGER,

    /**
     * 小数验证
     */
    DECIMAL,

    /**
     * 正数验证
     */
    POSITIVE,

    /**
     * 范围验证
     */
    RANGE,

    /**
     * 长度验证
     */
    LENGTH,

    /**
     * 电话号码验证（固定电话）
     */
    PHONE,

    /**
     * 正则表达式验证
     */
    REGEX,

    /**
     * 枚举值验证
     */
    ENUM,

    /**
     * URL验证
     */
    URL,

    /**
     * IP地址验证
     */
    IP
}
