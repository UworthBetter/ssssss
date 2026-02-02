"""
QKYD AI Algorithms Service - 异步 FastAPI 主服务
使用 Pydantic 验证 + 异步 LLM 客户端
"""
import os
import time
from contextlib import asynccontextmanager
from typing import Dict, Any, Optional
from fastapi import FastAPI, HTTPException, Body, status, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from fastapi_offline import FastAPIOffline
import uvicorn

# 导入异步 LLM 客户端
from utils.async_llm_client import AsyncLLMClient, LLMError, LLMConfigError
from utils.logger import service_logger, api_logger, init_logging

# 导入算法模块
from algorithms.fall_detection.safety_guardian import SafetyGuardian
from algorithms.risk_assessment.health_oracle import HealthOracle
from algorithms.data_quality.data_sentinel import DataSentinel

# 导入同步检测器（暂时保持同步）
from algorithms.abnormal_detection.threshold_detector import ThresholdDetector
from algorithms.abnormal_detection.statistical_detector import StatisticalDetector

# 导入 Pydantic 模型
from schemas import (
    FallDetectionRequest,
    FallDetectionResponse,
    AbnormalDetectionRequest,
    AbnormalDetectionResponse,
    RiskAssessmentRequest,
    RiskAssessmentResponse,
    DataQualityRequest,
    DataQualityResponse,
    ApiResponse,
    HealthCheckResponse,
    HealthCheckRequest,
    AlgoHealthCheckResponse
)


# ============================================================================
# 全局状态管理
# ============================================================================

class ServiceState:
    """服务状态容器"""
    def __init__(self):
        self.start_time = time.time()
        self.llm_client: Optional[AsyncLLMClient] = None
        self.algorithms: Dict[str, Any] = {}

    @property
    def uptime(self) -> float:
        """运行时间（秒）"""
        return time.time() - self.start_time


state = ServiceState()


# ============================================================================
# 应用生命周期
# ============================================================================

@asynccontextmanager
async def lifespan(app: FastAPI):
    """应用生命周期管理"""
    # 启动时初始化
    service_logger.info("[INIT] Initializing QKYD AI Algorithms Service...")

    try:
        # 初始化 LLM 客户端
        state.llm_client = AsyncLLMClient()
        service_logger.info("[OK] LLM Client initialized")

        # 初始化所有算法（共享 LLM 客户端）
        state.algorithms = {
            "safety_guardian": SafetyGuardian(llm_client=state.llm_client),
            "health_oracle": HealthOracle(llm_client=state.llm_client),
            "data_sentinel": DataSentinel(llm_client=state.llm_client),
            "threshold_detector": ThresholdDetector(),
            "statistical_detector": StatisticalDetector(),
        }
        service_logger.info("[OK] All algorithms initialized")

    except LLMConfigError as e:
        service_logger.warning(f"[WARN] LLM Client initialization failed: {e}")
        service_logger.warning("[WARN] Service will run in degraded mode (LLM features disabled)")

    yield

    # 关闭时清理
    service_logger.info("[SHUTDOWN] Shutting down service...")
    if state.llm_client:
        await state.llm_client.close()
    service_logger.info("[OK] Cleanup complete")


# ============================================================================
# FastAPI 应用
# ============================================================================

app = FastAPIOffline(
    title="QKYD AI Algorithms Service",
    version="2.0.0",
    description="耆康云盾智能健康算法服务（异步 + 类型安全）",
    lifespan=lifespan
)

@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    """捕获并记录验证错误详情"""
    error_msg = exc.errors()
    api_logger.error(f"Validation Error: {error_msg}")
    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_CONTENT,
        content={"detail": error_msg, "msg": "数据验证失败", "code": 422},
    )


# ============================================================================
# 健康检查
# ============================================================================

@app.get("/", response_model=ApiResponse)
async def read_root():
    """根端点"""
    return ApiResponse(
        code=200,
        msg="success",
        data={
            "service": "QKYD-AI-Python-Backend",
            "version": "2.0.0",
            "uptime": state.uptime
        }
    )


@app.get("/health", response_model=HealthCheckResponse)
async def health_check():
    """健康检查端点"""
    # 检查各组件状态
    checks = {
        "llm_service": state.llm_client is not None,
        "algorithms": len(state.algorithms) > 0
    }

    # 确定整体状态
    if all(checks.values()):
        health_status = "healthy"
    elif any(checks.values()):
        health_status = "degraded"
    else:
        health_status = "unhealthy"

    return HealthCheckResponse(
        status=health_status,
        service="qkyd-ai-algorithms",
        version="2.0.0",
        uptime=state.uptime,
        checks=checks
    )


