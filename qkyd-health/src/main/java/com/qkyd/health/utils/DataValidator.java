package com.qkyd.health.utils;

import com.qkyd.health.common.enums.ValidationType;
import com.qkyd.health.validator.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 数据验证工具类
 *
 * 功能：
 * 1. 必填项检查
 * 2. 格式验证（日期、数字、手机号等）
 * 3. 数据长度和范围验证
 * 4. 重复数据检测
 * 5. 外键关联验证
 * 6. 支持自定义验证规则（策略模式）
 *
 * @author OpenClaw
 * @since 2026-02-06
 */
public class DataValidator {

    private static final Logger logger = LoggerFactory.getLogger(DataValidator.class);

    // 验证器策略映射表
    private final Map<ValidationType, ValidationStrategy> validatorMap;

    // 自定义验证规则缓存
    private final Map<String, ValidationStrategy> customValidators;

    public DataValidator() {
        this.validatorMap = new HashMap<>();
        this.customValidators = new HashMap<>();
        initDefaultValidators();
    }

    /**
     * 初始化默认验证器
     */
    private void initDefaultValidators() {
        validatorMap.put(ValidationType.REQUIRED, new RequiredValidator());
        validatorMap.put(ValidationType.MOBILE, new MobileValidator());
        validatorMap.put(ValidationType.EMAIL, new EmailValidator());
        validatorMap.put(ValidationType.ID_CARD, new IdCardValidator());
        validatorMap.put(ValidationType.DATE, new DateValidator());
        validatorMap.put(ValidationType.NUMBER, new NumberValidator());
        validatorMap.put(ValidationType.INTEGER, new IntegerValidator());
        validatorMap.put(ValidationType.DECIMAL, new DecimalValidator());
        validatorMap.put(ValidationType.POSITIVE, new PositiveValidator());
        validatorMap.put(ValidationType.RANGE, new RangeValidator());
        validatorMap.put(ValidationType.LENGTH, new LengthValidator());
        validatorMap.put(ValidationType.PHONE, new PhoneValidator());
    }

    /**
     * 注册自定义验证器
     *
     * @param name 验证器名称
     * @param validator 验证器策略
     */
    public void registerValidator(String name, ValidationStrategy validator) {
        customValidators.put(name, validator);
        logger.info("注册自定义验证器: {}", name);
    }

    /**
     * 验证单个字段
     *
     * @param fieldName 字段名
     * @param value 字段值
     * @param validationTypes 验证类型列表
     * @return 验证结果
     */
    public ValidationResult validateField(String fieldName, Object value, ValidationType... validationTypes) {
        return validateField(fieldName, value, null, validationTypes);
    }

    /**
     * 验证单个字段（带参数）
     *
     * @param fieldName 字段名
     * @param value 字段值
     * @param params 验证参数（如范围、长度等）
     * @param validationTypes 验证类型列表
     * @return 验证结果
     */
    public ValidationResult validateField(String fieldName, Object value, Map<String, Object> params, ValidationType... validationTypes) {
        ValidationResult result = new ValidationResult(fieldName);
        result.setValid(true);

        if (validationTypes == null || validationTypes.length == 0) {
            return result;
        }

        for (ValidationType type : validationTypes) {
            ValidationStrategy validator = validatorMap.get(type);
            if (validator == null) {
                logger.warn("未找到验证器: {}", type);
                continue;
            }

            try {
                ValidationResult subResult = validator.validate(fieldName, value, params);
                if (!subResult.isValid()) {
                    result.setValid(false);
                    result.addError(subResult.getErrorMessage());
                    logger.debug("字段 {} 验证失败: {}", fieldName, subResult.getErrorMessage());
                    // 根据配置决定是否继续验证
                    break;
                }
            } catch (Exception e) {
                result.setValid(false);
                result.addError("验证异常: " + e.getMessage());
                logger.error("字段 {} 验证异常", fieldName, e);
            }
        }

        return result;
    }

    /**
     * 使用自定义验证器验证字段
     *
     * @param validatorName 验证器名称
     * @param fieldName 字段名
     * @param value 字段值
     * @return 验证结果
     */
    public ValidationResult validateWithCustom(String validatorName, String fieldName, Object value) {
        return validateWithCustom(validatorName, fieldName, value, null);
    }

    /**
     * 使用自定义验证器验证字段（带参数）
     *
     * @param validatorName 验证器名称
     * @param fieldName 字段名
     * @param value 字段值
     * @param params 验证参数
     * @return 验证结果
     */
    public ValidationResult validateWithCustom(String validatorName, String fieldName, Object value, Map<String, Object> params) {
        ValidationStrategy validator = customValidators.get(validatorName);
        if (validator == null) {
            return new ValidationResult(fieldName, false, "未找到自定义验证器: " + validatorName);
        }

        return validator.validate(fieldName, value, params);
    }

    /**
     * 批量验证多个字段
     *
     * @param dataMap 字段名到值的映射
     * @param validationConfig 验证配置（字段名 -> 验证类型列表）
     * @return 验证结果列表
     */
    public List<ValidationResult> batchValidate(Map<String, Object> dataMap,
                                                Map<String, ValidationType[]> validationConfig) {
        return batchValidate(dataMap, validationConfig, null);
    }

