package com.qkyd.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qkyd.ai.mapper.EventInsightSnapshotMapper;
import com.qkyd.ai.model.dto.EventInsightResultDTO;
import com.qkyd.ai.model.entity.EventInsightSnapshotRecord;
import com.qkyd.ai.service.IDispositionRuleEngine;
import com.qkyd.ai.service.IEventInsightService;
import com.qkyd.ai.service.IEventProcessingPipelineService;
import com.qkyd.ai.service.IOperationAuditService;
import com.qkyd.ai.service.IRiskScoreService;
import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.health.domain.UeitException;
import com.qkyd.health.service.IUeitExceptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventProcessingPipelineServiceImpl implements IEventProcessingPipelineService {

    private static final Logger log = LoggerFactory.getLogger(EventProcessingPipelineServiceImpl.class);

    @Autowired
    private IEventInsightService eventInsightService;

    @Autowired
    private IRiskScoreService riskScoreService;

    @Autowired
    private IDispositionRuleEngine dispositionRuleEngine;

    @Autowired
    private IOperationAuditService auditService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EventInsightSnapshotMapper eventInsightSnapshotMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IUeitExceptionService ueitExceptionService;

    @Override
    @Transactional
    public void startPipeline(Long eventId, Map<String, Object> abnormalData) {
        if (eventId == null) {
            throw new IllegalArgumentException("eventId cannot be null");
        }

        Map<String, Object> workingData = abnormalData != null ? new HashMap<>(abnormalData) : new HashMap<>();
        enrichFromSourceEvent(eventId, workingData);
        String abnormalType = stringValue(workingData.getOrDefault("abnormalType", "未知异常"));
        int priority = calculatePriority(workingData);
        Long pipelineId = ensurePipelineRecord(eventId, abnormalType, workingData, priority);

        try {
            updatePipelineStage(pipelineId, "DETECTED", priority, null, null, "IN_PROGRESS", null);
            auditService.logOperation(eventId, "DETECT", "异常事件进入处理流水线", abnormalType, priority, null);

            if (priority < 30) {
                updatePipelineStage(pipelineId, "COMPLETED", priority, "normal", null, "SUCCESS", "低优先级事件保留留痕，不触发处置");
                auditService.logOperation(eventId, "SKIP", "低优先级事件，结束流水线", abnormalType, priority, null);
                return;
            }

            EventInsightResultDTO insight = eventInsightService.buildInsight(eventId, false);
            persistInsight(pipelineId, insight);
            auditService.logOperation(eventId, "INSIGHT_BUILD", "AI 解析与上下文补全完成", abnormalType, priority, null);

            Map<String, Object> riskData = new HashMap<>(workingData);
            riskData.put("insight", insight);
            AjaxResult riskResult = riskScoreService.assessRisk(riskData);
            Map<String, Object> riskPayload = extractRiskPayload(riskResult, insight, priority);
            int riskScore = intValue(riskPayload.get("riskScore"), intValue(riskPayload.get("risk_score"), priority));
            String riskLevel = stringValue(
                    riskPayload.containsKey("riskLevel") ? riskPayload.get("riskLevel") : riskPayload.get("risk_level"));

            persistRisk(pipelineId, riskPayload);
            auditService.logOperation(eventId, "RISK_ASSESS", "风险评估完成，等级=" + riskLevel, abnormalType, riskScore, null);

            String disposition = dispositionRuleEngine.generateDisposition(abnormalType, riskScore);
            String notificationLevel = dispositionRuleEngine.getNotificationLevel(abnormalType, riskScore);
            boolean autoExecute = dispositionRuleEngine.shouldAutoExecute(abnormalType, riskScore);
            persistDisposition(pipelineId, disposition, notificationLevel, autoExecute);
            auditService.logOperation(eventId, "DISPOSITION", disposition, abnormalType, riskScore, disposition);

            if (autoExecute) {
                executeDisposition(pipelineId, disposition, notificationLevel);
                auditService.logOperation(eventId, "EXECUTE", "自动执行处置动作", abnormalType, riskScore, disposition);
            } else {
                updatePipelineStage(pipelineId, "COMPLETED", riskScore, riskLevel, disposition, "PENDING", "待人工确认处置");
            }
        } catch (Exception e) {
            markFailed(pipelineId, e.getMessage());
            auditService.logOperation(eventId, "ERROR", e.getMessage(), abnormalType, priority, null);
            throw new RuntimeException("Pipeline execution failed for event " + eventId, e);
        }
    }

    @Override
    public Map<String, Object> getPipelineStatus(Long eventId) {
        try {
            String sql = "SELECT id, event_id, abnormal_type, abnormal_value, stage, priority, risk_score, risk_level, "
                    + "disposition_suggestion, notification_level, auto_execute, execution_status, execution_result, "
                    + "actual_outcome, feedback_score, created_at, updated_at "
                    + "FROM ai_event_processing_pipeline WHERE event_id = ? ORDER BY id DESC LIMIT 1";

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, eventId);
            if (rows.isEmpty()) {
                return Map.of();
            }

            Map<String, Object> result = new LinkedHashMap<>(rows.get(0));
            List<Map<String, Object>> auditTrail = auditService.getEventAuditTrail(eventId);
            result.put("auditTrail", auditTrail);

            List<Map<String, Object>> snapshots = jdbcTemplate.queryForList(
                    "SELECT id, summary, risk_level, risk_score, generated_at "
                            + "FROM ai_event_insight_snapshot WHERE event_id = ? ORDER BY id DESC LIMIT 3",
                    eventId
            );
            result.put("recentInsightSnapshots", snapshots);
            result.put("agentTrace", List.of());
            if (!snapshots.isEmpty()) {
                result.put("insightSummary", stringValue(snapshots.get(0).get("summary")));
            }
            result.put("stages", buildStages(result, auditTrail, snapshots, null));
            result.put("totalDuration", calculateDurationMinutes(result));

            return result;
        } catch (Exception e) {
            log.warn("failed to load pipeline status for event {}, fallback to minimal status", eventId, e);
            return loadMinimalPipelineStatus(eventId);
        }
    }

    @Override
    public void recordFeedback(Long pipelineId, String actualOutcome, Integer feedbackScore) {
        jdbcTemplate.update("UPDATE ai_event_processing_pipeline SET actual_outcome = ?, feedback_score = ?, "
                        + "updated_at = NOW() WHERE id = ? OR event_id = ?",
                actualOutcome,
                feedbackScore,
                pipelineId,
                pipelineId);
        auditService.logOperation(pipelineId,
                "FEEDBACK",
                "记录处置反馈: " + stringValue(actualOutcome),
                null,
                feedbackScore != null ? feedbackScore : 0,
                null);
    }

    private int calculatePriority(Map<String, Object> data) {
        String riskLevel = stringValue(data.getOrDefault("riskLevel", data.get("risk_level")));
        Integer anomalyCount = intValue(data.get("anomalyCount"), 0);
        int priority = switch (riskLevel) {
            case "critical", "danger" -> 90;
            case "high", "warning" -> 70;
            case "medium", "attention" -> 50;
            default -> 35;
        };
        if (anomalyCount > 0) {
            priority += Math.min(10, anomalyCount * 2);
        }
        return Math.min(priority, 100);
    }

    private void enrichFromSourceEvent(Long eventId, Map<String, Object> workingData) {
        try {
            UeitException source = ueitExceptionService.selectUeitExceptionById(eventId);
            if (source == null) {
                source = jdbcTemplate.query(
                        "SELECT id, user_id, device_id, type, value FROM qkyd_exception WHERE id = ?",
                        rs -> rs.next() ? mapSourceEvent(rs) : null,
                        eventId
                );
            }
            if (source == null) {
                return;
            }

            if (isBlank(workingData.get("abnormalType")) && !isBlank(source.getType())) {
                workingData.put("abnormalType", source.getType());
            }
            if (isBlank(workingData.get("abnormalValue")) && !isBlank(source.getValue())) {
                workingData.put("abnormalValue", source.getValue());
            }
            if (workingData.get("userId") == null && source.getUserId() != null) {
                workingData.put("userId", source.getUserId());
            }
            if (workingData.get("deviceId") == null && source.getDeviceId() != null) {
                workingData.put("deviceId", source.getDeviceId());
            }
        } catch (Exception ignored) {
            // Keep the pipeline running even if source enrichment fails.
        }
    }

    private UeitException mapSourceEvent(java.sql.ResultSet rs) throws java.sql.SQLException {
        UeitException source = new UeitException();
        source.setId(rs.getLong("id"));
        source.setUserId(rs.getLong("user_id"));
        source.setDeviceId(rs.getLong("device_id"));
        source.setType(rs.getString("type"));
        source.setValue(rs.getString("value"));
        return source;
    }

    private Long ensurePipelineRecord(Long eventId, String abnormalType, Map<String, Object> abnormalData, int priority) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT id FROM ai_event_processing_pipeline WHERE event_id = ? ORDER BY id DESC LIMIT 1",
                    eventId);
            if (!rows.isEmpty()) {
                Long pipelineId = ((Number) rows.get(0).get("id")).longValue();
                jdbcTemplate.update("UPDATE ai_event_processing_pipeline SET abnormal_type = ?, abnormal_value = ?, "
                                + "priority = ?, stage = 'DETECTED', updated_at = NOW() WHERE id = ?",
                        abnormalType,
                        stringValue(abnormalData.get("abnormalValue")),
                        priority,
                        pipelineId);
                return pipelineId;
            }
        } catch (Exception ignored) {
            // Try insert path below
        }

        jdbcTemplate.update("INSERT INTO ai_event_processing_pipeline "
                        + "(event_id, abnormal_id, stage, priority, abnormal_type, abnormal_value, execution_status, created_at, updated_at) "
                        + "VALUES (?, ?, 'DETECTED', ?, ?, ?, 'PENDING', NOW(), NOW())",
                eventId,
                longValue(abnormalData.get("abnormalId")),
                priority,
                abnormalType,
                stringValue(abnormalData.get("abnormalValue")));

        return jdbcTemplate.queryForObject(
                "SELECT id FROM ai_event_processing_pipeline WHERE event_id = ? ORDER BY id DESC LIMIT 1",
                Long.class,
                eventId
        );
    }

    private void persistInsight(Long pipelineId, EventInsightResultDTO insight) {
        Long snapshotId = null;
        if (insight != null && insight.getEventId() != null) {
            var snapshot = eventInsightSnapshotMapper.selectLatestByEventId(insight.getEventId());
            if (snapshot != null) {
                snapshotId = snapshot.getId();
            }
        }

        jdbcTemplate.update("UPDATE ai_event_processing_pipeline SET stage = 'INSIGHT_BUILT', "
                        + "insight_snapshot_id = ?, updated_at = NOW() WHERE id = ?",
                snapshotId,
                pipelineId);
    }

    private Map<String, Object> extractRiskPayload(AjaxResult result, EventInsightResultDTO insight, int fallbackPriority) {
        Map<String, Object> payload = new LinkedHashMap<>();
        if (result != null && result.isSuccess() && result.get("data") instanceof Map<?, ?> data) {
            data.forEach((key, value) -> payload.put(String.valueOf(key), value));
        }

        if (!payload.containsKey("riskScore") && !payload.containsKey("risk_score")) {
            payload.put("riskScore", insight != null && insight.getRisk() != null && insight.getRisk().getRiskScore() != null
                    ? insight.getRisk().getRiskScore()
                    : fallbackPriority);
        }
        if (!payload.containsKey("riskLevel") && !payload.containsKey("risk_level")) {
            payload.put("riskLevel", insight != null && insight.getRisk() != null
                    ? insight.getRisk().getRiskLevel()
                    : "warning");
        }
        return payload;
    }

    private void persistRisk(Long pipelineId, Map<String, Object> riskPayload) {
        int riskScore = intValue(riskPayload.get("riskScore"), intValue(riskPayload.get("risk_score"), 50));
        String riskLevel = stringValue(
                riskPayload.containsKey("riskLevel") ? riskPayload.get("riskLevel") : riskPayload.get("risk_level"));
        String report = stringValue(riskPayload.get("doctorReport"));
        String factors = writeJson(riskPayload.get("factors"));
        String subScores = writeJson(riskPayload.get("subScores"));

        jdbcTemplate.update("UPDATE ai_event_processing_pipeline SET stage = 'RISK_ASSESSED', risk_score = ?, "
                        + "risk_level = ?, execution_result = ?, disposition_suggestion = COALESCE(disposition_suggestion, ?), "
                        + "updated_at = NOW() WHERE id = ?",
                riskScore,
                riskLevel,
                report,
                factors.isBlank() ? subScores : factors,
                pipelineId);
    }

    private void persistDisposition(Long pipelineId, String disposition, String notificationLevel, boolean autoExecute) {
        jdbcTemplate.update("UPDATE ai_event_processing_pipeline SET stage = 'DISPOSITION_SUGGESTED', "
                        + "disposition_suggestion = ?, notification_level = ?, auto_execute = ?, updated_at = NOW() "
                        + "WHERE id = ?",
                disposition,
                notificationLevel,
                autoExecute ? 1 : 0,
                pipelineId);
    }

    private void executeDisposition(Long pipelineId, String disposition, String notificationLevel) {
        String executionResult = "已按 " + notificationLevel + " 级通知规则执行: " + disposition;
        jdbcTemplate.update("UPDATE ai_event_processing_pipeline SET stage = 'COMPLETED', execution_status = 'SUCCESS', "
                        + "execution_result = ?, updated_at = NOW() WHERE id = ?",
                executionResult,
                pipelineId);
    }

    private void updatePipelineStage(Long pipelineId, String stage, Integer riskScore, String riskLevel,
                                     String disposition, String executionStatus, String executionResult) {
        jdbcTemplate.update("UPDATE ai_event_processing_pipeline SET stage = ?, risk_score = COALESCE(?, risk_score), "
                        + "risk_level = COALESCE(?, risk_level), disposition_suggestion = COALESCE(?, disposition_suggestion), "
                        + "execution_status = COALESCE(?, execution_status), execution_result = COALESCE(?, execution_result), "
                        + "updated_at = NOW() WHERE id = ?",
                stage, riskScore, riskLevel, disposition, executionStatus, executionResult, pipelineId);
    }

    private void markFailed(Long pipelineId, String message) {
        jdbcTemplate.update("UPDATE ai_event_processing_pipeline SET stage = 'FAILED', execution_status = 'FAILED', "
                        + "execution_result = ?, updated_at = NOW() WHERE id = ?",
                message,
                pipelineId);
    }

    private List<Map<String, Object>> buildStages(Map<String, Object> pipeline,
                                                  List<Map<String, Object>> auditTrail,
                                                  List<Map<String, Object>> snapshots,
                                                  EventInsightResultDTO latestInsight) {
        List<Map<String, Object>> agentStages = buildAgentStages(latestInsight, pipeline, snapshots);
        if (!agentStages.isEmpty()) {
            agentStages.add(stageItem(
                    "处置执行与闭环",
                    resolveExecutionStatus(stringValue(pipeline.get("stage")), stringValue(pipeline.get("execution_status"))),
                    pipeline.get("updated_at"),
                    Map.of(
                            "executionStatus", stringValue(pipeline.get("execution_status")),
                            "executionResult", stringValue(pipeline.get("execution_result")),
                            "actualOutcome", stringValue(pipeline.get("actual_outcome")),
                            "feedbackScore", stringValue(pipeline.get("feedback_score")),
                            "auditCount", auditTrail.size()
                    )));
            return agentStages;
        }

        List<Map<String, Object>> stages = new ArrayList<>();
        String currentStage = stringValue(pipeline.get("stage"));

        stages.add(stageItem(
                "异常检测",
                resolveStageStatus(currentStage, "DETECTED"),
                pipeline.get("created_at"),
                Map.of(
                        "abnormalType", stringValue(pipeline.get("abnormal_type")),
                        "abnormalValue", stringValue(pipeline.get("abnormal_value"))
                )));

        stages.add(stageItem(
                "AI解析与上下文补全",
                resolveStageStatus(currentStage, "INSIGHT_BUILT"),
                latestSnapshotTime(snapshots, pipeline.get("updated_at")),
                snapshots.isEmpty()
                        ? Map.of()
                        : Map.of(
                        "snapshotCount", snapshots.size(),
                        "summary", stringValue(snapshots.get(0).get("summary"))
                )));

        stages.add(stageItem(
                "风险评估",
                resolveStageStatus(currentStage, "RISK_ASSESSED"),
                pipeline.get("updated_at"),
                Map.of(
                        "riskLevel", stringValue(pipeline.get("risk_level")),
                        "riskScore", stringValue(pipeline.get("risk_score"))
                )));

        stages.add(stageItem(
                "处置建议",
                resolveStageStatus(currentStage, "DISPOSITION_SUGGESTED"),
                pipeline.get("updated_at"),
                Map.of(
                        "suggestion", stringValue(pipeline.get("disposition_suggestion")),
                        "notificationLevel", stringValue(pipeline.get("notification_level")),
                        "autoExecute", String.valueOf(pipeline.get("auto_execute"))
                )));

        stages.add(stageItem(
                "执行与闭环",
                resolveExecutionStatus(currentStage, stringValue(pipeline.get("execution_status"))),
                pipeline.get("updated_at"),
                Map.of(
                        "executionStatus", stringValue(pipeline.get("execution_status")),
                        "executionResult", stringValue(pipeline.get("execution_result")),
                        "actualOutcome", stringValue(pipeline.get("actual_outcome")),
                        "feedbackScore", stringValue(pipeline.get("feedback_score")),
                        "auditCount", auditTrail.size()
                )));

        return stages;
    }

    private List<Map<String, Object>> buildAgentStages(EventInsightResultDTO latestInsight,
                                                       Map<String, Object> pipeline,
                                                       List<Map<String, Object>> snapshots) {
        if (latestInsight == null
                || latestInsight.getTrace() == null
                || latestInsight.getTrace().getSteps() == null
                || latestInsight.getTrace().getSteps().isEmpty()) {
            return List.of();
        }

        List<Map<String, Object>> stages = new ArrayList<>();
        Object insightTimestamp = latestSnapshotTime(snapshots, pipeline.get("updated_at"));
        for (EventInsightResultDTO.TraceStep step : latestInsight.getTrace().getSteps()) {
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("agentKey", stringValue(step.getAgentKey()));
            details.put("summary", stringValue(step.getSummary()));
            details.put("detail", stringValue(step.getDetail()));
            if (step.getResolvedCount() != null || step.getTargetCount() != null) {
                details.put("progress", String.format("%s/%s",
                        step.getResolvedCount() == null ? 0 : step.getResolvedCount(),
                        step.getTargetCount() == null ? 0 : step.getTargetCount()));
            }
            stages.add(stageItem(
                    stringValue(step.getAgentName()),
                    normalizeAgentStageStatus(step.getStatus()),
                    insightTimestamp,
                    details
            ));
        }
        return stages;
    }

    private List<Map<String, Object>> extractAgentTrace(EventInsightResultDTO latestInsight) {
        if (latestInsight == null
                || latestInsight.getTrace() == null
                || latestInsight.getTrace().getSteps() == null) {
            return List.of();
        }

        List<Map<String, Object>> trace = new ArrayList<>();
        for (EventInsightResultDTO.TraceStep step : latestInsight.getTrace().getSteps()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("agentKey", stringValue(step.getAgentKey()));
            item.put("agentName", stringValue(step.getAgentName()));
            item.put("status", normalizeAgentStageStatus(step.getStatus()));
            item.put("resolvedCount", step.getResolvedCount());
            item.put("targetCount", step.getTargetCount());
            item.put("summary", stringValue(step.getSummary()));
            item.put("detail", stringValue(step.getDetail()));
            trace.add(item);
        }
        return trace;
    }

    private EventInsightResultDTO parseInsight(EventInsightSnapshotRecord snapshot) {
        if (snapshot == null || isBlank(snapshot.getPayloadJson())) {
            return null;
        }
        try {
            return objectMapper.readValue(snapshot.getPayloadJson(), EventInsightResultDTO.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String normalizeAgentStageStatus(String status) {
        String normalized = stringValue(status).trim().toLowerCase();
        if (normalized.isEmpty()) {
            return "pending";
        }
        if (List.of("done", "completed", "success", "resolved", "finished").contains(normalized)) {
            return "completed";
        }
        if (List.of("running", "processing", "in_progress", "active").contains(normalized)) {
            return "processing";
        }
        return "pending";
    }

    private Map<String, Object> loadMinimalPipelineStatus(Long eventId) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT id, event_id, abnormal_type, abnormal_value, stage, priority, risk_score, risk_level, "
                            + "disposition_suggestion, notification_level, auto_execute, execution_status, execution_result, "
                            + "actual_outcome, feedback_score, created_at, updated_at "
                            + "FROM ai_event_processing_pipeline WHERE event_id = ? ORDER BY id DESC LIMIT 1",
                    eventId
            );
            if (rows.isEmpty()) {
                return Map.of();
            }

            Map<String, Object> result = new LinkedHashMap<>(rows.get(0));
            result.put("auditTrail", List.of());
            result.put("recentInsightSnapshots", List.of());
            result.put("agentTrace", List.of());
            result.put("stages", List.of());
            result.put("totalDuration", calculateDurationMinutes(result));
            return result;
        } catch (Exception inner) {
            log.error("failed to load minimal pipeline status for event {}", eventId, inner);
            return Map.of();
        }
    }

    private Map<String, Object> stageItem(String name, String status, Object timestamp, Map<String, Object> details) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("status", status);
        item.put("timestamp", timestamp);
        item.put("details", details);
        return item;
    }

    private String resolveStageStatus(String currentStage, String stage) {
        List<String> order = List.of("DETECTED", "INSIGHT_BUILT", "RISK_ASSESSED", "DISPOSITION_SUGGESTED", "COMPLETED");
        int currentIndex = order.indexOf(currentStage);
        int targetIndex = order.indexOf(stage);

        if ("FAILED".equalsIgnoreCase(currentStage)) {
            return targetIndex <= 0 ? "completed" : "processing";
        }
        if (currentIndex == -1 || targetIndex == -1) {
            return "pending";
        }
        if (currentIndex > targetIndex) {
            return "completed";
        }
        if (currentIndex == targetIndex) {
            return "processing";
        }
        return "pending";
    }

    private String resolveExecutionStatus(String currentStage, String executionStatus) {
        if ("SUCCESS".equalsIgnoreCase(executionStatus) || "COMPLETED".equalsIgnoreCase(currentStage)) {
            return "completed";
        }
        if ("FAILED".equalsIgnoreCase(executionStatus) || "FAILED".equalsIgnoreCase(currentStage)) {
            return "processing";
        }
        if ("PENDING".equalsIgnoreCase(executionStatus) && "DISPOSITION_SUGGESTED".equalsIgnoreCase(currentStage)) {
            return "processing";
        }
        return "pending";
    }

    private Object latestSnapshotTime(List<Map<String, Object>> snapshots, Object fallback) {
        if (!snapshots.isEmpty() && snapshots.get(0).get("generated_at") != null) {
            return snapshots.get(0).get("generated_at");
        }
        return fallback;
    }

    private long calculateDurationMinutes(Map<String, Object> pipeline) {
        Object createdAt = pipeline.get("created_at");
        Object updatedAt = pipeline.get("updated_at");
        if (createdAt instanceof java.sql.Timestamp created && updatedAt instanceof java.sql.Timestamp updated) {
            long millis = Math.max(0L, updated.getTime() - created.getTime());
            return Math.max(1L, millis / 60000L);
        }
        return 1L;
    }

    private String writeJson(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String text) {
            return text;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }

    private int intValue(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private Long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value != null) {
            try {
                return Long.parseLong(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private boolean isBlank(Object value) {
        return value == null || String.valueOf(value).trim().isEmpty();
    }
}
