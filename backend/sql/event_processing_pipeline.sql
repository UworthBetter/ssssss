-- 统一事件处理管道表
CREATE TABLE IF NOT EXISTS ai_event_processing_pipeline (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id BIGINT NOT NULL COMMENT '事件ID',
    abnormal_id BIGINT COMMENT '异常记录ID',

    -- 流程阶段
    stage VARCHAR(32) NOT NULL COMMENT '当前阶段: DETECTED/INSIGHT_BUILT/RISK_ASSESSED/DISPOSITION_SUGGESTED/EXECUTED/COMPLETED',
    priority INT DEFAULT 50 COMMENT '优先级(1-100, 越高越优先)',

    -- 各阶段结果
    insight_snapshot_id BIGINT COMMENT '事件洞察快照ID',
    risk_score INT COMMENT '风险评分',
    risk_level VARCHAR(16) COMMENT '风险等级',
    disposition_suggestion VARCHAR(500) COMMENT '处置建议',

    -- 执行状态
    execution_status VARCHAR(32) DEFAULT 'PENDING' COMMENT 'PENDING/IN_PROGRESS/SUCCESS/FAILED',
    execution_result VARCHAR(500) COMMENT '执行结果',

    -- 反馈
    actual_outcome VARCHAR(32) COMMENT '实际结果: RESOLVED/ESCALATED/FALSE_ALARM',
    feedback_score INT COMMENT '反馈评分(1-5)',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    KEY idx_event_id (event_id),
    KEY idx_stage (stage),
    KEY idx_priority (priority),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='事件处理管道';

-- 处置建议规则表
CREATE TABLE IF NOT EXISTS ai_disposition_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    abnormal_type VARCHAR(64) NOT NULL COMMENT '异常类型',
    risk_level_min INT COMMENT '风险分数下限',
    risk_level_max INT COMMENT '风险分数上限',

    auto_execute TINYINT(1) DEFAULT 0 COMMENT '是否自动执行',
    disposition_action VARCHAR(256) NOT NULL COMMENT '处置动作',
    notification_level VARCHAR(16) COMMENT '通知级别: URGENT/HIGH/NORMAL',

    enabled TINYINT(1) DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    KEY idx_abnormal_type (abnormal_type),
    KEY idx_risk_level (risk_level_min, risk_level_max)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处置建议规则';