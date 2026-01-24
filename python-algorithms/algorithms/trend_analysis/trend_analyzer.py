import numpy as np

class TrendAnalyzer:
    def analyze(self, data):
        """
        Analyze trend from a list of numbers.
        data: { "values": [float], "period": str }
        Returns: { "trend": "up/down/stable", "slope": float, "next_value": float }
        """
        values = data.get('values', [])
        
        if not values or len(values) < 2:
            return {"trend": "unknown", "msg": "Insufficient data"}

        # X axis is just indices [0, 1, 2, ...]
        x = np.array(range(len(values)))
        y = np.array(values)
        
        # Linear Regression: y = mx + c
        A = np.vstack([x, np.ones(len(x))]).T
        m, c = np.linalg.lstsq(A, y, rcond=None)[0]
        
        # Determine trend
        trend = "stable"
        if m > 0.1:
            trend = "up"
        elif m < -0.1:
            trend = "down"
            
        # Forecast next value
        next_x = len(values)
        next_val = m * next_x + c
        
        return {
            "trend": trend,
            "slope": float(m),
            "intercept": float(c),
            "next_value": float(next_val),
            "msg": f"Trend is {trend} with slope {m:.2f}"
        }
