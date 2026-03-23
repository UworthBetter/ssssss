# Python风险评估模块 - 重新设计

## 问题分析

原始设计的问题:
- Python模块只返回一个风险分数(0-100)
- 浪费了算法检测异常的能力
- 显得算法设计用处小

## 新设计方案

### Python模块返回完整的异常检测结果

```python
{
  "risk_score": 85,                    # 风险分数(0-100)
  "risk_level": "DANGER",             # 风险等级
  "detected_anomalies": [             # 检测到的异常
    {
      "type": "heart_rate_spike",     # 异常类型
      "severity": "high",              # 严重程度
      "confidence": 0.95,              # 置信度
      "description": "心率突然升高20%",
      "recommendation": "建议立即就医"
    },
    {
      "type": "irregular_rhythm",
      "severity": "medium",
      "confidence": 0.87,
      "description": "检测到心律不齐",
      "recommendation": "建议监测并复查"
    }
  ],
  "trend_analysis": {                 # 趋势分析
    "trend": "deteriorating",         # 恶化中
    "change_rate": 0.15,              # 变化率
    "forecast": "可能进一步恶化"
  },
  "comparison_with_baseline": {       # 与基线对比
    "deviation": 2.5,                 # 偏离基线2.5个标准差
    "percentile": 98,                 # 在同龄人中排名98%
    "status": "abnormal"              # 异常
  },
  "model_confidence": 0.92,           # 模型整体置信度
  "algorithm_version": "v2.1",       # 算法版本
  "processing_time_ms": 145           # 处理时间
}
```

## 核心改进

### 1. 多维度异常检测
```
单一分数 (原始)
  ↓
完整的异常检测结果 (新设计)
  ├── 异常类型识别
  ├── 严重程度评估
  ├── 置信度计算
  ├── 趋势分析
  ├── 基线对比
  └── 个性化建议
```

### 2. 异常类型识别
```
心率异常:
  - heart_rate_spike (心率突增)
  - heart_rate_drop (心率下降)
  - irregular_rhythm (心律不齐)
  - tachycardia (心动过速)
  - bradycardia (心动过缓)

血氧异常:
  - oxygen_desaturation (血氧饱和度下降)
  - oxygen_fluctuation (血氧波动)

体温异常:
  - fever (发热)
  - hypothermia (体温过低)
  - temperature_spike (体温突升)

综合异常:
  - multiple_anomalies (多项异常)
  - critical_combination (危险组合)
```

### 3. 严重程度分级
```
CRITICAL (危急)  - 需要立即干预
HIGH (高)        - 需要紧急处理
MEDIUM (中)      - 需要监测
LOW (低)         - 需要关注
```

### 4. 置信度计算
```
基于:
- 数据质量
- 算法训练数据覆盖度
- 特征匹配度
- 历史准确率
```

### 5. 趋势分析
```
trend:
  - improving (改善中)
  - stable (稳定)
  - deteriorating (恶化中)
  - critical (危急)

change_rate: 变化速度
forecast: 未来预测
```

### 6. 基线对比
```
与患者个人基线对比:
  - 偏离程度(标准差)
  - 历史百分位
  - 同龄人对比

用于:
  - 个性化异常判断
  - 排除正常变异
  - 早期预警
```

## 数据库表更新

### ai_event_processing_pipeline 表

新增字段:
```sql
-- 异常检测详情
detected_anomalies JSON,           -- 检测到的异常列表
anomaly_types VARCHAR(500),        -- 异常类型(逗号分隔)
anomaly_count INT,                 -- 异常数量

-- 趋势分析
trend_analysis JSON,               -- 趋势分析结果
trend_status VARCHAR(50),          -- 趋势状态

-- 基线对比
baseline_comparison JSON,          -- 基线对比结果
deviation_sigma DECIMAL(5,2),      -- 偏离标准差

-- 模型信息
model_confidence DECIMAL(5,2),     -- 模型置信度
algorithm_version VARCHAR(50),     -- 算法版本
processing_time_ms INT             -- 处理时间
```

### ai_operation_audit_log 表

