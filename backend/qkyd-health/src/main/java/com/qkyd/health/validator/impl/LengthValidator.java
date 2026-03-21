package com.qkyd.health.validator.impl;

import com.qkyd.health.utils.DataValidator;

import java.util.Map;

/**
 * 长度验证器
 *
 * @author OpenClaw
 * @since 2026-02-06
 */
public class LengthValidator implements DataValidator.ValidationStrategy {

    @Override
    public DataValidator.ValidationResult validate(String fieldName, Object value, Map<String, Object> params) {
        if (value == null) {
            return new DataValidator.ValidationResult(fieldName, false, fieldName + "值不能为空");
        }

        String str = value.toString();
        int length = str.length();

        // 从参数中获取长度限制
        if (params != null) {
            // 检查最小长度
            if (params.containsKey("min")) {
                int min = Integer.parseInt(params.get("min").toString());
                if (length < min) {
                    return new DataValidator.ValidationResult(fieldName, false,
                            fieldName + "长度不能少于" + min + "个字符（当前" + length + "个）");
                }
            }

            // 检查最大长度
            if (params.containsKey("max")) {
                int max = Integer.parseInt(params.get("max").toString());
                if (length > max) {
                    return new DataValidator.ValidationResult(fieldName, false,
                            fieldName + "长度不能超过" + max + "个字符（当前" + length + "个）");
                }
            }

            // 检查固定长度
            if (params.containsKey("fixed")) {
                int fixed = Integer.parseInt(params.get("fixed").toString());
                if (length != fixed) {
                    return new DataValidator.ValidationResult(fieldName, false,
                            fieldName + "长度必须为" + fixed + "个字符（当前" + length + "个）");
                }
            }
        }

        return new DataValidator.ValidationResult(fieldName);
    }
}
