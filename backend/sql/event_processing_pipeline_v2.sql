-- 健康监测AI决策系统 - 事件处理管道表 (v2.0)
-- 支持完整的异常检测结果存储

CREATE TABLE IF NOT EXISTS `ai_event_processing_pipeline` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '事件ID',
  `abnormal_id` BIGINT NOT NULL COMMENT '异常记录ID',
  `patient_id` BIGINT NOT NULL COMMENT '患者ID',
  `patient_name` VARCHAR(100) COMMENT '患者名称',

  -- 处理阶段
  `stage` VARCHAR(50) NOT NULL DEFAULT 'DETECTED' COMMENT '处理阶段: DETECTED/INSIGHT_BUILT/RISK_ASSESSED/DISPOSITION_GENERATED/EXECUTED/COMPLETED',
  `priority` INT DEFAULT 50 COMMENT '优先级(0-100)',

  -- 异常信息
  `abnormal_type` VARCHAR(100) COMMENT '异常类型',
  `abnormal_value` VARCHAR(100) COMMENT '异常值',
  `abnormal_description` TEXT COMMENT '异常描述',

  -- 事件洞察
  `event_insight_id` BIGINT COMMENT '事件洞察ID',
  `event_insight_snapshot` LONGTEXT COMMENT '事件洞察快照(JSON)',

  -- 风险评估 - 基础信息
  `risk_score` INT COMMENT '风险分数(0-100)',
  `risk_level` VARCHAR(50) COMMENT '风险等级: CRITICAL/DANGER/WARNING/NORMAL',

  -- 风险评估 - 异常检测详情
  `detected_anomalies` LONGTEXT COMMENT '检测到的异常列表(JSON)',
  `anomaly_types` VARCHAR(500) COMMENT '异常类型(逗号分隔)',
  `anomaly_count` INT DEFAULT 0 COMMENT '异常数量',

  -- 风险评估 - 趋势分析
  `trend_analysis` LONGTEXT COMMENT '趋势分析结果(JSON)',
  `trend_status` VARCHAR(50) COMMENT '趋势状态: improving/stable/deteriorating/critical',

  -- 风险评估 - 基线对比
  `baseline_comparison` LONGTEXT COMMENT '基线对比结果(JSON)',
  `deviation_sigma` DECIMAL(5,2) COMMENT '偏离标准差',

  -- 风险评估 - 模型信息
  `model_confidence` DECIMAL(5,2) COMMENT '模型置信度(0-1)',
  `algorithm_version` VARCHAR(50) COMMENT '算法版本',
  `processing_time_ms` INT COMMENT '处理时间(毫秒)',

  -- 处置建议
  `disposition_suggestion` TEXT COMMENT '处置建议',
  `disposition_action` VARCHAR(200) COMMENT '处置动作',
  `notification_level` VARCHAR(50) COMMENT '通知级别: URGENT/HIGH/NORMAL/LOW',
  `auto_execute` TINYINT(1) DEFAULT 0 COMMENT '是否自动执行',

  -- 执行状态
  `execution_status` VARCHAR(50) COMMENT '执行状态: PENDING/EXECUTING/SUCCESS/FAILED',
  `execution_result` TEXT COMMENT '执行结果',
  `execution_time` DATETIME COMMENT '执行时间',

  -- 反馈信息
  `feedback_score` INT COMMENT '反馈评分(1-5)',
  `actual_outcome` VARCHAR(100) COMMENT '实际结果: RESOLVED/IMPROVED/UNCHANGED/WORSENED',
  `feedback_comment` TEXT COMMENT '反馈备注',
  `feedback_time` DATETIME COMMENT '反馈时间',

  -- 时间戳
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  PRIMARY KEY (`id`),
  KEY `idx_abnormal_id` (`abnormal_id`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_stage` (`stage`),
  KEY `idx_risk_level` (`risk_level`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='事件处理管道表';

-- 处置规则表
CREATE TABLE IF NOT EXISTS `ai_disposition_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `abnormal_type` VARCHAR(100) NOT NULL COMMENT '异常类型',
  `risk_level_min` INT NOT NULL COMMENT '风险下限',
  `risk_level_max` INT NOT NULL COMMENT '风险上限',
  `auto_execute` TINYINT(1) DEFAULT 0 COMMENT '是否自动执行',
  `disposition_action` TEXT NOT NULL COMMENT '处置动作',
  `notification_level` VARCHAR(50) NOT NULL COMMENT '通知级别: URGENT/HIGH/NORMAL/LOW',
  `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  PRIMARY KEY (`id`),
  KEY `idx_abnormal_type` (`abnormal_type`),
  KEY `idx_risk_level` (`risk_level_min`, `risk_level_max`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='处置规则表';
