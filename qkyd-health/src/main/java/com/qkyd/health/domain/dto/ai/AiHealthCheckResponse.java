package com.qkyd.health.domain.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * 健康检查响应 DTO
 * 与 Python 端 ueit-ai-service 的 HealthCheckResponse 结构对齐
 *
 * @author ueit
 * @date 2026-01-29
 */
public class AiHealthCheckResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码
     */
    @JsonProperty("code")
    private Integer code;

    /**
     * 响应消息
     */
    @JsonProperty("message")
    private String message;

    /**
     * 检测到的心率异常列表
     */
    @JsonProperty("anomalies")
    private List<HeartRateAnomaly> anomalies;

    /**
     * 异常数量
     */
    @JsonProperty("anomaly_count")
    private Integer anomalyCount;

    /**
     * 健康风险等级 (low/medium/high/critical)
     */
    @JsonProperty("risk_level")
    private String riskLevel;

    /**
     * 风险评分 (0.0-1.0)
     */
    @JsonProperty("risk_score")
    private Double riskScore;

    /**
     * 风险因素列表
     */
    @JsonProperty("risk_factors")
    private List<String> riskFactors;

    /**
     * 分析的数据点数量
     */
    @JsonProperty("data_points_analyzed")
    private Integer dataPointsAnalyzed;

    public AiHealthCheckResponse() {
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<HeartRateAnomaly> getAnomalies() {
        return anomalies;
    }

    public void setAnomalies(List<HeartRateAnomaly> anomalies) {
        this.anomalies = anomalies;
    }

    public Integer getAnomalyCount() {
        return anomalyCount;
    }

    public void setAnomalyCount(Integer anomalyCount) {
        this.anomalyCount = anomalyCount;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public List<String> getRiskFactors() {
        return riskFactors;
    }

    public void setRiskFactors(List<String> riskFactors) {
        this.riskFactors = riskFactors;
    }

    public Integer getDataPointsAnalyzed() {
        return dataPointsAnalyzed;
    }

    public void setDataPointsAnalyzed(Integer dataPointsAnalyzed) {
        this.dataPointsAnalyzed = dataPointsAnalyzed;
    }

    /**
     * 判断响应是否成功
     */
    public boolean isSuccess() {
        return code != null && code == 200;
    }

    @Override
    public String toString() {
        return "AiHealthCheckResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", anomalyCount=" + anomalyCount +
                ", riskLevel='" + riskLevel + '\'' +
                ", riskScore=" + riskScore +
                ", riskFactors=" + riskFactors +
                ", dataPointsAnalyzed=" + dataPointsAnalyzed +
                '}';
    }
}
