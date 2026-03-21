/* 异常记录表 */
DROP TABLE IF EXISTS `ai_abnormal_record`;
CREATE TABLE ai_abnormal_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    device_id VARCHAR(64) COMMENT '设备ID',
    metric_type VARCHAR(32) NOT NULL COMMENT '指标类型(heart_rate/blood_pressure/temp/spo2)',
    abnormal_value VARCHAR(64) NOT NULL COMMENT '异常值',
    normal_range VARCHAR(64) COMMENT '正常范围',
    abnormal_type VARCHAR(32) COMMENT '异常类型(too_high/too_low)',
    risk_level VARCHAR(16) COMMENT '风险等级(danger/warning/normal)',
    detection_method VARCHAR(32) COMMENT '检测方法(threshold/statistical)',
    detected_time DATETIME NOT NULL COMMENT '检测时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常检测记录表';

/* 趋势记录表 */
DROP TABLE IF EXISTS `ai_trend_record`;
CREATE TABLE ai_trend_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    metric_type VARCHAR(32) NOT NULL COMMENT '指标类型',
    trend_direction VARCHAR(16) COMMENT '趋势方向(up/down/stable)',
    trend_strength DECIMAL(5,2) COMMENT '趋势强度',
    predicted_value DECIMAL(10,2) COMMENT '预测值',
    prediction_confidence DECIMAL(5,2) COMMENT '预测置信度',
    analysis_time DATETIME NOT NULL COMMENT '分析时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='趋势分析记录表';

/* 风险评分记录表 */
DROP TABLE IF EXISTS `ai_risk_score_record`;
CREATE TABLE ai_risk_score_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_score INT COMMENT '总评分(0-100)',
    risk_level VARCHAR(16) COMMENT '风险等级(high/medium/low/normal)',
    heart_rate_score INT COMMENT '心率评分',
    blood_pressure_score INT COMMENT '血压评分',
    blood_oxygen_score INT COMMENT '血氧评分',
    temperature_score INT COMMENT '体温评分',
    warnings JSON COMMENT '异常原因列表',
    score_time DATETIME NOT NULL COMMENT '评分时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风险评分记录表';

/* 跌倒告警记录表 (Updated vs existing) */
DROP TABLE IF EXISTS `ai_fall_alarm_record`;
CREATE TABLE ai_fall_alarm_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    original_alarm_id BIGINT COMMENT '原始告警ID',
    is_valid BOOLEAN COMMENT '是否有效告警',
    validation_reason TEXT COMMENT '校验原因',
    acceleration_peak DECIMAL(10,2) COMMENT '加速度峰值',
    has_removal_alert BOOLEAN COMMENT '1小时内是否有摘脱告警',
    recent_steps INT COMMENT '最近1小时步数',
    validation_time DATETIME NOT NULL COMMENT '校验时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='跌倒告警记录表';

/* 数据质量记录表 */
DROP TABLE IF EXISTS `ai_data_quality_record`;
CREATE TABLE ai_data_quality_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    metric_type VARCHAR(32) NOT NULL COMMENT '指标类型',
    missing_count INT COMMENT '缺失数量',
    missing_rate DECIMAL(5,2) COMMENT '缺失率',
    outlier_count INT COMMENT '异常值数量',
    outlier_rate DECIMAL(5,2) COMMENT '异常值率',
    fill_method VARCHAR(32) COMMENT '填充方法',
    quality_score INT COMMENT '质量评分(0-100)',
    check_time DATETIME NOT NULL COMMENT '检查时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据质量记录表';
