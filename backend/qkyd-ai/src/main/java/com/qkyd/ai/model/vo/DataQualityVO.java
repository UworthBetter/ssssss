package com.qkyd.ai.model.vo;

import java.io.Serializable;

/**
 * Data Quality VO
 */
public class DataQualityVO implements Serializable {
    private Integer score;
    private Double missingRate;
    private Double outlierRate;
    private String suggestion;

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Double getMissingRate() {
        return missingRate;
    }

    public void setMissingRate(Double missingRate) {
        this.missingRate = missingRate;
    }

    public Double getOutlierRate() {
        return outlierRate;
    }

    public void setOutlierRate(Double outlierRate) {
        this.outlierRate = outlierRate;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }
}
