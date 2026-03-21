package com.qkyd.health.validator.impl;

import com.qkyd.health.utils.DataValidator;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * 电话号码验证器（固定电话）
 *
 * @author OpenClaw
 * @since 2026-02-06
 */
public class PhoneValidator implements DataValidator.ValidationStrategy {

    // 简单的电话号码验证（支持 0XX-XXXXXXXX 格式）
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^0\\d{2,3}-?\\d{7,8}$"
    );

    @Override
    public DataValidator.ValidationResult validate(String fieldName, Object value, Map<String, Object> params) {
        if (value == null) {
            return new DataValidator.ValidationResult(fieldName, false, fieldName + "电话号码不能为空");
        }

        String phone = value.toString().trim();
        if (phone.isEmpty()) {
            return new DataValidator.ValidationResult(fieldName, false, fieldName + "电话号码不能为空");
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return new DataValidator.ValidationResult(fieldName, false, fieldName + "电话号码格式不正确");
        }

        return new DataValidator.ValidationResult(fieldName);
    }
}
