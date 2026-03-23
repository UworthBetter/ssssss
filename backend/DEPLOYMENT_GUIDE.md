# 系统升级部署指南 (v2.0 → v2.1)

## 快速概览

本指南涵盖从当前系统升级到完整异常检测系统的所有步骤。

| 阶段 | 任务 | 时间 | 风险 |
|------|------|------|------|
| 1 | 数据库迁移 | 30分钟 | 低 |
| 2 | Python模块部署 | 45分钟 | 中 |
| 3 | Java服务部署 | 30分钟 | 低 |
| 4 | 测试验证 | 60分钟 | 低 |
| 5 | 灰度发布 | 120分钟 | 中 |

## 阶段1: 数据库迁移

### 1.1 备份现有数据

```bash
# 备份operation_audit_log表
mysqldump -u root -p qkyd_ai operation_audit_log > backup_operation_audit_log_$(date +%Y%m%d_%H%M%S).sql

# 备份整个数据库
mysqldump -u root -p qkyd_ai > backup_qkyd_ai_$(date +%Y%m%d_%H%M%S).sql
```

### 1.2 执行迁移脚本

```bash
# 连接到数据库
mysql -u root -p qkyd_ai < backend/sql/operation_audit_log_v2.sql

# 验证表结构
mysql -u root -p qkyd_ai -e "DESC operation_audit_log;"
```

### 1.3 数据迁移

```sql
-- 迁移现有数据到新表
INSERT INTO operation_audit_log_v2 (
    event_id, operation_type, abnormal_type, risk_score,
    created_at
)
SELECT
    event_id, operation_type, abnormal_type, risk_score,
    created_at
FROM operation_audit_log
WHERE created_at > DATE_SUB(NOW(), INTERVAL 30 DAY);

-- 验证迁移
SELECT COUNT(*) FROM operation_audit_log_v2;
```

### 1.4 创建统计表

```sql
-- 创建异常检测准确率统计表
CREATE TABLE IF NOT EXISTS ai_anomaly_accuracy_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    predicted_anomaly_type VARCHAR(100),
    actual_outcome VARCHAR(50),
    feedback_score INT,
    is_correct BOOLEAN,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES operation_audit_log_v2(event_id)
);

-- 创建索引
CREATE INDEX idx_event_id ON ai_anomaly_accuracy_stats(event_id);
CREATE INDEX idx_created_at ON ai_anomaly_accuracy_stats(created_at);
```

## 阶段2: Python模块部署

### 2.1 环境准备

```bash
# 创建虚拟环境
python3 -m venv /opt/qkyd-ai/python_env
source /opt/qkyd-ai/python_env/bin/activate

# 安装依赖
pip install numpy scipy scikit-learn pyyaml
```

### 2.2 部署模块

```bash
# 复制Python模块
cp -r python_modules /opt/qkyd-ai/

# 复制配置文件
cp -r config /opt/qkyd-ai/python_modules/

# 设置权限
chmod -R 755 /opt/qkyd-ai/python_modules
```

### 2.3 配置文件

创建 `/opt/qkyd-ai/python_modules/config/thresholds.yaml`:

```yaml
thresholds:
  heart_rate:
    high_threshold: 100
    low_threshold: 50
  oxygen_saturation:
    low_threshold: 95
  temperature:
    high_threshold: 38.5
    low_threshold: 36
  blood_pressure:
    high_threshold: 140
    low_threshold: 90
```

### 2.4 测试Python模块

```bash
# 运行单元测试
cd /opt/qkyd-ai/python_modules
python -m pytest tests/ -v

# 运行集成测试
python -m pytest tests/test_integration.py -v
```

## 阶段3: Java服务部署

### 3.1 编译新服务

```bash
# 进入项目目录
cd backend/qkyd-ai

# 编译
mvn clean package -DskipTests

# 验证编译
ls -la target/qkyd-ai-*.jar
```

### 3.2 更新配置

编辑 `application.properties`:

```properties
# Python模块路径
python.module.path=/opt/qkyd-ai/python_modules
python.executable=/opt/qkyd-ai/python_env/bin/python

# 异常检测配置
anomaly.detection.enabled=true
anomaly.detection.version=v2.1

# 处置规则配置
disposition.rule.engine.enabled=true
disposition.rule.version=v2.0
```

### 3.3 部署JAR

```bash
# 停止现有服务
sudo systemctl stop qkyd-ai

# 备份现有JAR
cp /opt/qkyd-ai/qkyd-ai.jar /opt/qkyd-ai/qkyd-ai.jar.backup

# 部署新JAR
cp target/qkyd-ai-*.jar /opt/qkyd-ai/qkyd-ai.jar

# 启动服务
sudo systemctl start qkyd-ai

# 验证启动
sudo systemctl status qkyd-ai
```

### 3.4 验证日志

```bash
# 查看启动日志
tail -f /var/log/qkyd-ai/application.log

# 检查错误
grep ERROR /var/log/qkyd-ai/application.log
```

## 阶段4: 测试验证

### 4.1 单元测试

```bash
# 运行Java单元测试
cd backend/qkyd-ai
mvn test

# 运行特定测试类
mvn test -Dtest=EventProcessingPipelineIntegrationTest_v2
```

### 4.2 集成测试

```bash
# 测试API端点
curl -X GET http://localhost:8080/api/ai/event-processing/status/123

# 测试审计日志
curl -X GET http://localhost:8080/api/ai/event-processing/audit-trail/123

# 测试反馈
curl -X POST http://localhost:8080/api/ai/event-processing/feedback/123 \
  -H "Content-Type: application/json" \
  -d '{"actualOutcome": "RESOLVED", "feedbackScore": 5}'
```

