"""
算法服务配置文件
集中管理所有配置项，避免硬编码
"""
from dataclasses import dataclass
from typing import Dict, Any
import os
from dotenv import load_dotenv

load_dotenv(override=True)


@dataclass
class FallDetectionConfig:
    """跌倒检测配置"""
    # 加速度阈值
    acceleration_threshold: float = 20.0
    # 重力加速度
    gravity: float = 9.8
    # 置信度阈值
    confidence_low: float = 0.6
    confidence_high: float = 0.9


@dataclass
class RiskAssessmentConfig:
    """风险评估配置"""
    # 心率正常范围
    heart_rate_min: int = 60
    heart_rate_max: int = 100
    heart_rate_optimal: int = 75

    # 风险因子权重
    heart_rate_weight: float = 1.5
    movement_weight: float = 10.0
    sleep_low_weight: int = 20
    sleep_medium_weight: int = 10

    # 风险等级阈值
    risk_threshold_high: float = 60.0
    risk_threshold_medium: float = 30.0

    # 年龄因素
    age_senior_threshold: int = 70
    age_bonus_rate: float = 0.5


@dataclass
class LLMConfig:
    """LLM 客户端配置"""
    api_key: str = os.getenv("LLM_API_KEY", "")
    base_url: str = os.getenv("LLM_BASE_URL", "https://api.openai.com/v1")
    model: str = os.getenv("LLM_MODEL", "gpt-3.5-turbo")
    timeout: float = float(os.getenv("LLM_TIMEOUT", "30.0"))
    max_retries: int = int(os.getenv("LLM_MAX_RETRIES", "3"))
    cache_enabled: bool = os.getenv("LLM_CACHE_ENABLED", "true").lower() == "true"
    cache_size: int = int(os.getenv("LLM_CACHE_SIZE", "1000"))


@dataclass
class ServerConfig:
    """服务器配置"""
    host: str = os.getenv("HOST", "0.0.0.0")
    port: int = int(os.getenv("PORT", "8000"))
    log_level: str = os.getenv("LOG_LEVEL", "info")
    reload: bool = os.getenv("RELOAD", "false").lower() == "true"
    workers: int = int(os.getenv("WORKERS", "1"))


@dataclass
class DataQualityConfig:
    """数据质量检查配置"""
    # 质量等级阈值
    excellent_threshold: float = 95.0
    good_threshold: float = 80.0
    fair_threshold: float = 60.0


class Config:
    """主配置类"""

    def __init__(self):
        self.fall_detection = FallDetectionConfig()
        self.risk_assessment = RiskAssessmentConfig()
        self.llm = LLMConfig()
        self.server = ServerConfig()
        self.data_quality = DataQualityConfig()

    def validate(self) -> Dict[str, Any]:
        """验证配置有效性"""
        issues = []

        # 验证 LLM 配置
        if not self.llm.api_key:
            issues.append("LLM_API_KEY not set")

        # 验证服务器配置
        if not (1 <= self.server.port <= 65535):
            issues.append(f"Invalid port: {self.server.port}")

        return {
            "valid": len(issues) == 0,
            "issues": issues
        }


# 全局配置实例
config = Config()
