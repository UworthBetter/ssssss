package com.qkyd.health.validator.impl;

import com.qkyd.health.utils.DataValidator;

import java.util.Map;

/**
 * 必填项验证器
 *
 * @author OpenClaw
 * @since 2026-02-06
 */
public class RequiredValidator implements DataValidator.ValidationStrategy {

    @Override
    public DataValidator.ValidationResult validate(String fieldName, Object value, Map<String, Object> params) {
        if (value == null) {
            return new DataValidator.ValidationResult(fieldName, false, fieldName + "不能为空");
        }

        if (value instanceof String) {
            String strValue = (String) value;
            if (strValue.trim().isEmpty()) {
                return new DataValidator.ValidationResult(fieldName, false, fieldName + "不能为空");
            }
        }

        return new DataValidator.ValidationResult(fieldName);
    }
}
