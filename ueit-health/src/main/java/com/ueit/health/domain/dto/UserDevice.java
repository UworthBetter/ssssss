package com.ueit.health.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ueit.common.annotation.Excel;

import java.util.Date;

/**
 * 用户设备列表页面需要的参数
 */
public class UserDevice {
    /**
     * 设备ID
     */
    private Long deviceId;
    /**
     * 用户昵称
     */
    @Excel(name = "用户昵称")
    private String nickName;
    /**
     * 用户ID
     */
    @Excel(name = "用户ID")
    private Long userId;
    /**
     * 手机号码
     */
    @Excel(name = "手机号")
    private String phone;
    /**
     * 电量
     */
    @Excel(name = "电量")
    private Integer batteryLevel;
    /**
     * 读取时间
     */
    @Excel(name = "读取时间", width = 22, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Excel.Type.EXPORT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date readTime;
    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * IMEI信息
     */
    private String imei;

    /**
     * 设备类型
     */
    @Excel(name = "设备类型")
    private String type;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getReadTime() {
        return readTime;
    }

    public void setReadTime(Date readTime) {
        this.readTime = readTime;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "UserDevice{" +
                "deviceId=" + deviceId +
                ", nickName='" + nickName + '\'' +
                ", userId=" + userId +
                ", phone='" + phone + '\'' +
                ", batteryLevel=" + batteryLevel +
                ", readTime=" + readTime +
                ", deviceName='" + deviceName + '\'' +
                ", imei='" + imei + '\'' +
                ", type='" + type + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
