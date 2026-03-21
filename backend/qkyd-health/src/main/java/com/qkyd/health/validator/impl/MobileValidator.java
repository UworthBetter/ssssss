package com.qkyd.health.validator.impl;

import com.qkyd.health.utils.DataValidator;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * 手机号验证器
 *
 * @author OpenClaw
 * @since 2026-02-06
 */
public class MobileValidator implements DataValidator.ValidationStrategy {

    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Override
    public DataValidator.ValidationResult validate(String fieldName, Object value, Map<String, Object> params) {
        if (value == null) {
            // 空值根据业务需求处理，这里认为空值不通过
            return new DataValidator.ValidationResult(fieldName, false, fieldName + "手机号不能为空");
        }

        String mobile = value.toString().trim();
        if (mobile.isEmpty()) {
            return new DataValidator.ValidationResult(fieldName, false, fieldName + "手机号不能为空");
        }

        if (!MOBILE_PATTERN.matcher(mobile).matches()) {
            return new DataValidator.ValidationResult(fieldName, false, fieldName + "手机号格式不正确");
        }

        return new DataValidator.ValidationResult(fieldName);
    }
}
