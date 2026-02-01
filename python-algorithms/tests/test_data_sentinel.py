import unittest
import os
import sys

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from algorithms.data_quality.data_sentinel import DataSentinel

class TestDataSentinel(unittest.TestCase):
    def setUp(self):
        self.checker = DataSentinel()

    def test_charging_scenario(self):
        """
        Scenario: Night time, device charging. Data missing.
        Expect: Strategy = IGNORE (Don't fill fake sleep data)
        """
        print("\n--- Testing Charging Scenario ---")
        data = {
            "rows": [], 
            "columns": ["heart_rate"],
            "missing_segment_context": {
                "time_range": "02:00 - 06:00",
                "device_status": "charging",
                "location": "Home",
                "prev_heart_rate": "Before gap: 65 bpm"
            }
        }
        result = self.checker.check(data)
        strategy = result.get('smart_strategy', {})
        print(f"Strategy: {strategy}")
        
        self.assertEqual(strategy.get('fill_strategy'), "IGNORE")

    def test_sensor_gap_scenario(self):
        """
        Scenario: Running, suddenly gap, then Data continues. Device on wrist.
        Expect: Strategy = INTERPOLATE or PREDICT
        """
        print("\n--- Testing Sensor Gap Scenario ---")
        data = {
            "rows": [], 
            "columns": ["heart_rate"],
            "missing_segment_context": {
                "time_range": "14:00 - 14:05",
                "device_status": "on_wrist_active",
                "location": "Park",
                "prev_heart_rate": "140 bpm",
                "next_heart_rate": "138 bpm"
            }
        }
        result = self.checker.check(data)
        strategy = result.get('smart_strategy', {})
        print(f"Strategy: {strategy}")
        
        self.assertIn(strategy.get('fill_strategy'), ["INTERPOLATE", "PREDICT", "MEAN"])

if __name__ == '__main__':
    unittest.main()
