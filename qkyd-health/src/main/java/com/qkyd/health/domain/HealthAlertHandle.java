package com.qkyd.health.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.qkyd.common.annotation.Excel;
import com.qkyd.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 告警处理记录对象 health_alert_handle
 *
 * @author qkyd
 * @date 2026-02-02
 */
public class HealthAlertHandle extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 关联告警ID */
    @Excel(name = "关联告警ID")
    private Long alertId;

    /** 告警类型(abnormal/fall/device) */
    @Excel(name = "告警类型", readConverterExp = "abnormal=异常,fall=跌倒,device=设备")
    private String alertType;

    /** 处理人ID */
    @Excel(name = "处理人ID")
    private Long handlerId;

    /** 处理人姓名 */
    @Excel(name = "处理人姓名")
    private String handlerName;

    /** 处理动作(confirm/ignore/forward) */
    @Excel(name = "处理动作", readConverterExp = "confirm=确认,ignore=忽略,forward=转发")
    private String handleAction;

    /** 处理结果说明 */
    @Excel(name = "处理结果说明")
    private String handleResult;

    /** 处理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "处理时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date handleTime;

    /** 后续跟进措施 */
    @Excel(name = "后续跟进措施")
    private String nextAction;

    /** 状态(1有效 0撤销) */
    @Excel(name = "状态", readConverterExp = "1=有效,0=撤销")
    private String status;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setAlertId(Long alertId)
    {
        this.alertId = alertId;
    }

    public Long getAlertId()
    {
        return alertId;
    }

    public void setAlertType(String alertType)
    {
        this.alertType = alertType;
    }

    public String getAlertType()
    {
        return alertType;
    }

    public void setHandlerId(Long handlerId)
    {
        this.handlerId = handlerId;
    }

    public Long getHandlerId()
    {
        return handlerId;
    }

    public void setHandlerName(String handlerName)
    {
        this.handlerName = handlerName;
    }

    public String getHandlerName()
    {
        return handlerName;
    }

    public void setHandleAction(String handleAction)
    {
        this.handleAction = handleAction;
    }

    public String getHandleAction()
    {
        return handleAction;
    }

    public void setHandleResult(String handleResult)
    {
        this.handleResult = handleResult;
    }

    public String getHandleResult()
    {
        return handleResult;
    }

    public void setHandleTime(Date handleTime)
    {
        this.handleTime = handleTime;
    }

    public Date getHandleTime()
    {
        return handleTime;
    }

    public void setNextAction(String nextAction)
    {
        this.nextAction = nextAction;
    }

    public String getNextAction()
    {
        return nextAction;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    @Override
    public String toString() {
        return "HealthAlertHandle{" +
                "id=" + id +
                ", alertId=" + alertId +
                ", alertType='" + alertType + '\'' +
                ", handlerId=" + handlerId +
                ", handlerName='" + handlerName + '\'' +
                ", handleAction='" + handleAction + '\'' +
                ", handleResult='" + handleResult + '\'' +
                ", handleTime=" + handleTime +
                ", nextAction='" + nextAction + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