# ============================================================================
# 跌倒检测端点
# ============================================================================

@app.post(
    "/api/algorithms/detect_fall",
    response_model=ApiResponse,
    summary="跌倒检测",
    description="使用 SafetyGuardian 进行智能跌倒检测"
)
async def detect_fall(request: FallDetectionRequest):
    """
    跌倒检测端点

    结合传统阈值检测与 LLM 上下文验证，
    提供高准确率的跌倒检测和误报过滤。
    """
    api_logger.info(f"Fall detection request - location: {request.location}, use_llm: {request.use_llm}")

    try:
        detector = state.algorithms.get("safety_guardian")
        if not detector:
            api_logger.error("SafetyGuardian algorithm not initialized")
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="SafetyGuardian algorithm not initialized"
            )

        # 转换 Pydantic 模型为字典
        data = request.model_dump()

        # 调用异步算法
        result = await detector.detect_fall(
            sensor_data=data,
            use_llm=request.use_llm
        )

        api_logger.info(f"Fall detection result - is_fall: {result.get('is_fall')}, confidence: {result.get('confidence')}")
        return ApiResponse(code=200, msg="success", data=result)

    except LLMError as e:
        api_logger.error(f"LLM service error: {e}")
        return ApiResponse(
            code=500,
            msg=f"LLM service error: {str(e)}",
            data=None
        )
    except Exception as e:
        api_logger.error(f"Fall detection failed: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e)
        )


# ============================================================================
# 异常检测端点
# ============================================================================

@app.post(
    "/api/abnormal/detect",
    response_model=ApiResponse,
    summary="异常检测",
    description="使用阈值或统计方法进行异常检测"
)
async def detect_abnormal(request: AbnormalDetectionRequest):
    """
    异常检测端点

    支持两种检测方法:
    - threshold: 简单阈值检测
    - statistical: 统计异常检测
    """
    api_logger.info(f"Abnormal detection request - method: {request.method}")

    try:
        data = request.model_dump(by_alias=True)

        if request.method == "statistical":
            detector = state.algorithms.get("statistical_detector")
            result = detector.run(data)  # 使用 run() 方法统一接口
        else:
            detector = state.algorithms.get("threshold_detector")
            result = detector.run(data)  # 使用 run() 方法统一接口

        api_logger.info(f"Abnormal detection result - is_abnormal: {result.get('is_abnormal')}")
        return ApiResponse(code=200, msg="success", data=result)

    except Exception as e:
        api_logger.error(f"Abnormal detection failed: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e)
        )


# ============================================================================
# 趋势分析端点
# ============================================================================

@app.post(
    "/api/trend/analyze",
    response_model=ApiResponse,
    summary="趋势分析",
    description="分析数据点的趋势方向和变化率"
)
async def analyze_trend(data: Dict[str, Any] = Body(...)):
    """
    趋势分析端点

    分析时间序列数据的趋势特征
    """
    api_logger.info(f"Trend analysis request - metric: {data.get('metric_name', 'unknown')}")

    try:
        from algorithms.trend_analysis.trend_analyzer import TrendAnalyzer
        analyzer = TrendAnalyzer()
        result = analyzer.analyze(data)

        api_logger.info(f"Trend analysis result - trend: {result.get('trend', 'unknown')}")
        return ApiResponse(code=200, msg="success", data=result)

    except Exception as e:
        api_logger.error(f"Trend analysis failed: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e)
        )


# ============================================================================
# 风险评估端点
# ============================================================================

@app.post(
    "/api/risk/assess",
    response_model=ApiResponse,
    summary="健康风险评估",
    description="使用 HealthOracle 进行综合健康风险评估"
)
async def assess_risk(request: RiskAssessmentRequest):
    """
    风险评估端点

    基于多维度健康指标进行风险评估，
    并提供 LLM 生成的专业健康建议。
    """
    api_logger.info(f"Risk assessment request - age: {request.age}, enable_llm: {request.enable_llm}")

    try:
        oracle = state.algorithms.get("health_oracle")
        if not oracle:
            api_logger.error("HealthOracle algorithm not initialized")
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="HealthOracle algorithm not initialized"
            )

        # 转换 Pydantic 模型为字典（使用别名）
        data = request.model_dump(by_alias=True)

        # 调用异步算法
        result = await oracle.evaluate(
            data=data,
            enable_llm=request.enable_llm
        )

        api_logger.info(f"Risk assessment result - level: {result.get('level')}, score: {result.get('total_score')}")
        return ApiResponse(code=200, msg="success", data=result)

    except LLMError as e:
        api_logger.error(f"LLM service error: {e}")
        return ApiResponse(
            code=500,
            msg=f"LLM service error: {str(e)}",
            data=None
        )
    except Exception as e:
        api_logger.error(f"Risk assessment failed: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e)
        )


