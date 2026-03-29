package com.qkyd.ai.agent;

import com.qkyd.ai.model.dto.EventInsightResultDTO;

import java.util.LinkedHashMap;
import java.util.Map;

public class EventPipelineAgentContext {
    private final Long eventId;
    private final Long pipelineId;
    private final Map<String, Object> workingData;
    private String abnormalType;
    private int priority;
    private EventInsightResultDTO insight;
    private Map<String, Object> riskPayload = new LinkedHashMap<>();
    private int riskScore;
    private String riskLevel;
    private String disposition;
    private String notificationLevel;
    private boolean autoExecute;
    private boolean stopped;
    private String stopReason;

    public EventPipelineAgentContext(Long eventId,
                                     Long pipelineId,
                                     Map<String, Object> workingData,
                                     String abnormalType,
                                     int priority) {
        this.eventId = eventId;
        this.pipelineId = pipelineId;
        this.workingData = new LinkedHashMap<>(workingData);
        this.abnormalType = abnormalType;
        this.priority = priority;
    }

    public Long getEventId() {
        return eventId;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public Map<String, Object> getWorkingData() {
        return workingData;
    }

    public Map<String, Object> copyWorkingData() {
        return new LinkedHashMap<>(workingData);
    }

    public String getAbnormalType() {
        return abnormalType;
    }

    public void setAbnormalType(String abnormalType) {
        this.abnormalType = abnormalType;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public EventInsightResultDTO getInsight() {
        return insight;
    }

    public void setInsight(EventInsightResultDTO insight) {
        this.insight = insight;
    }

    public Map<String, Object> getRiskPayload() {
        return riskPayload;
    }

    public void setRiskPayload(Map<String, Object> riskPayload) {
        this.riskPayload = riskPayload == null ? new LinkedHashMap<>() : new LinkedHashMap<>(riskPayload);
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getDisposition() {
        return disposition;
    }

    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    public String getNotificationLevel() {
        return notificationLevel;
    }

    public void setNotificationLevel(String notificationLevel) {
        this.notificationLevel = notificationLevel;
    }

    public boolean isAutoExecute() {
        return autoExecute;
    }

    public void setAutoExecute(boolean autoExecute) {
        this.autoExecute = autoExecute;
    }

    public boolean isStopped() {
        return stopped;
    }

    public String getStopReason() {
        return stopReason;
    }

    public void stopPipeline(String stopReason) {
        this.stopped = true;
        this.stopReason = stopReason;
    }
}
