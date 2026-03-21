package com.qkyd.ai.model.dto;

import java.io.Serializable;

/**
 * Result DTO from Python Trend Analysis
 */
public class TrendAnalysisResultDTO implements Serializable {
    private String trend_direction;
    private Double slope;
    private Double prediction;
    private Double confidence;
    private String metric_type;

    public String getTrend_direction() {
        return trend_direction;
    }

    public void setTrend_direction(String trend_direction) {
        this.trend_direction = trend_direction;
    }

    public Double getSlope() {
        return slope;
    }

    public void setSlope(Double slope) {
        this.slope = slope;
    }

    public Double getPrediction() {
        return prediction;
    }

    public void setPrediction(Double prediction) {
        this.prediction = prediction;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getMetric_type() {
        return metric_type;
    }

    public void setMetric_type(String metric_type) {
        this.metric_type = metric_type;
    }
}
