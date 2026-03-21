package com.qkyd.health.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.qkyd.common.annotation.Excel;
import com.qkyd.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 健康分析报告对象 health_report
 *
 * @author qkyd
 * @date 2026-02-02
 */
public class HealthReport extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 服务对象ID */
    @Excel(name = "服务对象ID")
    private Long userId;

    /** 报告类型(daily/weekly/monthly) */
    @Excel(name = "报告类型", readConverterExp = "daily=日报,weekly=周报,monthly=月报")
    private String reportType;

    /** 报告日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "报告日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date reportDate;

    /** 报告标题 */
    @Excel(name = "报告标题")
    private String reportTitle;

    /** 健康摘要 */
    @Excel(name = "健康摘要")
    private String summary;

    /** 风险等级 */
    @Excel(name = "风险等级")
    private String riskLevel;

    /** 异常次数 */
    @Excel(name = "异常次数")
    private Integer abnormalCount;

    /** 平均心率 */
    @Excel(name = "平均心率")
    private Integer avgHeartRate;

    /** 平均血压 */
    @Excel(name = "平均血压")
    private String avgBloodPressure;

    /** 总步数 */
    @Excel(name = "总步数")
    private Integer totalSteps;

    /** PDF文件URL */
    @Excel(name = "PDF文件URL")
    private String fileUrl;

    /** 生成人ID */
    private Long createdBy;

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

    public void setReportType(String reportType)
    {
        this.reportType = reportType;
    }

    public String getReportType()
    {
        return reportType;
    }

    public void setReportDate(Date reportDate)
    {
        this.reportDate = reportDate;
    }

    public Date getReportDate()
    {
        return reportDate;
    }

    public void setReportTitle(String reportTitle)
    {
        this.reportTitle = reportTitle;
    }

    public String getReportTitle()
    {
        return reportTitle;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setRiskLevel(String riskLevel)
    {
        this.riskLevel = riskLevel;
    }

    public String getRiskLevel()
    {
        return riskLevel;
    }

    public void setAbnormalCount(Integer abnormalCount)
    {
        this.abnormalCount = abnormalCount;
    }

    public Integer getAbnormalCount()
    {
        return abnormalCount;
    }

    public void setAvgHeartRate(Integer avgHeartRate)
    {
        this.avgHeartRate = avgHeartRate;
    }

    public Integer getAvgHeartRate()
    {
        return avgHeartRate;
    }

    public void setAvgBloodPressure(String avgBloodPressure)
    {
        this.avgBloodPressure = avgBloodPressure;
    }

    public String getAvgBloodPressure()
    {
        return avgBloodPressure;
    }

    public void setTotalSteps(Integer totalSteps)
    {
        this.totalSteps = totalSteps;
    }

    public Integer getTotalSteps()
    {
        return totalSteps;
    }

    public void setFileUrl(String fileUrl)
    {
        this.fileUrl = fileUrl;
    }

    public String getFileUrl()
    {
        return fileUrl;
    }

    public void setCreatedBy(Long createdBy)
    {
        this.createdBy = createdBy;
    }

    public Long getCreatedBy()
    {
        return createdBy;
    }

    @Override
    public String toString() {
        return "HealthReport{" +
                "id=" + id +
                ", userId=" + userId +
                ", reportType='" + reportType + '\'' +
                ", reportDate=" + reportDate +
                ", reportTitle='" + reportTitle + '\'' +
                ", summary='" + summary + '\'' +
                ", riskLevel='" + riskLevel + '\'' +
                ", abnormalCount=" + abnormalCount +
                ", avgHeartRate=" + avgHeartRate +
                ", avgBloodPressure='" + avgBloodPressure + '\'' +
                ", totalSteps=" + totalSteps +
                ", fileUrl='" + fileUrl + '\'' +
                ", createdBy=" + createdBy +
                '}';
    }
}
