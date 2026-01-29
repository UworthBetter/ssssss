"""
Pydantic 数据校验模型
用于 /algo/v1/health-check 接口的入参和出参校验
"""
from pydantic import BaseModel, Field, field_validator
from typing import List, Optional
from enum import Enum


class RiskLevel(str, Enum):
    """健康风险等级枚举"""
    LOW = "low"
    MEDIUM = "medium"
    HIGH = "high"
    CRITICAL = "critical"


class VitalSignData(BaseModel):
    """
    单条体征数据模型
    
    Attributes:
        heart_rate: 心率值 (30-220 bpm)
        blood_pressure: 血压字符串 (格式: 收缩压/舒张压)
        steps: 步数 (非负整数)
        timestamp: Unix 时间戳 (毫秒)
    """
    heart_rate: int = Field(
        ..., 
        ge=30, 
        le=220, 
        description="心率 (bpm)，有效范围 30-220"
    )
    blood_pressure: str = Field(
        ..., 
        pattern=r"^\d{2,3}/\d{2,3}$", 
        description="血压，格式: 收缩压/舒张压，如 120/80"
    )
    steps: int = Field(
        ..., 
        ge=0, 
        description="步数，非负整数"
    )
    timestamp: int = Field(
        ..., 
        gt=0, 
        description="Unix 时间戳 (毫秒)"
    )

    @field_validator("blood_pressure")
    @classmethod
    def validate_blood_pressure(cls, v: str) -> str:
        """验证血压值的合理性"""
        parts = v.split("/")
        systolic = int(parts[0])
        diastolic = int(parts[1])
        
        if not (60 <= systolic <= 250):
            raise ValueError(f"收缩压 {systolic} 超出有效范围 (60-250 mmHg)")
        if not (40 <= diastolic <= 150):
            raise ValueError(f"舒张压 {diastolic} 超出有效范围 (40-150 mmHg)")
        if systolic <= diastolic:
            raise ValueError("收缩压必须大于舒张压")
        
        return v

    class Config:
        json_schema_extra = {
            "example": {
                "heart_rate": 75,
                "blood_pressure": "120/80",
                "steps": 1500,
                "timestamp": 1706500000000
            }
        }


class HealthCheckRequest(BaseModel):
    """健康检查请求体"""
    data: List[VitalSignData] = Field(
        ..., 
        min_length=1,
        max_length=1000,
        description="体征数据列表，至少包含1条记录，最多1000条"
    )

    class Config:
        json_schema_extra = {
            "example": {
                "data": [
                    {
                        "heart_rate": 72,
                        "blood_pressure": "118/76",
                        "steps": 500,
                        "timestamp": 1706500000000
                    },
                    {
                        "heart_rate": 75,
                        "blood_pressure": "120/80",
                        "steps": 800,
                        "timestamp": 1706500060000
                    }
                ]
            }
        }


class HeartRateAnomaly(BaseModel):
    """心率异常记录"""
    timestamp: int = Field(..., description="异常发生时的时间戳")
    heart_rate: int = Field(..., description="异常心率值")
    window_mean: float = Field(..., description="滑动窗口均值")
    deviation_percent: float = Field(..., description="偏离百分比")
    message: str = Field(..., description="预警信息")


class HealthCheckResponse(BaseModel):
    """
    健康检查响应体
    
    包含心率异常检测结果和健康风险等级预测
    """
    code: int = Field(default=200, description="响应状态码")
    message: str = Field(default="success", description="响应消息")
    
    # 心率异常检测结果
    anomalies: List[HeartRateAnomaly] = Field(
        default_factory=list,
        description="检测到的心率异常列表"
    )
    anomaly_count: int = Field(default=0, description="异常数量")
    
    # 健康风险预测结果
    risk_level: RiskLevel = Field(
        default=RiskLevel.LOW,
        description="健康风险等级"
    )
    risk_score: float = Field(
        default=0.0,
        ge=0.0,
        le=1.0,
        description="风险评分 (0.0-1.0)"
    )
    risk_factors: List[str] = Field(
        default_factory=list,
        description="风险因素列表"
    )
    
    # 统计信息
    data_points_analyzed: int = Field(
        default=0,
        description="分析的数据点数量"
    )

    class Config:
        json_schema_extra = {
            "example": {
                "code": 200,
                "message": "success",
                "anomalies": [
                    {
                        "timestamp": 1706500600000,
                        "heart_rate": 135,
                        "window_mean": 75.5,
                        "deviation_percent": 78.8,
                        "message": "心率异常: 当前值 135 偏离均值 75.5 达 78.8%"
                    }
                ],
                "anomaly_count": 1,
                "risk_level": "medium",
                "risk_score": 0.45,
                "risk_factors": ["心率波动较大"],
                "data_points_analyzed": 15
            }
        }
