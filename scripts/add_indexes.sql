-- ==========================================
-- 添加health_subject表的索引
-- ==========================================
-- 目标: 优化查询性能，响应时间从578ms降至<200ms

USE qkyd_health;

-- ==========================================
-- 添加索引
-- ==========================================

-- 索引1: 用户+时间复合索引（关键）
-- 用途: 加速按用户ID和时间范围查询的组合查询
CREATE INDEX IF NOT EXISTS idx_user_time ON health_subject(user_id, record_time);

-- 索引2: 用户ID单列索引
-- 用途: 加速按用户ID查询
CREATE INDEX IF NOT EXISTS idx_user_id ON health_subject(user_id);

-- 索引3: 记录时间单列索引
-- 用途: 加速按时间范围查询
CREATE INDEX IF NOT EXISTS idx_record_time ON health_subject(record_time);

-- 验证索引
SELECT 
    '索引添加完成！' AS status,
    COUNT(*) AS '创建的索引数量'
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'qkyd_health'
AND TABLE_NAME = 'health_subject'
AND INDEX_NAME IN ('idx_user_time', 'idx_user_id', 'idx_record_time');

-- 显示索引详情
SELECT 
    INDEX_NAME,
    COLUMN_NAME,
    CARDINALITY,
    SEQ_IN_INDEX
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'qkyd_health'
AND TABLE_NAME = 'health_subject'
AND INDEX_NAME IN ('idx_user_time', 'idx_user_id', 'idx_record_time')
ORDER BY INDEX_NAME, SEQ_IN_INDEX;
