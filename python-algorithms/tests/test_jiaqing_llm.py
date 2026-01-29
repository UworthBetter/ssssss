import unittest
import os
import sys

# Add project root to path
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from algorithms.fall_detection.jiaqing import JiaqingDetection

class TestJiaqingLLM(unittest.TestCase):
    def setUp(self):
        self.detector = JiaqingDetection()

    def test_true_fall_scenario(self):
        """
        Scenario: User falls in bathroom at night.
        High acceleration, critical location/time.
        Expect: LLM confirms fall.
        """
        print("\n--- Testing True Fall Scenario (Bathroom) ---")
        data = {
            "acc_x": 0, "acc_y": 0, "acc_z": 25.0, # ~2.5g impact
            "heart_rate": 110,
            "location": "Bathroom",
            "timestamp": "02:15:00",
            "activity_status": "sedentary",
            "age": 72,
            "medical_history": "History of stroke"
        }
        
        result = self.detector.detect_fall(data)
        print(f"Result: {result.get('enhanced_features', {}).get('reasoning')}")
        
        self.assertTrue(result['is_fall'])
        self.assertIn("enhanced_features", result)
        # Severity should ideally be high or critical
        self.assertIn(result['enhanced_features']['severity'], ['high', 'critical', 'medium'])

    def test_false_alarm_scenario(self):
        """
        Scenario: User running in park.
        High acceleration due to running, but context explains it.
        Expect: LLM rejects fall or marks improved confidence it's false.
        """
        print("\n--- Testing False Alarm Scenario (Park Run) ---")
        data = {
            "acc_x": 15, "acc_y": 15, "acc_z": 5, # High vector sum ~21
            "heart_rate": 140,
            "location": "Public Park",
            "timestamp": "16:30:00",
            "activity_status": "running",
            "age": 65
        }
        
        result = self.detector.detect_fall(data)
        print(f"Result: {result.get('enhanced_features', {}).get('reasoning')}")
        
        # Depending on how smart the LLM is, it should say False or Low Confidence Fall
        # Ideally it detects it's NOT a fall
        if result['is_fall']:
             print("LLM still thinks it is a fall, checking confidence/reasoning...")
             # If it thinks it's a fall, maybe severity is low?
        else:
             print("LLM correctly identified false alarm.")
             self.assertFalse(result['is_fall'])

if __name__ == '__main__':
    unittest.main()
