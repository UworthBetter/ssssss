"""
SafetyGuardian - 智能跌倒检测系统（异步版本）
结合传统阈值检测与 LLM 语义分析
"""
import json
from typing import Dict, Any, Optional, List
from algorithms.base import BaseAlgorithm
from utils.async_llm_client import AsyncLLMClient, LLMError


class SafetyGuardian(BaseAlgorithm):
    """
    SafetyGuardian (安卫): 智能跌倒检测系统

    两阶段检测:
    1. 传统阈值检测（快速、低延迟）
    2. LLM 上下文验证（高准确率、降低误报）
    """
    def __init__(self, llm_client: Optional[AsyncLLMClient] = None):
        """
        初始化

        Args:
            llm_client: 异步 LLM 客户端（可选，默认创建新实例）
        """
        self.llm = llm_client or AsyncLLMClient()

    async def run(self, data: Dict[str, Any]) -> Dict[str, Any]:
        """
        标准入口点（异步）
        """
        return await self.detect_fall(data)

    async def detect_fall(
        self,
        sensor_data: Dict[str, Any],
        use_llm: bool = True
    ) -> Dict[str, Any]:
        """
        处理传感器数据检测跌倒

        Args:
            sensor_data: 传感器数据字典，包含:
                - acc_x, acc_y, acc_z: 三轴加速度
                - heart_rate: 心率（可选）
                - location: 位置
                - timestamp: 时间戳（可选）
                - activity_status: 活动状态（可选）
                - age: 年龄
                - medical_history: 病史（可选）
            use_llm: 是否使用 LLM 增强

        Returns:
            检测结果字典，包含:
                - is_fall: 是否跌倒
                - confidence: 置信度
                - method: 检测方法
                - magnitude: 加速度幅值
                - enhanced_features: LLM 增强分析（如果使用 LLM）
                - original_result: 传统算法结果（如果使用 LLM）
        """
        # 阶段1: 传统阈值检测
        traditional_result = self._threshold_detection(sensor_data)

        # 如果传统方法未检测到跌倒，直接返回
        if not traditional_result['is_fall']:
            return traditional_result

        # 阶段2: LLM 增强验证（减少误报）
        if use_llm:
            return await self._llm_enhancement(sensor_data, traditional_result)

        return traditional_result

    def _threshold_detection(self, data: Dict[str, Any]) -> Dict[str, Any]:
        """
        传统加速度阈值检测

        计算 SVM (Signal Vector Magnitude) 并与阈值比较
        """
        acc_x = data.get('acc_x', 0)
        acc_y = data.get('acc_y', 0)
        acc_z = data.get('acc_z', 0)

        # 计算加速度矢量幅值
        magnitude = (acc_x**2 + acc_y**2 + acc_z**2) ** 0.5

        # 阈值判断:
        # - 重力约 9.8 m/s²
        # - 跌倒冲击通常 > 20 m/s² (约 2g)
        is_fall = magnitude > 20.0

        return {
            "is_fall": is_fall,
            "confidence": 0.6 if is_fall else 0.9,
            "method": "traditional_threshold",
            "magnitude": round(magnitude, 2),
            "description": "High acceleration impact detected" if is_fall else "Normal movement"
        }

    async def _llm_enhancement(
        self,
        data: Dict[str, Any],
        traditional_result: Dict[str, Any]
    ) -> Dict[str, Any]:
        """
        LLM 上下文验证（SafetyGuardian 模式）

        分析位置、时间、生命体征等上下文信息，
        判断是否为真实跌倒或误报
        """
        system_prompt = """
        You are 'SafetyGuardian' (安卫), an intelligent Senior Safety Detective.
        Your mission is to analyze sensor data to determine if a senior citizen has TRULY fallen, or if it is a false alarm.

        # RULES
        1. **Language**: You MUST output your reasoning and recommendations in **Simplified Chinese (简体中文)**.
        2. **Context**: Analyze Location, Time, and Vitals.
        3. **False Alarm Detection**:
           - Running/Exercise locations + High HR = FALSE ALARM
           - Park/Gym + Movement = FALSE ALARM
           - Bathroom + Night + Sedentary = HIGH RISK
           - Bedroom + Night + Low activity after = FALL CONFIRMED

        # YOUR OUTPUT (JSON)
        {
            "is_fall": boolean,
            "confidence": float (0-1),
            "severity": "low" | "medium" | "high" | "critical",
            "reasoning": "用中文解释你的推理过程",
            "action_recommendation": "用中文建议后续行动 (e.g., '立即拨打120', '确认老人状态', '无需处理')"
        }
        """

        # 构建上下文数据
        context_data = {
            "evidence": {
                "sensor_metrics": {
                    "acceleration_magnitude": f"{traditional_result.get('magnitude', 0):.2f} G",
                    "acceleration_x": f"{data.get('acc_x', 0):.2f}",
                    "acceleration_y": f"{data.get('acc_y', 0):.2f}",
                    "acceleration_z": f"{data.get('acc_z', 0):.2f}",
                    "heart_rate": data.get('heart_rate', 'unknown'),
                    "blood_pressure": data.get('blood_pressure', 'unknown')
                },
                "context": {
                    "location": data.get('location', "unknown"),
                    "timestamp": data.get('timestamp', "unknown"),
                    "activity_status": data.get('activity_status', "unknown")
                },
                "user_profile": {
                    "age": data.get('age', 65),
                    "medical_history": data.get('medical_history', "Unknown")
                }
            },
            "initial_detection": traditional_result
        }

        try:
            response = await self.llm.analyze_json(system_prompt, context_data)
        except LLMError as e:
            # LLM 调用失败，返回传统结果但标记错误
            traditional_result['llm_verification_failed'] = True
            traditional_result['llm_error'] = str(e)
            return traditional_result

        # 解析 LLM 响应
        try:
            result_json = self._parse_llm_response(response)

            # 合并结果
            final_result = {
                "is_fall": result_json.get('is_fall', traditional_result['is_fall']),
                "confidence": result_json.get('confidence', traditional_result['confidence']),
                "method": "llm_enhanced",
                "magnitude": traditional_result.get('magnitude'),
                "enhanced_features": {
                    "severity": result_json.get('severity', "unknown"),
                    "reasoning": result_json.get('reasoning', "LLM validation"),
                    "recommendation": result_json.get('action_recommendation', "Check manually")
                },
                "original_result": traditional_result
            }
            return final_result

        except (json.JSONDecodeError, KeyError) as e:
            # 解析失败，返回传统结果但保留原始响应
            traditional_result['llm_parse_failed'] = True
            traditional_result['llm_raw_response'] = response
            return traditional_result

    def _parse_llm_response(self, response: str) -> Dict[str, Any]:
        """
        解析 LLM JSON 响应
        """
        clean = response.strip()

        # 移除 Markdown 代码块
        if "```json" in clean:
            clean = clean.split("```json")[1].split("```")[0].strip()
        elif "```" in clean:
            clean = clean.split("```")[1].split("```")[0].strip()

        return json.loads(clean)
