-- =============================================
-- Table: health_import_record
-- Description: 健康数据导入记录表
-- Created: 2026-02-06
-- =============================================

CREATE TABLE health_import_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    import_type VARCHAR(50) NOT NULL COMMENT '导入类型（如：step_data, heart_rate等）',
    file_name VARCHAR(255) NOT NULL COMMENT '导入文件名',
    total_rows INT DEFAULT 0 COMMENT '总记录数',
    success_rows INT DEFAULT 0 COMMENT '成功导入记录数',
    fail_rows INT DEFAULT 0 COMMENT '失败记录数',
    status VARCHAR(20) NOT NULL COMMENT '导入状态（pending:待处理, processing:处理中, success:成功, failed:失败）',
    error_msg TEXT COMMENT '错误信息',
    import_time DATETIME COMMENT '导入完成时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康数据导入记录表';

-- =============================================
-- Indexes
-- =============================================

-- 用户ID索引（用于查询用户的导入历史）
CREATE INDEX idx_user_id ON health_import_record(user_id);

-- 导入类型索引（用于按类型统计和查询）
CREATE INDEX idx_import_type ON health_import_record(import_type);

-- 创建时间索引（用于按时间范围查询）
CREATE INDEX idx_create_time ON health_import_record(create_time);

-- 组合索引（用户ID+创建时间，用于查询用户的导入历史记录）
CREATE INDEX idx_user_create ON health_import_record(user_id, create_time);
