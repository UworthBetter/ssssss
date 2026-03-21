package com.qkyd.health.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.qkyd.common.annotation.Excel;
import com.qkyd.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 设备绑定关系对象 health_device_binding
 *
 * @author qkyd
 * @date 2026-02-02
 */
public class HealthDeviceBinding extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 服务对象ID */
    @Excel(name = "服务对象ID")
    private Long subjectId;

    /** 设备ID */
    @Excel(name = "设备ID")
    private Long deviceId;

    /** 绑定时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "绑定时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date bindTime;

    /** 绑定状态(1正常 0解绑) */
    @Excel(name = "绑定状态", readConverterExp = "1=正常,0=解绑")
    private String status;

    /** 绑定人 */
    @Excel(name = "绑定人")
    private String bindBy;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setSubjectId(Long subjectId)
    {
        this.subjectId = subjectId;
    }

    public Long getSubjectId()
    {
        return subjectId;
    }

    public void setDeviceId(Long deviceId)
    {
        this.deviceId = deviceId;
    }

    public Long getDeviceId()
    {
        return deviceId;
    }

    public void setBindTime(Date bindTime)
    {
        this.bindTime = bindTime;
    }

    public Date getBindTime()
    {
        return bindTime;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public void setBindBy(String bindBy)
    {
        this.bindBy = bindBy;
    }

    public String getBindBy()
    {
        return bindBy;
    }

    @Override
    public String toString() {
        return "HealthDeviceBinding{" +
                "id=" + id +
                ", subjectId=" + subjectId +
                ", deviceId=" + deviceId +
                ", bindTime=" + bindTime +
                ", status='" + status + '\'' +
                ", bindBy='" + bindBy + '\'' +
                '}';
    }
}
