package com.ueit.health.domain;

import com.ueit.common.annotation.Excel;
import com.ueit.common.core.domain.BaseEntity;

import java.math.BigDecimal;

/**
 * AI健康分析记录对象 ai_health_record
 *
 * @author ueit
 * @date 2026-01-29
 */
public class AiHealthRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 设备ID
     */
    @Excel(name = "设备ID")
    private String deviceId;

    /**
     * 用户ID
     */
    @Excel(name = "用户ID")
    private Long userId;

    /**
     * 风险等级(low/medium/high/critical)
     */
    @Excel(name = "风险等级")
    private String riskLevel;

    /**
     * 风险评分(0.0000-1.0000)
     */
    @Excel(name = "风险评分")
    private BigDecimal riskScore;

    /**
     * 异常数量
     */
    @Excel(name = "异常数量")
    private Integer anomalyCount;

    /**
     * 风险因素JSON
     */
    private String riskFactors;

    /**
     * 原始体征数据JSON
     */
    private String rawData;

    /**
     * 数据点数量
     */
    @Excel(name = "数据点数量")
    private Integer dataPoints;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public BigDecimal getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(BigDecimal riskScore) {
        this.riskScore = riskScore;
    }

    public Integer getAnomalyCount() {
        return anomalyCount;
    }

    public void setAnomalyCount(Integer anomalyCount) {
        this.anomalyCount = anomalyCount;
    }

    public String getRiskFactors() {
        return riskFactors;
    }

    public void setRiskFactors(String riskFactors) {
        this.riskFactors = riskFactors;
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public Integer getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(Integer dataPoints) {
        this.dataPoints = dataPoints;
    }

    @Override
    public String toString() {
        return "AiHealthRecord{" +
                "id=" + id +
                ", deviceId='" + deviceId + '\'' +
                ", userId=" + userId +
                ", riskLevel='" + riskLevel + '\'' +
                ", riskScore=" + riskScore +
                ", anomalyCount=" + anomalyCount +
                ", dataPoints=" + dataPoints +
                '}';
    }
}
