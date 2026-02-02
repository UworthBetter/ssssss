# qkyd-ai 模块

[根目录](../CLAUDE.md) > **qkyd-ai**

---

## 变更记录 (Changelog)

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| 2026-02-01 14:18:39 | v3.8.7 | 初始化模块文档 |

---

## 模块职责

qkyd-ai 是 AI 算法服务集成模块，负责：

1. **异常检测**：基于统计分析的健康数据异常检测
2. **跌倒检测**：加速度传感器数据分析和跌倒识别
3. **趋势分析**：健康指标的时间序列趋势分析
4. **风险评分**：综合健康数据的风险评估
5. **数据质量评估**：检测数据的完整性和准确性
6. **Python 算法服务集成**：与外部 Python 算法服务通信

---

## 入口与启动

### 配置类
```java
// 文件: src/main/java/com/qkyd/ai/config/AiConfig.java
@Configuration
public class AiConfig {
    // Spring AI OpenAI 配置
}

// 文件: src/main/java/com/qkyd/ai/config/PythonIntegrationConfig.java
@Configuration
public class PythonIntegrationConfig {
    // Python 算法服务配置
    private String pythonServiceUrl;
    private int connectTimeout;
    private int readTimeout;
}
```

### 应用配置
```yaml
# application.yml
ueit:
  ai:
    service-url: http://localhost:8001  # Python 算法服务地址
    connect-timeout: 5000
    read-timeout: 30000
    async-enabled: true

spring:
  ai:
    openai:
      api-key: ${AI_API_KEY}
      base-url: ${AI_BASE_URL:https://api.openai.com}
      chat:
        options:
          model: ${AI_MODEL:gpt-3.5-turbo}
          temperature: ${AI_TEMPERATURE:0.7}
```

---

## 对外接口

### REST API

| 路径 | 控制器 | 功能 |
|------|--------|------|
| `/ai/abnormal/detect` | `AbnormalDetectionController` | 异常检测 |
| `/ai/fall/detect` | `FallDetectionController` | 跌倒检测 |
| `/ai/trend/analyze` | `TrendAnalysisController` | 趋势分析 |
| `/ai/risk/score` | `RiskScoreController` | 风险评分 |
| `/ai/dataQuality/evaluate` | `DataQualityController` | 数据质量评估 |
| `/ai/dashboard` | `AlgorithmDashboardController` | 算法仪表板 |

---

## 关键依赖与配置

### Maven 依赖
```xml
<!-- Spring AI OpenAI Starter -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>

<!-- 依赖的健康数据模块 -->
<dependency>
    <groupId>com.qkyd</groupId>
    <artifactId>qkyd-health</artifactId>
</dependency>
```

---

## 数据模型

### 实体类 (entity)
| 类名 | 表名 | 功能 |
|------|------|------|
| `AbnormalRecord` | `qkyd_abnormal_record` | 异常检测记录 |
| `FallAlarmRecord` | `qkyd_fall_alarm_record` | 跌倒告警记录 |
| `TrendRecord` | `qkyd_trend_record` | 趋势分析记录 |
| `RiskScoreRecord` | `qkyd_risk_score_record` | 风险评分记录 |
| `DataQualityRecord` | `qkyd_data_quality_record` | 数据质量记录 |

### DTO (请求/响应)
| 类名 | 功能 |
|------|------|
| `AbnormalDetectionResultDTO` | 异常检测结果 |
| `FallDetectionResultDTO` | 跌倒检测结果 |
| `TrendAnalysisResultDTO` | 趋势分析结果 |
| `RiskScoreResultDTO` | 风险评分结果 |
| `DataQualityResultDTO` | 数据质量评估结果 |

### VO (视图对象)
| 类名 | 功能 |
|------|------|
| `AbnormalDetectionVO` | 异常检测视图 |
| `FallDetectionVO` | 跌倒检测视图 |
| `TrendAnalysisVO` | 趋势分析视图 |
| `RiskScoreVO` | 风险评分视图 |
| `DataQualityVO` | 数据质量视图 |

---

## 核心服务

### 异常检测服务
```java
// 文件: src/main/java/com/qkyd/ai/service/impl/AbnormalDetectionServiceImpl.java
@Service
public class AbnormalDetectionServiceImpl implements IAbnormalDetectionService {
    // 调用 Python 算法服务进行异常检测
    // 保存异常记录到数据库
}
```

### 跌倒检测服务
```java
// 文件: src/main/java/com/qkyd/ai/service/impl/FallDetectionServiceImpl.java
@Service
public class FallDetectionServiceImpl implements IFallDetectionService {
    // 分析加速度数据
    // 判断是否跌倒并生成告警
}
```

