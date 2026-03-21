package com.qkyd.ai.model.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Result DTO from Python Risk Score
 */
public class RiskScoreResultDTO implements Serializable {
    private Integer risk_score;
    private String risk_level;
    private List<String> factors;
    private String llm_report;
    private Map<String, Integer> sub_scores;

    public Integer getRisk_score() {
        return risk_score;
    }

    public void setRisk_score(Integer risk_score) {
        this.risk_score = risk_score;
    }

    public String getRisk_level() {
        return risk_level;
    }

    public void setRisk_level(String risk_level) {
        this.risk_level = risk_level;
    }

    public List<String> getFactors() {
        return factors;
    }

    public void setFactors(List<String> factors) {
        this.factors = factors;
    }

    public String getLlm_report() {
        return llm_report;
    }

    public void setLlm_report(String llm_report) {
        this.llm_report = llm_report;
    }

    public Map<String, Integer> getSub_scores() {
        return sub_scores;
    }

    public void setSub_scores(Map<String, Integer> sub_scores) {
        this.sub_scores = sub_scores;
    }
}
