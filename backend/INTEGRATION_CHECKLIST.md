# 集成检查清单

## 第一阶段: 数据库准备

### 创建表结构
- [ ] 执行 `backend/sql/event_processing_pipeline.sql`
  ```bash
  mysql -u root -p your_database < backend/sql/event_processing_pipeline.sql
  ```
- [ ] 执行 `backend/sql/operation_audit_log.sql`
  ```bash
  mysql -u root -p your_database < backend/sql/operation_audit_log.sql
  ```
- [ ] 验证表创建成功
  ```sql
  SHOW TABLES LIKE 'ai_%';
  ```

### 初始化规则数据
- [ ] 插入处置规则
  ```sql
  INSERT INTO ai_disposition_rule VALUES
  (NULL, '心率异常', 80, 100, 1, '立即通知医生,建议患者就医', 'URGENT', 1, NOW()),
  (NULL, '心率异常', 50, 79, 0, '建议患者休息,监测心率变化', 'HIGH', 1, NOW()),
  (NULL, '血氧偏低', 80, 100, 1, '立即通知医生,建议患者吸氧', 'URGENT', 1, NOW()),
  (NULL, '体温偏高', 80, 100, 1, '立即通知医生,建议患者降温', 'URGENT', 1, NOW());
  ```
- [ ] 验证规则插入成功
  ```sql
  SELECT COUNT(*) FROM ai_disposition_rule;
  ```

## 第二阶段: 代码集成

### 复制服务层代码
- [ ] 复制 `IEventProcessingPipelineService.java` 到 `src/main/java/com/qkyd/ai/service/`
- [ ] 复制 `EventProcessingPipelineServiceImpl.java` 到 `src/main/java/com/qkyd/ai/service/impl/`
- [ ] 复制 `IDispositionRuleEngine.java` 到 `src/main/java/com/qkyd/ai/service/`
- [ ] 复制 `DispositionRuleEngineImpl.java` 到 `src/main/java/com/qkyd/ai/service/impl/`
- [ ] 复制 `IOperationAuditService.java` 到 `src/main/java/com/qkyd/ai/service/`
- [ ] 复制 `OperationAuditServiceImpl.java` 到 `src/main/java/com/qkyd/ai/service/impl/`

### 复制控制层代码
- [ ] 复制 `EventProcessingController.java` 到 `src/main/java/com/qkyd/ai/controller/`
- [ ] 更新 `AbnormalDetectionController.java`
  - [ ] 注入 `IEventProcessingPipelineService`
  - [ ] 在异常检测后调用 `pipelineService.startPipeline(eventId, abnormalData)`

### 验证编译
- [ ] 运行 `mvn clean compile`
- [ ] 确保没有编译错误
- [ ] 检查依赖是否完整

## 第三阶段: 配置验证

### Spring Bean 注册
- [ ] 确认 `EventProcessingPipelineServiceImpl` 被 `@Service` 注解
- [ ] 确认 `DispositionRuleEngineImpl` 被 `@Service` 注解
- [ ] 确认 `OperationAuditServiceImpl` 被 `@Service` 注解
- [ ] 确认 `EventProcessingController` 被 `@RestController` 注解

### 依赖注入
- [ ] 确认 `JdbcTemplate` 可用
- [ ] 确认 `IEventInsightService` 已实现
- [ ] 确认 `IRiskScoreService` 已实现
- [ ] 运行 `mvn dependency:tree` 检查依赖

## 第四阶段: 功能测试

### 单元测试
- [ ] 运行 `EventProcessingIntegrationTest`
  ```bash
  mvn test -Dtest=EventProcessingIntegrationTest
  ```
- [ ] 所有测试通过

### 集成测试
- [ ] 启动应用
  ```bash
  mvn spring-boot:run
  ```
- [ ] 测试异常检测接口
  ```bash
  curl -X POST http://localhost:8080/ai/abnormal/detect \
    -H "Content-Type: application/json" \
    -d '{
      "patientId": 1,
      "patientName": "张三",
      "metricType": "heart_rate",
      "abnormalValue": "150",
      "riskLevel": "danger"
    }'
  ```
- [ ] 记录返回的 `eventId`

### 流程验证
- [ ] 查看处理进度
  ```bash
  curl http://localhost:8080/ai/event-processing/status/{eventId}
  ```
- [ ] 验证返回的 `stage` 为 "COMPLETED"
- [ ] 验证 `riskScore` 已计算
- [ ] 验证 `disposition` 已生成

