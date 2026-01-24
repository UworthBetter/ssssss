from fastapi import FastAPI, HTTPException, Body
from algorithms.fall_detection.jiaqing import JiaqingDetection
from algorithms.abnormal_detection.threshold_detector import ThresholdDetector
from algorithms.abnormal_detection.statistical_detector import StatisticalDetector
import uvicorn
import os

app = FastAPI(title="UEIT AI Algorithms Service", version="1.0.0")

# Initialize Algorithms
jiaqing = JiaqingDetection()
threshold_detector = ThresholdDetector()
statistical_detector = StatisticalDetector()

@app.get("/")
def read_root():
    return {"status": "running", "service": "UEIT-AI-Python-Backend"}

@app.post("/api/algorithms/detect_fall")
def detect_fall(data: dict = Body(...)):
    """
    Endpoint for Jiaqing Fall Detection.
    Receives sensor data and returns analysis.
    """
    try:
        # data should contain: acc_x, acc_y, acc_z, age, location, timestamp, etc.
        result = jiaqing.detect_fall(data)
        return {"code": 200, "msg": "success", "data": result}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/abnormal/detect")
def detect_abnormal(data: dict = Body(...)):
    """
    Abnormal detection router.
    Supports 'threshold' and 'statistical' methods.
    """
    method = data.get("method", "threshold")
    try:
        if method == "statistical":
            result = statistical_detector.detect(data)
        else:
            result = threshold_detector.detect(data)
        return {"code": 200, "msg": "success", "data": result}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/trend/analyze")
def analyze_trend(data: dict = Body(...)):
    """
    Trend analysis router.
    """
    try:
        from algorithms.trend_analysis.trend_analyzer import TrendAnalyzer
        analyzer = TrendAnalyzer()
        result = analyzer.analyze(data)
        return {"code": 200, "msg": "success", "data": result}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/risk/assess")
def assess_risk(data: dict = Body(...)):
    """
    Risk assessment router.
    """
    try:
        from algorithms.risk_assessment.risk_scorer import RiskScorer
        scorer = RiskScorer()
        result = scorer.evaluate(data)
        return {"code": 200, "msg": "success", "data": result}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/data/quality")
def check_quality(data: dict = Body(...)):
    """
    Data quality check router.
    """
    try:
        from algorithms.data_quality.quality_checker import QualityChecker
        checker = QualityChecker()
        result = checker.check(data)
        return {"code": 200, "msg": "success", "data": result}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    port = int(os.getenv("PORT", 8000))
    uvicorn.run(app, host="0.0.0.0", port=port)
