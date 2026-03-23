-- 操作留痕表
CREATE TABLE IF NOT EXISTS ai_operation_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id BIGINT NOT NULL COMMENT '事件ID',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(64) COMMENT '操作人名称',

    operation_type VARCHAR(32) NOT NULL COMMENT '操作类型: DETECT/INSIGHT_BUILD/RISK_ASSESS/DISPOSITION/EXECUTE/FEEDBACK',
    operation_detail VARCHAR(500) COMMENT '操作详情',
    operation_result VARCHAR(32) COMMENT '操作结果: SUCCESS/FAILED',

    -- 上下文信息
    abnormal_type VARCHAR(64) COMMENT '异常类型',
    risk_score INT COMMENT '风险评分',
    disposition_action VARCHAR(256) COMMENT '处置动作',

    -- 审计信息
    ip_address VARCHAR(64) COMMENT 'IP地址',
    user_agent VARCHAR(256) COMMENT '用户代理',
    request_id VARCHAR(64) COMMENT '请求ID(用于链路追踪)',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    KEY idx_event_id (event_id),
    KEY idx_operator_id (operator_id),
    KEY idx_operation_type (operation_type),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作审计日志';