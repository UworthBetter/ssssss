# Python异常检测模块 v2.1 - 完整实现指南

## 模块结构

```
python_modules/
├── anomaly_detection/
│   ├── __init__.py
│   ├── detector.py              # 异常检测器
│   ├── trend_analyzer.py        # 趋势分析器
│   ├── baseline_comparator.py   # 基线对比器
│   ├── confidence_calculator.py # 置信度计算器
│   └── models.py                # 数据模型
├── config/
│   ├── thresholds.yaml          # 阈值配置
│   └── algorithms.yaml          # 算法配置
└── tests/
    ├── test_detector.py
    ├── test_trend_analyzer.py
    └── test_integration.py
```

## 核心实现

### 1. 数据模型 (models.py)

```python
from dataclasses import dataclass, asdict
from typing import List, Dict, Optional
from enum import Enum

class AnomalySeverity(Enum):
    """异常严重程度"""
    CRITICAL = "CRITICAL"
    HIGH = "HIGH"
    MEDIUM = "MEDIUM"
    LOW = "LOW"

class TrendType(Enum):
    """趋势类型"""
    STABLE = "stable"
    IMPROVING = "improving"
    DETERIORATING = "deteriorating"
    CRITICAL = "critical"

@dataclass
class DetectedAnomaly:
    """检测到的异常"""
    type: str                      # 异常类型
    severity: str                  # 严重程度
    confidence: float              # 置信度 (0-1)
    description: str               # 描述
    recommendation: str            # 建议
    value: Optional[float] = None  # 异常值
    threshold: Optional[float] = None  # 阈值

@dataclass
class TrendAnalysis:
    """趋势分析结果"""
    trend: str                     # 趋势类型
    change_rate: float             # 变化率
    forecast: str                  # 预测
    data_points: int               # 数据点数

@dataclass
class BaselineComparison:
    """基线对比结果"""
    deviation: float               # 偏离标准差数
    percentile: int                # 百分位
    status: str                    # 状态 (normal/abnormal)
    baseline_value: float          # 基线值

@dataclass
class RiskAssessmentResult:
    """风险评估结果"""
    risk_score: int                # 风险分数 (0-100)
    risk_level: str                # 风险等级
    detected_anomalies: List[DetectedAnomaly]  # 检测到的异常
    trend_analysis: TrendAnalysis  # 趋势分析
    baseline_comparison: BaselineComparison  # 基线对比
    model_confidence: float        # 模型置信度 (0-1)
    algorithm_version: str         # 算法版本
    processing_time_ms: int        # 处理时间

    def to_dict(self):
        """转换为字典"""
        return {
            'risk_score': self.risk_score,
            'risk_level': self.risk_level,
            'detected_anomalies': [asdict(a) for a in self.detected_anomalies],
            'trend_analysis': asdict(self.trend_analysis),
            'baseline_comparison': asdict(self.baseline_comparison),
            'model_confidence': self.model_confidence,
            'algorithm_version': self.algorithm_version,
            'processing_time_ms': self.processing_time_ms
        }
```

### 2. 异常检测器 (detector.py)

