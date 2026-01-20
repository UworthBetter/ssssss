package com.ueit.health.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ueit.common.annotation.Excel;
import com.ueit.common.core.domain.BaseEntity;

/**
 * 璁惧淇℃伅瀵硅薄 ueit_device_info
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

    /** 鐢ㄦ埛ID */
    @Excel(name = "鐢ㄦ埛ID")
    private Long userId;

    /** 璁惧鍚嶇О */
    @Excel(name = "璁惧鍚嶇О")
    private String name;

    /** IMEI淇℃伅 */
    @Excel(name = "IMEI淇℃伅")
    private String imei;

    /** 璁惧鍨嬪彿 */
    @Excel(name = "璁惧鍨嬪彿")
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
