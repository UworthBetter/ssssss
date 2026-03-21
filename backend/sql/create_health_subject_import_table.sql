-- =============================================
-- Table: health_subject_import
-- Description: 健康数据批量导入表
-- Created: 2026-02-06
-- Task: Week 3 - Task 1.2
-- =============================================

CREATE TABLE health_subject_import (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    import_record_id BIGINT NOT NULL COMMENT '导入记录ID（关联health_import_record表）',
    row_number INT NOT NULL COMMENT '行号（Excel/CSV中的行号）',
    subject_id BIGINT DEFAULT NULL COMMENT '服务对象ID（导入成功后关联）',
    data_type VARCHAR(50) NOT NULL COMMENT '数据类型（如：step_data, heart_rate, blood_pressure等）',
    measure_time DATETIME DEFAULT NULL COMMENT '测量时间',
    measure_value VARCHAR(100) DEFAULT NULL COMMENT '测量值',
    unit VARCHAR(20) DEFAULT NULL COMMENT '单位',
    device_info VARCHAR(100) DEFAULT NULL COMMENT '设备信息',
    additional_data JSON DEFAULT NULL COMMENT '附加数据（JSON格式，存储其他字段）',
    validation_status VARCHAR(20) DEFAULT 'pending' COMMENT '验证状态（pending:待验证, valid:有效, invalid:无效）',
    import_status VARCHAR(20) DEFAULT 'pending' COMMENT '导入状态（pending:待导入, success:成功, failed:失败）',
    error_msg TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (import_record_id) REFERENCES health_import_record(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康数据批量导入表';

-- =============================================
-- Indexes
-- =============================================

-- 导入记录ID索引（用于快速查询某次导入的所有数据）
CREATE INDEX idx_import_record_id ON health_subject_import(import_record_id);

-- 服务对象ID索引（用于查询某服务对象的所有导入数据）
CREATE INDEX idx_subject_id ON health_subject_import(subject_id);

-- 数据类型索引（用于按类型统计和查询）
CREATE INDEX idx_data_type ON health_subject_import(data_type);

-- 验证状态索引（用于筛选待验证的数据）
CREATE INDEX idx_validation_status ON health_subject_import(validation_status);

-- 导入状态索引（用于筛选待导入的数据）
CREATE INDEX idx_import_status ON health_subject_import(import_status);

-- 测量时间索引（用于按时间范围查询）
CREATE INDEX idx_measure_time ON health_subject_import(measure_time);

-- 组合索引（导入记录ID+验证状态，用于批量验证）
CREATE INDEX idx_import_valid ON health_subject_import(import_record_id, validation_status);

-- 组合索引（导入记录ID+导入状态，用于批量导入）
CREATE INDEX idx_import_success ON health_subject_import(import_record_id, import_status);
