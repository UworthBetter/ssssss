# 常见问题解答 (FAQ)

## 架构相关

### Q1: 为什么要分离优先级筛选?
A: 低优先级异常(如轻微波动)不需要调用昂贵的LLM和Python模型。通过优先级筛选:
- 降低30-50%的AI调用成本
- 提升系统吞吐量
- 保持高优先级异常的完整处理

### Q2: 处置规则引擎和事件洞察的区别?
A:
- **事件洞察**: 调用LLM补全医学上下文,生成医生报告
- **处置规则引擎**: 根据异常类型和风险分数生成处置建议

两者独立,事件洞察是可选的(用于医生参考),处置建议是必需的(用于自动处理)。

### Q3: 为什么需要操作审计日志?
A: 操作审计日志用于:
- **合规性**: 记录所有AI决策过程,满足医疗行业合规要求
- **可追溯性**: 支持完整的链路追踪,便于问题排查
- **模型优化**: 记录实际结果,用于优化AI模型
- **性能分析**: 分析各阶段的处理时间

## 集成相关

### Q4: 如何集成现有的异常检测模块?
A: 只需在 `AbnormalDetectionController` 中添加:
```java
@Autowired
private IEventProcessingPipelineService pipelineService;

// 在检测到异常后
if (eventId != null) {
    pipelineService.startPipeline(eventId, resultMap);
}
```

### Q5: 如何自定义处置规则?
A: 在 `ai_disposition_rule` 表中配置:
```sql
INSERT INTO ai_disposition_rule VALUES
(NULL, '异常类型', 风险下限, 风险上限, 是否自动执行, '处置动作', '通知级别', 1, NOW());
```

### Q6: 如何添加新的异常类型?
A:
1. 在异常检测模块中添加新的异常类型
2. 在 `ai_disposition_rule` 表中配置该异常类型的规则
3. 无需修改代码,规则配置自动生效

## 性能相关

### Q7: 系统能处理多少并发请求?
A: 取决于:
- 数据库连接池大小
- Python风险评估服务的吞吐量
- LLM服务的吞吐量

建议:
- 数据库连接池: 20-50
- 使用消息队列异步处理
- 缓存处置规则(5分钟过期)

### Q8: 处理延迟是多少?
A: 典型延迟:
- 异常检测: 100ms
- 事件洞察: 1-2s (调用LLM)
- 风险评估: 500ms (调用Python)
- 处置建议: 50ms (规则匹配)
- **总计**: 1.5-3s

优化方案:
- 并行调用洞察和风险评估
- 缓存LLM结果
- 使用异步处理

### Q9: 如何处理风险评估服务不可用?
A: 在 `EventProcessingPipelineServiceImpl` 中:
```java
try {
    AjaxResult riskResult = riskScoreService.assessRisk(riskData);
} catch (Exception e) {
    // 使用默认风险分数
    int riskScore = 50;
    auditService.logOperation(eventId, "RISK_ASSESS_FAILED", e.getMessage(), ...);
}
```

## 数据相关

### Q10: 如何查询某个患者的所有异常事件?
A:
```sql
SELECT p.*, a.operation_type, a.operation_detail
FROM ai_event_processing_pipeline p
JOIN ai_operation_audit_log a ON p.id = a.event_id
WHERE p.patient_id = ?
ORDER BY p.created_at DESC;
```

### Q11: 如何统计处置建议的准确率?
A:
```sql
SELECT
    COUNT(*) as total,
    SUM(CASE WHEN actual_outcome = 'RESOLVED' THEN 1 ELSE 0 END) as resolved,
    SUM(CASE WHEN actual_outcome = 'RESOLVED' THEN 1 ELSE 0 END) / COUNT(*) as accuracy
FROM ai_event_processing_pipeline
WHERE feedback_score IS NOT NULL;
```

### Q12: 如何清理历史数据?
A:
```sql
-- 删除30天前的低优先级异常
DELETE FROM ai_event_processing_pipeline
WHERE priority < 30 AND created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);

-- 删除对应的审计日志
DELETE FROM ai_operation_audit_log
WHERE event_id NOT IN (SELECT id FROM ai_event_processing_pipeline);
```

