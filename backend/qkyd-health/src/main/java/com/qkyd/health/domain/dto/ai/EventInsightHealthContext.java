package com.qkyd.health.domain.dto.ai;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * Aggregated health-domain context for event insight enrichment.
 */
public class EventInsightHealthContext implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long requestedDeviceId;
    private Integer age;
    private String subjectName;
    private String subjectStatus;
    private String lastKnownLocation;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLocationTime;

    private Boolean hasActiveBinding;
    private Long boundDeviceId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date bindingTime;

    private Integer historicalAbnormalCount;
    private Integer recentSameTypeCount;
    private String latestReportSummary;
    private String latestReportRiskLevel;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date latestReportDate;

    private String deviceSignalStatus;
    private String deviceSignalReason;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRequestedDeviceId() {
        return requestedDeviceId;
    }

    public void setRequestedDeviceId(Long requestedDeviceId) {
        this.requestedDeviceId = requestedDeviceId;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectStatus() {
        return subjectStatus;
    }

    public void setSubjectStatus(String subjectStatus) {
        this.subjectStatus = subjectStatus;
    }

    public String getLastKnownLocation() {
        return lastKnownLocation;
    }

    public void setLastKnownLocation(String lastKnownLocation) {
        this.lastKnownLocation = lastKnownLocation;
    }

    public Date getLastLocationTime() {
        return lastLocationTime;
    }

    public void setLastLocationTime(Date lastLocationTime) {
        this.lastLocationTime = lastLocationTime;
    }

    public Boolean getHasActiveBinding() {
        return hasActiveBinding;
    }

    public void setHasActiveBinding(Boolean hasActiveBinding) {
        this.hasActiveBinding = hasActiveBinding;
    }

    public Long getBoundDeviceId() {
        return boundDeviceId;
    }

    public void setBoundDeviceId(Long boundDeviceId) {
        this.boundDeviceId = boundDeviceId;
    }

    public Date getBindingTime() {
        return bindingTime;
    }

    public void setBindingTime(Date bindingTime) {
        this.bindingTime = bindingTime;
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

    public String getLatestReportSummary() {
        return latestReportSummary;
    }

    public void setLatestReportSummary(String latestReportSummary) {
        this.latestReportSummary = latestReportSummary;
    }

    public String getLatestReportRiskLevel() {
        return latestReportRiskLevel;
    }

    public void setLatestReportRiskLevel(String latestReportRiskLevel) {
        this.latestReportRiskLevel = latestReportRiskLevel;
    }

    public Date getLatestReportDate() {
        return latestReportDate;
    }

    public void setLatestReportDate(Date latestReportDate) {
        this.latestReportDate = latestReportDate;
    }

    public String getDeviceSignalStatus() {
        return deviceSignalStatus;
    }

    public void setDeviceSignalStatus(String deviceSignalStatus) {
        this.deviceSignalStatus = deviceSignalStatus;
    }

    public String getDeviceSignalReason() {
        return deviceSignalReason;
    }

    public void setDeviceSignalReason(String deviceSignalReason) {
        this.deviceSignalReason = deviceSignalReason;
    }
}
