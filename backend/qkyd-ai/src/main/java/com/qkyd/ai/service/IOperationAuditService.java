package com.qkyd.ai.service;

import java.util.Map;

public interface IOperationAuditService {
    /**
     * 记录操作日志
     */
    void logOperation(Long eventId, String operationType, String operationDetail,
                     String abnormalType, Integer riskScore, String dispositionAction);

    /**
     * 获取事件的完整操作链路
     */
    java.util.List<Map<String, Object>> getEventAuditTrail(Long eventId);
}