"""
Pydantic 请求/响应模型
提供类型安全和自动验证
"""
from typing import Optional, Literal, Dict, Any
from pydantic import BaseModel, Field, field_validator
from datetime import datetime


# ============================================================================
# 跌倒检测模型
# ============================================================================

class FallDetectionRequest(BaseModel):
    """跌倒检测请求"""
    # 加速度数据 (单位: g, 范围: -50~50)
    acc_x: float = Field(..., ge=-50, le=50, description="X轴加速度")
    acc_y: float = Field(..., ge=-50, le=50, description="Y轴加速度")
    acc_z: float = Field(..., ge=-50, le=50, description="Z轴加速度")

    # 生理数据
    heart_rate: Optional[int] = Field(None, ge=30, le=220, description="心率")
    blood_pressure: Optional[str] = Field(None, description="血压 (如: '120/80')")

    # 上下文信息
    location: str = Field(..., min_length=1, max_length=100, description="位置")
    timestamp: Optional[str] = Field(None, description="时间戳")
    activity_status: Optional[str] = Field(
        "unknown",
        description="活动状态: walking, running, sleeping, sedentary, etc."
    )

    # 用户画像
    age: int = Field(..., ge=0, le=150, description="年龄")
    medical_history: Optional[str] = Field("Unknown", description="病史")

    # 控制参数
    use_llm: bool = Field(True, description="是否使用LLM增强")

    @field_validator('timestamp')
    @classmethod
    def validate_timestamp(cls, v: Optional[str]) -> Optional[str]:
        if v is None:
            return v
        # 简单验证时间格式
        try:
            # 支持多种时间格式
            for fmt in ('%H:%M:%S', '%Y-%m-%d %H:%M:%S', '%Y-%m-%dT%H:%M:%S'):
                try:
                    datetime.strptime(v, fmt)
                    return v
                except ValueError:
                    continue
        except Exception:
            pass
        return v  # 让业务逻辑处理


class FallDetectionResponse(BaseModel):
    """跌倒检测响应"""
    is_fall: bool = Field(..., description="是否检测到跌倒")
    confidence: float = Field(..., ge=0, le=1, description="置信度")
    method: str = Field(..., description="检测方法")
    magnitude: Optional[float] = Field(None, description="加速度幅值")

    # LLM 增强字段
    enhanced_features: Optional[Dict[str, Any]] = Field(None, description="LLM增强分析")
    original_result: Optional[Dict[str, Any]] = Field(None, description="传统算法结果")

    # 错误标记
    llm_verification_failed: Optional[bool] = Field(None, description="LLM验证是否失败")


# ============================================================================
# 异常检测模型
# ============================================================================

class AbnormalDetectionRequest(BaseModel):
    """异常检测请求"""
    method: Literal['threshold', 'statistical'] = Field('threshold', description="检测方法")

    # 阈值检测参数
    value: Optional[float] = Field(None, description="待检测值")
    min_val: Optional[float] = Field(None, alias='min', description="最小阈值")
    max_val: Optional[float] = Field(None, alias='max', description="最大阈值")

    # 统计检测参数
    data_points: Optional[list[float]] = Field(None, description="数据点列表")
    mean: Optional[float] = Field(None, description="均值")
    std_dev: Optional[float] = Field(None, description="标准差")

    # 多指标检测（批量）
    metrics: Optional[Dict[str, float]] = Field(None, description="多指标值字典")
    thresholds: Optional[Dict[str, tuple[float, float]]] = Field(
        None, description="各指标阈值范围 {'name': (min, max)}"
    )


class AbnormalDetectionResponse(BaseModel):
    """异常检测响应"""
    is_abnormal: bool = Field(..., description="是否异常")
    type: Optional[str] = Field(None, description="异常类型: too_high, too_low, outlier")
    diff: Optional[float] = Field(None, description="差值")
    msg: str = Field(..., description="描述信息")
    details: Optional[Dict[str, Any]] = Field(None, description="详细信息")


# ============================================================================
# 趋势分析模型
# ============================================================================

class TrendAnalysisRequest(BaseModel):
    """趋势分析请求"""
    data_points: list[float] = Field(..., min_length=2, description="数据点序列")
    metric_name: str = Field(..., description="指标名称")
    time_window: Optional[str] = Field("24h", description="时间窗口")

    # 分析选项
    include_forecast: bool = Field(False, description="是否包含预测")
    forecast_periods: int = Field(5, ge=1, le=30, description="预测周期数")


class TrendPoint(BaseModel):
    """趋势点"""
    timestamp: str
    value: float
    is_forecast: bool = False