```python
import numpy as np
from typing import List, Dict
from .models import DetectedAnomaly, AnomalySeverity

class AnomalyDetector:
    """异常检测器"""

    def __init__(self, config: Dict):
        self.config = config
        self.thresholds = config.get('thresholds', {})

    def detect(self, patient_data: Dict) -> List[DetectedAnomaly]:
        """检测异常"""
        anomalies = []

        # 心率异常检测
        heart_rate = patient_data.get('heart_rate')
        if heart_rate:
            anomalies.extend(self._detect_heart_rate_anomalies(heart_rate))

        # 血氧异常检测
        oxygen_sat = patient_data.get('oxygen_saturation')
        if oxygen_sat:
            anomalies.extend(self._detect_oxygen_anomalies(oxygen_sat))

        # 体温异常检测
        temperature = patient_data.get('temperature')
        if temperature:
            anomalies.extend(self._detect_temperature_anomalies(temperature))

        # 血压异常检测
        systolic = patient_data.get('systolic_bp')
        diastolic = patient_data.get('diastolic_bp')
        if systolic and diastolic:
            anomalies.extend(self._detect_bp_anomalies(systolic, diastolic))

        return anomalies

    def _detect_heart_rate_anomalies(self, heart_rate: float) -> List[DetectedAnomaly]:
        """检测心率异常"""
        anomalies = []
        hr_config = self.thresholds.get('heart_rate', {})

        # 心率过高
        if heart_rate > hr_config.get('high_threshold', 100):
            anomalies.append(DetectedAnomaly(
                type='heart_rate_spike',
                severity=AnomalySeverity.HIGH.value if heart_rate > 120 else AnomalySeverity.MEDIUM.value,
                confidence=min(0.95, (heart_rate - 100) / 50),
                description=f'心率过高: {heart_rate} bpm',
                recommendation='建议立即就医',
                value=heart_rate,
                threshold=hr_config.get('high_threshold', 100)
            ))

        # 心率过低
        elif heart_rate < hr_config.get('low_threshold', 50):
            anomalies.append(DetectedAnomaly(
                type='heart_rate_drop',
                severity=AnomalySeverity.HIGH.value if heart_rate < 40 else AnomalySeverity.MEDIUM.value,
                confidence=min(0.95, (50 - heart_rate) / 30),
                description=f'心率过低: {heart_rate} bpm',
                recommendation='建议立即就医',
                value=heart_rate,
                threshold=hr_config.get('low_threshold', 50)
            ))

        return anomalies

    def _detect_oxygen_anomalies(self, oxygen_sat: float) -> List[DetectedAnomaly]:
        """检测血氧异常"""
        anomalies = []
        o2_config = self.thresholds.get('oxygen_saturation', {})

        if oxygen_sat < o2_config.get('low_threshold', 95):
            anomalies.append(DetectedAnomaly(
                type='low_oxygen_saturation',
                severity=AnomalySeverity.CRITICAL.value if oxygen_sat < 90 else AnomalySeverity.HIGH.value,
                confidence=min(0.98, (95 - oxygen_sat) / 10),
                description=f'血氧饱和度过低: {oxygen_sat}%',
                recommendation='建议立即就医',
                value=oxygen_sat,
                threshold=o2_config.get('low_threshold', 95)
            ))

        return anomalies

    def _detect_temperature_anomalies(self, temperature: float) -> List[DetectedAnomaly]:
        """检测体温异常"""
        anomalies = []
        temp_config = self.thresholds.get('temperature', {})

        if temperature > temp_config.get('high_threshold', 38.5):
            anomalies.append(DetectedAnomaly(
                type='high_temperature',
                severity=AnomalySeverity.HIGH.value if temperature > 39 else AnomalySeverity.MEDIUM.value,
                confidence=min(0.92, (temperature - 37) / 3),
                description=f'体温过高: {temperature}°C',
                recommendation='建议就医检查',
                value=temperature,
                threshold=temp_config.get('high_threshold', 38.5)
            ))

        elif temperature < temp_config.get('low_threshold', 36):
            anomalies.append(DetectedAnomaly(
                type='low_temperature',
                severity=AnomalySeverity.MEDIUM.value,
                confidence=min(0.90, (37 - temperature) / 2),
                description=f'体温过低: {temperature}°C',
                recommendation='建议保暖并监测',
                value=temperature,
                threshold=temp_config.get('low_threshold', 36)
            ))

        return anomalies

    def _detect_bp_anomalies(self, systolic: float, diastolic: float) -> List[DetectedAnomaly]:
        """检测血压异常"""
        anomalies = []
        bp_config = self.thresholds.get('blood_pressure', {})

        if systolic > bp_config.get('high_threshold', 140):
            anomalies.append(DetectedAnomaly(
                type='high_blood_pressure',
                severity=AnomalySeverity.HIGH.value if systolic > 160 else AnomalySeverity.MEDIUM.value,
                confidence=min(0.93, (systolic - 120) / 60),
                description=f'血压过高: {systolic}/{diastolic} mmHg',
                recommendation='建议就医检查',
                value=systolic,
                threshold=bp_config.get('high_threshold', 140)
            ))

        elif systolic < bp_config.get('low_threshold', 90):
            anomalies.append(DetectedAnomaly(
                type='low_blood_pressure',
                severity=AnomalySeverity.HIGH.value if systolic < 80 else AnomalySeverity.MEDIUM.value,
                confidence=min(0.91, (90 - systolic) / 30),
                description=f'血压过低: {systolic}/{diastolic} mmHg',
                recommendation='建议立即就医',
                value=systolic,
                threshold=bp_config.get('low_threshold', 90)
            ))

        return anomalies
```

### 3. 趋势分析器 (trend_analyzer.py)

