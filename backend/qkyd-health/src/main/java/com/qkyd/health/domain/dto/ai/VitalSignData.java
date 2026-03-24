package com.qkyd.health.domain.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Single vital-sign sample payload shared by mock uploads and the simulator.
 */
public class VitalSignData implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("heart_rate")
    private Integer heartRate;

    @JsonProperty("blood_pressure")
    private String bloodPressure;

    @JsonProperty("steps")
    private Integer steps;

    @JsonProperty("spo2")
    private Integer spo2;

    @JsonProperty("temperature")
    private Double temperature;

    @JsonProperty("battery_level")
    private Integer batteryLevel;

    @JsonProperty("signal_quality")
    private Integer signalQuality;

    @JsonProperty("device_status")
    private String deviceStatus;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("location")
    private String location;

    @JsonProperty("location_type")
    private String locationType;

    @JsonProperty("metric_type")
    private String metricType;

    @JsonProperty("abnormal_type")
    private String abnormalType;

    @JsonProperty("abnormal_value")
    private String abnormalValue;

    @JsonProperty("normal_range")
    private String normalRange;

    @JsonProperty("risk_level")
    private String riskLevel;

    @JsonProperty("risk_score")
    private Integer riskScore;

    @JsonProperty("message")
    private String message;

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

    public Integer getSpo2() {
        return spo2;
    }

    public void setSpo2(Integer spo2) {
        this.spo2 = spo2;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public Integer getSignalQuality() {
        return signalQuality;
    }

    public void setSignalQuality(Integer signalQuality) {
        this.signalQuality = signalQuality;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public String getAbnormalType() {
        return abnormalType;
    }

    public void setAbnormalType(String abnormalType) {
        this.abnormalType = abnormalType;
    }

    public String getAbnormalValue() {
        return abnormalValue;
    }

    public void setAbnormalValue(String abnormalValue) {
        this.abnormalValue = abnormalValue;
    }

    public String getNormalRange() {
        return normalRange;
    }

    public void setNormalRange(String normalRange) {
        this.normalRange = normalRange;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
                ", spo2=" + spo2 +
                ", temperature=" + temperature +
                ", batteryLevel=" + batteryLevel +
                ", signalQuality=" + signalQuality +
                ", deviceStatus='" + deviceStatus + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", location='" + location + '\'' +
                ", metricType='" + metricType + '\'' +
                ", abnormalType='" + abnormalType + '\'' +
                ", abnormalValue='" + abnormalValue + '\'' +
                ", riskLevel='" + riskLevel + '\'' +
                ", riskScore=" + riskScore +
                ", timestamp=" + timestamp +
                '}';
    }
}
