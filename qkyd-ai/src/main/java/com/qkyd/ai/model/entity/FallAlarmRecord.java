package com.qkyd.ai.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Fall Alarm Record Entity
 * corresponds to table ai_fall_alarm_record
 */
public class FallAlarmRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** User ID */
    private Long userId;

    /** Original Alarm ID */
    private Long originalAlarmId;

    /** Is Valid Alarm */
    private Boolean isValid;

    /** Validation Reason */
    private String validationReason;

    /** Acceleration Peak */
    private BigDecimal accelerationPeak;

    /** Has Removal Alert in 1 hour */
    private Boolean hasRemovalAlert;

    /** Recent Steps in 1 hour */
    private Integer recentSteps;

    /** Validation Time */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date validationTime;

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

    public Long getOriginalAlarmId() {
        return originalAlarmId;
    }

    public void setOriginalAlarmId(Long originalAlarmId) {
        this.originalAlarmId = originalAlarmId;
    }

    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }

    public String getValidationReason() {
        return validationReason;
    }

    public void setValidationReason(String validationReason) {
        this.validationReason = validationReason;
    }

    public BigDecimal getAccelerationPeak() {
        return accelerationPeak;
    }

    public void setAccelerationPeak(BigDecimal accelerationPeak) {
        this.accelerationPeak = accelerationPeak;
    }

    public Boolean getHasRemovalAlert() {
        return hasRemovalAlert;
    }

    public void setHasRemovalAlert(Boolean hasRemovalAlert) {
        this.hasRemovalAlert = hasRemovalAlert;
    }

    public Integer getRecentSteps() {
        return recentSteps;
    }

    public void setRecentSteps(Integer recentSteps) {
        this.recentSteps = recentSteps;
    }

    public Date getValidationTime() {
        return validationTime;
    }

    public void setValidationTime(Date validationTime) {
        this.validationTime = validationTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
