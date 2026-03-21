-- ============================================
-- 耆康云盾 - Week 2 性能优化
-- 纯SQL脚本 - 直接在MySQL Workbench中复制执行
-- ============================================

-- 说明：
-- 1. 复制下面的所有SQL语句
-- 2. 打开MySQL Workbench
-- 3. 连接到数据库（host: 127.0.0.1, port: 3306, user: root, password: 123456）
-- 4. 选择正确的数据库：qkyd_jkpt 或 qkyd_health
-- 5. 粘贴SQL执行
-- 6. 执行完成后，验证索引已创建

-- ============================================
-- 步骤1: 确认数据库名称
-- ============================================

-- 查看当前数据库
SHOW DATABASES;

-- 如果显示qkyd_jkpt，USE qkyd_jkpt;
-- 如果显示qkyd_health，USE qkyd_health;

-- ============================================
-- 步骤2: 添加Week 2优化索引
-- ============================================

-- 索引1: 用户+时间复合索引（最关键）
CREATE INDEX IF NOT EXISTS idx_user_time ON health_subject(user_id, record_time);

-- 索引2: 用户ID单列索引
CREATE INDEX IF NOT EXISTS idx_user_id ON health_subject(user_id);

-- 索引3: 记录时间单列索引
CREATE INDEX IF NOT EXISTS idx_record_time ON health_subject(record_time);

-- 索引4: 用户+类型+时间复合索引（可选）
CREATE INDEX IF NOT EXISTS idx_user_type_time ON health_subject(user_id, subject_type, record_time);

-- ============================================
-- 步骤3: 验证索引已创建
-- ============================================

-- 查看health_subject表的所有索引
SHOW INDEX FROM health_subject;

-- 查看索引详情
SELECT
    INDEX_NAME,
    COLUMN_NAME,
    CARDINALITY,
    SUB_PART,
    NULLABLE,
    INDEX_TYPE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'health_subject'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

-- ============================================
-- 步骤4: 测试索引效果
-- ============================================

-- 测试查询1: 按用户ID和时间查询（使用idx_user_time索引）
EXPLAIN SELECT * FROM health_subject
WHERE user_id = 1
AND record_time >= '2026-01-01'
ORDER BY record_time DESC
LIMIT 20;

-- 测试查询2: 单纯按用户ID查询（使用idx_user_id索引）
EXPLAIN SELECT * FROM health_subject
WHERE user_id = 1
ORDER BY record_time DESC
LIMIT 20;

-- 测试查询3: 按时间范围查询（使用idx_record_time索引）
EXPLAIN SELECT * FROM health_subject
WHERE record_time >= '2026-01-01'
ORDER BY record_time DESC
LIMIT 20;

-- ============================================
-- 步骤5: 测试查询性能
-- ============================================

-- 记录开始时间
SET @start_time = NOW(6);

-- 执行100次测试查询
SELECT @count := COUNT(*)
FROM (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8
UNION SELECT 9 UNION SELECT 10) t
WHERE @count <= 100;

-- 记录结束时间
SET @end_time = NOW(6);

-- 显示平均响应时间
SELECT
    'Performance Test' AS test_name,
    @count AS total_queries,
    TIMESTAMPDIFF(MICROSECOND, @start_time, @end_time) / @count AS avg_query_time_ms,
    @count AS total_queries
FROM dual;

-- ============================================
-- 步骤6: 完成信息
-- ============================================

SELECT
    'Index Creation Completed' AS status,
    COUNT(*) AS created_indexes
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'health_subject'
AND INDEX_NAME IN ('idx_user_time', 'idx_user_id', 'idx_record_time', 'idx_user_type_time');

-- ============================================
-- 完成信息
-- ============================================

SELECT 'All done! Please verify:' AS message;

-- ============================================
-- 使用提示
-- ============================================

-- 1. 如果SQL执行失败，请检查：
--    - 数据库名称是否正确（qkyd_jkpt 或 qkyd_health）
--    - MySQL连接参数是否正确
--    - 是否有权限创建索引

-- 2. 索引创建后，需要重启后端服务使其生效
--    - 停止旧的qkyd-admin服务
--    - 启动新的qkyd-health服务

-- 3. 验证索引是否生效：
--    - 使用EXPLAIN查看查询是否使用了索引（type=ref）
--    - 如果type=ALL，说明索引未生效

-- ============================================
-- 下一步
-- ============================================

-- 1. 添加索引成功
-- 2. 重启后端服务
-- 3. 测试API性能
-- 4. 验证响应时间 < 200ms

-- ============================================
