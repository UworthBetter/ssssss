-- =============================================
-- AI健康分析记录表
-- 用于存储 Python AI 服务的健康分析结果
-- =============================================

CREATE TABLE IF NOT EXISTS `ai_health_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `device_id` VARCHAR(64) NOT NULL COMMENT '设备ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `risk_level` VARCHAR(16) NOT NULL COMMENT '风险等级(low/medium/high/critical)',
    `risk_score` DECIMAL(5,4) NOT NULL COMMENT '风险评分(0.0000-1.0000)',
    `anomaly_count` INT DEFAULT 0 COMMENT '异常数量',
    `risk_factors` TEXT COMMENT '风险因素JSON',
    `raw_data` JSON COMMENT '原始体征数据',
    `data_points` INT DEFAULT 0 COMMENT '数据点数量',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_device_id` (`device_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_risk_level` (`risk_level`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI健康分析记录表';

-- =============================================
-- 示例数据（可选）
-- =============================================
-- INSERT INTO ai_health_record (device_id, user_id, risk_level, risk_score, anomaly_count, risk_factors, data_points)
-- VALUES ('TEST001', 1, 'low', 0.1500, 0, '["各项指标正常"]', 10);
