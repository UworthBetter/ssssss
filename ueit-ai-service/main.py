"""
UEIT AI Service - 时序体征数据处理微服务
基于 FastAPI 构建的算法中枢，支持心率异常检测和健康风险预测
"""
from fastapi import FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
import uvicorn
import os
from typing import List

from models.schemas import (
    VitalSignData,
    HealthCheckRequest,
    HealthCheckResponse,
    HeartRateAnomaly,
    RiskLevel,
)
from algorithms.heart_rate_detector import HeartRateDetector
from algorithms.health_risk_predictor import HealthRiskPredictor


def get_heart_rate_detector() -> HeartRateDetector:
    """获取心率检测器实例（懒加载）"""
    global _heart_rate_detector
    if _heart_rate_detector is None:
        _heart_rate_detector = HeartRateDetector(
            window_size=10,
            deviation_threshold=0.30
        )
    return _heart_rate_detector


def get_health_risk_predictor() -> HealthRiskPredictor:
    """获取健康风险预测器实例（懒加载）"""
    global _health_risk_predictor
    if _health_risk_predictor is None:
        _health_risk_predictor = HealthRiskPredictor()
    return _health_risk_predictor


# 全局算法实例（懒加载）
_heart_rate_detector: HeartRateDetector = None
_health_risk_predictor: HealthRiskPredictor = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    """应用生命周期管理"""
    # 启动时初始化算法实例
    detector = get_heart_rate_detector()
    predictor = get_health_risk_predictor()
    
    model_status = "已加载" if predictor.is_model_loaded() else "使用规则引擎"
    print(f"[ueit-ai-service] 服务启动完成")
    print(f"[ueit-ai-service] 心率检测器: window_size=10, threshold=30%")
    print(f"[ueit-ai-service] 健康风险预测器: {model_status}")
    
    yield
    
    # 关闭时清理资源
    print("[ueit-ai-service] 服务正在关闭...")


# 创建 FastAPI 应用
app = FastAPI(
    title="UEIT AI Service",
    description="""
## 时序体征数据处理微服务

耆康云盾健康监测平台的 AI 算法中枢，提供：

- 🫀 **心率异常检测**: 基于滑动窗口算法检测心率异常
- 📊 **健康风险预测**: 使用 scikit-learn 逻辑回归模型预测健康风险等级

### 技术特性

- ✅ Pydantic V2 严格数据校验
- ✅ 自动生成 OpenAPI (Swagger) 文档
- ✅ 支持预训练模型加载
- ✅ 工业级代码规范
    """,
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    openapi_url="/openapi.json",
    lifespan=lifespan,
)

# CORS 配置
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/", tags=["Health"])
async def root():
    """服务根路径"""
    return {
        "service": "ueit-ai-service",
        "version": "1.0.0",
        "status": "running",
        "docs": "/docs"
    }


@app.get("/health", tags=["Health"])
async def health():
    """健康检查端点"""
    predictor = get_health_risk_predictor()
    return {
        "status": "healthy",
        "model_loaded": predictor.is_model_loaded()
    }


@app.post(
    "/algo/v1/health-check",
    response_model=HealthCheckResponse,
    tags=["Algorithms"],
    summary="时序体征数据健康检查",
    description="""
接收时序体征数据列表，执行以下分析：

1. **心率异常检测**: 使用滑动窗口算法，当心率偏离过去 10 个数据点均值 30% 时触发预警
2. **健康风险预测**: 使用逻辑回归模型（或规则引擎）预测健康风险等级

### 输入要求
- `data`: 体征数据列表，每条记录包含 heart_rate, blood_pressure, steps, timestamp

### 输出内容
- `anomalies`: 检测到的心率异常列表
- `risk_level`: 健康风险等级 (low/medium/high/critical)
- `risk_score`: 风险评分 (0.0-1.0)
- `risk_factors`: 风险因素列表
    """
)
async def health_check(request: HealthCheckRequest) -> HealthCheckResponse:
    """
    时序体征数据健康检查接口
    
    接收包含 heart_rate, blood_pressure, steps, timestamp 的 JSON 列表，
    执行心率异常检测和健康风险等级预测。
    """
    try:
        data_list = request.data
        
        # 提取数据
        heart_rates = [item.heart_rate for item in data_list]
        blood_pressures = [item.blood_pressure for item in data_list]
        steps = [item.steps for item in data_list]
        timestamps = [item.timestamp for item in data_list]
        
        # 1. 心率异常检测
        detector = get_heart_rate_detector()
        anomaly_results = detector.detect(heart_rates, timestamps)
        
        # 转换异常结果为响应模型
        anomalies = [
            HeartRateAnomaly(
                timestamp=ar.timestamp,
                heart_rate=ar.heart_rate,
                window_mean=ar.window_mean,
                deviation_percent=ar.deviation_percent,
                message=ar.message
            )
            for ar in anomaly_results
        ]
        
        # 2. 健康风险预测
        predictor = get_health_risk_predictor()
        prediction = predictor.predict(
            heart_rates=heart_rates,
            blood_pressures=blood_pressures,
            steps=steps,
            anomaly_count=len(anomalies)
        )
        
        # 映射风险等级
        risk_level_map = {
            "low": RiskLevel.LOW,
            "medium": RiskLevel.MEDIUM,
            "high": RiskLevel.HIGH,
            "critical": RiskLevel.CRITICAL,
        }
        risk_level = risk_level_map.get(prediction.risk_level.value, RiskLevel.LOW)
        
        # 构建响应
        response = HealthCheckResponse(
            code=200,
            message="success",
            anomalies=anomalies,
            anomaly_count=len(anomalies),
            risk_level=risk_level,
            risk_score=prediction.risk_score,
            risk_factors=prediction.risk_factors,
            data_points_analyzed=len(data_list)
        )
        
        return response
        
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=f"数据验证错误: {str(e)}"
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"处理错误: {str(e)}"
        )


if __name__ == "__main__":
    port = int(os.getenv("PORT", 8001))
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=port,
        reload=True,
        log_level="info"
    )
