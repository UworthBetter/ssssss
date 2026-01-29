import json
from utils.llm_client import LLMClient

class JiaqingDetection:
    def __init__(self):
        self.llm = LLMClient()
        
    def detect_fall(self, sensor_data, use_llm=True):
        """
        Process sensor data to detect falls.
        Returns a dict with detection results.
        """
        # 1. Traditional Algorithm (Fast, Low Latency)
        # Using simple threshold logic on acceleration (mock implementation)
        traditional_result = self._threshold_detection(sensor_data)
        
        # If no fall detected by traditional means, usually we return early.
        # But if detected, we use LLM to verify (reduce false positives).
        if not traditional_result['is_fall']:
            return traditional_result

        # 2. LLM Enhancement (Slow, High Context)
        if use_llm:
            return self._llm_enhancement(sensor_data, traditional_result)
        
        return traditional_result

    def _threshold_detection(self, data):
        """
        Simulated traditional acceleration threshold detection.
        In a real scenario, this would compute SVM using x,y,z acc data.
        """
        acc_x = data.get('acc_x', 0)
        acc_y = data.get('acc_y', 0)
        acc_z = data.get('acc_z', 0)
        
        # Calculate vector sum (magnitude)
        magnitude = (acc_x**2 + acc_y**2 + acc_z**2) ** 0.5
        
        # Threshold adjustment: Gravity is ~9.8. Fall impact usually > 20.
        # If magnitude > 20 (approx 2g), consider it a potential fall.
        is_fall = magnitude > 20.0
        
        return {
            "is_fall": is_fall,
            "confidence": 0.6 if is_fall else 0.9,
            "method": "traditional_threshold",
            "magnitude": magnitude,
            "description": "High acceleration impact detected" if is_fall else "Normal movement"
        }

    def _llm_enhancement(self, data, traditional_result):
        """
        Use LLM to verify the fall and analyze context (Detective Mode).
        """
        system_prompt = """
        You are 'Jiaqing' (嘉庆), an intelligent Senior Safety Detective. 
        Your mission is to analyze sensor data to determine if a senior citizen has TRULY fallen, or if it is a false alarm.

        # RULES
        1. **Language**: You MUST output your reasoning and recommendations in **Simplified Chinese (简体中文)**.
        2. **Context**: Analyze Location, Time, and Vitals.
        
        # YOUR OUTPUT (JSON)
        {
            "is_fall": boolean, 
            "confidence": float, 
            "severity": "low" | "medium" | "critical",
            "reasoning": "用中文解释你的推理过程 (e.g., '检测到高加速度，但发生在公园跑步期间...').",
            "action_recommendation": "用中文建议后续行动 (e.g., '立即拨打120', '确认老人状态', '无需处理')."
        }
        """
        
        # Enrich data with derived metrics for LLM
        # In a real app, 'history' comes from a database.
        context_data = {
            "evidence": {
                "sensor_metrics": {
                    "acceleration_magnitude": f"{traditional_result.get('magnitude', 0):.2f} G",
                    "heart_rate": data.get('heart_rate', 'unknown'),
                    "blood_pressure": data.get('blood_pressure', 'unknown')
                },
                "context": {
                    "location": data.get('location', "unknown"),
                    "timestamp": data.get('timestamp', "unknown"),
                    "activity_status": data.get('activity_status', "unknown") # e.g., 'walking', 'sleeping'
                },
                "suspect_profile": {
                    "age": data.get('age', 65),
                    "notes": data.get('medical_history', "Hypertension")
                }
            },
            "initial_suspect_analysis": traditional_result
        }
        
        response = self.llm.analyze_json(system_prompt, context_data)
        
        try:
            # Robust JSON cleaning
            response_clean = response.strip()
            if "```json" in response_clean:
                response_clean = response_clean.split("```json")[1].split("```")[0].strip()
            elif "```" in response_clean:
                response_clean = response_clean.split("```")[1].split("```")[0].strip()
                
            result_json = json.loads(response_clean)
            
            # Merge results
            final_result = {
                "is_fall": result_json.get('is_fall', traditional_result['is_fall']),
                "confidence": result_json.get('confidence', traditional_result['confidence']),
                "enhanced_features": {
                    "severity": result_json.get('severity', "unknown"),
                    "reasoning": result_json.get('reasoning', "LLM validation"),
                    "recommendation": result_json.get('action_recommendation', "Check manually")
                },
                "original_result": traditional_result
            }
            return final_result
            
        except Exception as e:
            print(f"LLM parsing failed: {e}. Raw response: {response}")
            # Fallback to traditional result if LLM fails, but mark error
            traditional_result['llm_verification_failed'] = True
            traditional_result['llm_raw_response'] = response
            return traditional_result