# ============================================================================
# 数据质量检查端点
# ============================================================================

@app.post(
    "/api/data/quality",
    response_model=ApiResponse,
    summary="数据质量检查",
    description="使用 DataSentinel 进行数据质量评估和修复建议"
)
async def check_quality(request: DataQualityRequest):
    """
    数据质量检查端点

    评估数据集质量，并提供缺失数据处理策略。
    """
    row_count = len(request.rows)
    api_logger.info(f"Data quality check request - rows: {row_count}, columns: {len(request.columns)}")

    try:
        sentinel = state.algorithms.get("data_sentinel")
        if not sentinel:
            api_logger.error("DataSentinel algorithm not initialized")
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="DataSentinel algorithm not initialized"
            )

        # 转换 Pydantic 模型为字典
        data = request.model_dump()

        # 调用异步算法
        result = await sentinel.check(data=data)

        api_logger.info(f"Data quality result - score: {result.get('score'):.1f}")
        return ApiResponse(code=200, msg="success", data=result)

    except LLMError as e:
        api_logger.error(f"LLM service error: {e}")
        return ApiResponse(
            code=500,
            msg=f"LLM service error: {str(e)}",
            data=None
        )
    except Exception as e:
        api_logger.error(f"Data quality check failed: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e)
        )


# ============================================================================
# 兼容性桥接接口 (对接旧版 Java 调用)
# ============================================================================

@app.post(
    "/algo/v1/health-check",
    response_model=AlgoHealthCheckResponse,
    summary="[兼容性接口] 综合健康检查",
    description="支持 qkyd-health 直接调用，内部调用 HealthOracle 进行分析"
)
async def bridge_health_check(request: HealthCheckRequest):
    """
    桥接接口：将旧版数据格式转为新版 RiskAssessment 逻辑。
    """
    api_logger.info(f"Bridge health check request - points: {len(request.data)}")
    
    try:
        oracle = state.algorithms.get("health_oracle")
        if not oracle:
            raise HTTPException(status_code=503, detail="HealthOracle not initialized")

        # 1. 转换数据 (提取最近一个点的主要指标)
        last_point = request.data[-1]
        
        # 兼容性映射
        assess_data = {
            "heart_rate": last_point.heart_rate,
            "movement": 5.0, # 默认值
            "sleep": 8.0,    # 默认值
            "age": 65,       # 默认值
        }

        # 2. 调用新版评估逻辑
        result = await oracle.evaluate(data=assess_data, enable_llm=False)

        # 3. 映射回旧版响应格式
        return AlgoHealthCheckResponse(
            code=200,
            message="success",
            risk_level=result.get("level", "low").lower(),
            risk_score=result.get("total_score", 0.0) / 100.0,
            anomaly_count=1 if result.get("level") == "High" else 0,
            risk_factors=[result.get("msg", "正常")],
            data_points_analyzed=len(request.data)
        )

    except Exception as e:
        api_logger.error(f"Bridge assessment failed: {e}", exc_info=True)
        return AlgoHealthCheckResponse(
            code=500,
            message=f"Analysis Error: {str(e)}",
            data_points_analyzed=len(request.data)
        )


# ============================================================================
# 清除缓存端点
# ============================================================================

@app.post("/api/cache/clear", response_model=ApiResponse)
async def clear_cache():
    """清除 LLM 响应缓存"""
    api_logger.info("Cache clear request")
    if state.llm_client:
        state.llm_client.clear_cache()
        api_logger.info("Cache cleared successfully")
        return ApiResponse(code=200, msg="Cache cleared", data={"cleared": True})
    api_logger.warning("No cache to clear (LLM client not initialized)")
    return ApiResponse(code=404, msg="No cache to clear", data={"cleared": False})


# ============================================================================
# 主函数
# ============================================================================

if __name__ == "__main__":
    port = int(os.getenv("PORT", 8001))
    uvicorn.run(
        app,
        host="0.0.0.0",
        port=port,
        log_level="info"
    )
