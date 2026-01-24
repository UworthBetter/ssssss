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
        Use LLM to verify the fall and analyze context.
        """
        system_prompt = """
        You are "Jiaqing", an advanced AI Fall Detection Analyst for smart health watches. 
        Your goal is to distinguish between REAL FALLS and FALSE ALARMS (like jumping, clapping, hard sitting).
        
        Analyze the provided sensor context JSON.
        
        Output valid JSON only:
        {
            "is_fall": boolean,
            "confidence": float (0.0-1.0),
            "reasoning": "string explanation",
            "severity": "low"|"medium"|"high",
            "action_recommendation": "string"
        }
        """
        
        # Enrich data with derived metrics for LLM
        context_data = {
            "sensor_raw": data,
            "traditional_analysis": traditional_result,
            "user_profile": {
                "age": data.get('age', 60),
                "history": "Previous fall recorded 2 months ago"
            },
            "environment": {
                "location": data.get('location', "unknown"),
                "time": data.get('timestamp', "unknown")
            }
        }
        
        response = self.llm.analyze_json(system_prompt, context_data)
        
        try:
            # Try to parse LLM JSON
            # Clean up potential markdown code blocks
            response_clean = response.replace("```json", "").replace("```", "").strip()
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
            # Fallback to traditional result if LLM fails
            traditional_result['llm_error'] = str(e)
            return traditional_result
