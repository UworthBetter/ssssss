package com.qkyd.health.service.impl;

import com.qkyd.health.domain.HealthSubject;
import com.qkyd.health.domain.HealthSubjectImportLog;
import com.qkyd.health.mapper.HealthSubjectImportLogMapper;
import com.qkyd.health.service.IHealthSubjectService;
import com.qkyd.health.utils.CsvParser;
import com.qkyd.health.utils.ExcelParser;
import com.qkyd.health.utils.IFileParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 批量导入服务类
 * 
 * 功能特性:
 * - 支持Excel和CSV文件批量导入
 * - 数据验证（必填项、格式、重复检测）
 * - 批量插入优化
 * - 事务管理（失败自动回滚）
 * - 详细的错误信息反馈
 * 
 * @author QKYD Team
 * @version 1.0
 */
@Service
public class BatchImportService {
    
    private static final Logger logger = LoggerFactory.getLogger(BatchImportService.class);
    
    /**
     * 批量插入批次大小
     */
    private static final int BATCH_SIZE = 500;
    
    /**
     * 手机号正则表达式
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    
    /**
     * 邮箱正则表达式
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    
    /**
     * 身份证号正则表达式
     */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$");
    
    @Autowired
    private IHealthSubjectService healthSubjectService;

    @Autowired
    private HealthSubjectImportLogMapper importLogMapper;
    
    /**
     * Excel解析器
     */
    private final ExcelParser excelParser = new ExcelParser();
    
    /**
     * CSV解析器
     */
    private final CsvParser csvParser = new CsvParser();
    
    /**
     * 批量导入结果类
     */
    public static class ImportResult {
        /** 是否成功 */
        private boolean success;
        
        /** 成功导入数量 */
        private int successCount;
        
        /** 失败数量 */
        private int failCount;
        
        /** 错误信息列表 */
        private List<String> errorMessages;
        
        /** 导入的实体列表（仅成功时有效） */
        private List<HealthSubject> importedSubjects;
        
        public ImportResult(boolean success, int successCount, int failCount, 
                           List<String> errorMessages, List<HealthSubject> importedSubjects) {
            this.success = success;
            this.successCount = successCount;
            this.failCount = failCount;
            this.errorMessages = errorMessages;
            this.importedSubjects = importedSubjects;
        }
        
        public static ImportResult success(int successCount, List<HealthSubject> importedSubjects) {
            return new ImportResult(true, successCount, 0, new ArrayList<>(), importedSubjects);
        }
        
        public static ImportResult fail(List<String> errorMessages) {
            return new ImportResult(false, 0, 0, errorMessages, null);
        }
        
        public static ImportResult partial(int successCount, int failCount, 
                                          List<String> errorMessages, List<HealthSubject> importedSubjects) {
            return new ImportResult(false, successCount, failCount, errorMessages, importedSubjects);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public int getSuccessCount() { return successCount; }
        public int getFailCount() { return failCount; }
        public List<String> getErrorMessages() { return errorMessages; }
        public List<HealthSubject> getImportedSubjects() { return importedSubjects; }
        
        @Override
        public String toString() {
            return String.format("ImportResult{success=%s, successCount=%d, failCount=%d, errors=%d}", 
                    success, successCount, failCount, 
                    errorMessages != null ? errorMessages.size() : 0);
        }
    }
    
    /**
     * 批量导入服务对象数据
     * 
     * @param filePath 文件路径
     * @return 导入结果
     */
    public ImportResult importSubjects(String filePath) {
        logger.info("开始批量导入，文件路径: {}", filePath);
        Date startTime = new Date();
        
        try {
            // 第一步：文件解析（在事务外执行，提高性能）
            IFileParser parser = getFileParser(filePath);
            List<Map<String, Object>> data = parser.parse(filePath);
            
            if (data == null || data.isEmpty()) {
                logger.warn("文件为空或没有数据");
                return ImportResult.fail(Collections.singletonList("文件为空或没有数据"));
            }
            
            logger.info("文件解析成功，共{}条数据", data.size());
            
            // 第二步：数据验证（在事务外执行，避免不必要的数据库查询）
            List<String> validationErrors = validateData(data);
            if (!validationErrors.isEmpty()) {
                logger.warn("数据验证失败，错误数: {}", validationErrors.size());
                return ImportResult.fail(validationErrors);
            }
            
            // 第三步：转换为实体对象（在事务外执行）
            List<HealthSubject> subjects = convertToEntities(data);
            if (subjects.isEmpty()) {
                logger.warn("数据转换失败");
                return ImportResult.fail(Collections.singletonList("数据转换失败"));
            }
            
            // 第四步：检查重复数据（在事务外执行，避免长事务）
            List<String> duplicateErrors = checkDuplicates(data);
            if (!duplicateErrors.isEmpty()) {
                logger.warn("检测到重复数据，错误数: {}", duplicateErrors.size());
                return ImportResult.fail(duplicateErrors);
            }
            
            // 第五步：执行事务性数据库操作
            return executeTransactionalImport(filePath, startTime, data, subjects);
            
        } catch (Exception e) {
            logger.error("批量导入失败", e);
            return ImportResult.fail(Collections.singletonList("导入过程发生异常: " + e.getMessage()));
        }
    }
    
