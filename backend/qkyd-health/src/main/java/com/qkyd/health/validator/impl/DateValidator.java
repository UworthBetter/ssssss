package com.qkyd.health.validator.impl;

import com.qkyd.health.utils.DataValidator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * 日期验证器
 *
 * @author OpenClaw
 * @since 2026-02-06
 */
public class DateValidator implements DataValidator.ValidationStrategy {

    @Override
    public DataValidator.ValidationResult validate(String fieldName, Object value, Map<String, Object> params) {
        if (value == null) {
            return new DataValidator.ValidationResult(fieldName, false, fieldName + "日期不能为空");
        }

        String dateStr = value.toString().trim();
        if (dateStr.isEmpty()) {
            return new DataValidator.ValidationResult(fieldName, false, fieldName + "日期不能为空");
        }

        // 从参数中获取日期格式，默认为 yyyy-MM-dd
        String format = params != null && params.get("format") != null
                ? params.get("format").toString()
                : "yyyy-MM-dd";

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false); // 严格模式

        try {
            sdf.parse(dateStr);
            return new DataValidator.ValidationResult(fieldName);
        } catch (ParseException e) {
            return new DataValidator.ValidationResult(fieldName, false,
                    fieldName + "日期格式不正确，期望格式: " + format);
        }
    }
}
