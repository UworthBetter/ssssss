package com.qkyd.ai.model.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * Result DTO from Python Service
 */
public class FallDetectionResultDTO implements Serializable {
    private Boolean is_fall;
    private Double confidence;
    private EnhancedFeatures enhanced_features;
    private Map<String, Object> original_result;

    public Boolean getIs_fall() {
        return is_fall;
    }

    public void setIs_fall(Boolean is_fall) {
        this.is_fall = is_fall;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public EnhancedFeatures getEnhanced_features() {
        return enhanced_features;
    }

    public void setEnhanced_features(EnhancedFeatures enhanced_features) {
        this.enhanced_features = enhanced_features;
    }

    public Map<String, Object> getOriginal_result() {
        return original_result;
    }

    public void setOriginal_result(Map<String, Object> original_result) {
        this.original_result = original_result;
    }

    public static class EnhancedFeatures implements Serializable {
        private String severity;
        private String reasoning;
        private String recommendation;

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getReasoning() {
            return reasoning;
        }

        public void setReasoning(String reasoning) {
            this.reasoning = reasoning;
        }

        public String getRecommendation() {
            return recommendation;
        }

        public void setRecommendation(String recommendation) {
            this.recommendation = recommendation;
        }
    }
}
