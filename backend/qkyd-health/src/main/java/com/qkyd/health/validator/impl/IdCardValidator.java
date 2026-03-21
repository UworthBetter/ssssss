package com.qkyd.health.validator.impl;

import com.qkyd.health.utils.DataValidator;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * 身份证验证器
 *
 * @author OpenClaw
 * @since 2026-02-06
 */
public class IdCardValidator implements DataValidator.ValidationStrategy {

    // 简单验证18位身份证
    private static final Pattern ID_CARD_PATTERN = Pattern.compile(
            "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$"
    );

    @Override
    public DataValidator.ValidationResult validate(String fieldName, Object value, Map<String, Object> params) {
        if (value == null) {
            return new DataValidator.ValidationResult(fieldName, false, fieldName + "身份证号不能为空");
        }

        String idCard = value.toString().trim();
        if (idCard.isEmpty()) {
            return new DataValidator.ValidationResult(fieldName, false, fieldName + "身份证号不能为空");
        }

        if (!ID_CARD_PATTERN.matcher(idCard).matches()) {
            return new DataValidator.ValidationResult(fieldName, false, fieldName + "身份证号格式不正确");
        }

        // 可以添加更复杂的校验位验证逻辑

        return new DataValidator.ValidationResult(fieldName);
    }
}
