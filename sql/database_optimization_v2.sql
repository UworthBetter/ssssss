-- ============================================
-- 数据库优化脚本 V2 - 耆康云盾健康监测平台
-- 特性：幂等性，可重复执行
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

-- 2. 添加复合索引优化查询性能（使用存储过程确保幂等性）
DELIMITER //
DROP PROCEDURE IF EXISTS add_index_if_not_exists //
CREATE PROCEDURE add_index_if_not_exists()
BEGIN
    -- qkyd_heart_rate
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema='qkyd_jkpt' AND table_name='qkyd_heart_rate' AND index_name='idx_device_time') THEN
        ALTER TABLE qkyd_heart_rate ADD INDEX idx_device_time (device_id, read_time);
    END IF;

    -- qkyd_blood
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema='qkyd_jkpt' AND table_name='qkyd_blood' AND index_name='idx_device_time') THEN
        ALTER TABLE qkyd_blood ADD INDEX idx_device_time (device_id, read_time);
    END IF;

    -- qkyd_temp
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema='qkyd_jkpt' AND table_name='qkyd_temp' AND index_name='idx_device_time') THEN
        ALTER TABLE qkyd_temp ADD INDEX idx_device_time (device_id, read_time);
    END IF;

    -- qkyd_spo2
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema='qkyd_jkpt' AND table_name='qkyd_spo2' AND index_name='idx_device_time') THEN
        ALTER TABLE qkyd_spo2 ADD INDEX idx_device_time (device_id, read_time);
    END IF;

    -- qkyd_location
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema='qkyd_jkpt' AND table_name='qkyd_location' AND index_name='idx_device_time') THEN
        ALTER TABLE qkyd_location ADD INDEX idx_device_time (device_id, read_time);
    END IF;

    -- ai_health_record
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema='qkyd_jkpt' AND table_name='ai_health_record' AND index_name='idx_device_create') THEN
        ALTER TABLE ai_health_record ADD INDEX idx_device_create (device_id, create_time);
    END IF;

    -- ai_abnormal_record
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema='qkyd_jkpt' AND table_name='ai_abnormal_record' AND index_name='idx_user_time') THEN
        ALTER TABLE ai_abnormal_record ADD INDEX idx_user_time (user_id, detected_time);
    END IF;

    -- ai_risk_score_record
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema='qkyd_jkpt' AND table_name='ai_risk_score_record' AND index_name='idx_user_time') THEN
        ALTER TABLE ai_risk_score_record ADD INDEX idx_user_time (user_id, score_time);
    END IF;
END //
DELIMITER ;

CALL add_index_if_not_exists();
DROP PROCEDURE IF EXISTS add_index_if_not_exists;

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

-- 4. 添加设备表字段（如果不存在）
DELIMITER //
DROP PROCEDURE IF EXISTS add_device_columns_if_not_exists //
CREATE PROCEDURE add_device_columns_if_not_exists()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='qkyd_jkpt' AND table_name='qkyd_device_info' AND column_name='status') THEN
        ALTER TABLE qkyd_device_info ADD COLUMN status VARCHAR(10) DEFAULT '1' COMMENT '设备状态：1在线；2离线' AFTER positioning_time;
    END IF;
END //
DELIMITER ;

CALL add_device_columns_if_not_exists();
DROP PROCEDURE IF EXISTS add_device_columns_if_not_exists;

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

-- 6. 优化异常表状态字段注释
ALTER TABLE qkyd_exception
    MODIFY COLUMN state VARCHAR(10) DEFAULT '0' COMMENT '状态（0未处理、1已处理、2处理中）';

-- ============================================
-- 新增业务表 (支持完整业务流程)
-- ============================================

-- 7. 告警处理记录表 (闭环管理)
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

-- 8. 健康报告表
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

-- 9. 设备-服务对象绑定关系表 (支持多人共享设备历史)
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
