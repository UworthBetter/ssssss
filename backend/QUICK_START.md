# 快速开始

## 核心流程

### 1. 异常检测触发管道
```bash
POST /ai/abnormal/detect
Content-Type: application/json

{
  "patientId": 1,
  "patientName": "张三",
  "metricType": "heart_rate",
  "abnormalValue": "150",
  "normalRange": "60-100",
  "abnormalType": "too_high",
  "riskLevel": "danger"
}
```

**响应**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "eventId": 12345,
    "isAbnormal": true,
    "abnormalType": "心率异常",
    "riskLevel": "danger"
  }
}
```

### 2. 查看处理进度
```bash
GET /ai/event-processing/status/12345
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "stage": "COMPLETED",
    "priority": 95,
    "riskScore": 85,
    "disposition": "立即通知医生,建议患者就医",
    "executionStatus": "SUCCESS"
  }
}
```

### 3. 查看完整操作链路
```bash
GET /ai/event-processing/audit-trail/12345
```

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "operation_type": "DETECT",
      "operation_detail": "异常检测完成",
      "abnormal_type": "心率异常",
      "risk_score": 95,
      "created_at": "2026-03-23 08:00:00"
    },
    {
      "operation_type": "INSIGHT_BUILD",
      "operation_detail": "事件洞察构建完成",
      "created_at": "2026-03-23 08:00:01"
    },
    {
      "operation_type": "RISK_ASSESS",
      "operation_detail": "风险评估完成",
      "risk_score": 85,
      "created_at": "2026-03-23 08:00:02"
    },
    {
      "operation_type": "DISPOSITION",
      "operation_detail": "立即通知医生,建议患者就医",
      "disposition_action": "立即通知医生,建议患者就医",
      "created_at": "2026-03-23 08:00:03"
    },
    {
      "operation_type": "EXECUTE",
      "operation_detail": "自动执行处置",
      "created_at": "2026-03-23 08:00:04"
    }
  ]
}
```

### 4. 记录处置反馈
```bash
POST /ai/event-processing/feedback/12345
Content-Type: application/json

{
  "actualOutcome": "RESOLVED",
  "feedbackScore": 5
}
```

## 关键特性

### ✅ 完整的流程编排
- 异常检测 → 事件洞察 → 风险评估 → 处置建议 → 自动执行
- 所有步骤自动串联,无需手动调用

### ✅ 智能优先级筛选
- 低优先级异常直接记录,不触发完整流程
- 减少不必要的AI调用,降低成本

### ✅ 完整的操作审计
- 每个操作步骤都有详细的日志记录
- 支持完整的链路追踪和合规性审计

### ✅ 反馈闭环
- 记录实际处置结果
- 用于模型优化和准确率提升

## 配置处置规则

```sql
-- 高风险心率异常: 自动执行
INSERT INTO ai_disposition_rule VALUES
(NULL, '心率异常', 80, 100, 1, '立即通知医生,建议患者就医', 'URGENT', 1, NOW());

-- 中等风险心率异常: 不自动执行,只通知
INSERT INTO ai_disposition_rule VALUES
(NULL, '心率异常', 50, 79, 0, '建议患者休息,监测心率变化', 'HIGH', 1, NOW());

-- 低风险: 继续监测
INSERT INTO ai_disposition_rule VALUES
(NULL, '心率异常', 0, 49, 0, '继续监测', 'NORMAL', 1, NOW());
```

## 故障排查

### 问题: 管道没有自动启动
**解决**: 检查 `AbnormalDetectionController` 是否注入了 `IEventProcessingPipelineService`

### 问题: 操作日志为空
**解决**: 检查 `ai_operation_audit_log` 表是否创建,检查 `OperationAuditService` 是否正确注入

### 问题: 处置建议不正确
**解决**: 检查 `ai_disposition_rule` 表中的规则配置是否正确