### 趋势分析服务
```java
// 文件: src/main/java/com/qkyd/ai/service/impl/TrendAnalysisServiceImpl.java
@Service
public class TrendAnalysisServiceImpl implements ITrendAnalysisService {
    // 时间序列分析
    // 趋势方向判断（上升/下降/稳定）
}
```

### 风险评分服务
```java
// 文件: src/main/java/com/qkyd/ai/service/impl/RiskScoreServiceImpl.java
@Service
public class RiskScoreServiceImpl implements IRiskScoreService {
    // 综合多指标计算健康风险分
}
```

### 数据质量服务
```java
// 文件: src/main/java/com/qkyd/ai/service/impl/DataQualityServiceImpl.java
@Service
public class DataQualityServiceImpl implements IDataQualityService {
    // 评估数据完整性、准确性、一致性
}
```

---

## 枚举与常量

### 枚举类
| 枚举类 | 功能 |
|--------|------|
| `MetricTypeEnum` | 指标类型（心率、血氧、体温等） |
| `AbnormalTypeEnum` | 异常类型（偏高、偏低、突变等） |
| `RiskLevelEnum` | 风险等级（低、中、高） |
| `TrendDirectionEnum` | 趋势方向（上升、下降、稳定） |
| `DetectionMethodEnum` | 检测方法（统计、机器学习、规则） |
| `DataQualityLevelEnum` | 数据质量等级（优、良、差） |

### 常量类
| 常量类 | 功能 |
|--------|------|
| `HealthThresholdConstant` | 健康指标阈值常量 |
| `AlgorithmConfigConstant` | 算法配置常量 |

---

## Python 算法服务集成

### 通信方式
- **协议**: HTTP REST
- **数据格式**: JSON
- **超时配置**: 连接 5s，读取 30s

### API 端点
```java
// 异常检测
String url = pythonConfig.getPythonServiceUrl() + "/api/abnormal/detect";

// 跌倒检测
String url = pythonConfig.getPythonServiceUrl() + "/api/fall/detect";
```

---

## 测试与质量

### 单元测试
- 测试目录：`src/test/java/com/qkyd/ai/`
- 运行命令：`mvn test -Dtest=*ServiceTest`

### API 测试
- Swagger UI: http://localhost:8098/doc.html
- 测试端点：`/ai/*`

---

## 常见问题 (FAQ)

### Q1: 如何添加新的 AI 算法？
1. 创建新的 Controller 和 Service
2. 定义对应的 DTO/VO/Entity
3. 配置 Python 服务端点（如需要）
4. 添加路由和权限配置

### Q2: Python 服务不可用怎么办？
- 检查 `ueit.ai.service-url` 配置
- 查看 Python 服务日志
- 使用熔断降级机制（待实现）

### Q3: 如何调整异常检测阈值？
修改 `HealthThresholdConstant` 中的阈值常量，或通过数据库动态配置。

---

## 相关文件清单

```
qkyd-ai/
├── src/main/java/com/qkyd/ai/
│   ├── config/
│   │   ├── AiConfig.java                  # Spring AI 配置
│   │   └── PythonIntegrationConfig.java   # Python 服务配置
│   ├── controller/
│   │   ├── AbnormalDetectionController.java
│   │   ├── FallDetectionController.java
│   │   ├── TrendAnalysisController.java
│   │   ├── RiskScoreController.java
│   │   ├── DataQualityController.java
│   │   ├── AlgorithmDashboardController.java
│   │   └── AiController.java
│   ├── service/
│   │   ├── IAbnormalDetectionService.java
│   │   ├── IFallDetectionService.java
│   │   ├── ITrendAnalysisService.java
│   │   ├── IRiskScoreService.java
│   │   ├── IDataQualityService.java
│   │   └── impl/
│   │       ├── AbnormalDetectionServiceImpl.java
│   │       ├── FallDetectionServiceImpl.java
│   │       ├── TrendAnalysisServiceImpl.java
│   │       ├── RiskScoreServiceImpl.java
│   │       └── DataQualityServiceImpl.java
│   ├── mapper/
│   │   ├── AbnormalRecordMapper.java
│   │   ├── FallAlarmRecordMapper.java
│   │   ├── TrendRecordMapper.java
│   │   ├── RiskScoreRecordMapper.java
│   │   └── DataQualityRecordMapper.java
│   ├── model/
│   │   ├── entity/
│   │   ├── dto/
│   │   ├── vo/
│   │   ├── enums/
│   │   └── constant/
│   └── domain/FallDetectionRequest.java
└── pom.xml
```

---

**最后更新**: 2026-02-01 14:18:39
