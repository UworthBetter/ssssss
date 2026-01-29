"""
心率异常检测算法
基于滑动窗口的心率异常检测实现
"""
from typing import List, Tuple
from dataclasses import dataclass
import numpy as np


@dataclass
class AnomalyResult:
    """异常检测结果"""
    timestamp: int
    heart_rate: int
    window_mean: float
    deviation_percent: float
    message: str


class HeartRateDetector:
    """
    滑动窗口心率异常检测器
    
    算法说明:
    - 使用滑动窗口计算过去 N 个数据点的心率均值
    - 当当前心率偏离均值超过阈值时，触发异常预警
    
    Attributes:
        window_size: 滑动窗口大小，默认 10 个数据点
        deviation_threshold: 偏离阈值，默认 0.30 (30%)
    """
    
    def __init__(
        self, 
        window_size: int = 10, 
        deviation_threshold: float = 0.30
    ):
        """
        初始化心率异常检测器
        
        Args:
            window_size: 滑动窗口大小 (默认10)
            deviation_threshold: 偏离阈值百分比 (默认0.30 = 30%)
        """
        self.window_size = window_size
        self.deviation_threshold = deviation_threshold
    
    def detect(
        self, 
        heart_rates: List[int], 
        timestamps: List[int]
    ) -> List[AnomalyResult]:
        """
        检测心率异常
        
        Args:
            heart_rates: 心率值列表
            timestamps: 对应的时间戳列表
            
        Returns:
            异常检测结果列表
        """
        if len(heart_rates) != len(timestamps):
            raise ValueError("心率列表和时间戳列表长度必须一致")
        
        anomalies: List[AnomalyResult] = []
        
        # 需要至少 window_size + 1 个数据点才能开始检测
        if len(heart_rates) <= self.window_size:
            return anomalies
        
        for i in range(self.window_size, len(heart_rates)):
            # 获取滑动窗口内的心率数据
            window_data = heart_rates[i - self.window_size:i]
            
            # 计算窗口均值
            window_mean = float(np.mean(window_data))
            
            # 获取当前心率
            current_hr = heart_rates[i]
            
            # 计算偏离百分比
            if window_mean > 0:
                deviation = abs(current_hr - window_mean) / window_mean
            else:
                deviation = 0.0
            
            # 判断是否异常
            if deviation >= self.deviation_threshold:
                deviation_percent = round(deviation * 100, 2)
                
                # 判断偏离方向
                if current_hr > window_mean:
                    direction = "偏高"
                else:
                    direction = "偏低"
                
                anomaly = AnomalyResult(
                    timestamp=timestamps[i],
                    heart_rate=current_hr,
                    window_mean=round(window_mean, 2),
                    deviation_percent=deviation_percent,
                    message=f"心率异常: 当前值 {current_hr} {direction}，偏离均值 {round(window_mean, 2)} 达 {deviation_percent}%"
                )
                anomalies.append(anomaly)
        
        return anomalies
    
    def get_statistics(
        self, 
        heart_rates: List[int]
    ) -> dict:
        """
        获取心率统计信息
        
        Args:
            heart_rates: 心率值列表
            
        Returns:
            包含统计信息的字典
        """
        if not heart_rates:
            return {
                "count": 0,
                "mean": 0.0,
                "std": 0.0,
                "min": 0,
                "max": 0,
            }
        
        hr_array = np.array(heart_rates)
        
        return {
            "count": len(heart_rates),
            "mean": round(float(np.mean(hr_array)), 2),
            "std": round(float(np.std(hr_array)), 2),
            "min": int(np.min(hr_array)),
            "max": int(np.max(hr_array)),
        }
