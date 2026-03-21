package com.qkyd.health.domain;

import com.qkyd.common.annotation.Excel;
import com.qkyd.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 服务对象导入日志对象 health_subject_import_log
 *
 * @author qkyd
 * @date 2026-02-06
 */
public class HealthSubjectImportLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 操作用户ID */
    @Excel(name = "操作用户ID")
    private Long userId;

    /** 操作用户名 */
    @Excel(name = "操作用户名")
    private String userName;

    /** 导入类型（EXCEL/CSV） */
    @Excel(name = "导入类型", readConverterExp = "EXCEL=Excel,CSV=CSV")
    private String importType;

    /** 导入文件名 */
    @Excel(name = "导入文件名")
    private String fileName;

    /** 文件路径 */
    private String filePath;

    /** 总记录数 */
    @Excel(name = "总记录数")
    private Integer totalRows;

    /** 成功导入记录数 */
    @Excel(name = "成功导入记录数")
    private Integer successRows;

    /** 失败记录数 */
    @Excel(name = "失败记录数")
    private Integer failRows;

    /** 导入状态（PENDING:待处理, PROCESSING:处理中, SUCCESS:成功, FAILED:失败, PARTIAL:部分成功） */
    @Excel(name = "导入状态", readConverterExp = "PENDING=待处理,PROCESSING=处理中,SUCCESS=成功,FAILED=失败,PARTIAL=部分成功")
    private String status;

    /** 导入开始时间 */
    @Excel(name = "导入开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date startTime;

    /** 导入结束时间 */
    @Excel(name = "导入结束时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date endTime;

    /** 耗时（毫秒） */
    @Excel(name = "耗时(毫秒)")
    private Integer costTime;

    /** 错误信息 */
    private String errorMsg;

    /** 备注 */
    @Excel(name = "备注")
    private String remark;

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

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setImportType(String importType)
    {
        this.importType = importType;
    }

    public String getImportType()
    {
        return importType;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public void setTotalRows(Integer totalRows)
    {
        this.totalRows = totalRows;
    }

    public Integer getTotalRows()
    {
        return totalRows;
    }

    public void setSuccessRows(Integer successRows)
    {
        this.successRows = successRows;
    }

    public Integer getSuccessRows()
    {
        return successRows;
    }

    public void setFailRows(Integer failRows)
    {
        this.failRows = failRows;
    }

    public Integer getFailRows()
    {
        return failRows;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStartTime(java.util.Date startTime)
    {
        this.startTime = startTime;
    }

    public java.util.Date getStartTime()
    {
        return startTime;
    }

    public void setEndTime(java.util.Date endTime)
    {
        this.endTime = endTime;
    }

    public java.util.Date getEndTime()
    {
        return endTime;
    }

    public void setCostTime(Integer costTime)
    {
        this.costTime = costTime;
    }

    public Integer getCostTime()
    {
        return costTime;
    }

    public void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg()
    {
        return errorMsg;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public String getRemark()
    {
        return remark;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("userName", getUserName())
            .append("importType", getImportType())
            .append("fileName", getFileName())
            .append("filePath", getFilePath())
            .append("totalRows", getTotalRows())
            .append("successRows", getSuccessRows())
            .append("failRows", getFailRows())
            .append("status", getStatus())
            .append("startTime", getStartTime())
            .append("endTime", getEndTime())
            .append("costTime", getCostTime())
            .append("errorMsg", getErrorMsg())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
