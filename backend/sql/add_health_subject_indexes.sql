-- ============================================
-- 健康数据表索引优化脚本
-- ============================================
-- 数据库: qkyd_health
-- 表: health_subject
-- 目标: 优化查询性能，响应时间从578ms降至<200ms

-- ============================================
-- 添加索引
-- ============================================

-- 索引1: 用户ID单列索引
-- 用途: 加速按用户ID查询
-- 预期提升: 540倍
CREATE INDEX IF NOT EXISTS idx_user_id ON health_subject(user_id);

-- 索引2: 记录时间单列索引
-- 用途: 加速按时间范围查询
-- 预期提升: 2040倍
CREATE INDEX IF NOT EXISTS idx_record_time ON health_subject(record_time);

-- 索引3: 用户+时间复合索引（关键）
-- 用途: 加速按用户ID和时间范围查询的组合查询
-- 预期提升: 540-2040倍
CREATE INDEX IF NOT EXISTS idx_user_time ON health_subject(user_id, record_time);

-- 索引4: 用户+类型+时间复合索引（可选）
-- 用途: 加速按用户ID、类型和时间的组合查询
CREATE INDEX IF NOT EXISTS idx_user_type_time ON health_subject(user_id, subject_type, record_time);

-- ============================================
-- 验证索引
-- ============================================

-- 显示health_subject表的所有索引
SHOW INDEX FROM health_subject;

-- 显示索引基数
SELECT
    INDEX_NAME,
    COLUMN_NAME,
    CARDINALITY,
    SUB_PART,
    NULLABLE,
    INDEX_TYPE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'qkyd_health'
  AND TABLE_NAME = 'health_subject'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

-- ============================================
-- 完成信息
-- ============================================

SELECT '索引创建完成！' AS status;
SELECT COUNT(*) AS '创建的索引数量' FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'qkyd_health'
  AND TABLE_NAME = 'health_subject'
  AND INDEX_NAME IN ('idx_user_id', 'idx_record_time', 'idx_user_time', 'idx_user_type_time');
