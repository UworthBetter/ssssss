
import numpy as np

class QualityChecker:
    def check(self, data):
        """
        Check data quality for a list of records.
        data: { "columns": ["col1", "col2"], "rows": [ [val1, val2], ... ] }
        Returns: { 
            "score": float (0-100), 
            "issues": [str], 
            "stats": { "missing": int, "rows": int } 
        }
        """
        rows = data.get('rows', [])
        columns = data.get('columns', [])
        
        if not rows:
            return {"score": 0, "issues": ["No data provided"], "stats": {"rows": 0}}

        total_cells = len(rows) * len(columns)
        missing_count = 0
        issues = []
        
        # 1. Check Missing Values
        for r_idx, row in enumerate(rows):
            for c_idx, val in enumerate(row):
                if val is None or val == "" or val == "null":
                    missing_count += 1
                    if len(issues) < 5:
                        issues.append(f"Row {r_idx+1}, Col '{columns[c_idx]}' is missing")
                        
        if len(issues) >= 5:
            issues.append(f"...and {missing_count - 5} more missing values")

        # 2. Calculate Score
        missing_ratio = missing_count / total_cells if total_cells > 0 else 1
        integrity_score = 100 * (1 - missing_ratio)
        
        return {
            "score": round(integrity_score, 1),
            "issues": issues,
            "stats": {
                "rows": len(rows),
                "columns": len(columns),
                "total_cells": total_cells,
                "missing_cells": missing_count
            },
            "msg": f"Data Integrity: {integrity_score:.1f}%"
        }
