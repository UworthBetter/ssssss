package com.qkyd.ai.service.impl;

import com.qkyd.ai.service.IOperationAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 操作审计服务 - 记录所有AI决策过程的操作留痕
 */
@Service
public class OperationAuditServiceImpl implements IOperationAuditService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void logOperation(Long eventId, String operationType, String operationDetail,
                           String abnormalType, Integer riskScore, String dispositionAction) {
        String sql = "INSERT INTO ai_operation_audit_log " +
                "(event_id, operation_type, operation_detail, abnormal_type, risk_score, disposition_action, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql, eventId, operationType, operationDetail, abnormalType, riskScore, dispositionAction, LocalDateTime.now());
    }

    @Override
    public List<Map<String, Object>> getEventAuditTrail(Long eventId) {
        String sql = "SELECT operation_type, operation_detail, abnormal_type, risk_score, " +
                "disposition_action, operation_result, created_at " +
                "FROM ai_operation_audit_log WHERE event_id = ? ORDER BY created_at ASC";

        return jdbcTemplate.queryForList(sql, eventId);
    }
}