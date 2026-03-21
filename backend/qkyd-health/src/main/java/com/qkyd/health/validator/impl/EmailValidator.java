package com.qkyd.health.validator.impl;

import com.qkyd.health.utils.DataValidator;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * 邮箱验证器
 *
 * @author OpenClaw
 * @since 2026-02-06
 */
public class EmailValidator implements DataValidator.ValidationStrategy {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    @Override
    public DataValidator.ValidationResult validate(String fieldName, Object value, Map<String, Object> params) {
        if (value == null) {
            return new DataValidator.ValidationResult(fieldName, false, fieldName + "邮箱不能为空");
        }

        String email = value.toString().trim();
        if (email.isEmpty()) {
            return new DataValidator.ValidationResult(fieldName, false, fieldName + "邮箱不能为空");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return new DataValidator.ValidationResult(fieldName, false, fieldName + "邮箱格式不正确");
        }

        return new DataValidator.ValidationResult(fieldName);
    }
}
