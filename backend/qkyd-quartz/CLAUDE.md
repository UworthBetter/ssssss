# qkyd-quartz 模块

[根目录](../CLAUDE.md) > **qkyd-quartz**

---

## 变更记录 (Changelog)

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| 2026-02-01 14:18:39 | v3.8.7 | 初始化模块文档 |

---

## 模块职责

qkyd-quartz 是定时任务调度模块，基于 Quartz 框架实现：

1. **任务管理**：创建、编辑、删除、暂停、恢复定时任务
2. **任务调度**：基于 Cron 表达式的灵活调度
3. **执行日志**：任务执行日志记录与查询
4. **并发控制**：任务并发执行控制
5. **失败重试**：任务失败处理策略

---

## 入口与启动

### 配置类
```java
// 文件: src/main/java/com/qkyd/quartz/config/ScheduleConfig.java
@Configuration
public class ScheduleConfig {
    // Quartz 调度器配置
}
```

---

## 对外接口

### REST API

| 路径 | 控制器 | 功能 |
|------|--------|------|
| `/monitor/job/list` | `SysJobController` | 任务列表 |
| `/monitor/job/{id}` | `SysJobController` | 任务详情 |
| `/monitor/job` | `SysJobController` | 新增任务 |
| `/monitor/job/edit` | `SysJobController` | 修改任务 |
| `/monitor/job/changeStatus` | `SysJobController` | 修改状态 |
| `/monitor/job/deptTree` | `SysJobController` | 部门树 |
| `/monitor/job/exec` | `SysJobController` | 执行任务 |
| `/monitor/job/remove` | `SysJobController` | 删除任务 |
| `/monitor/jobLog/{id}` | `SysJobLogController` | 任务日志 |
| `/monitor/jobLog/list` | `SysJobLogController` | 日志列表 |
| `/monitor/jobLog/remove` | `SysJobLogController` | 删除日志 |

---

## 数据模型

### 实体类
| 类名 | 表名 | 功能 |
|------|------|------|
| `SysJob` | `sys_job` | 定时任务 |
| `SysJobLog` | `sys_job_log` | 任务执行日志 |

### 任务字段
| 字段 | 类型 | 说明 |
|------|------|------|
| jobId | Long | 任务ID |
| jobName | String | 任务名称 |
| jobGroup | String | 任务分组 |
| invokeTarget | String | 调用目标 |
| cronExpression | String | Cron表达式 |
| misfirePolicy | String | 错误策略 |
| concurrent | String | 是否并发 |
| status | String | 状态 |

---

## 核心服务

### 任务服务
```java
// 文件: src/main/java/com/qkyd/quartz/service/ISysJobService.java
public interface ISysJobService {
    // 任务增删改查
    // 任务调度管理
    // Cron 表达式校验
}
```

### 任务日志服务
```java
// 文件: src/main/java/com/qkyd/quartz/service/ISysJobLogService.java
public interface ISysJobLogService {
    // 日志查询、删除
}
```

---

## 工具类 (util)

| 类名 | 功能 |
|------|------|
| `ScheduleUtils` | 调度工具类 |
| `CronUtils` | Cron 表达式工具 |
| `JobInvokeUtil` | 任务调用工具 |
| `AbstractQuartzJob` | 抽象 Quartz 任务 |
| `QuartzJobExecution` | Quartz 任务执行 |
| `QuartzDisallowConcurrentExecution` | 禁止并发注解 |

---

## 任务示例

### 示例任务
```java
// 文件: src/main/java/com/qkyd/quartz/task/RyTask.java
@Component("ryTask")
public class RyTask {
    public void ryMultipleParams(String s, Boolean b, Long l, Double d, Integer i) {
        // 多参数任务示例
    }

    public void ryParams(String params) {
        // 单参数任务示例
    }

    public void ryNoParams() {
        // 无参数任务示例
    }
}
```

---

## Cron 表达式

### 常用示例
| 表达式 | 说明 |
|--------|------|
| `0 0 2 * * ?` | 每天凌晨2点执行 |
| `0 0/5 * * * ?` | 每5分钟执行 |
| `0 0 12 * * ?` | 每天中午12点执行 |
| `0 15 10 ? * MON-FRI` | 周一到周五10:15执行 |
| `0 0 10,14,16 * * ?` | 每天10点、14点、16点执行 |
| `0 0/30 9-17 * * ?` | 朝九晚五每半小时执行 |
| `0 0 12 ? * Wed` | 每周三中午12点执行 |

---

## 错误策略

| 策略 | 说明 |
|------|------|
| 立即执行 | 立即执行所有错过的执行 |
| 执行一次 | 执行一次错过的执行 |
| 放弃执行 | 放弃所有错过的执行 |

---

## 并发控制

| 禁止并发 | 允许并发 |
|----------|----------|
| 同一任务前次未完成时，下次不执行 | 同一任务前次未完成时，下次也执行 |

使用注解 `@QuartzDisallowConcurrentExecution` 标记禁止并发的任务。

---

## 相关文件清单

```
qkyd-quartz/
├── src/main/java/com/qkyd/quartz/
│   ├── config/
│   │   └── ScheduleConfig.java
│   ├── controller/
│   │   ├── SysJobController.java
│   │   └── SysJobLogController.java
│   ├── domain/
│   │   ├── SysJob.java
│   │   └── SysJobLog.java
│   ├── mapper/
│   │   ├── SysJobMapper.java
│   │   └── SysJobLogMapper.java
│   ├── service/
│   │   ├── ISysJobService.java
│   │   ├── ISysJobLogService.java
│   │   ├── impl/
│   │   │   ├── SysJobServiceImpl.java
│   │   │   └── SysJobLogServiceImpl.java
│   ├── task/
│   │   └── RyTask.java
│   └── util/
│       ├── ScheduleUtils.java
│       ├── CronUtils.java
│       ├── JobInvokeUtil.java
│       ├── AbstractQuartzJob.java
│       ├── QuartzJobExecution.java
│       └── QuartzDisallowConcurrentExecution.java
├── src/main/resources/mapper/quartz/
│   ├── SysJobMapper.xml
│   └── SysJobLogMapper.xml
└── pom.xml
```

---

**最后更新**: 2026-02-01 14:18:39