```python
import numpy as np
from typing import List, Dict
from .models import TrendAnalysis, TrendType

class TrendAnalyzer:
    """趋势分析器"""

    def analyze(self, historical_data: List[float], current_value: float) -> TrendAnalysis:
        """分析趋势"""
        if len(historical_data) < 2:
            return TrendAnalysis(
                trend=TrendType.STABLE.value,
                change_rate=0.0,
                forecast='数据不足',
                data_points=len(historical_data)
            )

        # 计算变化率
        change_rate = self._calculate_change_rate(historical_data, current_value)

        # 判断趋势
        trend = self._determine_trend(historical_data, current_value, change_rate)

        # 生成预测
        forecast = self._generate_forecast(trend, change_rate)

        return TrendAnalysis(
            trend=trend,
            change_rate=change_rate,
            forecast=forecast,
            data_points=len(historical_data) + 1
        )

    def _calculate_change_rate(self, historical_data: List[float], current_value: float) -> float:
        """计算变化率"""
        if len(historical_data) == 0:
            return 0.0

        previous_value = historical_data[-1]
        if previous_value == 0:
            return 0.0

        return (current_value - previous_value) / abs(previous_value)

    def _determine_trend(self, historical_data: List[float], current_value: float, change_rate: float) -> str:
        """判断趋势"""
        # 计算平均值
        avg = np.mean(historical_data)

        # 计算标准差
        std = np.std(historical_data) if len(historical_data) > 1 else 0

        # 判断趋势
        if abs(change_rate) > 0.2:  # 变化率大于20%
            if change_rate > 0:
                return TrendType.DETERIORATING.value if current_value > avg + 2 * std else TrendType.IMPROVING.value
            else:
                return TrendType.IMPROVING.value if current_value < avg - 2 * std else TrendType.DETERIORATING.value
        else:
            return TrendType.STABLE.value

    def _generate_forecast(self, trend: str, change_rate: float) -> str:
        """生成预测"""
        if trend == TrendType.DETERIORATING.value:
            return '可能进一步恶化,建议密切监测'
        elif trend == TrendType.IMPROVING.value:
            return '状态可能改善,继续观察'
        else:
            return '状态稳定,继续监测'
```

### 4. 基线对比器 (baseline_comparator.py)

```python
import numpy as np
from typing import Dict, List
from .models import BaselineComparison

class BaselineComparator:
    """基线对比器"""

    def __init__(self, baseline_data: Dict[str, Dict]):
        """
        baseline_data格式:
        {
            'heart_rate': {'mean': 70, 'std': 10, 'percentiles': {...}},
            'oxygen_saturation': {'mean': 98, 'std': 1, 'percentiles': {...}},
            ...
        }
        """
        self.baseline_data = baseline_data

    def compare(self, metric_name: str, current_value: float) -> BaselineComparison:
        """对比基线"""
        if metric_name not in self.baseline_data:
            return BaselineComparison(
                deviation=0.0,
                percentile=50,
                status='unknown',
                baseline_value=0.0
            )

        baseline = self.baseline_data[metric_name]
        mean = baseline.get('mean', 0)
        std = baseline.get('std', 1)

        # 计算偏离标准差数
        if std == 0:
            deviation = 0.0
        else:
            deviation = abs(current_value - mean) / std

        # 计算百分位
        percentile = self._calculate_percentile(current_value, baseline)

        # 判断状态
        status = 'abnormal' if deviation > 2.0 else 'normal'

        return BaselineComparison(
            deviation=round(deviation, 2),
            percentile=percentile,
            status=status,
            baseline_value=mean
        )

    def _calculate_percentile(self, current_value: float, baseline: Dict) -> int:
        """计算百分位"""
        percentiles = baseline.get('percentiles', {})
        if not percentiles:
            return 50

        # 简单的百分位计算
        for p in sorted(percentiles.keys()):
            if current_value <= percentiles[p]:
                return int(p)

        return 100
```

### 5. 置信度计算器 (confidence_calculator.py)

```python
from typing import List
from .models import DetectedAnomaly

class ConfidenceCalculator:
    """置信度计算器"""

    def calculate(self, anomalies: List[DetectedAnomaly]) -> float:
        """计算模型置信度"""
        if not anomalies:
            return 0.95  # 没有异常时置信度高

        # 计算平均置信度
        avg_confidence = sum(a.confidence for a in anomalies) / len(anomalies)

        # 根据异常数量调整
        if len(anomalies) == 1:
            return min(0.99, avg_confidence * 1.05)
        elif len(anomalies) == 2:
            return min(0.98, avg_confidence * 1.02)
        else:
            return min(0.95, avg_confidence * 0.98)
```

### 6. 主入口 (detector.py - 完整版)

