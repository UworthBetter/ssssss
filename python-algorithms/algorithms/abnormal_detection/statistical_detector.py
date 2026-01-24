import numpy as np

class StatisticalDetector:
    def detect(self, data):
        """
        Z-Score based detection.
        data: { "value": float, "history": [float, float, ...] }
        """
        value = data.get('value')
        history = data.get('history', [])
        
        if value is None or not history or len(history) < 2:
            return {"is_abnormal": False, "msg": "Insufficient data"}

        arr = np.array(history)
        mean = np.mean(arr)
        std = np.std(arr)
        
        if std == 0:
             return {"is_abnormal": False, "msg": "Zero variance in history"}

        z_score = (value - mean) / std
        
        threshold = data.get('threshold', 3.0) # Standard 3-sigma
        
        is_abnormal = bool(abs(z_score) > threshold)
        
        return {
            "is_abnormal": is_abnormal,
            "z_score": float(z_score),
            "mean": float(mean),
            "std": float(std),
            "msg": f"Z-Score {z_score:.2f} exceeds threshold {threshold}" if is_abnormal else "Normal"
        }
