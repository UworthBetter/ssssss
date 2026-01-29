package com.ueit.health.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ueit.common.annotation.Excel;
import com.ueit.common.core.domain.BaseEntity;

/**
 * 定位数据对象 ueit_location
 *
 * @author z
 * @date 2024-01-05
 */
public class UeitLocation extends BaseEntity
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

    /** 精度 */
    @Excel(name = "精度")
    private Long accuracy;

    /** 高度 */
    @Excel(name = "高度")
    private Long altitude;

    /** 纬度 */
    @Excel(name = "纬度")
    private String latitude;

    /** 经度 */
    @Excel(name = "经度")
    private String longitude;

    /** 详细地址 */
    @Excel(name = "详细地址")
    private String location;

    /** 读取时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "读取时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date readTime;

    /** 定位方式 */
    @Excel(name = "定位方式")
    private String type;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date readTime1;

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
    public void setAccuracy(Long accuracy)
    {
        this.accuracy = accuracy;
    }

    public Long getAccuracy()
    {
        return accuracy;
    }
    public void setAltitude(Long altitude)
    {
        this.altitude = altitude;
    }

    public Long getAltitude()
    {
        return altitude;
    }
    public void setLatitude(String latitude)
    {
        this.latitude = latitude;
    }

    public String getLatitude()
    {
        return latitude;
    }
    public void setLongitude(String longitude)
    {
        this.longitude = longitude;
    }

    public String getLongitude()
    {
        return longitude;
    }
    public void setLocation(String location)
    {
        this.location = location;
    }

    public String getLocation()
    {
        return location;
    }
    public void setReadTime(Date readTime)
    {
        this.readTime = readTime;
    }

    public Date getReadTime()
    {
        return readTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("deviceId", getDeviceId())
            .append("accuracy", getAccuracy())
            .append("altitude", getAltitude())
            .append("latitude", getLatitude())
            .append("longitude", getLongitude())
            .append("location", getLocation())
            .append("createTime", getCreateTime())
            .append("readTime", getReadTime())
            .append("type", getType())
            .toString();
    }
}

