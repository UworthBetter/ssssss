package com.qkyd.ai.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Trend Record Entity
 * corresponds to table ai_trend_record
 */
public class TrendRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** User ID */
    private Long userId;

    /** Metric Type */
    private String metricType;

    /** Trend Direction (up/down/stable) */
    private String trendDirection;

    /** Trend Strength */
    private BigDecimal trendStrength;

    /** Predicted Value */
    private BigDecimal predictedValue;

    /** Prediction Confidence */
    private BigDecimal predictionConfidence;

    /** Analysis Time */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date analysisTime;

    /** Create Time */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public String getTrendDirection() {
        return trendDirection;
    }

    public void setTrendDirection(String trendDirection) {
        this.trendDirection = trendDirection;
    }

    public BigDecimal getTrendStrength() {
        return trendStrength;
    }

    public void setTrendStrength(BigDecimal trendStrength) {
        this.trendStrength = trendStrength;
    }

    public BigDecimal getPredictedValue() {
        return predictedValue;
    }

    public void setPredictedValue(BigDecimal predictedValue) {
        this.predictedValue = predictedValue;
    }

    public BigDecimal getPredictionConfidence() {
        return predictionConfidence;
    }

    public void setPredictionConfidence(BigDecimal predictionConfidence) {
        this.predictionConfidence = predictionConfidence;
    }

    public Date getAnalysisTime() {
        return analysisTime;
    }

    public void setAnalysisTime(Date analysisTime) {
        this.analysisTime = analysisTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
