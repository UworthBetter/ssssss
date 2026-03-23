-- 健康监测AI决策系统 - 操作审计日志表 (v2.0)
-- 记录完整的异常检测信息

CREATE TABLE IF NOT EXISTS `ai_operation_audit_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '审计日志ID',
  `event_id` BIGINT NOT NULL COMMENT '事件ID',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型: DETECT/INSIGHT_BUILD/RISK_ASSESS/DISPOSITION/EXECUTE/ERROR',
  `abnormal_type` VARCHAR(100) COMMENT '异常类型',

  -- 风险评估信息
  `risk_score` INT COMMENT '风险分数(0-100)',
  `detected_anomalies` LONGTEXT COMMENT '检测到的异常列表(JSON)',
  `anomaly_types` VARCHAR(500) COMMENT '异常类型(逗号分隔)',

  -- 模型信息
  `model_confidence` DECIMAL(5,2) COMMENT '模型置信度(0-1)',
  `algorithm_version` VARCHAR(50) COMMENT '算法版本',

  -- 错误信息
  `error_message` TEXT COMMENT '错误信息',

  -- 时间戳
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  PRIMARY KEY (`id`),
  KEY `idx_event_id` (`event_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作审计日志表';

-- 异常检测准确率统计表
CREATE TABLE IF NOT EXISTS `ai_anomaly_accuracy_stats` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `abnormal_type` VARCHAR(100) NOT NULL COMMENT '异常类型',
  `total_detected` INT DEFAULT 0 COMMENT '总检测数',
  `true_positive` INT DEFAULT 0 COMMENT '真正例',
  `false_positive` INT DEFAULT 0 COMMENT '假正例',
  `false_negative` INT DEFAULT 0 COMMENT '假负例',
  `accuracy` DECIMAL(5,4) COMMENT '准确率',
  `precision` DECIMAL(5,4) COMMENT '精确率',
  `recall` DECIMAL(5,4) COMMENT '召回率',
  `f1_score` DECIMAL(5,4) COMMENT 'F1分数',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_abnormal_type` (`abnormal_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='异常检测准确率统计表';
