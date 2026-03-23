# 健康监测AI决策系统 - 完整解决方案

## 📋 项目概述

这是一个为健康监测系统设计的完整AI决策解决方案,解决了从异常检测到处置执行的完整流程编排问题。

### 核心问题

原始系统存在的问题:
1. **流程断裂** - 各模块独立,没有统一编排
2. **数据流不完整** - 信息在各表之间断裂
3. **价值导向不足** - 所有异常都走完整流程,浪费资源
4. **缺少审计** - 没有完整的操作链路记录

### 解决方案

✅ **统一流程编排** - 自动串联异常→洞察→风险→处置→执行
✅ **智能优先级筛选** - 低优先级异常直接记录,降低成本
✅ **完整操作审计** - 每个操作都有详细日志,支持合规性审计
✅ **反馈闭环** - 记录实际结果,用于模型优化

## 🏗️ 系统架构

```
设备数据
   ↓
[异常检测] → 启动管道
   ↓
[优先级筛选] → 低优先级跳过
   ↓
[事件洞察] → AI补全医学上下文
   ↓
[风险评估] → Python模型计算分数
   ↓
[处置规则] → 根据风险等级生成建议
   ↓
[自动执行] → 高风险自动处理
   ↓
[操作审计] → 记录完整决策链路
   ↓
[反馈闭环] → 实际结果反馈优化
```

## 📦 核心模块

### 1. 事件处理管道 (EventProcessingPipeline)
- **职责**: 统一编排所有处理步骤
- **特性**: 自动串联、优先级筛选、错误处理
- **文件**: `EventProcessingPipelineServiceImpl.java`

### 2. 处置规则引擎 (DispositionRuleEngine)
- **职责**: 根据异常类型和风险分数生成处置建议
- **特性**: 规则配置、自动执行判断、通知级别
- **文件**: `DispositionRuleEngineImpl.java`

### 3. 操作审计服务 (OperationAuditService)
- **职责**: 记录完整的操作链路
- **特性**: 审计日志、链路追踪、合规性支持
- **文件**: `OperationAuditServiceImpl.java`

### 4. 事件处理控制器 (EventProcessingController)
- **职责**: 提供查询和反馈接口
- **特性**: 进度查询、审计查询、反馈记录
- **文件**: `EventProcessingController.java`

## 📊 数据库设计

### ai_event_processing_pipeline
统一的事件处理管道表
```sql
- id: 事件ID
- abnormal_id: 异常记录ID
- stage: 处理阶段 (DETECTED/INSIGHT_BUILT/RISK_ASSESSED/...)
- priority: 优先级 (0-100)
- risk_score: 风险分数 (0-100)
- disposition_suggestion: 处置建议
- execution_status: 执行状态
- feedback_score: 反馈评分
```

### ai_operation_audit_log
操作审计日志表
```sql
- id: 日志ID
- event_id: 事件ID
- operation_type: 操作类型 (DETECT/INSIGHT_BUILD/RISK_ASSESS/...)
- operation_detail: 操作详情
- abnormal_type: 异常类型
- risk_score: 风险分数
- created_at: 创建时间
```

### ai_disposition_rule
处置规则配置表
```sql
- id: 规则ID
- abnormal_type: 异常类型
- risk_level_min: 风险下限
- risk_level_max: 风险上限
- auto_execute: 是否自动执行
- disposition_action: 处置动作
- notification_level: 通知级别
```

## 🚀 快速开始

### 1. 数据库初始化
```bash
mysql -u root -p your_database < backend/sql/event_processing_pipeline.sql
mysql -u root -p your_database < backend/sql/operation_audit_log.sql
```

### 2. 配置处置规则
```sql
INSERT INTO ai_disposition_rule VALUES
(NULL, '心率异常', 80, 100, 1, '立即通知医生,建议患者就医', 'URGENT', 1, NOW()),
(NULL, '心率异常', 50, 79, 0, '建议患者休息,监测心率变化', 'HIGH', 1, NOW());
```

### 3. 编译和部署
```bash
mvn clean package
java -jar qkyd-ai.jar
```