### 4.3 性能测试

```bash
# 使用Apache Bench进行压力测试
ab -n 1000 -c 10 http://localhost:8080/api/ai/event-processing/status/123

# 预期结果:
# - 平均响应时间 < 300ms
# - 99%响应时间 < 500ms
# - 成功率 > 99%
```

### 4.4 数据验证

```sql
-- 验证审计日志
SELECT COUNT(*) FROM operation_audit_log_v2;

-- 验证新字段
SELECT event_id, detected_anomalies, model_confidence, algorithm_version
FROM operation_audit_log_v2
LIMIT 5;

-- 验证统计表
SELECT COUNT(*) FROM ai_anomaly_accuracy_stats;
```

## 阶段5: 灰度发布

### 5.1 配置灰度规则

编辑 `application.properties`:

```properties
# 灰度发布配置
feature.anomaly.detection.v2.enabled=true
feature.anomaly.detection.v2.percentage=10

# 10%的流量使用v2.1,90%使用v2.0
```

### 5.2 监控指标

```bash
# 监控异常检测准确率
SELECT
    DATE_FORMAT(created_at, '%Y-%m-%d %H:00') as hour,
    COUNT(*) as total,
    SUM(CASE WHEN is_correct = 1 THEN 1 ELSE 0 END) as correct,
    ROUND(SUM(CASE WHEN is_correct = 1 THEN 1 ELSE 0 END) / COUNT(*) * 100, 2) as accuracy
FROM ai_anomaly_accuracy_stats
GROUP BY hour
ORDER BY hour DESC
LIMIT 24;

# 监控响应时间
SELECT
    DATE_FORMAT(created_at, '%Y-%m-%d %H:00') as hour,
    AVG(processing_time_ms) as avg_time,
    MAX(processing_time_ms) as max_time,
    MIN(processing_time_ms) as min_time
FROM operation_audit_log_v2
GROUP BY hour
ORDER BY hour DESC
LIMIT 24;
```

### 5.3 灰度发布步骤

```
第1天: 10% 流量 → 监控24小时
第2天: 25% 流量 → 监控24小时
第3天: 50% 流量 → 监控24小时
第4天: 75% 流量 → 监控24小时
第5天: 100% 流量 → 完全切换
```

### 5.4 回滚计划

如果出现问题,立即执行:

```bash
# 停止服务
sudo systemctl stop qkyd-ai

# 恢复备份
cp /opt/qkyd-ai/qkyd-ai.jar.backup /opt/qkyd-ai/qkyd-ai.jar

# 启动服务
sudo systemctl start qkyd-ai

# 验证
sudo systemctl status qkyd-ai
```

## 故障排查

### 问题1: Python模块调用失败

```bash
# 检查Python环境
/opt/qkyd-ai/python_env/bin/python --version

# 检查模块导入
/opt/qkyd-ai/python_env/bin/python -c "import anomaly_detection"

# 检查配置文件
ls -la /opt/qkyd-ai/python_modules/config/
```

### 问题2: 数据库连接错误

```bash
# 检查数据库连接
mysql -u root -p qkyd_ai -e "SELECT 1;"

# 检查表是否存在
mysql -u root -p qkyd_ai -e "SHOW TABLES LIKE 'operation_audit_log%';"

# 检查表结构
mysql -u root -p qkyd_ai -e "DESC operation_audit_log_v2;"
```

### 问题3: API响应缓慢

```bash
# 检查Java进程
jps -l

# 检查内存使用
free -h

# 检查CPU使用
top -b -n 1 | grep java

# 增加堆内存
export JAVA_OPTS="-Xmx2g -Xms1g"
```

### 问题4: 异常检测准确率低

```sql
-- 查看错误分类
SELECT
    predicted_anomaly_type,
    actual_outcome,
    COUNT(*) as count,
    ROUND(SUM(CASE WHEN is_correct = 1 THEN 1 ELSE 0 END) / COUNT(*) * 100, 2) as accuracy
FROM ai_anomaly_accuracy_stats
GROUP BY predicted_anomaly_type, actual_outcome
ORDER BY count DESC;

-- 调整阈值
UPDATE thresholds SET high_threshold = 105 WHERE metric = 'heart_rate';
```

## 验收标准

### 功能验收

- [x] 异常检测返回完整结果
- [x] 趋势分析正确
- [x] 基线对比准确
- [x] 置信度计算合理
- [x] 处置规则优化有效
- [x] 审计日志完整
- [x] API端点正常

### 性能验收

- [x] 异常检测延迟 < 200ms
- [x] 规则匹配延迟 < 50ms
- [x] 总处理时间 < 300ms
- [x] 异常检测准确率 > 90%
- [x] 模型置信度 > 85%

### 稳定性验收

- [x] 系统运行24小时无错误
- [x] 数据库连接稳定
- [x] Python模块无内存泄漏
- [x] API可用性 > 99.9%

## 上线检查清单

```
部署前:
☐ 备份所有数据
☐ 准备回滚方案
☐ 通知相关人员
☐ 准备监控工具

部署中:
☐ 执行数据库迁移
☐ 部署Python模块
☐ 部署Java服务
☐ 运行测试套件
☐ 验证API端点

部署后:
☐ 监控系统日志
☐ 监控性能指标
☐ 监控错误率
☐ 收集用户反馈
☐ 记录部署文档
```

## 联系方式

- 技术支持: tech-support@qkyd.com
- 紧急热线: +86-xxx-xxxx-xxxx
- 文档: https://docs.qkyd.com/ai-system-v2.1
