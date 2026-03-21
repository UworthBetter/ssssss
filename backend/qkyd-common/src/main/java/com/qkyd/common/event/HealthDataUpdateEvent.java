package com.qkyd.common.event;

import java.util.Map;

/**
 * 鍋ュ悍鏁版嵁鏇存柊WebSocket鎺ㄩ€佷簨浠?
 * 
 * 褰撴湇鍔″璞＄殑鍋ュ悍鏁版嵁鏇存柊鏃讹紝鍙戝竷姝や簨浠?
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