### 4. 测试流程
```bash
# 发送异常检测请求
POST /ai/abnormal/detect
{
  "patientId": 1,
  "patientName": "张三",
  "metricType": "heart_rate",
  "abnormalValue": "150",
  "riskLevel": "danger"
}

# 查看处理进度
GET /ai/event-processing/status/{eventId}

# 查看操作链路
GET /ai/event-processing/audit-trail/{eventId}

# 记录反馈
POST /ai/event-processing/feedback/{eventId}
{
  "actualOutcome": "RESOLVED",
  "feedbackScore": 5
}
```

## 📈 性能指标

| 指标 | 目标 | 说明 |
|------|------|------|
| 处理延迟 | < 3秒 | 从异常检测到处置建议 |
| 准确率 | > 90% | 实际结果与建议的匹配度 |
| 自动执行率 | 10-20% | 自动执行的异常占比 |
| 误报率 | < 5% | 低优先级异常的实际严重程度 |
| 成本降低 | 30-50% | 通过优先级筛选降低AI调用成本 |

## 📚 文档

- **ARCHITECTURE_GUIDE.md** - 完整架构文档
- **QUICK_START.md** - 快速开始指南
- **FLOW_DIAGRAM.md** - 流程图解
- **FAQ.md** - 常见问题解答
- **DEPLOYMENT_CHECKLIST.md** - 部署清单
- **INTEGRATION_CHECKLIST.md** - 集成检查清单
- **SOLUTION_SUMMARY.md** - 解决方案总结

## 🔧 集成步骤

### 第一步: 复制文件
```
backend/sql/
├── event_processing_pipeline.sql
└── operation_audit_log.sql

backend/qkyd-ai/src/main/java/com/qkyd/ai/
├── service/
│   ├── IEventProcessingPipelineService.java
│   ├── IDispositionRuleEngine.java
│   ├── IOperationAuditService.java
│   └── impl/
│       ├── EventProcessingPipelineServiceImpl.java
│       ├── DispositionRuleEngineImpl.java
│       └── OperationAuditServiceImpl.java
└── controller/
    └── EventProcessingController.java
```

### 第二步: 更新异常检测控制器
```java
@Autowired
private IEventProcessingPipelineService pipelineService;

// 在异常检测后
if (eventId != null) {
    pipelineService.startPipeline(eventId, abnormalData);
}
```

### 第三步: 执行数据库脚本
```bash
mysql -u root -p < backend/sql/event_processing_pipeline.sql
mysql -u root -p < backend/sql/operation_audit_log.sql
```

### 第四步: 配置规则
```sql
INSERT INTO ai_disposition_rule VALUES (...);
```

### 第五步: 测试验证
```bash
mvn test -Dtest=EventProcessingIntegrationTest
```

## 🎯 关键特性

### ✅ 自动流程编排
- 异常检测后自动启动完整管道
- 无需手动调用各个服务
- 支持错误处理和重试

### ✅ 智能优先级筛选
- 低优先级异常直接记录
- 不调用昂贵的AI和模型服务
- 预计降低30-50%的成本

### ✅ 完整操作审计
- 每个操作步骤都有详细日志
- 支持完整的链路追踪
- 满足医疗行业合规要求

### ✅ 规则驱动处置
- 处置建议配置化
- 支持自动执行判断
- 易于维护和扩展

### ✅ 反馈闭环
- 记录实际处置结果
- 用于模型优化
- 提升系统准确率

## 🔍 故障排查

### 问题: 管道没有自动启动
**解决**: 检查 `AbnormalDetectionController` 是否注入了 `IEventProcessingPipelineService`

### 问题: 操作日志为空
**解决**: 检查 `ai_operation_audit_log` 表是否创建,检查 `OperationAuditService` 是否正确注入

### 问题: 处置建议不正确
**解决**: 检查 `ai_disposition_rule` 表中的规则配置是否正确

更多问题请参考 **FAQ.md**

## 📞 支持

- 查看 **ARCHITECTURE_GUIDE.md** 了解系统架构
- 查看 **QUICK_START.md** 快速开始
- 查看 **FAQ.md** 常见问题
- 查看 **INTEGRATION_CHECKLIST.md** 集成检查清单

## 📝 许可证

Copyright © 2026. All rights reserved.

## 🙏 致谢

感谢所有贡献者和用户的支持!
