
import json
from utils.llm_client import LLMClient

class QualityChecker:
    def __init__(self):
        self.llm = LLMClient()

    def check(self, data):
        """
        Generic check + Smart Strategy if context provided.
        """
        rows = data.get('rows', [])
        columns = data.get('columns', [])
        
        # ... (Existing basic logic)
        total_cells = len(rows) * len(columns) if rows else 0
        missing_count = sum(1 for r in rows for v in r if v in [None, "", "null"])
        
        basic_report = {
            "score": 100 * (1 - missing_count/total_cells) if total_cells else 0,
            "stats": {"missing": missing_count, "total": total_cells}
        }

        # Qihuang Enhancement: Smart Strategy for Missing Segments
        # If the input contains 'missing_segment_context', we invoke the LLM.
        if 'missing_segment_context' in data:
            strategy = self.determine_filling_strategy(data['missing_segment_context'])
            basic_report['smart_strategy'] = strategy
            
        return basic_report

    def determine_filling_strategy(self, context):
        """
        Use LLM to decide HOW to handle missing data.
        context: {
            "time_range": "23:00 - 07:00",
            "device_status": "charging",
            "prev_heart_rate": "70 bpm (stable)",
            "location": "Home/Bedroom"
        }
        """
        system_prompt = """
        You are 'Qihuang' (岐黄), a Data Quality TCM Physician. 
        Your job is to diagnose MISSING DATA segments and prescribe a repair strategy.

        # RULES
        1. **Language**: Output `diagnosis` and `explanation` in **Simplified Chinese (简体中文)**.
        2. **Logic**:
           - Charging/Sleeping -> IGNORE (无需填充)
           - Active/Daytime gap -> INTERPOLATE (插值修复)
           - Critical -> FLAG_WARNING (标记警告)

        # OUTPUT FORMAT (JSON)
        {
            "diagnosis": "中文诊断 (e.g. '推测用户正在充电').",
            "fill_strategy": "IGNORE" | "INTERPOLATE" | "PREDICT" | "FLAG_WARNING",
            "confidence": 0.95,
            "explanation": "中文解释 (e.g. '设备状态显示充电中，且为深夜时段')."
        }
        """
        
        response = self.llm.analyze_json(system_prompt, context)
        
        try:
            clean_resp = response.strip()
            if "```json" in clean_resp: clean_resp = clean_resp.split("```json")[1].split("```")[0]
            elif "```" in clean_resp: clean_resp = clean_resp.split("```")[1].split("```")[0]
            return json.loads(clean_resp.strip())
        except Exception as e:
            return {"error": str(e), "diagnosis": "LLM Parse Error", "fill_strategy": "MANUAL_REVIEW"}