class TrendAnalysisResponse(BaseModel):
    """趋势分析响应"""
    trend: Literal['increasing', 'decreasing', 'stable', 'volatile'] = Field(..., description="趋势方向")
    change_rate: float = Field(..., description="变化率")
    confidence: float = Field(..., ge=0, le=1, description="置信度")
    points: list[TrendPoint] = Field(default_factory=list, description="趋势点")
    summary: str = Field(..., description="趋势摘要")


# ============================================================================
# 风险评估模型
# ============================================================================

class RiskAssessmentRequest(BaseModel):
    """风险评估请求"""
    # 生理指标
    heart_rate: int = Field(..., ge=30, le=220, description="心率")
    movement_score: float = Field(..., ge=0, le=10, description="活动评分", alias='movement')
    sleep_hours: float = Field(..., ge=0, le=24, description="睡眠时长", alias='sleep')
    age: int = Field(..., ge=0, le=150, description="年龄")

    # 可选指标
    blood_pressure: Optional[tuple[int, int]] = Field(None, description="血压 (收缩压, 舒张压)")
    weight: Optional[float] = Field(None, ge=0, le=300, description="体重(kg)")
    height: Optional[float] = Field(None, ge=0, le=250, description="身高(cm)")

    # 控制参数
    enable_llm: bool = Field(True, description="是否启用LLM报告")


class RiskFactor(BaseModel):
    """风险因子"""
    heart_rate_risk: float = Field(..., description="心率风险分")
    sedentary_risk: float = Field(..., description="久坐风险分")
    sleep_risk: float = Field(..., description="睡眠风险分")
    age_load: float = Field(..., description="年龄加成")


class RiskMetrics(BaseModel):
    """风险指标"""
    hr: int = Field(..., description="心率")
    sleep: float = Field(..., description="睡眠时长")
    movement: float = Field(..., description="活动评分")
    age: int = Field(..., description="年龄")


class DoctorReport(BaseModel):
    """医生报告"""
    diagnosis_summary: str = Field(..., description="诊断摘要")
    prescription: Dict[str, str] = Field(..., description="处方建议")
    encouragement: str = Field(..., description="鼓励语")


class RiskAssessmentResponse(BaseModel):
    """风险评估响应"""
    total_score: float = Field(..., ge=0, le=100, description="总分")
    level: Literal['Low', 'Medium', 'High'] = Field(..., description="风险等级")
    factors: RiskFactor = Field(..., description="风险因子详情")
    metrics: RiskMetrics = Field(..., description="原始指标")
    msg: str = Field(..., description="消息")
    doctor_report: Optional[DoctorReport] = Field(None, description="医生报告")


# ============================================================================
# 数据质量检查模型
# ============================================================================

class DataRow(BaseModel):
    """数据行"""
    values: list[Any] = Field(..., description="行数据")


class DataQualityRequest(BaseModel):
    """数据质量检查请求"""
    rows: list[list[Any]] = Field(..., description="数据行列表")
    columns: list[str] = Field(..., description="列名列表")

    # 缺失数据段上下文
    missing_segment_context: Optional[Dict[str, Any]] = Field(
        None, description="缺失数据段的上下文信息"
    )


class QualityStats(BaseModel):
    """质量统计"""
    missing: int = Field(..., description="缺失数量")
    total: int = Field(..., description="总数量")


class FillingStrategy(BaseModel):
    """填充策略"""
    diagnosis: str = Field(..., description="诊断（中文）")
    fill_strategy: Literal['IGNORE', 'INTERPOLATE', 'PREDICT', 'FLAG_WARNING', 'MANUAL_REVIEW'] = Field(
        ..., description="填充策略"
    )
    confidence: float = Field(..., ge=0, le=1, description="置信度")
    explanation: str = Field(..., description="解释（中文）")


class DataQualityResponse(BaseModel):
    """数据质量检查响应"""
    score: float = Field(..., ge=0, le=100, description="质量分数")
    stats: QualityStats = Field(..., description="统计信息")
    smart_strategy: Optional[FillingStrategy] = Field(None, description="智能填充策略")


# ============================================================================
# 通用响应包装
# ============================================================================

class ApiResponse(BaseModel):
    """API 响应包装"""
    code: int = Field(200, description="状态码")
    msg: str = Field("success", description="消息")
    data: Optional[Dict[str, Any]] = Field(None, description="数据")

    class Config:
        # 允许任意类型的数据字段
        json_encoders = {
            # 自定义编码器（如果需要）
        }


# ============================================================================
# 健康检查模型
# ============================================================================

class HealthCheckResponse(BaseModel):
    """健康检查响应"""
    status: Literal['healthy', 'degraded', 'unhealthy'] = Field(..., description="服务状态")
    service: str = Field(..., description="服务名称")
    version: str = Field(..., description="版本号")
    uptime: float = Field(..., description="运行时间(秒)")
    checks: Dict[str, bool] = Field(default_factory=dict, description="各组件状态")
