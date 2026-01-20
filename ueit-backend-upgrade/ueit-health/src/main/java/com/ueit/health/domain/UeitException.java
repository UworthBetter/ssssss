package com.ueit.health.domain;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ueit.common.annotation.Excel;
import com.ueit.common.core.domain.BaseEntity;

/**
 * 异常数据对象 ueit_exception
 *
 * @author z
 * @date 2024-01-05
 */
public class UeitException
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 异常人员ID */
    @Excel(name = "异常人员ID")
    private Long userId;
    private Long userIdWho;

    /** 异常来源设备ID */
    @Excel(name = "异常来源设备ID")
    private Long deviceId;

    /** 异常类型 */
    @Excel(name = "异常类型")
    private String type;

    /** 异常数值 */
    @Excel(name = "异常数值")
    private String value;

    /** 经度 */
    @Excel(name = "经度")
    private BigDecimal longitude;

    /** 纬度 */
    @Excel(name = "纬度")
    private BigDecimal latitude;

    /** 状态（0未处理、1已处理） */
    @Excel(name = "状态", readConverterExp = "0=未处理、1已处理")
    private String state;

    /** 处理说明 */
    @Excel(name = "处理说明")
    private String updateContent;

    /** 所在地址 */
    @Excel(name = "所在地址")
    private String location;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date readTime;
    private String nickName;
    private String sex;
    private String phone;
    private int age;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    private String updateBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTimeWho;
    private String updateByWho;
    public Date getUpdateTime() {
        return updateTime;
    }
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startCreateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endCreateTime;

    public Date getStartCreateTime() {
        return startCreateTime;
    }

    public void setStartCreateTime(Date startCreateTime) {
        this.startCreateTime = startCreateTime;
    }

    public Date getEndCreateTime() {
        return endCreateTime;
    }

    public void setEndCreateTime(Date endCreateTime) {
        this.endCreateTime = endCreateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

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
    public void setType(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return type;
    }
    public void setValue(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }
    public void setLongitude(BigDecimal longitude)
    {
        this.longitude = longitude;
    }

    public BigDecimal getLongitude()
    {
        return longitude;
    }
    public void setLatitude(BigDecimal latitude)
    {
        this.latitude = latitude;
    }

    public BigDecimal getLatitude()
    {
        return latitude;
    }
    public void setState(String state)
    {
        this.state = state;
    }

    public String getState()
    {
        return state;
    }
    public void setUpdateContent(String updateContent)
    {
        this.updateContent = updateContent;
    }

    public String getUpdateContent()
    {
        return updateContent;
    }
    public void setLocation(String location)
    {
        this.location = location;
    }

    public String getLocation()
    {
        return location;
    }

    public Date getReadTime() {
        return readTime;
    }

    public void setReadTime(Date readTime) {
        this.readTime = readTime;
    }

    public Date getUpdateTimeWho() {
        return updateTimeWho;
    }

    public void setUpdateTimeWho(Date updateTimeWho) {
        this.updateTimeWho = updateTimeWho;
    }

    public String getUpdateByWho() {
        return updateByWho;
    }

    public void setUpdateByWho(String updateByWho) {
        this.updateByWho = updateByWho;
    }

    public Long getUserIdWho() {
        return userIdWho;
    }

    public void setUserIdWho(Long userIdWho) {
        this.userIdWho = userIdWho;
    }

    @Override
    public String toString() {
        return "UeitException{" +
                "id=" + id +
                ", userId=" + userId +
                ", deviceId=" + deviceId +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", state='" + state + '\'' +
                ", updateContent='" + updateContent + '\'' +
                ", location='" + location + '\'' +
                ", readTime=" + readTime +
                ", nickName='" + nickName + '\'' +
                ", sex='" + sex + '\'' +
                ", phone='" + phone + '\'' +
                ", age=" + age +
                '}';
    }
}
