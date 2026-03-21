package com.qkyd.health.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.qkyd.common.annotation.Excel;
import com.qkyd.common.core.domain.BaseEntity;

/**
 * 步数对象 ueit_steps
 *
 * @author z
 * @date 2024-01-05
 */
public class UeitSteps extends BaseEntity
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

    /** 日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date date;

    /** 值 */
    @Excel(name = "值")
    private Integer value;

    /** 卡路里 */
    @Excel(name = "卡路里")
    private Long calories;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date dateTime;
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
    public void setDate(Date date)
    {
        this.date = date;
    }

    public Date getDate()
    {
        return date;
    }
    public void setValue(Integer value)
    {
        this.value = value;
    }

    public Integer getValue()
    {
        return value;
    }
    public void setCalories(Long calories)
    {
        this.calories = calories;
    }

    public Long getCalories()
    {
        return calories;
    }
    public void setDateTime(Date dateTime)
    {
        this.dateTime = dateTime;
    }

    public Date getDateTime()
    {
        return dateTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("deviceId", getDeviceId())
            .append("date", getDate())
            .append("value", getValue())
            .append("calories", getCalories())
            .append("dateTime", getDateTime())
            .toString();
    }
}

