package com.ueit.health.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ueit.common.annotation.Excel;
import com.ueit.common.core.domain.BaseEntity;

/**
 * 围栏对象 ueit_fence
 *
 * @author z
 * @date 2024-01-23
 */
public class UeitFence extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 用户id */
    @Excel(name = "用户id")
    private Long userId;

    /** 围栏名称 */
    @Excel(name = "围栏名称")
    private String name;

    /** 围栏类型 */
    @Excel(name = "围栏类型")
    private String fenceType;

    /** 围栏详情 */
    @Excel(name = "围栏详情")
    private String detail;

    /** 半径 */
    @Excel(name = "半径")
    private String radius;

    /** 报警类型：1进入；2离开；3进入&离开 */
    @Excel(name = "报警类型：1进入；2离开；3进入&离开")
    private String warnType;

    /** 围栏状态：1生效；2失效 */
    @Excel(name = "围栏状态：1生效；2失效")
    private String status;

    /** 经度 */
    @Excel(name = "经度")
    private String longitude;

    /** 纬度 */
    @Excel(name = "纬度")
    private String latitude;

    /** 围栏形状 */
    @Excel(name = "围栏形状")
    private String shape;

    /** 报警等级：1红；2橙；3黄 */
    @Excel(name = "报警等级：1红；2橙；3黄")
    private String level;

    /** 开始报警时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "开始报警时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date beginReadTime;

    /** 结束报警时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "结束报警时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endReadTime;

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
    public void setFenceType(String fenceType)
    {
        this.fenceType = fenceType;
    }

    public String getFenceType()
    {
        return fenceType;
    }
    public void setDetail(String detail)
    {
        this.detail = detail;
    }

    public String getDetail()
    {
        return detail;
    }
    public void setRadius(String radius)
    {
        this.radius = radius;
    }

    public String getRadius()
    {
        return radius;
    }
    public void setWarnType(String warnType)
    {
        this.warnType = warnType;
    }

    public String getWarnType()
    {
        return warnType;
    }
    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }
    public void setLongitude(String longitude)
    {
        this.longitude = longitude;
    }

    public String getLongitude()
    {
        return longitude;
    }
    public void setLatitude(String latitude)
    {
        this.latitude = latitude;
    }

    public String getLatitude()
    {
        return latitude;
    }
    public void setShape(String shape)
    {
        this.shape = shape;
    }

    public String getShape()
    {
        return shape;
    }
    public void setLevel(String level)
    {
        this.level = level;
    }

    public String getLevel()
    {
        return level;
    }
    public void setBeginReadTime(Date beginReadTime)
    {
        this.beginReadTime = beginReadTime;
    }

    public Date getBeginReadTime()
    {
        return beginReadTime;
    }
    public void setEndReadTime(Date endReadTime)
    {
        this.endReadTime = endReadTime;
    }

    public Date getEndReadTime()
    {
        return endReadTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("userId", getUserId())
                .append("name", getName())
                .append("fenceType", getFenceType())
                .append("detail", getDetail())
                .append("radius", getRadius())
                .append("warnType", getWarnType())
                .append("status", getStatus())
                .append("longitude", getLongitude())
                .append("latitude", getLatitude())
                .append("createTime", getCreateTime())
                .append("shape", getShape())
                .append("level", getLevel())
                .append("beginReadTime", getBeginReadTime())
                .append("endReadTime", getEndReadTime())
                .toString();
    }
}
