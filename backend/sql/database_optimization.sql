-- ============================================
-- 数据库优化脚本 - 耆康云盾健康监测平台
-- ============================================

-- 1. 统一 device_id 字段类型 (全部改为 VARCHAR(64))
ALTER TABLE qkyd_heart_rate MODIFY COLUMN device_id VARCHAR(64) COMMENT '设备ID';
ALTER TABLE qkyd_blood MODIFY COLUMN device_id VARCHAR(64) COMMENT '设备ID';
ALTER TABLE qkyd_temp MODIFY COLUMN device_id VARCHAR(64) COMMENT '设备ID';
ALTER TABLE qkyd_spo2 MODIFY COLUMN device_id VARCHAR(64) COMMENT '设备ID';
ALTER TABLE qkyd_steps MODIFY COLUMN device_id VARCHAR(64) COMMENT '设备ID';
ALTER TABLE qkyd_location MODIFY COLUMN device_id VARCHAR(64) COMMENT '设备ID';
ALTER TABLE qkyd_exception MODIFY COLUMN device_id VARCHAR(64) COMMENT '设备ID';
ALTER TABLE qkyd_detection_enhanced MODIFY COLUMN device_id VARCHAR(64) COMMENT '设备ID';

-- 2. 添加复合索引优化查询性能
-- 健康数据时间范围查询优化
ALTER TABLE qkyd_heart_rate ADD INDEX idx_device_time (device_id, read_time);
ALTER TABLE qkyd_blood ADD INDEX idx_device_time (device_id, read_time);
ALTER TABLE qkyd_temp ADD INDEX idx_device_time (device_id, read_time);
ALTER TABLE qkyd_spo2 ADD INDEX idx_device_time (device_id, read_time);
ALTER TABLE qkyd_location ADD INDEX idx_device_time (device_id, read_time);

-- AI分析记录时间索引
ALTER TABLE ai_health_record ADD INDEX idx_device_create (device_id, create_time);
ALTER TABLE ai_abnormal_record ADD INDEX idx_user_time (user_id, detected_time);
ALTER TABLE ai_risk_score_record ADD INDEX idx_user_time (user_id, score_time);

-- 3. 优化经纬度字段类型 (提高精度)
ALTER TABLE qkyd_location 
    MODIFY COLUMN longitude DECIMAL(12, 8) COMMENT '经度',
    MODIFY COLUMN latitude DECIMAL(12, 8) COMMENT '纬度';

ALTER TABLE qkyd_exception 
    MODIFY COLUMN longitude DECIMAL(12, 8) COMMENT '经度',
    MODIFY COLUMN latitude DECIMAL(12, 8) COMMENT '纬度';

ALTER TABLE qkyd_fence 
    MODIFY COLUMN longitude DECIMAL(12, 8) COMMENT '经度',
    MODIFY COLUMN latitude DECIMAL(12, 8) COMMENT '纬度';

-- 4. 合并设备扩展表到主表 (减少JOIN操作)
-- 先添加新字段到主表
ALTER TABLE qkyd_device_info 
    ADD COLUMN last_communication_time DATETIME NULL COMMENT '最后通讯时间' AFTER type,
    ADD COLUMN battery_level INT NULL COMMENT '电量' AFTER last_communication_time,
    ADD COLUMN step INT NULL COMMENT '步数' AFTER battery_level,
    ADD COLUMN alarm_content VARCHAR(255) NULL COMMENT '最近告警内容' AFTER step,
    ADD COLUMN alarm_time DATETIME NULL COMMENT '最近告警时间' AFTER alarm_content,
    ADD COLUMN temp FLOAT NULL COMMENT '体温' AFTER alarm_time,
    ADD COLUMN temp_time DATETIME NULL COMMENT '体温测量时间' AFTER temp,
    ADD COLUMN heart_rate FLOAT NULL COMMENT '心率' AFTER temp_time,
    ADD COLUMN heart_rate_time DATETIME NULL COMMENT '心率测量时间' AFTER heart_rate,
    ADD COLUMN blood_diastolic INT NULL COMMENT '舒张压' AFTER heart_rate_time,
    ADD COLUMN blood_systolic INT NULL COMMENT '收缩压' AFTER blood_diastolic,
    ADD COLUMN blood_time DATETIME NULL COMMENT '血压测量时间' AFTER blood_systolic,
    ADD COLUMN spo2 FLOAT NULL COMMENT '血氧' AFTER blood_time,
    ADD COLUMN spo2_time DATETIME NULL COMMENT '血氧测量时间' AFTER spo2,
    ADD COLUMN longitude DECIMAL(12, 8) NULL COMMENT '经度' AFTER spo2_time,
    ADD COLUMN latitude DECIMAL(12, 8) NULL COMMENT '纬度' AFTER longitude,
    ADD COLUMN location VARCHAR(255) NULL COMMENT '详细地址' AFTER latitude,
    ADD COLUMN positioning_type VARCHAR(50) NULL COMMENT '定位方式' AFTER location,
    ADD COLUMN positioning_time DATETIME NULL COMMENT '定位时间' AFTER positioning_type;

