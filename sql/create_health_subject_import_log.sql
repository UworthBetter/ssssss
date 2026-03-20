-- =============================================
-- Table: health_subject_import_log
-- Description: 服务对象导入日志表
-- Created: 2026-02-06
-- Task: Week 3 - Task 3.35
-- =============================================

CREATE TABLE health_subject_import_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '操作用户ID',
    user_name VARCHAR(50) DEFAULT NULL COMMENT '操作用户名',
    import_type VARCHAR(50) NOT NULL DEFAULT 'EXCEL' COMMENT '导入类型（EXCEL/CSV）',
    file_name VARCHAR(255) NOT NULL COMMENT '导入文件名',
    file_path VARCHAR(500) DEFAULT NULL COMMENT '文件路径',
    total_rows INT DEFAULT 0 COMMENT '总记录数',
    success_rows INT DEFAULT 0 COMMENT '成功导入记录数',
    fail_rows INT DEFAULT 0 COMMENT '失败记录数',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '导入状态（PENDING:待处理, PROCESSING:处理中, SUCCESS:成功, FAILED:失败, PARTIAL:部分成功）',
    start_time DATETIME DEFAULT NULL COMMENT '导入开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '导入结束时间',
    cost_time INT DEFAULT NULL COMMENT '耗时（毫秒）',
    error_msg TEXT COMMENT '错误信息',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务对象导入日志表';

-- =============================================
-- Indexes
-- =============================================

-- 用户ID索引（用于查询用户的导入历史）
CREATE INDEX idx_user_id ON health_subject_import_log(user_id);

-- 导入类型索引（用于按类型统计和查询）
CREATE INDEX idx_import_type ON health_subject_import_log(import_type);

-- 导入状态索引（用于按状态筛选）
CREATE INDEX idx_status ON health_subject_import_log(status);

-- 创建时间索引（用于按时间范围查询）
CREATE INDEX idx_create_time ON health_subject_import_log(create_time);

-- 组合索引（用户ID+创建时间，用于查询用户的导入历史记录）
CREATE INDEX idx_user_create ON health_subject_import_log(user_id, create_time);

-- 组合索引（导入状态+创建时间，用于查询历史记录）
CREATE INDEX idx_status_create ON health_subject_import_log(status, create_time);