```python
import time
from typing import Dict, List
from .models import (
    RiskAssessmentResult, DetectedAnomaly, TrendAnalysis,
    BaselineComparison
)
from .anomaly_detector import AnomalyDetector
from .trend_analyzer import TrendAnalyzer
from .baseline_comparator import BaselineComparator
from .confidence_calculator import ConfidenceCalculator

class RiskAssessmentEngine:
    """风险评估引擎"""

    def __init__(self, config: Dict, baseline_data: Dict):
        self.config = config
        self.anomaly_detector = AnomalyDetector(config)
        self.trend_analyzer = TrendAnalyzer()
        self.baseline_comparator = BaselineComparator(baseline_data)
        self.confidence_calculator = ConfidenceCalculator()

    def assess_risk(self, patient_data: Dict, historical_data: Dict = None) -> RiskAssessmentResult:
        """评估风险"""
        start_time = time.time()

        # 1. 异常检测
        anomalies = self.anomaly_detector.detect(patient_data)

        # 2. 趋势分析
        heart_rate_history = historical_data.get('heart_rate_history', []) if historical_data else []
        trend = self.trend_analyzer.analyze(
            heart_rate_history,
            patient_data.get('heart_rate', 0)
        )

        # 3. 基线对比
        baseline = self.baseline_comparator.compare(
            'heart_rate',
            patient_data.get('heart_rate', 0)
        )

        # 4. 计算置信度
        confidence = self.confidence_calculator.calculate(anomalies)

        # 5. 计算风险分数
        risk_score = self._calculate_risk_score(anomalies, trend, baseline)

        # 6. 判断风险等级
        risk_level = self._determine_risk_level(risk_score)

        # 7. 计算处理时间
        processing_time = int((time.time() - start_time) * 1000)

        return RiskAssessmentResult(
            risk_score=risk_score,
            risk_level=risk_level,
            detected_anomalies=anomalies,
            trend_analysis=trend,
            baseline_comparison=baseline,
            model_confidence=confidence,
            algorithm_version='v2.1',
            processing_time_ms=processing_time
        )

    def _calculate_risk_score(self, anomalies: List[DetectedAnomaly], trend: TrendAnalysis, baseline: BaselineComparison) -> int:
        """计算风险分数"""
        score = 0

        # 基于异常严重程度
        for anomaly in anomalies:
            if anomaly.severity == 'CRITICAL':
                score += 40
            elif anomaly.severity == 'HIGH':
                score += 25
            elif anomaly.severity == 'MEDIUM':
                score += 15
            else:
                score += 5

        # 基于趋势
        if trend.trend == 'deteriorating':
            score += 15
        elif trend.trend == 'critical':
            score += 25

        # 基于基线对比
        if baseline.deviation > 3.0:
            score += 20
        elif baseline.deviation > 2.0:
            score += 10

        return min(100, score)

    def _determine_risk_level(self, risk_score: int) -> str:
        """判断风险等级"""
        if risk_score >= 80:
            return 'DANGER'
        elif risk_score >= 60:
            return 'WARNING'
        elif risk_score >= 40:
            return 'CAUTION'
        else:
            return 'NORMAL'
```

## 配置文件示例

### thresholds.yaml

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

## 使用示例

```python
from anomaly_detection import RiskAssessmentEngine
import yaml

# 加载配置
with open('config/thresholds.yaml') as f:
    config = yaml.safe_load(f)

# 基线数据
baseline_data = {
    'heart_rate': {'mean': 70, 'std': 10, 'percentiles': {'50': 70, '95': 90}},
    'oxygen_saturation': {'mean': 98, 'std': 1, 'percentiles': {'50': 98, '95': 99}},
}

# 创建引擎
engine = RiskAssessmentEngine(config, baseline_data)

# 患者数据
patient_data = {
    'heart_rate': 120,
    'oxygen_saturation': 95,
    'temperature': 37.5,
    'systolic_bp': 140,
    'diastolic_bp': 90
}

# 历史数据
historical_data = {
    'heart_rate_history': [70, 75, 80, 90, 110]
}

# 评估风险
result = engine.assess_risk(patient_data, historical_data)

# 转换为字典
result_dict = result.to_dict()
print(result_dict)
```

## 集成到Java

### 调用Python模块

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class PythonAnomalyDetectionService {

    @Autowired
    private ObjectMapper objectMapper;

    public RiskAssessmentResult assessRisk(Map<String, Object> patientData) throws Exception {
        // 调用Python脚本
        ProcessBuilder pb = new ProcessBuilder(
            "python", "/path/to/python_modules/main.py",
            objectMapper.writeValueAsString(patientData)
        );

        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }

        // 解析结果
        return objectMapper.readValue(output.toString(), RiskAssessmentResult.class);
    }
}
```

## 性能优化建议

1. **缓存基线数据** - 避免重复加载
2. **异步处理** - 使用线程池处理多个患者
3. **批量处理** - 支持批量风险评估
4. **模型优化** - 使用更高效的算法
5. **数据预处理** - 提前清理和验证数据

## 测试覆盖

- 单元测试: 各个组件的独立测试
- 集成测试: 完整流程的测试
- 性能测试: 处理时间和准确率
- 边界测试: 极端值的处理