新增字段:
```sql
-- 异常检测详情
detected_anomalies JSON,           -- 检测到的异常
anomaly_types VARCHAR(500),        -- 异常类型

-- 模型信息
model_confidence DECIMAL(5,2),     -- 模型置信度
algorithm_version VARCHAR(50)      -- 算法版本
```

## Java代码更新

### RiskScoreService 接口

```java
public interface IRiskScoreService {
    /**
     * 调用Python模型进行风险评估
     * @param abnormalData 异常数据
     * @param eventInsight 事件洞察
     * @return 完整的风险评估结果
     */
    RiskAssessmentResult assessRisk(AbnormalData abnormalData, EventInsight eventInsight);
}
```

### RiskAssessmentResult 类

```java
public class RiskAssessmentResult {
    private Integer riskScore;                    // 风险分数(0-100)
    private String riskLevel;                     // 风险等级
    private List<DetectedAnomaly> detectedAnomalies;  // 检测到的异常
    private TrendAnalysis trendAnalysis;          // 趋势分析
    private BaselineComparison baselineComparison; // 基线对比
    private Double modelConfidence;               // 模型置信度
    private String algorithmVersion;              // 算法版本
    private Long processingTimeMs;                // 处理时间
}

public class DetectedAnomaly {
    private String type;                          // 异常类型
    private String severity;                      // 严重程度
    private Double confidence;                    // 置信度
    private String description;                   // 描述
    private String recommendation;                // 建议
}

public class TrendAnalysis {
    private String trend;                         // 趋势
    private Double changeRate;                    // 变化率
    private String forecast;                      // 预测
}

public class BaselineComparison {
    private Double deviation;                     // 偏离基线(标准差)
    private Integer percentile;                   // 百分位
    private String status;                        // 状态
}
```

## EventProcessingPipelineService 更新

```java
private void recordRiskAssessment(Long eventId, RiskAssessmentResult result) {
    String sql = "UPDATE ai_event_processing_pipeline SET " +
        "risk_score = ?, " +
        "risk_level = ?, " +
        "detected_anomalies = ?, " +
        "anomaly_types = ?, " +
        "anomaly_count = ?, " +
        "trend_analysis = ?, " +
        "trend_status = ?, " +
        "baseline_comparison = ?, " +
        "deviation_sigma = ?, " +
        "model_confidence = ?, " +
        "algorithm_version = ?, " +
        "processing_time_ms = ? " +
        "WHERE id = ?";

    jdbcTemplate.update(sql,
        result.getRiskScore(),
        result.getRiskLevel(),
        objectMapper.writeValueAsString(result.getDetectedAnomalies()),
        String.join(",", result.getDetectedAnomalies().stream()
            .map(DetectedAnomaly::getType).collect(Collectors.toList())),
        result.getDetectedAnomalies().size(),
        objectMapper.writeValueAsString(result.getTrendAnalysis()),
        result.getTrendAnalysis().getTrend(),
        objectMapper.writeValueAsString(result.getBaselineComparison()),
        result.getBaselineComparison().getDeviation(),
        result.getModelConfidence(),
        result.getAlgorithmVersion(),
        result.getProcessingTimeMs(),
        eventId
    );
}
```

## 处置规则引擎更新

现在可以基于更多维度的信息生成处置建议:

```java
public DispositionSuggestion generateDisposition(
    String abnormalType,
    RiskAssessmentResult riskResult) {

    // 基于异常类型
    List<DispositionRule> rules = ruleRepository.findByAbnormalType(abnormalType);

    // 基于风险分数
    DispositionRule rule = rules.stream()
        .filter(r -> r.getRiskLevelMin() <= riskResult.getRiskScore() &&
                     r.getRiskLevelMax() >= riskResult.getRiskScore())
        .findFirst()
        .orElse(null);

    if (rule == null) return null;

    DispositionSuggestion suggestion = new DispositionSuggestion();
    suggestion.setAction(rule.getDispositionAction());
    suggestion.setNotificationLevel(rule.getNotificationLevel());
    suggestion.setAutoExecute(rule.isAutoExecute());

    // 新增: 基于异常详情优化建议
    if (riskResult.getDetectedAnomalies().size() > 1) {
        suggestion.setRemark("检测到多项异常,建议综合评估");
    }

    // 新增: 基于趋势优化建议
    if ("deteriorating".equals(riskResult.getTrendAnalysis().getTrend())) {
        suggestion.setRemark("患者状态恶化中,建议加强监测");
    }

    // 新增: 基于置信度优化建议
    if (riskResult.getModelConfidence() < 0.7) {
        suggestion.setRemark("模型置信度较低,建议人工复核");
    }

    return suggestion;
}
```

