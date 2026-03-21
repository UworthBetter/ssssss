package com.qkyd.health.validator.impl;

import com.qkyd.health.utils.DataValidator;

import java.util.Map;

/**
 * 小数验证器
 *
 * @author OpenClaw
 * @since 2026-02-06
 */
public class DecimalValidator implements DataValidator.ValidationStrategy {

    @Override
    public DataValidator.ValidationResult validate(String fieldName, Object value, Map<String, Object> params) {
        if (value == null) {
            return new DataValidator.ValidationResult(fieldName, false, fieldName + "小数不能为空");
        }

        String numStr = value.toString().trim();
        if (numStr.isEmpty()) {
            return new DataValidator.ValidationResult(fieldName, false, fieldName + "小数不能为空");
        }

        try {
            double num = Double.parseDouble(numStr);

            // 检查是否为小数（非整数）
            if (num == (long) num) {
                // 如果传入的是整数，根据业务需求决定是否通过
                // 这里假设整数也通过（因为整数也是小数的一种）
                return new DataValidator.ValidationResult(fieldName);
            }

            // 从参数中获取精度要求
            if (params != null && params.containsKey("scale")) {
                int scale = Integer.parseInt(params.get("scale").toString());
                String[] parts = numStr.split("\\.");
                if (parts.length > 1 && parts[1].length() > scale) {
                    return new DataValidator.ValidationResult(fieldName, false,
                            fieldName + "小数位数不能超过" + scale + "位");
                }
            }

            return new DataValidator.ValidationResult(fieldName);
        } catch (NumberFormatException e) {
            return new DataValidator.ValidationResult(fieldName, false, fieldName + "必须是有效的小数");
        }
    }
}
