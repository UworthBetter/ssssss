"""
HealthOracle - 健康风险评估系统（异步版本）
结合传统风险评分与 LLM 健康咨询
"""
import json
from typing import Dict, Any, Optional, List
from algorithms.base import BaseAlgorithm
from utils.async_llm_client import AsyncLLMClient, LLMError


class HealthOracle(BaseAlgorithm):
    """
    HealthOracle (康谏): 健康风险评估系统

    两阶段评估:
    1. 传统多因子风险评分（快速）
    2. LLM 健康报告生成（专业建议）
    """
    def __init__(self, llm_client: Optional[AsyncLLMClient] = None):
        self.llm = llm_client or AsyncLLMClient()

    async def run(self, data: Dict[str, Any]) -> Dict[str, Any]:
        return await self.evaluate(data)

    async def evaluate(
        self,
        data: Dict[str, Any],
        enable_llm: bool = True
    ) -> Dict[str, Any]:
        """
        评估健康风险

        Args:
            data: 健康数据，包含:
                - heart_rate: 心率
                - movement_score/movement: 活动评分 (0-10)
                - sleep_hours/sleep: 睡眠时长
                - age: 年龄
            enable_llm: 是否启用 LLM 报告

        Returns:
            风险评估结果
        """
        # 提取参数（支持别名）
        hr = data.get('heart_rate', 75)
        move = data.get('movement_score') or data.get('movement', 5.0)
        sleep = data.get('sleep_hours') or data.get('sleep', 7.5)
        age = data.get('age', 60)

        # 计算各因子风险分
        hr_risk = self._calculate_heart_rate_risk(hr)
        move_risk = self._calculate_movement_risk(move)
        sleep_risk = self._calculate_sleep_risk(sleep)
        age_bonus = self._calculate_age_factor(age)

        total_score = hr_risk + move_risk + sleep_risk + age_bonus
        total_score = min(max(total_score, 0), 100)

        # 确定风险等级
        level = "Low"
        if total_score > 60:
            level = "High"
        elif total_score > 30:
            level = "Medium"

        result = {
            "total_score": round(total_score, 1),
            "level": level,
            "factors": {
                "heart_rate_risk": round(hr_risk, 1),
                "sedentary_risk": round(move_risk, 1),
                "sleep_risk": round(sleep_risk, 1),
                "age_load": round(age_bonus, 1)
            },
            "metrics": {
                "hr": hr,
                "sleep": sleep,
                "movement": move,
                "age": age
            },
            "msg": f"Risk Level is {level} ({total_score})"
        }

        # LLM 健康报告
        if enable_llm:
            report = await self._generate_doctor_report(result)
            result['doctor_report'] = report

        return result

    def _calculate_heart_rate_risk(self, hr: int) -> float:
        """计算心率风险（偏离 75 的程度）"""
        hr_risk = 0
        if hr < 60 or hr > 100:
            hr_risk = min(abs(hr - 75) * 1.5, 40)
        return hr_risk

    def _calculate_movement_risk(self, move: float) -> float:
        """计算久坐风险（活动少 = 风险高）"""
        move_risk = 0
        if move < 3:
            move_risk = (3 - move) * 10
        return move_risk

    def _calculate_sleep_risk(self, sleep: float) -> float:
        """计算睡眠风险"""
        sleep_risk = 0
        if sleep < 5:
            sleep_risk = 20
        elif sleep < 6:
            sleep_risk = 10
        return sleep_risk

    def _calculate_age_factor(self, age: int) -> float:
        """计算年龄加成"""
        age_bonus = 0
        if age > 70:
            age_bonus = (age - 70) * 0.5
        return age_bonus

    async def _generate_doctor_report(self, risk_profile: Dict[str, Any]) -> Dict[str, Any]:
        """
        使用 LLM 生成医生风格健康报告
        """
        system_prompt = """
        You are 'HealthOracle' (康谏), a distinguished AI Health Consultant.
        Based on the User's Risk Profile, provide a "Private Doctor" style health report.

        # RULES
        1. **Language**: You MUST output all text in **Simplified Chinese (简体中文)**.
        2. **Tone**: Professional, caring, and authoritative.
        3. Use the risk profile to provide targeted advice.

        # KNOWLEDGE BASE
        - **Hypertension**: HR > 100 -> warn about stress/diet.
        - **Sedentary**: Risk > 10 -> recommend walking.
        - **Sleep**: < 7h -> recommend rest.
        - **Age**: > 70 -> emphasize fall prevention.

        # OUTPUT FORMAT (JSON)
        {
            "diagnosis_summary": "一句话中文诊断摘要。",
            "prescription": {
                "diet": "具体的中文饮食建议",
                "exercise": "具体的中文运动建议",
                "lifestyle": "睡眠与减压建议"
            },
            "encouragement": "一句温暖的中文鼓励语。"
        }
        """

        try:
            response = await self.llm.analyze_json(system_prompt, risk_profile)
            return self._parse_response(response)
        except LLMError:
            return {
                "error": "LLM service unavailable",
                "diagnosis_summary": "建议咨询专业医生。",
                "prescription": {
                    "diet": "均衡饮食",
                    "exercise": "适度运动",
                    "lifestyle": "保持良好作息"
                },
                "encouragement": "健康从现在开始。"
            }

    def _parse_response(self, response: str) -> Dict[str, Any]:
        """解析 LLM 响应"""
        clean = response.strip()
        if "```json" in clean:
            clean = clean.split("```json")[1].split("```")[0].strip()
        elif "```" in clean:
            clean = clean.split("```")[1].split("```")[0].strip()
        return json.loads(clean)
