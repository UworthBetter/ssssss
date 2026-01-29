
import json
from utils.llm_client import LLMClient

class RiskScorer:
    def __init__(self):
        self.llm = LLMClient()

    def evaluate(self, data, enable_llm=True):
        """
        Evaluate health risk based on multiple factors.
        data: {
            "heart_rate": int, (60-100 is normal)
            "movement_score": float, (0-10, low involves risk)
            "sleep_hours": float, (7-9 is normal)
            "age": int
        }
        Returns: {
            "total_score": float (0-100, 100 is highest risk),
            "level": str,
            "breakdown": { "heart": float, "movement": float, "sleep": float, "age_factor": float }
        }
        """
        hr = data.get('heart_rate', 75)
        move = data.get('movement_score', 5.0)
        sleep = data.get('sleep_hours', 7.5)
        age = data.get('age', 60)
        
        # 1. Heart Rate Risk (Deviation from 75)
        hr_risk = 0
        if hr < 60 or hr > 100:
            hr_risk = min(abs(hr - 75) * 1.5, 40) # Max 40 pts
        
        # 2. Movement Risk (Low movement = higher risk)
        move_risk = 0
        if move < 3:
            move_risk = (3 - move) * 10 # Max 30 pts
            
        # 3. Sleep Risk
        sleep_risk = 0
        if sleep < 5:
            sleep_risk = 20
        elif sleep < 6:
            sleep_risk = 10
            
        # 4. Age Factor (Base multiplier)
        age_bonus = 0
        if age > 70:
            age_bonus = (age - 70) * 0.5
            
        total_score = hr_risk + move_risk + sleep_risk + age_bonus
        total_score = min(max(total_score, 0), 100)
        
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
            "metrics": {"hr": hr, "sleep": sleep, "movement": move, "age": age},
            "msg": f"Risk Level is {level} ({total_score})"
        }

        # Bianque Enhancement: "Private Doctor" Report
        if enable_llm:
            report = self._generate_doctor_report(result)
            result['doctor_report'] = report
            
        return result

    def _generate_doctor_report(self, risk_profile):
        """
        Use LLM (simulating RAG with guidelines) to generate a report.
        """
        system_prompt = """
        You are 'Dr. Bianque' (扁鹊), a distinguished AI Health Consultant. 
        Based on the User's Risk Profile, provide a "Private Doctor" style health report.
        
        # RULES
        1. **Language**: You MUST output all text in **Simplified Chinese (简体中文)**.
        2. **Tone**: Professional, caring, and authoritative (like a TCM doctor).
        
        # KNOWLEDGE BASE
        - **Hypertension**: HR > 100 -> warn about stress/diet.
        - **Sedentary**: Risk > 10 -> recommend walking.
        - **Sleep**: < 7h -> recommend rest.
        
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
        
        response = self.llm.analyze_json(system_prompt, risk_profile)
        
        try:
            clean_resp = response.strip()
            if "```json" in clean_resp: clean_resp = clean_resp.split("```json")[1].split("```")[0]
            elif "```" in clean_resp: clean_resp = clean_resp.split("```")[1].split("```")[0]
            return json.loads(clean_resp.strip())
        except Exception as e:
            return {"error": str(e), "diagnosis_summary": "Consult a real doctor."}
