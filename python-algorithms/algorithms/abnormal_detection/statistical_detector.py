"""
统计异常检测器
继承 BaseAlgorithm 以保持接口一致性
"""
from typing import Dict, Any, Optional
import numpy as np
from algorithms.base import BaseAlgorithm


class StatisticalDetector(BaseAlgorithm):
    """
    基于 Z-Score 的统计异常检测器

    使用历史数据计算均值和标准差，
    检测新值是否偏离统计分布
    """

    def __init__(self, config: Optional[Dict[str, Any]] = None):
        """
        初始化检测器

        Args:
            config: 可选配置字典，可包含:
                - default_threshold: 默认 Z-Score 阈值（默认 3.0）
                - min_history_size: 最小历史数据量（默认 2）
        """
        self.config = config or {}
        self.default_threshold = self.config.get('default_threshold', 3.0)
        self.min_history_size = self.config.get('min_history_size', 2)

    def run(self, data: Dict[str, Any]) -> Dict[str, Any]:
        """
        标准入口点（满足 BaseAlgorithm 接口）

        Args:
            data: 输入数据字典

        Returns:
            检测结果字典
        """
        return self.detect(data)

    def detect(self, data: Dict[str, Any]) -> Dict[str, Any]:
        """
        执行 Z-Score 异常检测

        Args:
            data: 包含以下字段:
                - value: 待检测的值
                - history: 历史数据列表
                - threshold: Z-Score 阈值（可选，默认 3.0）

        Returns:
            检测结果，包含:
                - is_abnormal: 是否异常
                - z_score: 计算的 Z-Score 值
                - mean: 历史数据均值
                - std: 历史数据标准差
                - msg: 描述信息
                - method: 检测方法标识
        """
        value = data.get('value')
        history = data.get('history', [])

        # 数据验证
        if value is None:
            return {
                "is_abnormal": False,
                "msg": "No value provided",
                "method": "statistical"
            }

        if not history or len(history) < self.min_history_size:
            return {
                "is_abnormal": False,
                "msg": f"Insufficient history data (need at least {self.min_history_size} points)",
                "method": "statistical"
            }

        # 计算统计量
        arr = np.array(history, dtype=float)
        mean = float(np.mean(arr))
        std = float(np.std(arr))

        # 零方差检查
        if std == 0:
            return {
                "is_abnormal": False,
                "msg": "Zero variance in history data",
                "mean": mean,
                "std": std,
                "method": "statistical"
            }

        # 计算 Z-Score
        z_score = (value - mean) / std
        threshold = data.get('threshold', self.default_threshold)
        is_abnormal = bool(abs(z_score) > threshold)

        return {
            "is_abnormal": is_abnormal,
            "z_score": round(float(z_score), 4),
            "mean": round(mean, 4),
            "std": round(std, 4),
            "threshold": threshold,
            "msg": (
                f"Z-Score {z_score:.2f} exceeds threshold {threshold}"
                if is_abnormal
                else f"Normal (Z-Score: {z_score:.2f})"
            ),
            "method": "statistical"
        }

    def validate_input(self, data: Dict[str, Any]) -> bool:
        """
        验证输入数据

        Args:
            data: 输入数据字典

        Returns:
            是否有效
        """
        return 'value' in data and 'history' in data
