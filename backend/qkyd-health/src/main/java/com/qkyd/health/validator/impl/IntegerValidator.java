package com.qkyd.health.validator.impl;

import com.qkyd.health.utils.DataValidator;

import java.util.Map;

/**
 * 整数验证器
 *
 * @author OpenClaw
 * @since 2026-02-06
 */
public class IntegerValidator implements DataValidator.ValidationStrategy {

    @Override
    public DataValidator.ValidationResult validate(String fieldName, Object value, Map<String, Object> params) {
        if (value == null) {
            return new DataValidator.ValidationResult(fieldName, false, fieldName + "整数不能为空");
        }

        String numStr = value.toString().trim();
        if (numStr.isEmpty()) {
            return new DataValidator.ValidationResult(fieldName, false, fieldName + "整数不能为空");
        }

        try {
            Integer.parseInt(numStr);
            return new DataValidator.ValidationResult(fieldName);
        } catch (NumberFormatException e) {
            return new DataValidator.ValidationResult(fieldName, false, fieldName + "必须是有效的整数");
        }
    }
}