-- 迁移数据 (执行前请备份)
-- INSERT INTO qkyd_device_info (...) SELECT ... FROM qkyd_device_info_extend;

-- 5. 优化 AI 记录表注释 (明确服务对象概念)
ALTER TABLE ai_health_record 
    MODIFY COLUMN user_id BIGINT COMMENT '服务对象ID(关联sys_user)';

ALTER TABLE ai_abnormal_record 
    MODIFY COLUMN user_id BIGINT COMMENT '服务对象ID(关联sys_user)';

ALTER TABLE ai_risk_score_record 
    MODIFY COLUMN user_id BIGINT COMMENT '服务对象ID(关联sys_user)';

ALTER TABLE ai_trend_record 
    MODIFY COLUMN user_id BIGINT COMMENT '服务对象ID(关联sys_user)';

ALTER TABLE ai_fall_alarm_record 
    MODIFY COLUMN user_id BIGINT COMMENT '服务对象ID(关联sys_user)';

ALTER TABLE ai_data_quality_record 
    MODIFY COLUMN user_id BIGINT COMMENT '服务对象ID(关联sys_user)';

-- 6. 添加状态字段
-- 如果字段已存在会报错，可忽略
SET @exist_status = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='qkyd_jkpt' AND table_name='qkyd_device_info' AND column_name='status');
SET @sql = IF(@exist_status = 0, 'ALTER TABLE qkyd_device_info ADD COLUMN status VARCHAR(10) DEFAULT ''1'' COMMENT ''设备状态：1在线；2离线'' AFTER positioning_time', 'SELECT ''Column status already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 7. 优化异常表状态字段注释
ALTER TABLE qkyd_exception 
    MODIFY COLUMN state VARCHAR(10) DEFAULT '0' COMMENT '状态（0未处理、1已处理、2处理中）';

-- ============================================
-- 新增业务表 (支持完整业务流程)
-- ============================================

-- 8. 告警处理记录表 (闭环管理)
CREATE TABLE IF NOT EXISTS health_alert_handle (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    alert_id BIGINT NOT NULL COMMENT '关联告警ID',
    alert_type VARCHAR(32) NOT NULL COMMENT '告警类型(abnormal/fall/device)',
    handler_id BIGINT NOT NULL COMMENT '处理人ID',
    handler_name VARCHAR(50) COMMENT '处理人姓名',
    handle_action VARCHAR(50) COMMENT '处理动作(confirm/ignore/forward)',
    handle_result VARCHAR(500) COMMENT '处理结果说明',
    handle_time DATETIME NOT NULL COMMENT '处理时间',
    next_action VARCHAR(200) COMMENT '后续跟进措施',
    status VARCHAR(10) DEFAULT '1' COMMENT '状态(1有效 0撤销)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_alert_id (alert_id),
    INDEX idx_handler_time (handler_id, handle_time)
) ENGINE=InnoDB COMMENT='告警处理记录表';

-- 9. 健康报告表
CREATE TABLE IF NOT EXISTS health_report (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '服务对象ID',
    report_type VARCHAR(32) NOT NULL COMMENT '报告类型(daily/weekly/monthly)',
    report_date DATE NOT NULL COMMENT '报告日期',
    report_title VARCHAR(100) COMMENT '报告标题',
    summary TEXT COMMENT '健康摘要',
    risk_level VARCHAR(16) COMMENT '风险等级',
    abnormal_count INT DEFAULT 0 COMMENT '异常次数',
    avg_heart_rate INT COMMENT '平均心率',
    avg_blood_pressure VARCHAR(20) COMMENT '平均血压',
    total_steps INT COMMENT '总步数',
    file_url VARCHAR(500) COMMENT 'PDF文件URL',
    created_by BIGINT COMMENT '生成人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_user_date (user_id, report_date),
    INDEX idx_type_date (report_type, report_date)
) ENGINE=InnoDB COMMENT='健康分析报告表';

-- 10. 设备-服务对象绑定关系表 (支持多人共享设备历史)
CREATE TABLE IF NOT EXISTS health_device_binding (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    device_id VARCHAR(64) NOT NULL COMMENT '设备ID',
    user_id BIGINT NOT NULL COMMENT '服务对象ID',
    bind_type VARCHAR(20) DEFAULT 'primary' COMMENT '绑定类型(primary主设备/secondary副设备)',
    start_time DATETIME NOT NULL COMMENT '绑定开始时间',
    end_time DATETIME COMMENT '绑定结束时间(空表示当前绑定)',
    status VARCHAR(10) DEFAULT '1' COMMENT '状态(1有效 0解绑)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_device_user (device_id, user_id),
    INDEX idx_user_time (user_id, start_time)
) ENGINE=InnoDB COMMENT='设备绑定关系表';
