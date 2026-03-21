package com.qkyd.ai.model.vo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Risk Score VO
 */
public class RiskScoreVO implements Serializable {
    private Integer score;
    private String level;
    private List<String> factors;
    private String report;
    private Map<String, Integer> subScores;

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public List<String> getFactors() {
        return factors;
    }

    public void setFactors(List<String> factors) {
        this.factors = factors;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public Map<String, Integer> getSubScores() {
        return subScores;
    }

    public void setSubScores(Map<String, Integer> subScores) {
        this.subScores = subScores;
    }
}
