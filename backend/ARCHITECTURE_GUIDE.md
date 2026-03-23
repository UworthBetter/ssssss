# 健康监测AI决策系统 - 完整架构指南

## 系统流程图

```
设备数据
   ↓
[异常检测] → 发布事件 → 启动管道
   ↓
[事件洞察] → AI补全医学上下文
   ↓
[风险评估] → Python模型计算风险分数
   ↓
[处置规则] → 根据风险等级生成建议
   ↓
[自动执行] → 高风险自动处理(可选)
   ↓
[操作留痕] → 记录完整决策链路
   ↓
[反馈闭环] → 实际结果反馈优化模型
```

## 核心模块

### 1. 异常检测 (AbnormalDetectionController)
- 输入: 设备数据(心率、血氧、体温等)
- 输出: 异常检测结果 + 事件ID
- **关键改进**: 检测到异常后自动启动完整管道

### 2. 事件处理管道 (EventProcessingPipelineService)
- 统一编排所有后续处理步骤
- 优先级筛选(低优先级直接记录,不处理)
- 自动调用: 洞察 → 风险 → 处置
- 记录每一步的操作日志

### 3. 事件洞察 (EventInsightService)
- 调用LLM补全医学上下文
- 生成医生报告
- 存储到快照表供后续使用

### 4. 风险评估 (RiskScoreService)
- 调用Python模型计算风险分数(0-100)
- 输入包含: 异常数据 + 事件洞察
- 输出: 风险分数 + 风险等级

### 5. 处置规则引擎 (DispositionRuleEngine)
- 根据异常类型和风险分数生成处置建议
- 判断是否应该自动执行
- 确定通知级别(URGENT/HIGH/NORMAL)

### 6. 操作审计 (OperationAuditService)
- 记录每个操作步骤的详细信息
- 支持完整的链路追踪
- 用于合规性审计和模型优化

## 数据库表结构

### ai_event_processing_pipeline
统一的事件处理管道表,记录:
- 当前处理阶段
- 各阶段的结果(洞察ID、风险分数、处置建议)
- 执行状态和结果
- 反馈信息

### ai_operation_audit_log
操作审计日志,记录:
- 每个操作的类型和详情
- 操作的上下文(异常类型、风险分数)
- 操作结果
- 时间戳和操作人信息

### ai_disposition_rule
处置规则配置表,支持:
- 按异常类型和风险范围配置规则
- 自动执行开关
- 通知级别配置

## 集成步骤

### 1. 执行SQL脚本
```bash
mysql -u root -p < backend/sql/event_processing_pipeline.sql
mysql -u root -p < backend/sql/operation_audit_log.sql
```

### 2. 配置处置规则
在`ai_disposition_rule`表中插入规则:
```sql
INSERT INTO ai_disposition_rule VALUES
(NULL, '心率异常', 80, 100, 1, '立即通知医生,建议患者就医', 'URGENT', 1, NOW()),
(NULL, '血氧偏低', 80, 100, 1, '立即通知医生,建议患者吸氧', 'URGENT', 1, NOW()),
(NULL, '心率异常', 50, 79, 0, '建议患者休息,监测心率变化', 'HIGH', 1, NOW());
```

### 3. 验证流程
```bash
# 1. 发送异常检测请求
POST /ai/abnormal/detect
{
  "patientId": 1,
  "patientName": "张三",
  "metricType": "heart_rate",
  "abnormalValue": "120",
  "riskLevel": "warning"
}

# 2. 查看处理进度
GET /ai/event-processing/status/{eventId}

# 3. 查看完整操作链路
GET /ai/event-processing/audit-trail/{eventId}

# 4. 记录处置反馈
POST /ai/event-processing/feedback/{eventId}
{
  "actualOutcome": "RESOLVED",
  "feedbackScore": 5
}
```

## 性能优化

### 优先级筛选
- 低优先级异常(priority < 30)直接记录,不触发完整流程
- 减少不必要的AI调用,降低成本

### 异步处理
建议使用消息队列异步处理:
```java
// 在AbnormalDetectionController中
if (eventId != null) {
    messageQueue.send("event.processing", eventId);
}
```

### 缓存策略
- 缓存处置规则(5分钟过期)
- 缓存风险评分模型版本

## 监控指标

1. **处理延迟**: 从异常检测到处置建议的时间
2. **准确率**: 实际结果与建议的匹配度
3. **自动执行率**: 自动执行的异常占比
4. **误报率**: 低优先级异常的实际严重程度

## 故障处理

### 风险评估服务不可用
- 使用默认风险分数(50)
- 记录错误日志
- 继续处理流程

### LLM服务超时
- 使用快速洞察(不调用LLM)
- 异步补全完整洞察

## 安全考虑

1. **操作审计**: 所有操作都有完整的审计日志
2. **权限控制**: 自动执行需要特殊权限
3. **数据隐私**: 敏感信息加密存储
4. **链路追踪**: 支持完整的请求链路追踪
