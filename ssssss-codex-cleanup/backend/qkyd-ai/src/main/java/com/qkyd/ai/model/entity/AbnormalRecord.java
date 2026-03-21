package com.qkyd.ai.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

/**
 * Abnormal Record Entity
 * corresponds to table ai_abnormal_record
 */
public class AbnormalRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** User ID */
    private Long userId;

    /** Device ID */
    private String deviceId;

    /** Metric Type */
    private String metricType;

    /** Abnormal Value */
    private String abnormalValue;

    /** Normal Range */
    private String normalRange;

    /** Abnormal Type */
    private String abnormalType;

    /** Risk Level */
    private String riskLevel;

    /** Detection Method */
    private String detectionMethod;

    /** Detected Time */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date detectedTime;

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

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
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

    public String getAbnormalType() {
        return abnormalType;
    }

    public void setAbnormalType(String abnormalType) {
        this.abnormalType = abnormalType;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getDetectionMethod() {
        return detectionMethod;
    }

    public void setDetectionMethod(String detectionMethod) {
        this.detectionMethod = detectionMethod;
    }

    public Date getDetectedTime() {
        return detectedTime;
    }

    public void setDetectedTime(Date detectedTime) {
        this.detectedTime = detectedTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
