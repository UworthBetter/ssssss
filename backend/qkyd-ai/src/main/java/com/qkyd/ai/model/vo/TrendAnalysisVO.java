package com.qkyd.ai.model.vo;

import java.io.Serializable;

/**
 * Trend Analysis VO
 */
public class TrendAnalysisVO implements Serializable {
    private String trendDirection;
    private Double slope;
    private Double confidence;
    private String message;

    public String getTrendDirection() {
        return trendDirection;
    }

    public void setTrendDirection(String trendDirection) {
        this.trendDirection = trendDirection;
    }

    public Double getSlope() {
        return slope;
    }

    public void setSlope(Double slope) {
        this.slope = slope;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
