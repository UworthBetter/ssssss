package com.qkyd.ai.model.dto;

import java.io.Serializable;

/**
 * Result DTO from Python Data Quality
 */
public class DataQualityResultDTO implements Serializable {
    private Integer quality_score;
    private Double missing_rate;
    private Double outlier_rate;
    private String fill_strategy;
    private String suggestion;

    public Integer getQuality_score() {
        return quality_score;
    }

    public void setQuality_score(Integer quality_score) {
        this.quality_score = quality_score;
    }

    public Double getMissing_rate() {
        return missing_rate;
    }

    public void setMissing_rate(Double missing_rate) {
        this.missing_rate = missing_rate;
    }

    public Double getOutlier_rate() {
        return outlier_rate;
    }

    public void setOutlier_rate(Double outlier_rate) {
        this.outlier_rate = outlier_rate;
    }

    public String getFill_strategy() {
        return fill_strategy;
    }

    public void setFill_strategy(String fill_strategy) {
        this.fill_strategy = fill_strategy;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }
}
