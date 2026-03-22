package com.qkyd.ai.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Structured event insight payload for the event center card.
 */
public class EventInsightResultDTO implements Serializable {
    private Long eventId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date generatedAt;

    private Freshness freshness;

    private String summary;
    private ParsedEvent parsedEvent;
    private ContextSnapshot context;
    private RiskAssessment risk;
    private Advice advice;
    private Trace trace;

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Date getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Date generatedAt) {
        this.generatedAt = generatedAt;
    }

    public Freshness getFreshness() {
        return freshness;
    }

    public void setFreshness(Freshness freshness) {
        this.freshness = freshness;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public ParsedEvent getParsedEvent() {
        return parsedEvent;
    }

    public void setParsedEvent(ParsedEvent parsedEvent) {
        this.parsedEvent = parsedEvent;
    }

    public ContextSnapshot getContext() {
        return context;
    }

    public void setContext(ContextSnapshot context) {
        this.context = context;
    }

    public RiskAssessment getRisk() {
        return risk;
    }

    public void setRisk(RiskAssessment risk) {
        this.risk = risk;
    }

    public Advice getAdvice() {
        return advice;
    }

    public void setAdvice(Advice advice) {
        this.advice = advice;
    }

    public Trace getTrace() {
        return trace;
    }

    public void setTrace(Trace trace) {
        this.trace = trace;
    }

    public static class ParsedEvent implements Serializable {
        private Long userId;
        private Long deviceId;
        private String abnormalType;
        private String metricName;
        private String metricValue;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private Date occurredAt;

        private String location;
        private String source;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(Long deviceId) {
            this.deviceId = deviceId;
        }

        public String getAbnormalType() {
            return abnormalType;
        }

        public void setAbnormalType(String abnormalType) {
            this.abnormalType = abnormalType;
        }

        public String getMetricName() {
            return metricName;
        }

        public void setMetricName(String metricName) {
            this.metricName = metricName;
        }

        public String getMetricValue() {
            return metricValue;
        }

        public void setMetricValue(String metricValue) {
            this.metricValue = metricValue;
        }

        public Date getOccurredAt() {
            return occurredAt;
        }

        public void setOccurredAt(Date occurredAt) {
            this.occurredAt = occurredAt;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }
    }

    public static class ContextSnapshot implements Serializable {
        private Integer age;
        private List<String> chronicDiseases;
        private String recentHealthTrend;
        private Integer historicalAbnormalCount;
        private Integer recentSameTypeCount;
        private String deviceStatus;
        private String deviceStatusReason;
        private String lastKnownLocation;
        private String dataConfidence;

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public List<String> getChronicDiseases() {
            return chronicDiseases;
        }

        public void setChronicDiseases(List<String> chronicDiseases) {
            this.chronicDiseases = chronicDiseases;
        }

        public String getRecentHealthTrend() {
            return recentHealthTrend;
        }

        public void setRecentHealthTrend(String recentHealthTrend) {
            this.recentHealthTrend = recentHealthTrend;
        }

        public Integer getHistoricalAbnormalCount() {
            return historicalAbnormalCount;
        }

        public void setHistoricalAbnormalCount(Integer historicalAbnormalCount) {
            this.historicalAbnormalCount = historicalAbnormalCount;
        }

        public Integer getRecentSameTypeCount() {
            return recentSameTypeCount;
        }

        public void setRecentSameTypeCount(Integer recentSameTypeCount) {
            this.recentSameTypeCount = recentSameTypeCount;
        }

        public String getDeviceStatus() {
            return deviceStatus;
        }

        public void setDeviceStatus(String deviceStatus) {
            this.deviceStatus = deviceStatus;
        }

        public String getDeviceStatusReason() {
            return deviceStatusReason;
        }

        public void setDeviceStatusReason(String deviceStatusReason) {
            this.deviceStatusReason = deviceStatusReason;
        }

        public String getLastKnownLocation() {
            return lastKnownLocation;
        }

        public void setLastKnownLocation(String lastKnownLocation) {
            this.lastKnownLocation = lastKnownLocation;
        }

        public String getDataConfidence() {
            return dataConfidence;
        }

        public void setDataConfidence(String dataConfidence) {
            this.dataConfidence = dataConfidence;
        }
    }

    public static class Freshness implements Serializable {
        private String state;
        private String tone;
        private String note;

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getTone() {
            return tone;
        }

        public void setTone(String tone) {
            this.tone = tone;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }

    public static class RiskAssessment implements Serializable {
        private String riskLevel;
        private Integer riskScore;
        private Boolean immediateAction;
        private List<String> possibleCauses;
        private List<String> analysisReasons;
        private List<String> ruleHits;

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

        public Boolean getImmediateAction() {
            return immediateAction;
        }

        public void setImmediateAction(Boolean immediateAction) {
            this.immediateAction = immediateAction;
        }

        public List<String> getPossibleCauses() {
            return possibleCauses;
        }

        public void setPossibleCauses(List<String> possibleCauses) {
            this.possibleCauses = possibleCauses;
        }

        public List<String> getAnalysisReasons() {
            return analysisReasons;
        }

        public void setAnalysisReasons(List<String> analysisReasons) {
            this.analysisReasons = analysisReasons;
        }

        public List<String> getRuleHits() {
            return ruleHits;
        }

        public void setRuleHits(List<String> ruleHits) {
            this.ruleHits = ruleHits;
        }
    }

    public static class Advice implements Serializable {
        private List<String> notifyWho;
        private List<String> suggestedActions;
        private Boolean offlineCheck;
        private Boolean contactFamily;
        private Boolean contactOrganization;
        private String operatorNote;

        public List<String> getNotifyWho() {
            return notifyWho;
        }

        public void setNotifyWho(List<String> notifyWho) {
            this.notifyWho = notifyWho;
        }

        public List<String> getSuggestedActions() {
            return suggestedActions;
        }

        public void setSuggestedActions(List<String> suggestedActions) {
            this.suggestedActions = suggestedActions;
        }

        public Boolean getOfflineCheck() {
            return offlineCheck;
        }

        public void setOfflineCheck(Boolean offlineCheck) {
            this.offlineCheck = offlineCheck;
        }

        public Boolean getContactFamily() {
            return contactFamily;
        }

        public void setContactFamily(Boolean contactFamily) {
            this.contactFamily = contactFamily;
        }

        public Boolean getContactOrganization() {
            return contactOrganization;
        }

        public void setContactOrganization(Boolean contactOrganization) {
            this.contactOrganization = contactOrganization;
        }

        public String getOperatorNote() {
            return operatorNote;
        }

        public void setOperatorNote(String operatorNote) {
            this.operatorNote = operatorNote;
        }
    }

    public static class Trace implements Serializable {
        private String orchestratorVersion;
        private Boolean fallbackUsed;
        private String fallbackReason;
        private List<String> missingFields;
        private List<TraceStep> steps;

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

        public String getFallbackReason() {
            return fallbackReason;
        }

        public void setFallbackReason(String fallbackReason) {
            this.fallbackReason = fallbackReason;
        }

        public List<String> getMissingFields() {
            return missingFields;
        }

        public void setMissingFields(List<String> missingFields) {
            this.missingFields = missingFields;
        }

        public List<TraceStep> getSteps() {
            return steps;
        }

        public void setSteps(List<TraceStep> steps) {
            this.steps = steps;
        }
    }

    public static class TraceStep implements Serializable {
        private String agentKey;
        private String agentName;
        private String status;
        private Integer resolvedCount;
        private Integer targetCount;
        private String summary;
        private String detail;

        public String getAgentKey() {
            return agentKey;
        }

        public void setAgentKey(String agentKey) {
            this.agentKey = agentKey;
        }

        public String getAgentName() {
            return agentName;
        }

        public void setAgentName(String agentName) {
            this.agentName = agentName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Integer getResolvedCount() {
            return resolvedCount;
        }

        public void setResolvedCount(Integer resolvedCount) {
            this.resolvedCount = resolvedCount;
        }

        public Integer getTargetCount() {
            return targetCount;
        }

        public void setTargetCount(Integer targetCount) {
            this.targetCount = targetCount;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }
    }
}