## API 响应示例

### 查看处理进度

```json
GET /ai/event-processing/status/123

{
  "eventId": 123,
  "stage": "COMPLETED",
  "riskScore": 85,
  "riskLevel": "DANGER",
  "detectedAnomalies": [
    {
      "type": "heart_rate_spike",
      "severity": "high",
      "confidence": 0.95,
      "description": "心率突然升高20%",
      "recommendation": "建议立即就医"
    },
    {
      "type": "irregular_rhythm",
      "severity": "medium",
      "confidence": 0.87,
      "description": "检测到心律不齐",
      "recommendation": "建议监测并复查"
    }
  ],
  "trendAnalysis": {
    "trend": "deteriorating",
    "changeRate": 0.15,
    "forecast": "可能进一步恶化"
  },
  "baselineComparison": {
    "deviation": 2.5,
    "percentile": 98,
    "status": "abnormal"
  },
  "modelConfidence": 0.92,
  "algorithmVersion": "v2.1",
  "processingTimeMs": 145,
  "dispositionSuggestion": "立即通知医生,建议患者就医",
  "autoExecute": true
}
```

### 查看操作审计

```json
GET /ai/event-processing/audit-trail/123

[
  {
    "operationType": "DETECT",
    "abnormalType": "heart_rate_spike",
    "riskScore": null,
    "createdAt": "2026-03-23 10:00:00"
  },
  {
    "operationType": "INSIGHT_BUILD",
    "abnormalType": "heart_rate_spike",
    "riskScore": null,
    "createdAt": "2026-03-23 10:00:05"
  },
  {
    "operationType": "RISK_ASSESS",
    "abnormalType": "heart_rate_spike",
    "riskScore": 85,
    "detectedAnomalies": [
      {"type": "heart_rate_spike", "severity": "high", "confidence": 0.95},
      {"type": "irregular_rhythm", "severity": "medium", "confidence": 0.87}
    ],
    "modelConfidence": 0.92,
    "algorithmVersion": "v2.1",
    "createdAt": "2026-03-23 10:00:10"
  },
  {
    "operationType": "DISPOSITION",
    "abnormalType": "heart_rate_spike",
    "riskScore": 85,
    "createdAt": "2026-03-23 10:00:12"
  },
  {
    "operationType": "EXECUTE",
    "abnormalType": "heart_rate_spike",
    "riskScore": 85,
    "createdAt": "2026-03-23 10:00:15"
  }
]
```

## 优势总结

### 1. 充分展示算法价值
- ✅ 不仅返回分数,还返回完整的异常检测结果
- ✅ 展示算法的多维度分析能力
- ✅ 体现算法的专业性和可信度

### 2. 更好的决策支持
- ✅ 医生可以看到具体检测到的异常
- ✅ 可以基于异常类型和严重程度做出更好的决策
- ✅ 可以基于趋势和基线对比做出预防性决策

### 3. 更强的可解释性
- ✅ 每个异常都有描述和建议
- ✅ 置信度显示算法的确定性
- ✅ 处理时间显示算法的效率

### 4. 更好的反馈闭环
- ✅ 可以记录每个异常的实际结果
- ✅ 可以计算每个异常类型的准确率
- ✅ 可以针对性地优化算法

### 5. 更灵活的规则配置
- ✅ 可以基于异常类型配置不同的规则
- ✅ 可以基于异常数量配置不同的规则
- ✅ 可以基于趋势配置不同的规则

## 实施步骤

1. 更新Python模块,返回完整的异常检测结果
2. 更新数据库表,添加新字段
3. 更新Java代码,处理新的数据结构
4. 更新API响应,展示完整的异常信息
5. 更新处置规则引擎,基于异常详情优化建议
6. 更新审计日志,记录异常详情
7. 测试验证
