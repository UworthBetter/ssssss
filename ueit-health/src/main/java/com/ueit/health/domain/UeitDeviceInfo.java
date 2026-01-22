package com.ueit.health.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ueit.common.annotation.Excel;
import com.ueit.common.core.domain.BaseEntity;

/**
 * 设备信息对象 ueit_device_info
 *
 * @author douwq
 * @date 2023-12-22
 */
public class UeitDeviceInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;
    private Long deviceId;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 设备名称 */
    @Excel(name = "设备名称")
    private String name;

    /** IMEI信息 */
    @Excel(name = "IMEI信息")
    private String imei;

    /** 设备型号 */
    @Excel(name = "设备型号")
    private String type;

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
    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
    public void setImei(String imei)
    {
        this.imei = imei;
    }

    public String getImei()
    {
        return imei;
    }
    public void setType(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return type;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("name", getName())
            .append("imei", getImei())
            .append("type", getType())
            .append("createTime", getCreateTime())
            .toString();
    }
}