    /**
     * 批量验证多个字段（带参数配置）
     *
     * @param dataMap 字段名到值的映射
     * @param validationConfig 验证配置（字段名 -> 验证类型列表）
     * @param paramsConfig 参数配置（字段名 -> 参数映射）
     * @return 验证结果列表
     */
    public List<ValidationResult> batchValidate(Map<String, Object> dataMap,
                                                 Map<String, ValidationType[]> validationConfig,
                                                 Map<String, Map<String, Object>> paramsConfig) {
        List<ValidationResult> results = new ArrayList<>();

        for (Map.Entry<String, ValidationType[]> entry : validationConfig.entrySet()) {
            String fieldName = entry.getKey();
            Object value = dataMap.get(fieldName);
            ValidationType[] types = entry.getValue();

            Map<String, Object> params = paramsConfig != null ? paramsConfig.get(fieldName) : null;
            ValidationResult result = validateField(fieldName, value, params, types);
            results.add(result);
        }

        return results;
    }

    /**
     * 检测重复数据
     *
     * @param dataList 数据列表
     * @param uniqueFields 唯一性字段列表
     * @return 重复数据信息（字段名 -> 重复值集合）
     */
    public Map<String, Set<Object>> detectDuplicates(List<Map<String, Object>> dataList, String... uniqueFields) {
        Map<String, Set<Object>> duplicates = new HashMap<>();
        if (uniqueFields == null || uniqueFields.length == 0) {
            return duplicates;
        }

        for (String field : uniqueFields) {
            Map<Object, List<Integer>> valueIndexMap = new HashMap<>();
            Set<Object> duplicateValues = new HashSet<>();

            for (int i = 0; i < dataList.size(); i++) {
                Object value = dataList.get(i).get(field);
                if (value == null) {
                    continue;
                }

                valueIndexMap.computeIfAbsent(value, k -> new ArrayList<>()).add(i);

                if (valueIndexMap.get(value).size() > 1) {
                    duplicateValues.add(value);
                }
            }

            if (!duplicateValues.isEmpty()) {
                duplicates.put(field, duplicateValues);
                logger.info("检测到字段 {} 的重复数据: {} 个", field, duplicateValues.size());
            }
        }

        return duplicates;
    }

    /**
     * 检测重复数据（返回详细行号信息）
     *
     * @param dataList 数据列表
     * @param uniqueFields 唯一性字段列表
     * @return 重复数据详情（字段名 -> 值 -> 行号集合）
     */
    public Map<String, Map<Object, Set<Integer>>> detectDuplicatesWithRows(List<Map<String, Object>> dataList,
                                                                              String... uniqueFields) {
        Map<String, Map<Object, Set<Integer>>> duplicateDetails = new HashMap<>();
        if (uniqueFields == null || uniqueFields.length == 0) {
            return duplicateDetails;
        }

        for (String field : uniqueFields) {
            Map<Object, Set<Integer>> valueRowMap = new HashMap<>();

            for (int i = 0; i < dataList.size(); i++) {
                Object value = dataList.get(i).get(field);
                if (value == null) {
                    continue;
                }

                valueRowMap.computeIfAbsent(value, k -> new HashSet<>()).add(i + 1); // 行号从1开始
            }

            // 筛选出重复的数据（行号数>1）
            Map<Object, Set<Integer>> duplicates = valueRowMap.entrySet().stream()
                    .filter(entry -> entry.getValue().size() > 1)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            if (!duplicates.isEmpty()) {
                duplicateDetails.put(field, duplicates);
            }
        }

        return duplicateDetails;
    }

    /**
     * 检查必填字段
     *
     * @param dataMap 数据映射
     * @param requiredFields 必填字段列表
     * @return 缺失字段集合
     */
    public Set<String> checkRequiredFields(Map<String, Object> dataMap, String... requiredFields) {
        Set<String> missingFields = new HashSet<>();

        for (String field : requiredFields) {
            Object value = dataMap.get(field);
            if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
                missingFields.add(field);
            }
        }

        return missingFields;
    }

    /**
     * 快速验证手机号
     *
     * @param mobile 手机号
     * @return 是否有效
     */
    public static boolean isValidMobile(String mobile) {
        if (mobile == null || mobile.isEmpty()) {
            return false;
        }
        String regex = "^1[3-9]\\d{9}$";
        return Pattern.matches(regex, mobile);
    }

    /**
     * 快速验证邮箱
     *
     * @param email 邮箱
     * @return 是否有效
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(regex, email);
    }

    /**
     * 快速验证身份证号
     *
     * @param idCard 身份证号
     * @return 是否有效
     */
    public static boolean isValidIdCard(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return false;
        }
        // 简单验证18位身份证
        String regex = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$";
        return Pattern.matches(regex, idCard);
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private String fieldName;
        private boolean valid;
        private List<String> errors;

        public ValidationResult(String fieldName) {
            this.fieldName = fieldName;
            this.valid = true;
            this.errors = new ArrayList<>();
        }

        public ValidationResult(String fieldName, boolean valid, String errorMessage) {
            this.fieldName = fieldName;
            this.valid = valid;
            this.errors = new ArrayList<>();
            if (errorMessage != null) {
                this.errors.add(errorMessage);
            }
        }

        public void addError(String error) {
            this.errors.add(error);
        }

        public String getErrorMessage() {
            return errors.isEmpty() ? null : String.join("; ", errors);
        }

        public List<String> getErrors() {
            return errors;
        }

        // Getters and Setters
        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        @Override
        public String toString() {
            return String.format("ValidationResult{fieldName='%s', valid=%s, errors=%s}",
                    fieldName, valid, errors);
        }
    }

    /**
     * 验证器策略接口
     */
    public interface ValidationStrategy {
        /**
         * 执行验证
         *
         * @param fieldName 字段名
         * @param value 字段值
         * @param params 验证参数
         * @return 验证结果
         */
        ValidationResult validate(String fieldName, Object value, Map<String, Object> params);
    }
}
