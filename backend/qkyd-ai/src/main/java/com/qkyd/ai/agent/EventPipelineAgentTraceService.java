package com.qkyd.ai.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventPipelineAgentTraceService {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public EventPipelineAgentTraceService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void ensureSchema() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS ai_agent_execution_log (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    event_id BIGINT NOT NULL,
                    pipeline_id BIGINT NOT NULL,
                    sequence_no INT NOT NULL,
                    agent_key VARCHAR(64) NOT NULL,
                    agent_name VARCHAR(128) NOT NULL,
                    status VARCHAR(32) NOT NULL,
                    summary VARCHAR(255) NULL,
                    detail_text TEXT NULL,
                    handoff_to VARCHAR(64) NULL,
                    input_payload LONGTEXT NULL,
                    output_payload LONGTEXT NULL,
                    started_at DATETIME NULL,
                    finished_at DATETIME NULL,
                    duration_ms BIGINT NULL,
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (id),
                    KEY idx_ai_agent_execution_event (event_id),
                    KEY idx_ai_agent_execution_pipeline (pipeline_id),
                    KEY idx_ai_agent_execution_agent (agent_key)
                )
                """);
    }

    public void resetPipelineTrace(Long pipelineId) {
        jdbcTemplate.update("DELETE FROM ai_agent_execution_log WHERE pipeline_id = ?", pipelineId);
    }

    public void appendExecution(EventPipelineAgentContext context,
                                EventPipelineAgent agent,
                                EventPipelineAgentResult result,
                                int sequenceNo,
                                Instant startedAt,
                                Instant finishedAt) {
        long durationMs = Math.max(0L, finishedAt.toEpochMilli() - startedAt.toEpochMilli());
        jdbcTemplate.update("""
                        INSERT INTO ai_agent_execution_log
                        (event_id, pipeline_id, sequence_no, agent_key, agent_name, status, summary, detail_text,
                         handoff_to, input_payload, output_payload, started_at, finished_at, duration_ms)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                context.getEventId(),
                context.getPipelineId(),
                sequenceNo,
                agent.getAgentKey(),
                agent.getAgentName(),
                result.getStatus(),
                safeText(result.getSummary()),
                safeText(result.getDetail()),
                safeText(result.getHandoffTo()),
                writeJson(context.copyWorkingData()),
                writeJson(result.getOutputPayload()),
                Timestamp.from(startedAt),
                Timestamp.from(finishedAt),
                durationMs);
    }

    public List<Map<String, Object>> listByEventId(Long eventId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT agent_key, agent_name, status, summary, detail_text, handoff_to,
                       output_payload, started_at, finished_at, duration_ms
                FROM ai_agent_execution_log
                WHERE event_id = ?
                ORDER BY pipeline_id DESC, sequence_no ASC, id ASC
                """, eventId);
        if (rows.isEmpty()) {
            return List.of();
        }

        List<Map<String, Object>> trace = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("agentKey", row.get("agent_key"));
            item.put("agentName", row.get("agent_name"));
            item.put("status", row.get("status"));
            item.put("summary", row.get("summary"));
            item.put("detail", row.get("detail_text"));
            item.put("handoffTo", row.get("handoff_to"));
            item.put("startedAt", row.get("started_at"));
            item.put("finishedAt", row.get("finished_at"));
            item.put("durationMs", row.get("duration_ms"));
            item.put("details", readJsonMap((String) row.get("output_payload")));
            trace.add(item);
        }
        return trace;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ignored) {
            return "{}";
        }
    }

    private Map<String, Object> readJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception ignored) {
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("raw", json);
            return fallback;
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}
