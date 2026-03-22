package com.qkyd.ai.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * Lightweight event insight snapshot summary for operator history views.
 */
public class EventInsightSnapshotSummaryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long eventId;
    private String summary;
    private String riskLevel;
    private Integer riskScore;
    private String orchestratorVersion;
    private Boolean fallbackUsed;
    private String freshnessState;
    private String freshnessTone;
    private String freshnessNote;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date generatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public String getOrchestratorVersion() {
        return orchestratorVersion;
    }

    public void setOrchestratorVersion(String orchestratorVersion) {
        this.orchestratorVersion = orchestratorVersion;
    }

    public Boolean getFallbackUsed() {
        return fallbackUsed;
    }

    public void setFallbackUsed(Boolean fallbackUsed) {
        this.fallbackUsed = fallbackUsed;
    }

    public String getFreshnessState() {
        return freshnessState;
    }

    public void setFreshnessState(String freshnessState) {
        this.freshnessState = freshnessState;
    }

    public String getFreshnessTone() {
        return freshnessTone;
    }

    public void setFreshnessTone(String freshnessTone) {
        this.freshnessTone = freshnessTone;
    }

    public String getFreshnessNote() {
        return freshnessNote;
    }

    public void setFreshnessNote(String freshnessNote) {
        this.freshnessNote = freshnessNote;
    }

    public Date getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Date generatedAt) {
        this.generatedAt = generatedAt;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
