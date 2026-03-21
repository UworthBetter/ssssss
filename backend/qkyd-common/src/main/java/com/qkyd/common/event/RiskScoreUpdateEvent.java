package com.qkyd.common.event;

import java.util.Map;

/**
 * 椋庨櫓璇勫垎WebSocket鎺ㄩ€佷簨浠?
 * 
 * 褰揂I妯″潡瀹屾垚椋庨櫓璇勫垎璁＄畻鏃讹紝鍙戝竷姝や簨浠?
 * 
 * @author qkyd
 * @date 2026-02-02
 */
public class RiskScoreUpdateEvent {

    private final Long patientId;
    private final Integer riskScore;
    private final String riskLevel;
    private final Map<String, Object> features;
    private final long timestamp;

    public RiskScoreUpdateEvent(Long patientId, Integer riskScore, String riskLevel, Map<String, Object> features) {
        this.patientId = patientId;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.features = features;
        this.timestamp = System.currentTimeMillis();
    }

    public Long getPatientId() {
        return patientId;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public Map<String, Object> getFeatures() {
        return features;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "RiskScoreUpdateEvent{" +
                "patientId=" + patientId +
                ", riskScore=" + riskScore +
                ", riskLevel='" + riskLevel + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