    /**
     * 执行事务性导入操作
     * 
     * 事务管理策略：
     * 1. 所有数据库操作（日志插入、数据插入、日志更新）在同一事务中完成
     * 2. 任何异常都会触发自动回滚
     * 3. 使用RuntimeException确保Spring事务管理器能够正确回滚
     * 
     * @param filePath 文件路径
     * @param startTime 开始时间
     * @param data 原始数据
     * @param subjects 转换后的实体列表
     * @return 导入结果
     */
    @Transactional(rollbackFor = Exception.class)
    protected ImportResult executeTransactionalImport(String filePath, Date startTime, 
                                                     List<Map<String, Object>> data, 
                                                     List<HealthSubject> subjects) {
        HealthSubjectImportLog importLog = null;
        List<String> errors = new ArrayList<>();
        
        try {
            // 1. 创建并插入导入日志记录（状态为 PROCESSING）
            importLog = createImportLog(filePath, startTime);
            importLogMapper.insertHealthSubjectImportLog(importLog);
            logger.info("导入日志记录已创建，日志ID: {}", importLog.getId());
            
            // 2. 批量插入数据（核心业务操作）
            List<String> insertErrors = batchInsert(subjects);
            if (!insertErrors.isEmpty()) {
                // 插入失败，抛出异常触发回滚
                String errorMsg = String.join("; ", insertErrors);
                throw new RuntimeException("数据插入失败: " + errorMsg);
            }
            
            // 3. 更新日志为成功状态（确保事务内执行）
            updateImportLog(importLog, "SUCCESS", data.size(), subjects.size(), 0, null);
            logger.info("批量导入成功，共导入{}条数据，日志ID: {}", subjects.size(), importLog.getId());
            
            return ImportResult.success(subjects.size(), subjects);
            
        } catch (RuntimeException e) {
            // 捕获运行时异常，记录日志并重新抛出以触发回滚
            logger.error("事务性导入失败，触发回滚", e);
            
            // 尝试更新导入日志为失败状态（在当前事务内，如果此步失败也会回滚）
            if (importLog != null) {
                try {
                    updateImportLog(importLog, "FAILED", data.size(), 0, subjects.size(), 
                                  Collections.singletonList(e.getMessage()));
                } catch (Exception logEx) {
                    logger.error("更新失败日志时出错", logEx);
                    // 忽略日志更新异常，优先保证业务数据回滚
                }
            }
            
            // 重新抛出异常以触发事务回滚
            throw e;
            
        } catch (Exception e) {
            // 捕获其他异常，记录日志并包装为运行时异常以触发回滚
            logger.error("事务性导入发生未预期异常，触发回滚", e);
            
            if (importLog != null) {
                try {
                    updateImportLog(importLog, "FAILED", data.size(), 0, subjects.size(), 
                                  Collections.singletonList(e.getMessage()));
                } catch (Exception logEx) {
                    logger.error("更新失败日志时出错", logEx);
                }
            }
            
            throw new RuntimeException("导入过程中发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 创建导入日志记录
     * 
     * @param filePath 文件路径
     * @param startTime 开始时间
     * @return 导入日志对象
     */
    private HealthSubjectImportLog createImportLog(String filePath, Date startTime) {
        HealthSubjectImportLog importLog = new HealthSubjectImportLog();
        
        // 获取文件名
        File file = new File(filePath);
        importLog.setFileName(file.getName());
        importLog.setFilePath(filePath);
        
        // 获取导入类型
        String extension = getExtension(filePath);
        importLog.setImportType(extension.equals("csv") ? "CSV" : "EXCEL");
        
        // 获取当前用户信息（从 Spring Security 上下文获取）
        try {
            // TODO: 从 Spring Security 上下文获取当前用户信息
            // importLog.setUserId(SecurityUtils.getUserId());
            // importLog.setUserName(SecurityUtils.getUsername());
            importLog.setUserId(1L); // 临时设置，后续改为从上下文获取
            importLog.setUserName("admin"); // 临时设置，后续改为从上下文获取
        } catch (Exception e) {
            logger.warn("无法获取当前用户信息，使用默认值");
            importLog.setUserId(1L);
            importLog.setUserName("system");
        }
        
        // 设置初始状态
        importLog.setStatus("PROCESSING");
        importLog.setStartTime(startTime);
        importLog.setTotalRows(0);
        importLog.setSuccessRows(0);
        importLog.setFailRows(0);
        
        return importLog;
    }
    
    /**
     * 更新导入日志记录
     * 
     * @param importLog 导入日志对象
     * @param status 导入状态
     * @param totalRows 总行数
     * @param successRows 成功行数
     * @param failRows 失败行数
     * @param errors 错误信息列表
     */
    private void updateImportLog(HealthSubjectImportLog importLog, String status, 
                                 int totalRows, int successRows, int failRows, List<String> errors) {
        try {
            Date endTime = new Date();
            long costTime = endTime.getTime() - importLog.getStartTime().getTime();
            
            importLog.setStatus(status);
            importLog.setEndTime(endTime);
            importLog.setCostTime((int) costTime);
            importLog.setTotalRows(totalRows);
            importLog.setSuccessRows(successRows);
            importLog.setFailRows(failRows);
            
            // 如果有错误信息，拼接错误消息
            if (errors != null && !errors.isEmpty()) {
                String errorMsg = String.join("; ", errors);
                // 限制错误信息长度，避免超出数据库字段限制
                if (errorMsg.length() > 2000) {
                    errorMsg = errorMsg.substring(0, 2000) + "...";
                }
                importLog.setErrorMsg(errorMsg);
            }
            
            importLogMapper.updateHealthSubjectImportLog(importLog);
            logger.info("导入日志已更新: 状态={}, 总行数={}, 成功={}, 失败={}, 耗时={}ms", 
                    status, totalRows, successRows, failRows, costTime);
        } catch (Exception e) {
            logger.error("更新导入日志失败", e);
        }
    }
    
    /**
     * 根据文件类型获取解析器
     * 
     * @param filePath 文件路径
     * @return 文件解析器
     * @throws Exception 不支持的文件类型
     */
    private IFileParser getFileParser(String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new Exception("文件不存在: " + filePath);
        }
        
        String extension = getExtension(filePath);
        
        if (extension.equals("xls") || extension.equals("xlsx")) {
            return excelParser;
        } else if (extension.equals("csv")) {
            return csvParser;
        } else {
            throw new Exception("不支持的文件类型: " + extension);
        }
    }
    
    /**
     * 获取文件扩展名
     * 
     * @param filePath 文件路径
     * @return 文件扩展名（小写，不含点号）
     */
    private String getExtension(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filePath.length() - 1) {
            return "";
        }
        return filePath.substring(lastDotIndex + 1).toLowerCase();
    }
    
    /**
     * 验证数据
     * 
     * @param data 待验证数据
     * @return 验证错误列表
     */
    private List<String> validateData(List<Map<String, Object>> data) {
        List<String> errors = new ArrayList<>();
        
        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> row = data.get(i);
            int rowNum = i + 2; // Excel/CSV从第2行开始是数据（第1行是表头）
            
            // 验证必填项
            if (row.get("姓名") == null || row.get("姓名").toString().trim().isEmpty()) {
                errors.add(String.format("第%d行: 姓名不能为空", rowNum));
                continue; // 必填项为空，跳过后续验证
            }
            
            // 验证手机号
            String phone = row.get("手机号码") != null ? row.get("手机号码").toString().trim() : "";
            if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
                errors.add(String.format("第%d行: 手机号格式错误: %s", rowNum, phone));
            }
            
            // 验证邮箱
            String email = row.get("用户邮箱") != null ? row.get("用户邮箱").toString().trim() : "";
            if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
                errors.add(String.format("第%d行: 邮箱格式错误: %s", rowNum, email));
            }
            
