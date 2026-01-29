"""
健康风险预测器
基于 scikit-learn 逻辑回归模型实现健康风险等级预测
"""
import os
from typing import List, Tuple, Optional
from dataclasses import dataclass
from enum import Enum
import numpy as np

# 尝试导入 joblib 用于模型加载
try:
    import joblib
    JOBLIB_AVAILABLE = True
except ImportError:
    JOBLIB_AVAILABLE = False

# 尝试导入 sklearn
try:
    from sklearn.linear_model import LogisticRegression
    from sklearn.preprocessing import StandardScaler
    SKLEARN_AVAILABLE = True
except ImportError:
    SKLEARN_AVAILABLE = False


class RiskLevel(str, Enum):
    """健康风险等级"""
    LOW = "low"
    MEDIUM = "medium"
    HIGH = "high"
    CRITICAL = "critical"


@dataclass
class PredictionResult:
    """预测结果"""
    risk_level: RiskLevel
    risk_score: float
    risk_factors: List[str]
    confidence: float


class HealthRiskPredictor:
    """
    健康风险预测器
    
    使用逻辑回归模型预测健康风险等级
    支持从文件加载预训练模型，或使用基于规则的默认预测
    
    Features:
        - 心率均值 (heart_rate_mean)
        - 心率标准差 (heart_rate_std)
        - 收缩压均值 (systolic_mean)
        - 舒张压均值 (diastolic_mean)
        - 步数总计 (total_steps)
        - 心率异常次数 (anomaly_count)
    """
    
    # 模型文件默认路径
    DEFAULT_MODEL_PATH = os.path.join(
        os.path.dirname(os.path.dirname(__file__)), 
        "ml_models", 
        "health_risk_model.joblib"
    )
    
    # 风险等级阈值
    RISK_THRESHOLDS = {
        RiskLevel.LOW: 0.25,
        RiskLevel.MEDIUM: 0.50,
        RiskLevel.HIGH: 0.75,
        RiskLevel.CRITICAL: 1.0,
    }
    
    def __init__(self, model_path: Optional[str] = None):
        """
        初始化健康风险预测器
        
        Args:
            model_path: 预训练模型文件路径 (可选)
                       如果提供且文件存在，将加载该模型
                       否则使用基于规则的默认预测
        """
        self.model: Optional[LogisticRegression] = None
        self.scaler: Optional[StandardScaler] = None
        self.model_loaded = False
        
        # 尝试加载模型
        load_path = model_path or self.DEFAULT_MODEL_PATH
        self._try_load_model(load_path)
    
    def _try_load_model(self, model_path: str) -> bool:
        """
        尝试加载预训练模型
        
        Args:
            model_path: 模型文件路径
            
        Returns:
            是否成功加载模型
        """
        if not JOBLIB_AVAILABLE:
            return False
        
        if not os.path.exists(model_path):
            return False
        
        try:
            model_data = joblib.load(model_path)
            self.model = model_data.get("model")
            self.scaler = model_data.get("scaler")
            self.model_loaded = True
            return True
        except Exception:
            return False
    
    def _extract_features(
        self,
        heart_rates: List[int],
        blood_pressures: List[str],
        steps: List[int],
        anomaly_count: int
    ) -> np.ndarray:
        """
        提取特征向量
        
        Args:
            heart_rates: 心率值列表
            blood_pressures: 血压字符串列表
            steps: 步数列表
            anomaly_count: 心率异常次数
            
        Returns:
            特征向量 (1, 6)
        """
        # 心率特征
        hr_array = np.array(heart_rates) if heart_rates else np.array([70])
        hr_mean = float(np.mean(hr_array))
        hr_std = float(np.std(hr_array)) if len(hr_array) > 1 else 0.0
        
        # 解析血压
        systolic_values = []
        diastolic_values = []
        for bp in blood_pressures:
            try:
                parts = bp.split("/")
                systolic_values.append(int(parts[0]))
                diastolic_values.append(int(parts[1]))
            except (ValueError, IndexError):
                systolic_values.append(120)
                diastolic_values.append(80)
        
        systolic_mean = float(np.mean(systolic_values)) if systolic_values else 120.0
        diastolic_mean = float(np.mean(diastolic_values)) if diastolic_values else 80.0
        
        # 步数特征
        total_steps = sum(steps) if steps else 0
        
        # 构建特征向量
        features = np.array([
            [hr_mean, hr_std, systolic_mean, diastolic_mean, total_steps, anomaly_count]
        ])
        
        return features
    
    def _rule_based_prediction(
        self,
        features: np.ndarray,
        anomaly_count: int
    ) -> PredictionResult:
        """
        基于规则的风险预测 (当模型未加载时使用)
        
        Args:
            features: 特征向量
            anomaly_count: 心率异常次数
            
        Returns:
            预测结果
        """
        hr_mean, hr_std, systolic, diastolic, total_steps, _ = features[0]
        
        risk_score = 0.0
        risk_factors = []
        
        # 心率评估 (正常范围: 60-100 bpm)
        if hr_mean < 50 or hr_mean > 110:
            risk_score += 0.25
            if hr_mean < 50:
                risk_factors.append(f"心率过缓 ({hr_mean:.0f} bpm)")
            else:
                risk_factors.append(f"心率过速 ({hr_mean:.0f} bpm)")
        elif hr_mean < 55 or hr_mean > 100:
            risk_score += 0.10
            risk_factors.append(f"心率轻度异常 ({hr_mean:.0f} bpm)")
        
        # 心率变异性评估
        if hr_std > 20:
            risk_score += 0.15
            risk_factors.append(f"心率波动较大 (标准差: {hr_std:.1f})")
        
        # 血压评估 (正常: 收缩压 90-140, 舒张压 60-90)
        if systolic > 160 or diastolic > 100:
            risk_score += 0.30
            risk_factors.append(f"血压过高 ({systolic:.0f}/{diastolic:.0f} mmHg)")
        elif systolic > 140 or diastolic > 90:
            risk_score += 0.15
            risk_factors.append(f"血压偏高 ({systolic:.0f}/{diastolic:.0f} mmHg)")
        elif systolic < 90 or diastolic < 60:
            risk_score += 0.20
            risk_factors.append(f"血压偏低 ({systolic:.0f}/{diastolic:.0f} mmHg)")
        
        # 心率异常评估
        if anomaly_count > 5:
            risk_score += 0.25
            risk_factors.append(f"心率异常频繁 (共 {anomaly_count} 次)")
        elif anomaly_count > 2:
            risk_score += 0.10
            risk_factors.append(f"存在心率异常 (共 {anomaly_count} 次)")
        
        # 活动量评估 (步数过低可能表示活动不足)
        if total_steps < 100 and len(risk_factors) > 0:
            risk_score += 0.05
            risk_factors.append("活动量偏低")
        
        # 限制风险分数在 0-1 范围
        risk_score = min(1.0, max(0.0, risk_score))
        
        # 确定风险等级
        if risk_score < self.RISK_THRESHOLDS[RiskLevel.LOW]:
            risk_level = RiskLevel.LOW
        elif risk_score < self.RISK_THRESHOLDS[RiskLevel.MEDIUM]:
            risk_level = RiskLevel.MEDIUM
        elif risk_score < self.RISK_THRESHOLDS[RiskLevel.HIGH]:
            risk_level = RiskLevel.HIGH
        else:
            risk_level = RiskLevel.CRITICAL
        
        return PredictionResult(
            risk_level=risk_level,
            risk_score=round(risk_score, 4),
            risk_factors=risk_factors if risk_factors else ["各项指标正常"],
            confidence=0.85  # 规则方法的置信度
        )
    
    def _model_based_prediction(
        self,
        features: np.ndarray
    ) -> PredictionResult:
        """
        基于模型的风险预测
        
        Args:
            features: 特征向量
            
        Returns:
            预测结果
        """
        # 特征标准化
        if self.scaler:
            features_scaled = self.scaler.transform(features)
        else:
            features_scaled = features
        
        # 模型预测
        prediction = self.model.predict(features_scaled)[0]
        probabilities = self.model.predict_proba(features_scaled)[0]
        
        # 映射预测结果到风险等级
        risk_levels = [RiskLevel.LOW, RiskLevel.MEDIUM, RiskLevel.HIGH, RiskLevel.CRITICAL]
        risk_level = risk_levels[int(prediction)] if int(prediction) < len(risk_levels) else RiskLevel.MEDIUM
        
        # 计算风险分数 (加权概率)
        weights = [0.1, 0.3, 0.6, 0.9]
        risk_score = sum(p * w for p, w in zip(probabilities, weights[:len(probabilities)]))
        
        return PredictionResult(
            risk_level=risk_level,
            risk_score=round(risk_score, 4),
            risk_factors=["基于 ML 模型预测"],
            confidence=round(float(max(probabilities)), 4)
        )
    
    def predict(
        self,
        heart_rates: List[int],
        blood_pressures: List[str],
        steps: List[int],
        anomaly_count: int = 0
    ) -> PredictionResult:
        """
        预测健康风险等级
        
        Args:
            heart_rates: 心率值列表
            blood_pressures: 血压字符串列表
            steps: 步数列表
            anomaly_count: 心率异常次数
            
        Returns:
            预测结果
        """
        # 提取特征
        features = self._extract_features(
            heart_rates, blood_pressures, steps, anomaly_count
        )
        
        # 使用模型或规则进行预测
        if self.model_loaded and self.model is not None:
            return self._model_based_prediction(features)
        else:
            return self._rule_based_prediction(features, anomaly_count)
    
    def is_model_loaded(self) -> bool:
        """检查是否已加载预训练模型"""
        return self.model_loaded
