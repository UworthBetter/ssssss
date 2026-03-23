package com.qkyd.ai.service;

import java.util.Map;

public interface IDispositionRuleEngine {
    /**
     * 根据异常类型和风险评分生成处置建议
     */
    String generateDisposition(String abnormalType, int riskScore);

    /**
     * 判断是否应该自动执行
     */
    boolean shouldAutoExecute(String abnormalType, int riskScore);

    /**
     * 获取通知级别
     */
    String getNotificationLevel(String abnormalType, int riskScore);
}