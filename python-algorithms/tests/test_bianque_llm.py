import unittest
import os
import sys

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from algorithms.risk_assessment.risk_scorer import RiskScorer

class TestBianqueLLM(unittest.TestCase):
    def setUp(self):
        self.scorer = RiskScorer()

    def test_high_risk_scenario(self):
        """
        Scenario: High HR, Low Movement, Poor Sleep.
        Expect: High Score + Detailed Doctor Report.
        """
        print("\n--- Testing Bianque High Risk Scenario ---")
        data = {
            "heart_rate": 110, # Risk
            "movement_score": 1.0, # High Risk (Sedentary)
            "sleep_hours": 4.5, # High Risk
            "age": 75 # Age bonus
        }
        
        result = self.scorer.evaluate(data, enable_llm=True)
        print(f"Risk Level: {result['level']} ({result['total_score']})")
        report = result.get('doctor_report', {})
        print(f"Dr. Bianque Says: {report.get('diagnosis_summary')}")
        print(f"Prescription: {report.get('prescription')}")
        
        self.assertEqual(result['level'], "High")
        self.assertIn("doctor_report", result)
        self.assertIsNotNone(report.get('prescription'))
        self.assertIn("exercise", report['prescription'])

if __name__ == '__main__':
    unittest.main()