            // 验证年龄
            Object ageObj = row.get("年龄");
            if (ageObj != null && !ageObj.toString().trim().isEmpty()) {
                try {
                    int age = Integer.parseInt(ageObj.toString());
                    if (age < 0 || age > 150) {
                        errors.add(String.format("第%d行: 年龄超出合理范围: %d", rowNum, age));
                    }
                } catch (NumberFormatException e) {
                    errors.add(String.format("第%d行: 年龄格式错误: %s", rowNum, ageObj));
                }
            }
            
            // 验证性别
            String sex = row.get("用户性别") != null ? row.get("用户性别").toString().trim() : "";
            if (!sex.isEmpty() && !sex.equals("0") && !sex.equals("1") && !sex.equals("2")) {
                errors.add(String.format("第%d行: 性别值无效: %s（应为0-男/1-女/2-未知）", rowNum, sex));
            }
        }
        
        return errors;
    }
    
    /**
     * 检查重复数据
     * 
     * @param data 待检查数据
     * @return 重复错误列表
     */
    private List<String> checkDuplicates(List<Map<String, Object>> data) {
        List<String> errors = new ArrayList<>();
        Set<String> phoneSet = new HashSet<>();
        
        // 检查文件内部重复
        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> row = data.get(i);
            int rowNum = i + 2;
            
            String phone = row.get("手机号码") != null ? row.get("手机号码").toString().trim() : "";
            
            if (!phone.isEmpty()) {
                if (phoneSet.contains(phone)) {
                    errors.add(String.format("第%d行: 手机号在文件中重复: %s", rowNum, phone));
                }
                phoneSet.add(phone);
            }
        }
        
        // 检查数据库重复（如果有手机号）
        if (!phoneSet.isEmpty()) {
            for (String phone : phoneSet) {
                HealthSubject query = new HealthSubject();
                query.setPhonenumber(phone);
                List<HealthSubject> existing = healthSubjectService.selectHealthSubjectList(query);
                
                if (existing != null && !existing.isEmpty()) {
                    errors.add(String.format("手机号在数据库中已存在: %s", phone));
                }
            }
        }
        
        return errors;
    }
    
    /**
     * 转换为实体对象
     * 
     * @param data 原始数据
     * @return 实体对象列表
     */
    private List<HealthSubject> convertToEntities(List<Map<String, Object>> data) {
        List<HealthSubject> subjects = new ArrayList<>();
        
        for (Map<String, Object> row : data) {
            HealthSubject subject = new HealthSubject();
            
            // 姓名（必填）
            String nickName = row.get("姓名") != null ? row.get("姓名").toString().trim() : "";
            subject.setNickName(nickName);
            subject.setSubjectName(nickName); // 默认服务对象名称为姓名
            
            // 手机号
            String phone = row.get("手机号码") != null ? row.get("手机号码").toString().trim() : "";
            if (!phone.isEmpty()) {
                subject.setPhonenumber(phone);
            }
            
            // 邮箱
            String email = row.get("用户邮箱") != null ? row.get("用户邮箱").toString().trim() : "";
            if (!email.isEmpty()) {
                subject.setEmail(email);
            }
            
            // 年龄
            Object ageObj = row.get("年龄");
            if (ageObj != null && !ageObj.toString().trim().isEmpty()) {
                try {
                    int age = Integer.parseInt(ageObj.toString());
                    subject.setAge(age);
                } catch (NumberFormatException e) {
                    // 忽略转换错误
                }
            }
            
            // 性别
            String sex = row.get("用户性别") != null ? row.get("用户性别").toString().trim() : "";
            if (!sex.isEmpty()) {
                subject.setSex(sex);
            } else {
                subject.setSex("2"); // 默认未知
            }
            
            // 账号状态（默认正常）
            subject.setStatus("0");
            subject.setDelFlag("0");
            
            // 用户类型（默认系统用户）
            subject.setUserType("00");
            
            subjects.add(subject);
        }
        
        return subjects;
    }
    
    /**
     * 批量插入数据
     * 
     * @param subjects 待插入的实体列表
     * @return 错误信息列表（如果有）
     */
    private List<String> batchInsert(List<HealthSubject> subjects) {
        List<String> errors = new ArrayList<>();
        
        try {
            // 分批插入，避免单次数据量过大
            int total = subjects.size();
            int batchCount = (total + BATCH_SIZE - 1) / BATCH_SIZE;
            
            for (int i = 0; i < batchCount; i++) {
                int fromIndex = i * BATCH_SIZE;
                int toIndex = Math.min(fromIndex + BATCH_SIZE, total);
                List<HealthSubject> batch = subjects.subList(fromIndex, toIndex);
                
                // 逐条插入（如果需要批量插入，需要在Mapper中实现）
                for (HealthSubject subject : batch) {
                    try {
                        int result = healthSubjectService.insertHealthSubject(subject);
                        if (result <= 0) {
                            errors.add(String.format("插入失败: %s", subject.getNickName()));
                        }
                    } catch (Exception e) {
                        logger.error("插入数据失败: {}", subject.getNickName(), e);
                        errors.add(String.format("插入异常: %s - %s", subject.getNickName(), e.getMessage()));
                    }
                }
                
                logger.info("批量插入进度: {}/{}", (i + 1) * BATCH_SIZE, total);
            }
            
        } catch (Exception e) {
            logger.error("批量插入异常", e);
            errors.add("批量插入过程发生异常: " + e.getMessage());
        }
        
        return errors;
    }
}
