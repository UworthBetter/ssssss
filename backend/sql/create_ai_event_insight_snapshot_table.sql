CREATE TABLE IF NOT EXISTS ai_event_insight_snapshot (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    event_id BIGINT NOT NULL COMMENT 'Event identifier from health exception',
    summary VARCHAR(500) DEFAULT NULL COMMENT 'Operator-facing summary',
    risk_level VARCHAR(32) DEFAULT NULL COMMENT 'Persisted risk level',
    risk_score INT DEFAULT NULL COMMENT 'Persisted risk score',
    orchestrator_version VARCHAR(128) DEFAULT NULL COMMENT 'Insight orchestrator version',
    fallback_used TINYINT(1) DEFAULT 0 COMMENT 'Whether fallback generation was used',
    payload_json LONGTEXT NOT NULL COMMENT 'Full serialized EventInsightResultDTO payload',
    generated_at DATETIME DEFAULT NULL COMMENT 'Insight generation time',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Snapshot create time',
    PRIMARY KEY (id),
    KEY idx_ai_event_insight_snapshot_event (event_id),
    KEY idx_ai_event_insight_snapshot_generated (generated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Event insight snapshots';
