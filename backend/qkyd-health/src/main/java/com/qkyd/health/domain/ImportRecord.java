package com.qkyd.health.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

/**
 * 导入记录对象 import_record
 *
 * @author qkyd
 * @date 2026-02-06
 */
public class ImportRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 导入记录ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 导入类型 */
    private String importType;

    /** 文件名 */
    private String fileName;

    /** 总行数 */
    private Integer totalRows;

    /** 成功行数 */
    private Integer successRows;

    /** 失败行数 */
    private Integer failRows;

    /** 状态（0待处理 1处理中 2成功 3失败） */
    private String status;

    /** 错误信息 */
    private String errorMsg;

    /** 导入时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date importTime;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getImportType() {
        return importType;
    }

    public void setImportType(String importType) {
        this.importType = importType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Integer totalRows) {
        this.totalRows = totalRows;
    }

    public Integer getSuccessRows() {
        return successRows;
    }

    public void setSuccessRows(Integer successRows) {
        this.successRows = successRows;
    }

    public Integer getFailRows() {
        return failRows;
    }

    public void setFailRows(Integer failRows) {
        this.failRows = failRows;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Date getImportTime() {
        return importTime;
    }

    public void setImportTime(Date importTime) {
        this.importTime = importTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "ImportRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", importType='" + importType + '\'' +
                ", fileName='" + fileName + '\'' +
                ", totalRows=" + totalRows +
                ", successRows=" + successRows +
                ", failRows=" + failRows +
                ", status='" + status + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                ", importTime=" + importTime +
                ", createTime=" + createTime +
                '}';
    }
}
