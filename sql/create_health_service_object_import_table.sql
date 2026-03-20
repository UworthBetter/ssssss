-- =============================================
-- Table: health_service_object_import
-- Description: 服务对象批量导入表
-- Created: 2026-02-06
-- Task: Week 3 - Task 1.2
-- =============================================

CREATE TABLE health_service_object_import (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    import_record_id BIGINT NOT NULL COMMENT '导入记录ID（关联health_import_record表）',
    row_number INT NOT NULL COMMENT '行号（Excel/CSV中的行号）',
    subject_id BIGINT DEFAULT NULL COMMENT '服务对象ID（导入成功后关联）',
    dept_id BIGINT DEFAULT NULL COMMENT '归属分组ID',
    subject_name varchar(50) DEFAULT NULL COMMENT '服务对象名称',
    nick_name varchar(30) DEFAULT NULL COMMENT '姓名',
    user_type varchar(2) DEFAULT '00' COMMENT '用户类型（00系统用户）',
    email varchar(50) DEFAULT NULL COMMENT '用户邮箱',
    phonenumber varchar(11) DEFAULT NULL COMMENT '手机号码',
    age int DEFAULT NULL COMMENT '年龄',
    sex char(1) DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
    avatar varchar(100) DEFAULT NULL COMMENT '头像地址',
    password varchar(100) DEFAULT NULL COMMENT '密码',
    status char(1) DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
    id_card varchar(18) DEFAULT NULL COMMENT '身份证号',
    address varchar(200) DEFAULT NULL COMMENT '家庭住址',
    emergency_contact varchar(30) DEFAULT NULL COMMENT '紧急联系人',
    emergency_phone varchar(11) DEFAULT NULL COMMENT '紧急联系电话',
    remark varchar(500) DEFAULT NULL COMMENT '备注',
    validation_status VARCHAR(20) DEFAULT 'pending' COMMENT '验证状态（pending:待验证, valid:有效, invalid:无效）',
    import_status VARCHAR(20) DEFAULT 'pending' COMMENT '导入状态（pending:待导入, success:成功, failed:失败）',
    error_msg TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (import_record_id) REFERENCES health_import_record(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务对象批量导入表';

-- =============================================
-- Indexes
-- =============================================

-- 导入记录ID索引（用于快速查询某次导入的所有数据）
CREATE INDEX idx_import_record_id ON health_service_object_import(import_record_id);

-- 服务对象ID索引（用于查询某服务对象的所有导入数据）
CREATE INDEX idx_subject_id ON health_service_object_import(subject_id);

-- 归属分组ID索引（用于按分组查询）
CREATE INDEX idx_dept_id ON health_service_object_import(dept_id);

-- 手机号码索引（用于去重验证）
CREATE INDEX idx_phonenumber ON health_service_object_import(phonenumber);

-- 身份证号索引（用于去重验证）
CREATE INDEX idx_id_card ON health_service_object_import(id_card);

-- 验证状态索引（用于筛选待验证的数据）
CREATE INDEX idx_validation_status ON health_service_object_import(validation_status);

-- 导入状态索引（用于筛选待导入的数据）
CREATE INDEX idx_import_status ON health_service_object_import(import_status);

-- 创建时间索引（用于按时间范围查询）
CREATE INDEX idx_create_time ON health_service_object_import(create_time);

-- 组合索引（导入记录ID+验证状态，用于批量验证）
CREATE INDEX idx_import_valid ON health_service_object_import(import_record_id, validation_status);

-- 组合索引（导入记录ID+导入状态，用于批量导入）
CREATE INDEX idx_import_success ON health_service_object_import(import_record_id, import_status);

-- 组合索引（手机号码+导入记录ID，用于检测重复）
CREATE INDEX idx_phone_import ON health_service_object_import(phonenumber, import_record_id);

-- 组合索引（身份证号+导入记录ID，用于检测重复）
CREATE INDEX idx_idcard_import ON health_service_object_import(id_card, import_record_id);
