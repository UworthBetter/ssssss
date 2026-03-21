-- =============================================
-- Table: health_subject
-- Description: 健康数据主表
-- Created: 2026-02-06
-- =============================================

CREATE TABLE health_subject (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    gender VARCHAR(10) NOT NULL COMMENT '性别（男/女）',
    age INT NOT NULL COMMENT '年龄',
    height DECIMAL(5,2) NOT NULL COMMENT '身高（cm）',
    weight DECIMAL(5,2) NOT NULL COMMENT '体重（kg）',
    blood_pressure VARCHAR(20) COMMENT '血压（如：120/80）',
    heart_rate INT COMMENT '心率（次/分钟）',
    blood_sugar DECIMAL(5,2) COMMENT '血糖（mmol/L）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康数据主表';

-- =============================================
-- Indexes
-- =============================================

-- 用户ID索引（用于查询用户的健康数据）
CREATE INDEX idx_user_id ON health_subject(user_id);

-- 创建时间索引（用于按时间范围查询）
CREATE INDEX idx_create_time ON health_subject(create_time);

-- 组合索引（用户ID+创建时间，用于查询用户的历史数据）
CREATE INDEX idx_user_create ON health_subject(user_id, create_time);
