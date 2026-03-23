# 部署清单

## 数据库初始化
- [ ] 执行 `event_processing_pipeline.sql`
- [ ] 执行 `operation_audit_log.sql`
- [ ] 在 `ai_disposition_rule` 表中配置处置规则

## 代码集成
- [ ] 新增 `EventProcessingPipelineService` 接口和实现
- [ ] 新增 `DispositionRuleEngine` 接口和实现
- [ ] 新增 `OperationAuditService` 接口和实现
- [ ] 更新 `AbnormalDetectionController` 启动管道
- [ ] 新增 `EventProcessingController` 查询接口

## 配置检查
- [ ] 确认 `IEventInsightService` 已实现
- [ ] 确认 `IRiskScoreService` 已实现
- [ ] 确认 Python 风险评估服务可用
- [ ] 确认 LLM 服务可用

## 测试验证
- [ ] 运行 `EventProcessingIntegrationTest`
- [ ] 手动测试高风险异常流程
- [ ] 手动测试低优先级异常跳过
- [ ] 验证操作审计日志完整性

## 监控部署
- [ ] 配置日志收集(操作审计日志)
- [ ] 配置性能监控(处理延迟)
- [ ] 配置告警规则(自动执行失败)

## 文档更新
- [ ] 更新 API 文档
- [ ] 更新架构文档
- [ ] 更新运维手册

## 灰度发布
- [ ] 10% 流量灰度
- [ ] 监控 24 小时
- [ ] 100% 全量发布
