package com.qkyd.common.event;

import java.util.Map;

/**
 * 异常检测WebSocket推送事件
 * 
 * 当AI模块检测到健康数据异常时，发布此事件
 * 
 * @author qkyd
 * @date 2026-02-02
 */
public class AbnormalDetectionEvent {

    private final Long patientId;
    private final String patientName;
    private final String abnormalType;
    private final String abnormalValue;
    private final String riskLevel;
    private final String message;
    private final Map<String, Object> details;
    private final long timestamp;

    public AbnormalDetectionEvent(Long patientId, String patientName, String abnormalType, 
                                  String abnormalValue, String riskLevel, String message, 
                                  Map<String, Object> details) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.abnormalType = abnormalType;
        this.abnormalValue = abnormalValue;
        this.riskLevel = riskLevel;
        this.message = message;
        this.details = details;
        this.timestamp = System.currentTimeMillis();
    }

    public Long getPatientId() {
        return patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getAbnormalType() {
        return abnormalType;
    }

    public String getAbnormalValue() {
        return abnormalValue;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "AbnormalDetectionEvent{" +
                "patientId=" + patientId +
                ", patientName='" + patientName + '\'' +
                ", abnormalType='" + abnormalType + '\'' +
                ", abnormalValue='" + abnormalValue + '\'' +
                ", riskLevel='" + riskLevel + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
