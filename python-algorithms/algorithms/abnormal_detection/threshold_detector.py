"""
阈值异常检测器
继承 BaseAlgorithm 以保持接口一致性
"""
from typing import Dict, Any, Optional
from algorithms.base import BaseAlgorithm


class ThresholdDetector(BaseAlgorithm):
    """
    基于阈值的简单异常检测器

    检测值是否超出指定的最小/最大范围
    """

    def __init__(self, config: Optional[Dict[str, Any]] = None):
        """
        初始化检测器

        Args:
            config: 可选配置字典
        """
        self.config = config or {}

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
        执行阈值检测

        Args:
            data: 包含以下字段:
                - value: 待检测的值
                - min: 最小阈值（可选）
                - max: 最大阈值（可选）

        Returns:
            检测结果，包含:
                - is_abnormal: 是否异常
                - type: 异常类型 (too_high/too_low)
                - diff: 差值
                - msg: 描述信息
        """
        value = data.get('value')
        min_val = data.get('min')
        max_val = data.get('max')

        if value is None:
            return {
                "is_abnormal": False,
                "msg": "No value provided",
                "method": "threshold"
            }

        # 检查下限
        if min_val is not None and value < min_val:
            return {
                "is_abnormal": True,
                "type": "too_low",
                "diff": round(value - min_val, 4),
                "msg": f"Value {value} is lower than min {min_val}",
                "method": "threshold"
            }

        # 检查上限
        if max_val is not None and value > max_val:
            return {
                "is_abnormal": True,
                "type": "too_high",
                "diff": round(value - max_val, 4),
                "msg": f"Value {value} is higher than max {max_val}",
                "method": "threshold"
            }

        return {
            "is_abnormal": False,
            "msg": "Normal",
            "method": "threshold"
        }

    def validate_input(self, data: Dict[str, Any]) -> bool:
        """
        验证输入数据

        Args:
            data: 输入数据字典

        Returns:
            是否有效
        """
        return 'value' in data
