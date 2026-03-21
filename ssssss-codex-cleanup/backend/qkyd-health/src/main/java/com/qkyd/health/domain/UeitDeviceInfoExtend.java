package com.qkyd.health.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.qkyd.common.annotation.Excel;
import com.qkyd.common.core.domain.BaseEntity;

/**
 * 设备信息扩展对象 ueit_device_info_extend
 *
 * @author douwq
 * @date 2024-01-03
 */
public class UeitDeviceInfoExtend extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long deviceId;

    /**
     * 最后通讯时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后通讯时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastCommunicationTime;
    /**
     * 电量
     */
    @Excel(name = "电量")
    private Integer batteryLevel;

    /**
     * 步数
     */
    @Excel(name = "步数")
    private Integer step;

    /**
     * 最近告警内容
     */
    @Excel(name = "最近告警内容")
    private String alarmContent;

    /**
     * 最近告警时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最近告警时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date alarmTime;

    /**
     * 体温
     */
    @Excel(name = "体温")
    private Float temp;

    /**
     * 体温测量时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "体温测量时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date tempTime;

    /**
     * 心率
     */
    @Excel(name = "心率")
    private Float heartRate;

    /**
     * 心率测量时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "心率测量时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date heartRateTime;

    /**
     * 血压（舒张压）
     */
    @Excel(name = "血压", readConverterExp = "舒=张压")
    private Integer bloodDiastolic;

    /**
     * 血压（收缩压）
     */
    @Excel(name = "血压", readConverterExp = "收=缩压")
    private Integer bloodSystolic;

    /**
     * 血压测量时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "血压测量时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date bloodTime;

    /**
     * 血氧
     */
    @Excel(name = "血氧")
    private Float spo2;

    /**
     * 血氧测量时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "血氧测量时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date spo2Time;

    /**
     * 经度
     */
    @Excel(name = "经度")
    private BigDecimal longitude;

    /**
     * 纬度
     */
    @Excel(name = "纬度")
    private BigDecimal latitude;

    /**
     * 详细地址
     */
    @Excel(name = "详细地址")
    private String location;

    /**
     * 定位方式
     */
    @Excel(name = "定位方式")
    private String type;

    /**
     * 定位时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "定位时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date positioningTime;
    /**
     * 昵称
     */
    private String nickName;
    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public Integer getStep() {
        return step;
    }

    public void setAlarmContent(String alarmContent) {
        this.alarmContent = alarmContent;
    }

    public String getAlarmContent() {
        return alarmContent;
    }

    public void setAlarmTime(Date alarmTime) {
        this.alarmTime = alarmTime;
    }

    public Date getAlarmTime() {
        return alarmTime;
    }

    public void setTemp(Float temp) {
        this.temp = temp;
    }

    public Float getTemp() {
        return temp;
    }

    public void setTempTime(Date tempTime) {
        this.tempTime = tempTime;
    }

    public Date getTempTime() {
        return tempTime;
    }

    public void setHeartRate(Float heartRate) {
        this.heartRate = heartRate;
    }

    public Float getHeartRate() {
        return heartRate;
    }

    public void setHeartRateTime(Date heartRateTime) {
        this.heartRateTime = heartRateTime;
    }

    public Date getHeartRateTime() {
        return heartRateTime;
    }

    public void setBloodDiastolic(Integer bloodDiastolic) {
        this.bloodDiastolic = bloodDiastolic;
    }

    public Integer getBloodDiastolic() {
        return bloodDiastolic;
    }

    public void setBloodSystolic(Integer bloodSystolic) {
        this.bloodSystolic = bloodSystolic;
    }

    public Integer getBloodSystolic() {
        return bloodSystolic;
    }

    public void setBloodTime(Date bloodTime) {
        this.bloodTime = bloodTime;
    }

    public Date getBloodTime() {
        return bloodTime;
    }

    public void setSpo2(Float spo2) {
        this.spo2 = spo2;
    }

    public Float getSpo2() {
        return spo2;
    }

    public void setSpo2Time(Date spo2Time) {
        this.spo2Time = spo2Time;
    }

    public Date getSpo2Time() {
        return spo2Time;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public Date getLastCommunicationTime() {
        return lastCommunicationTime;
    }

    public void setLastCommunicationTime(Date lastCommunicationTime) {
        this.lastCommunicationTime = lastCommunicationTime;
    }

    public Date getPositioningTime() {
        return positioningTime;
    }

    public void setPositioningTime(Date positioningTime) {
        this.positioningTime = positioningTime;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("deviceId", getDeviceId())
                .append("lastCommunicationTime", getUpdateTime())
                .append("positioningTime", getUpdateTime())
                .append("batteryLevel", getBatteryLevel())
                .append("step", getStep())
                .append("alarmContent", getAlarmContent())
                .append("alarmTime", getAlarmTime())
                .append("temp", getTemp())
                .append("tempTime", getTempTime())
                .append("heartRate", getHeartRate())
                .append("heartRateTime", getHeartRateTime())
                .append("bloodDiastolic", getBloodDiastolic())
                .append("bloodSystolic", getBloodSystolic())
                .append("bloodTime", getBloodTime())
                .append("spo2", getSpo2())
                .append("spo2Time", getSpo2Time())
                .append("longitude", getLongitude())
                .append("latitude", getLatitude())
                .append("location", getLocation())
                .append("type", getType())
                .append("nickName", getNickName())
                .toString();
    }
}

