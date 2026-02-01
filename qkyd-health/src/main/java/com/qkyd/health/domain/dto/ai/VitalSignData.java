package com.qkyd.health.domain.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * 单条体征数据 DTO
 * 与 Python 端 ueit-ai-service 的 VitalSignData 结构对齐
 *
 * @author ueit
 * @date 2026-01-29
 */
public class VitalSignData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 心率 (bpm)，有效范围 30-220
     */
    @JsonProperty("heart_rate")
    private Integer heartRate;

    /**
     * 血压，格式: 收缩压/舒张压，如 120/80
     */
    @JsonProperty("blood_pressure")
    private String bloodPressure;

    /**
     * 步数，非负整数
     */
    @JsonProperty("steps")
    private Integer steps;

    /**
     * Unix 时间戳 (毫秒)
     */
    @JsonProperty("timestamp")
    private Long timestamp;

    public VitalSignData() {
    }

    public VitalSignData(Integer heartRate, String bloodPressure, Integer steps, Long timestamp) {
        this.heartRate = heartRate;
        this.bloodPressure = bloodPressure;
        this.steps = steps;
        this.timestamp = timestamp;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public Integer getSteps() {
        return steps;
    }

    public void setSteps(Integer steps) {
        this.steps = steps;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "VitalSignData{" +
                "heartRate=" + heartRate +
                ", bloodPressure='" + bloodPressure + '\'' +
                ", steps=" + steps +
                ", timestamp=" + timestamp +
                '}';
    }
}
