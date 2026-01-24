class ThresholdDetector:
    def detect(self, data):
        """
        Simple threshold detection.
        data: { "value": float, "min": float, "max": float }
        """
        value = data.get('value')
        min_val = data.get('min')
        max_val = data.get('max')
        
        if value is None:
            return {"is_abnormal": False, "msg": "No value provided"}

        if min_val is not None and value < min_val:
            return {
                "is_abnormal": True, 
                "type": "too_low", 
                "diff": value - min_val,
                "msg": f"Value {value} is lower than min {min_val}"
            }
            
        if max_val is not None and value > max_val:
            return {
                "is_abnormal": True, 
                "type": "too_high", 
                "diff": value - max_val,
                "msg": f"Value {value} is higher than max {max_val}"
            }
            
        return {"is_abnormal": False, "msg": "Normal"}
