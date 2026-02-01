"""
DataSentinel - 数据质量检查引擎（异步版本）
"""
import json
from typing import Dict, Any, Optional, List
from algorithms.base import BaseAlgorithm
from utils.async_llm_client import AsyncLLMClient, LLMError


class DataSentinel(BaseAlgorithm):
    """
    DataSentinel (数觉): 智能数据质量与治理引擎

    功能:
    1. 基础数据质量评分
    2. LLM 驱动的缺失数据修复策略
    """
    def __init__(self, llm_client: Optional[AsyncLLMClient] = None):
        self.llm = llm_client or AsyncLLMClient()

    async def run(self, data: Dict[str, Any]) -> Dict[str, Any]:
        return await self.check(data)

    async def check(self, data: Dict[str, Any]) -> Dict[str, Any]:
        """
        通用数据质量检查 + 智能策略（如果提供上下文）

        Args:
            data: 数据字典，包含:
                - rows: 数据行列表
                - columns: 列名列表
                - missing_segment_context: 缺失数据段上下文（可选）

        Returns:
            质量检查报告
        """
        rows = data.get('rows', [])
        columns = data.get('columns', [])

        # 计算缺失值
        total_cells = len(rows) * len(columns) if rows else 0
        missing_count = sum(1 for r in rows for v in r if v in [None, "", "null"])

        basic_report = {
            "score": 100 * (1 - missing_count / total_cells) if total_cells else 100,
            "stats": {
                "missing": missing_count,
                "total": total_cells,
                "missing_rate": round(missing_count / total_cells * 100, 2) if total_cells else 0
            }
        }

        # 智能填充策略（如果提供上下文）
        if 'missing_segment_context' in data:
            strategy = await self.determine_filling_strategy(data['missing_segment_context'])
            basic_report['smart_strategy'] = strategy

        return basic_report

    async def determine_filling_strategy(self, context: Dict[str, Any]) -> Dict[str, Any]:
        """
        使用 LLM 决定如何处理缺失数据

        Args:
            context: 缺失数据段的上下文信息

        Returns:
            填充策略字典
        """
        system_prompt = """
        You are 'DataSentinel' (数觉), a Data Quality Assurance Expert.
        Your job is to diagnose MISSING DATA segments and prescribe a repair strategy.

        # RULES
        1. **Language**: Output `diagnosis` and `explanation` in **Simplified Chinese (简体中文)**.
        2. **Logic**:
           - Charging/Sleeping device + Night gap -> IGNORE (无需填充)
           - Active/Daytime short gap -> INTERPOLATE (插值修复)
           - Critical sensor data -> FLAG_WARNING (标记警告)
           - Long gap -> PREDICT (预测填充) or MANUAL_REVIEW

        # OUTPUT FORMAT (JSON)
        {
            "diagnosis": "中文诊断 (e.g. '推测用户正在充电')",
            "fill_strategy": "IGNORE" | "INTERPOLATE" | "PREDICT" | "FLAG_WARNING" | "MANUAL_REVIEW",
            "confidence": 0.95,
            "explanation": "中文解释 (e.g. '设备状态显示充电中，且为深夜时段')"
        }
        """

        try:
            response = await self.llm.analyze_json(system_prompt, context)
            return self._parse_response(response)
        except LLMError:
            return {
                "error": "LLM service unavailable",
                "diagnosis": "LLM 服务不可用",
                "fill_strategy": "MANUAL_REVIEW",
                "confidence": 0.0,
                "explanation": "需要人工审查"
            }

    def _parse_response(self, response: str) -> Dict[str, Any]:
        """解析 LLM 响应"""
        clean = response.strip()
        if "```json" in clean:
            clean = clean.split("```json")[1].split("```")[0].strip()
        elif "```" in clean:
            clean = clean.split("```")[1].split("```")[0].strip()
        return json.loads(clean)
