-- ============================================
-- 添加health_subject表索引的SQL脚本
-- ============================================
-- 数据库: qkyd_health
-- 表: health_subject
-- 目标: 优化查询性能，响应时间从578ms降至<200ms

-- 使用方法：
-- 1. 在MySQL客户端中复制下面的SQL
-- 2. 执行SQL
-- 3. 验证索引已添加

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
  AND INDEX_NAME IN ('idx_user_id', 'idx_record_time', 'idx_user_time', 'idx_user_type_time')
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

-- ============================================
-- 测试索引效果
-- ============================================

-- 测试查询1: 按用户ID查询
-- 使用索引: idx_user_id
-- EXPLAIN SELECT * FROM health_subject WHERE user_id = 1 ORDER BY record_time DESC LIMIT 20;

-- 测试查询2: 按时间范围查询
-- 使用索引: idx_record_time
-- EXPLAIN SELECT * FROM health_subject WHERE record_time >= '2026-01-01' ORDER BY record_time DESC LIMIT 20;

-- 测试查询3: 用户+时间组合查询
-- 使用索引: idx_user_time
-- EXPLAIN SELECT * FROM health_subject WHERE user_id = 1 AND record_time >= '2026-01-01' ORDER BY record_time DESC LIMIT 20;

-- ============================================
-- 完成信息
-- ============================================

SELECT '索引创建完成！' AS status;
SELECT COUNT(*) AS '创建的索引数量' FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'qkyd_health'
  AND TABLE_NAME = 'health_subject'
  AND INDEX_NAME IN ('idx_user_id', 'idx_record_time', 'idx_user_time', 'idx_user_type_time');

-- ============================================
-- 使用说明
-- ============================================

-- 1. 索引添加后，需要重新启动后端服务以使其生效
-- 2. 首次查询可能较慢（缓存未命中）
-- 3. 后续查询性能会显著提升
-- 4. 建议使用EXPLAIN验证索引使用情况
-- 5. 如果查询仍然慢，检查是否使用了索引

-- ============================================
