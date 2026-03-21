package com.qkyd.health.validator.impl;

import com.qkyd.health.utils.DataValidator;

import java.util.Map;

/**
 * 范围验证器
 *
 * @author OpenClaw
 * @since 2026-02-06
 */
public class RangeValidator implements DataValidator.ValidationStrategy {

    @Override
    public DataValidator.ValidationResult validate(String fieldName, Object value, Map<String, Object> params) {
        if (value == null) {
            return new DataValidator.ValidationResult(fieldName, false, fieldName + "数值不能为空");
        }

        if (params == null || (!params.containsKey("min") && !params.containsKey("max"))) {
            return new DataValidator.ValidationResult(fieldName, false,
                    "范围验证必须指定 min 或 max 参数");
        }

        try {
            double num = Double.parseDouble(value.toString());

            // 检查最小值
            if (params.containsKey("min")) {
                double min = Double.parseDouble(params.get("min").toString());
                if (num < min) {
                    return new DataValidator.ValidationResult(fieldName, false,
                            fieldName + "不能小于" + min);
                }
            }

            // 检查最大值
            if (params.containsKey("max")) {
                double max = Double.parseDouble(params.get("max").toString());
                if (num > max) {
                    return new DataValidator.ValidationResult(fieldName, false,
                            fieldName + "不能大于" + max);
                }
            }

            return new DataValidator.ValidationResult(fieldName);
        } catch (NumberFormatException e) {
            return new DataValidator.ValidationResult(fieldName, false,
                    fieldName + "必须是有效的数字");
        }
    }
}