### 审计日志验证
- [ ] 查看操作链路
  ```bash
  curl http://localhost:8080/ai/event-processing/audit-trail/{eventId}
  ```
- [ ] 验证包含以下操作:
  - [ ] DETECT
  - [ ] INSIGHT_BUILD
  - [ ] RISK_ASSESS
  - [ ] DISPOSITION
  - [ ] EXECUTE (如果自动执行)

### 数据库验证
- [ ] 查询 `ai_event_processing_pipeline` 表
  ```sql
  SELECT * FROM ai_event_processing_pipeline WHERE id = {eventId};
  ```
- [ ] 验证所有字段已填充
- [ ] 查询 `ai_operation_audit_log` 表
  ```sql
  SELECT * FROM ai_operation_audit_log WHERE event_id = {eventId};
  ```
- [ ] 验证至少有5条操作记录

## 第五阶段: 性能测试

### 延迟测试
- [ ] 测试单个异常处理的延迟
  - [ ] 目标: < 3秒
  - [ ] 记录实际延迟
- [ ] 测试并发处理
  - [ ] 10个并发请求
  - [ ] 100个并发请求
  - [ ] 记录吞吐量和延迟

### 资源使用
- [ ] 监控内存使用
- [ ] 监控数据库连接数
- [ ] 监控CPU使用率

## 第六阶段: 故障处理

### 异常场景测试
- [ ] 测试风险评估服务不可用
  - [ ] 停止Python服务
  - [ ] 验证系统使用默认风险分数继续处理
  - [ ] 验证错误被记录到审计日志

- [ ] 测试LLM服务超时
  - [ ] 模拟LLM超时
  - [ ] 验证系统继续处理
  - [ ] 验证使用快速洞察

- [ ] 测试数据库连接失败
  - [ ] 停止数据库
  - [ ] 验证系统返回错误
  - [ ] 恢复数据库后验证系统恢复

### 错误日志验证
- [ ] 查看应用日志
  ```bash
  tail -f logs/application.log
  ```
- [ ] 验证错误被正确记录
- [ ] 验证错误不影响其他请求

## 第七阶段: 文档和培训

### 文档检查
- [ ] 阅读 `ARCHITECTURE_GUIDE.md`
- [ ] 阅读 `QUICK_START.md`
- [ ] 阅读 `FLOW_DIAGRAM.md`
- [ ] 阅读 `FAQ.md`
- [ ] 更新项目README

### 团队培训
- [ ] 讲解系统架构
- [ ] 演示完整流程
- [ ] 讲解如何添加新规则
- [ ] 讲解如何排查问题

## 第八阶段: 灰度发布

### 灰度配置
- [ ] 配置灰度规则(10%流量)
- [ ] 部署到灰度环境
- [ ] 监控灰度指标

### 灰度监控(24小时)
- [ ] 监控处理延迟
- [ ] 监控错误率
- [ ] 监控自动执行率
- [ ] 收集用户反馈

### 全量发布
- [ ] 灰度指标正常
- [ ] 用户反馈良好
- [ ] 部署到生产环境
- [ ] 监控生产指标

## 第九阶段: 上线后监控

### 关键指标
- [ ] 处理延迟: 目标 < 3秒
- [ ] 错误率: 目标 < 0.1%
- [ ] 自动执行率: 目标 10-20%
- [ ] 准确率: 目标 > 90%

### 告警规则
- [ ] 处理延迟 > 5秒
- [ ] 错误率 > 1%
- [ ] 自动执行失败
- [ ] 数据库连接异常

### 定期检查
- [ ] 每天检查错误日志
- [ ] 每周检查性能指标
- [ ] 每月检查准确率
- [ ] 每月清理历史数据

## 检查清单总结

| 阶段 | 项目数 | 完成数 | 状态 |
|------|--------|--------|------|
| 数据库准备 | 6 | _ | ☐ |
| 代码集成 | 8 | _ | ☐ |
| 配置验证 | 8 | _ | ☐ |
| 功能测试 | 12 | _ | ☐ |
| 性能测试 | 5 | _ | ☐ |
| 故障处理 | 6 | _ | ☐ |
| 文档培训 | 6 | _ | ☐ |
| 灰度发布 | 7 | _ | ☐ |
| 上线监控 | 8 | _ | ☐ |
| **总计** | **66** | **_** | **☐** |

## 关键联系人

- **架构师**: 负责系统设计和集成
- **DBA**: 负责数据库创建和优化
- **测试**: 负责功能和性能测试
- **运维**: 负责部署和监控
- **产品**: 负责需求和反馈
