package com.qkyd.common.event;

import java.util.Map;

/**
 * 健康数据更新WebSocket推送事件
 * 
 * 当服务对象的健康数据更新时，发布此事件
 * 
 * @author qkyd
 * @date 2026-02-02
 */
public class HealthDataUpdateEvent {

    private final Long patientId;
    private final Map<String, Object> healthData;
    private final long timestamp;

    public HealthDataUpdateEvent(Long patientId, Map<String, Object> healthData) {
        this.patientId = patientId;
        this.healthData = healthData;
        this.timestamp = System.currentTimeMillis();
    }

    public Long getPatientId() {
        return patientId;
    }

    public Map<String, Object> getHealthData() {
        return healthData;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "HealthDataUpdateEvent{" +
                "patientId=" + patientId +
                ", timestamp=" + timestamp +
                '}';
    }
}