## 故障排查

### Q13: 管道没有自动启动,怎么办?
A: 检查清单:
1. ✓ `AbnormalDetectionController` 是否注入了 `IEventProcessingPipelineService`
2. ✓ `eventId` 是否正确生成
3. ✓ 异常检测是否返回 `isAbnormal = true`
4. ✓ 查看日志是否有异常

### Q14: 操作审计日志为空,怎么办?
A: 检查清单:
1. ✓ `ai_operation_audit_log` 表是否创建
2. ✓ `OperationAuditService` 是否正确注入
3. ✓ 数据库连接是否正常
4. ✓ 查看日志是否有SQL错误

### Q15: 处置建议不正确,怎么办?
A: 检查清单:
1. ✓ `ai_disposition_rule` 表中的规则是否正确
2. ✓ 异常类型是否匹配
3. ✓ 风险分数范围是否正确
4. ✓ 规则是否启用 (enabled = 1)

### Q16: 自动执行没有触发,怎么办?
A: 检查清单:
1. ✓ 风险分数是否 >= 80
2. ✓ 异常类型是否在自动执行列表中
3. ✓ `ai_disposition_rule.auto_execute` 是否为 1
4. ✓ 查看日志中的 `shouldAutoExecute()` 返回值

## 最佳实践

### Q17: 如何设置合理的优先级阈值?
A: 建议:
- **danger** (95): 需要立即处理的异常
- **warning** (70): 需要监测的异常
- **normal** (40): 轻微波动
- **阈值 30**: 低于30的异常只记录,不处理

根据实际情况调整。

### Q18: 如何设置合理的风险分数范围?
A: 建议:
- **80-100**: 高风险,立即通知医生,可自动执行
- **50-79**: 中等风险,建议监测,不自动执行
- **0-49**: 低风险,继续监测

根据医学标准调整。

### Q19: 如何优化系统性能?
A:
1. **缓存规则**: 缓存 `ai_disposition_rule` (5分钟过期)
2. **异步处理**: 使用消息队列异步处理低优先级异常
3. **并行调用**: 并行调用洞察和风险评估
4. **数据库优化**: 添加索引,定期清理历史数据
5. **监控告警**: 监控处理延迟,及时发现瓶颈

### Q20: 如何保证数据安全?
A:
1. **加密存储**: 敏感信息(患者ID、诊断结果)加密存储
2. **访问控制**: 限制操作审计日志的访问权限
3. **审计日志**: 记录所有数据访问
4. **备份恢复**: 定期备份数据库
5. **合规性**: 满足HIPAA、GDPR等医疗数据保护要求

## 扩展相关

### Q21: 如何添加新的处置动作?
A:
1. 在 `ai_disposition_rule.disposition_action` 中定义新的动作
2. 在处置执行模块中实现该动作
3. 例如: "发送短信通知", "启动应急响应", "调用外部系统"

### Q22: 如何集成第三方通知系统?
A: 在 `EventProcessingPipelineServiceImpl.executeDisposition()` 中:
```java
private void executeDisposition(Long eventId, String disposition) {
    // 调用第三方通知系统
    notificationService.send(eventId, disposition);
}
```

### Q23: 如何支持多语言?
A:
1. 在 `ai_disposition_rule` 中添加语言字段
2. 在处置建议生成时根据语言选择
3. 在操作审计日志中记录语言信息

## 其他

### Q24: 这个解决方案是否支持实时处理?
A: 是的。系统设计支持实时处理:
- 异常检测: 实时
- 事件洞察: 实时(1-2s)
- 风险评估: 实时(500ms)
- 处置建议: 实时(50ms)
- 自动执行: 实时

### Q25: 如何进行A/B测试?
A:
1. 在 `ai_disposition_rule` 中添加版本字段
2. 根据患者ID或时间段选择不同版本的规则
3. 在操作审计日志中记录使用的规则版本
4. 对比不同版本的准确率
