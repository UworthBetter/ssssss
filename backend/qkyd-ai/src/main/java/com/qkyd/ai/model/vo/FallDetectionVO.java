package com.qkyd.ai.model.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * VO for Frontend Display
 */
public class FallDetectionVO implements Serializable {
    private Boolean detected;
    private String riskLevel;
    private String analysis;
    private String advice;
    private BigDecimal confidence;

    public Boolean getDetected() {
        return detected;
    }

    public void setDetected(Boolean detected) {
        this.detected = detected;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }
}
