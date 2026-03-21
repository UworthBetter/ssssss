package com.qkyd.health.domain;

import com.qkyd.common.annotation.Excel;
import com.qkyd.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 服务对象对象 health_subject
 * 
 * @author qkyd
 * @date 2026-02-01
 */
public class HealthSubject extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 服务对象ID */
    private Long subjectId;

    /** 归属分组ID */
    @Excel(name = "归属分组ID")
    private Long deptId;

    /** 服务对象名称 */
    @Excel(name = "服务对象名称")
    private String subjectName;

    /** 姓名 */
    @Excel(name = "姓名")
    private String nickName;

    /** 用户类型（00系统用户） */
    private String userType;

    /** 用户邮箱 */
    @Excel(name = "用户邮箱")
    private String email;

    /** 手机号码 */
    @Excel(name = "手机号码")
    private String phonenumber;

    /** 年龄 */
    @Excel(name = "年龄")
    private Integer age;

    /** 用户性别（0男 1女 2未知） */
    @Excel(name = "用户性别", readConverterExp = "0=男,1=女,2=未知")
    private String sex;

    /** 头像地址 */
    private String avatar;

    /** 密码 */
    private String password;

    /** 帐号状态（0正常 1停用） */
    @Excel(name = "帐号状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 最后登录IP */
    @Excel(name = "最后登录IP")
    private String loginIp;

    /** 最后登录时间 */
    @Excel(name = "最后登录时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date loginDate;

    /** 备注 */
    @Excel(name = "备注")
    private String remark;

    /** 部门对象 */
    private com.qkyd.common.core.domain.entity.SysDept dept;
    
    /** 部门名称 */
    private String deptName;

    public void setSubjectId(Long subjectId)
    {
        this.subjectId = subjectId;
    }

    public Long getSubjectId()
    {
        return subjectId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setSubjectName(String subjectName)
    {
        this.subjectName = subjectName;
    }

    public String getSubjectName()
    {
        return subjectName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    public String getNickName()
    {
        return nickName;
    }

    public void setUserType(String userType)
    {
        this.userType = userType;
    }

    public String getUserType()
    {
        return userType;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getEmail()
    {
        return email;
    }

    public void setPhonenumber(String phonenumber)
    {
        this.phonenumber = phonenumber;
    }

    public String getPhonenumber()
    {
        return phonenumber;
    }

    public void setAge(Integer age)
    {
        this.age = age;
    }

    public Integer getAge()
    {
        return age;
    }

    public void setSex(String sex)
    {
        this.sex = sex;
    }

    public String getSex()
    {
        return sex;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getPassword()
    {
        return password;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setLoginIp(String loginIp)
    {
        this.loginIp = loginIp;
    }

    public String getLoginIp()
    {
        return loginIp;
    }

    public void setLoginDate(java.util.Date loginDate)
    {
        this.loginDate = loginDate;
    }

    public java.util.Date getLoginDate()
    {
        return loginDate;
    }

    public com.qkyd.common.core.domain.entity.SysDept getDept()
    {
        return dept;
    }

    public void setDept(com.qkyd.common.core.domain.entity.SysDept dept)
    {
        this.dept = dept;
    }
    
    public String getDeptName()
    {
        return deptName;
    }
    
    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("subjectId", getSubjectId())
            .append("deptId", getDeptId())
            .append("subjectName", getSubjectName())
            .append("nickName", getNickName())
            .append("userType", getUserType())
            .append("email", getEmail())
            .append("phonenumber", getPhonenumber())
            .append("age", getAge())
            .append("sex", getSex())
            .append("avatar", getAvatar())
            .append("password", getPassword())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("loginIp", getLoginIp())
            .append("loginDate", getLoginDate())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
