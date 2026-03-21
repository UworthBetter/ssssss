package com.qkyd.ai.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

/**
 * Risk Score Record Entity
 * corresponds to table ai_risk_score_record
 */
public class RiskScoreRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** User ID */
    private Long userId;

    /** Risk Score (0-100) */
    private Integer riskScore;

    /** Risk Level (high/medium/low/normal) */
    private String riskLevel;

    /** Risk Factors (JSON) */
    private String factors;

    /** LLM Report */
    private String llmReport;

    /** Sub Scores (JSON) */
    private String subScores;

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

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getFactors() {
        return factors;
    }

    public void setFactors(String factors) {
        this.factors = factors;
    }

    public String getLlmReport() {
        return llmReport;
    }

    public void setLlmReport(String llmReport) {
        this.llmReport = llmReport;
    }

    public String getSubScores() {
        return subScores;
    }

    public void setSubScores(String subScores) {
        this.subScores = subScores;
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
