package com.qkyd.health.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.qkyd.common.annotation.Excel;
import com.qkyd.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 心率数据对象 ueit_heart_rate
 *
 * @author z
 * @date 2024-01-05
 */
public class UeitHeartRate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 用户id */
    @Excel(name = "用户id")
    private Long userId;

    /** 设备id */
    @Excel(name = "设备id")
    private Long deviceId;

    /** 值 */
    @Excel(name = "值")
    private Float value;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date readTime;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }
    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
    }
    public void setDeviceId(Long deviceId)
    {
        this.deviceId = deviceId;
    }

    public Long getDeviceId()
    {
        return deviceId;
    }
    public void setValue(Float value)
    {
        this.value = value;
    }

    public Float getValue()
    {
        return value;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("deviceId", getDeviceId())
            .append("value", getValue())
            .append("createTime", getCreateTime())
            .toString();
    }
}

