"""
健康检查 API 测试
测试 /algo/v1/health-check 接口
"""
import pytest
from fastapi.testclient import TestClient
import sys
import os

# 添加父目录到路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from main import app


@pytest.fixture
def client():
    """测试客户端"""
    return TestClient(app)


class TestHealthEndpoints:
    """健康检查端点测试"""
    
    def test_root(self, client):
        """测试根路径"""
        response = client.get("/")
        assert response.status_code == 200
        data = response.json()
        assert data["service"] == "qkyd-ai-service"
        assert data["status"] == "running"
    
    def test_health(self, client):
        """测试健康检查端点"""
        response = client.get("/health")
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "healthy"


class TestHealthCheckAPI:
    """健康检查 API 测试"""
    
    def test_basic_health_check(self, client):
        """测试基本健康检查请求"""
        request_data = {
            "data": [
                {
                    "heart_rate": 72,
                    "blood_pressure": "120/80",
                    "steps": 500,
                    "timestamp": 1706500000000
                },
                {
                    "heart_rate": 75,
                    "blood_pressure": "118/78",
                    "steps": 800,
                    "timestamp": 1706500060000
                }
            ]
        }
        
        response = client.post("/algo/v1/health-check", json=request_data)
        assert response.status_code == 200
        
        data = response.json()
        assert data["code"] == 200
        assert data["message"] == "success"
        assert data["data_points_analyzed"] == 2
        assert "risk_level" in data
        assert "risk_score" in data
    
    def test_anomaly_detection(self, client):
        """测试心率异常检测"""
        # 创建 11 条数据，前 10 条正常，第 11 条异常
        normal_hr = 75
        data_list = []
        
        for i in range(10):
            data_list.append({
                "heart_rate": normal_hr + (i % 3),  # 73-77 范围
                "blood_pressure": "120/80",
                "steps": 100,
                "timestamp": 1706500000000 + i * 60000
            })
        
        # 第 11 条数据心率异常 (偏离 30% 以上)
        data_list.append({
            "heart_rate": 130,  # 远超过 75 * 1.3 = 97.5
            "blood_pressure": "120/80",
            "steps": 100,
            "timestamp": 1706500600000
        })
        
        request_data = {"data": data_list}
        response = client.post("/algo/v1/health-check", json=request_data)
        
        assert response.status_code == 200
        data = response.json()
        
        # 应该检测到异常
        assert data["anomaly_count"] >= 1
        assert len(data["anomalies"]) >= 1
        
        # 检查异常详情
        anomaly = data["anomalies"][0]
        assert anomaly["heart_rate"] == 130
        assert anomaly["deviation_percent"] > 30
    
    def test_invalid_heart_rate(self, client):
        """测试无效心率值 (超出范围)"""
        request_data = {
            "data": [
                {
                    "heart_rate": 250,  # 超出有效范围 (30-220)
                    "blood_pressure": "120/80",
                    "steps": 100,
                    "timestamp": 1706500000000
                }
            ]
        }
        
        response = client.post("/algo/v1/health-check", json=request_data)
        assert response.status_code == 422  # Validation Error
    
    def test_invalid_blood_pressure_format(self, client):
        """测试无效血压格式"""
        request_data = {
            "data": [
                {
                    "heart_rate": 75,
                    "blood_pressure": "120-80",  # 错误格式
                    "steps": 100,
                    "timestamp": 1706500000000
                }
            ]
        }
        
        response = client.post("/algo/v1/health-check", json=request_data)
        assert response.status_code == 422  # Validation Error
    
    def test_invalid_blood_pressure_values(self, client):
        """测试无效血压值 (收缩压小于舒张压)"""
        request_data = {
            "data": [
                {
                    "heart_rate": 75,
                    "blood_pressure": "80/120",  # 收缩压 < 舒张压
                    "steps": 100,
                    "timestamp": 1706500000000
                }
            ]
        }
        
        response = client.post("/algo/v1/health-check", json=request_data)
        assert response.status_code == 422  # Validation Error
    
    def test_negative_steps(self, client):
        """测试负数步数"""
        request_data = {
            "data": [
                {
                    "heart_rate": 75,
                    "blood_pressure": "120/80",
                    "steps": -100,  # 负数步数
                    "timestamp": 1706500000000
                }
            ]
        }
        
        response = client.post("/algo/v1/health-check", json=request_data)
        assert response.status_code == 422  # Validation Error
    
    def test_empty_data_list(self, client):
        """测试空数据列表"""
        request_data = {"data": []}
        
        response = client.post("/algo/v1/health-check", json=request_data)
        assert response.status_code == 422  # Validation Error (min_length=1)
    
    def test_risk_level_response(self, client):
        """测试风险等级响应"""
        # 高风险数据
        request_data = {
            "data": [
                {
                    "heart_rate": 120,  # 偏高
                    "blood_pressure": "160/100",  # 高血压
                    "steps": 50,
                    "timestamp": 1706500000000
                }
            ]
        }
        
        response = client.post("/algo/v1/health-check", json=request_data)
        assert response.status_code == 200
        
        data = response.json()
        assert data["risk_level"] in ["low", "medium", "high", "critical"]
        assert 0.0 <= data["risk_score"] <= 1.0
        assert len(data["risk_factors"]) > 0


class TestAlgorithms:
    """算法单元测试"""
    
    def test_heart_rate_detector(self):
        """测试心率检测器"""
        from algorithms.heart_rate_detector import HeartRateDetector
        
        detector = HeartRateDetector(window_size=5, deviation_threshold=0.30)
        
        # 创建测试数据
        heart_rates = [70, 72, 68, 71, 69, 100]  # 最后一个异常
        timestamps = [1000, 2000, 3000, 4000, 5000, 6000]
        
        anomalies = detector.detect(heart_rates, timestamps)
        
        # 应该检测到最后一个数据点异常
        assert len(anomalies) == 1
        assert anomalies[0].heart_rate == 100
    
    def test_health_risk_predictor(self):
        """测试健康风险预测器"""
        from algorithms.health_risk_predictor import HealthRiskPredictor
        
        predictor = HealthRiskPredictor()
        
        # 测试正常数据
        result = predictor.predict(
            heart_rates=[70, 72, 68, 71, 69],
            blood_pressures=["120/80", "118/78", "122/82"],
            steps=[1000, 1500, 2000],
            anomaly_count=0
        )
        
        assert result.risk_level.value in ["low", "medium", "high", "critical"]
        assert 0.0 <= result.risk_score <= 1.0
        assert len(result.risk_factors) > 0


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
