package com.qkyd.health.domain.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;

public class WatchDto {

    /**
     * 时间戳
     */
    private String time;

    /**
     * 校验值
     */
    private String hash;

    /**
     * 数据类型
     */
    private String action;

    /**
     * 设备唯一编码
     */
    private String imei;

    /**
     * 电量
     */
    private Integer volt;

    /**
     * 步数
     */
    private Integer step;

    /**
     * 翻转次数
     */
    private Integer reversal;

    /**
     * 体温、血氧值、心率值
     */
    private float number;

    /**
     * 舒张压
     */
    private Integer diastolic;

    /**
     * 收缩压
     */
    private Integer systolic;

    /**
     * 告警类型：3SOS；4低电量告警；8摘脱告警;9跌倒告警
     */
    private Integer data;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 详细地址
     */
    private String location;

    /**
     * 定位方式：1GPS；2基站；3WIFI
     */
    private String type;
    //	告警类型：3SOS；4低电量告警；8摘脱告警;9跌倒告警
    private Integer alarm;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public Integer getVolt() {
        return volt;
    }

    public void setVolt(Integer volt) {
        this.volt = volt;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public Integer getReversal() {
        return reversal;
    }

    public void setReversal(Integer reversal) {
        this.reversal = reversal;
    }

    public float getNumber() {
        return number;
    }

    public void setNumber(float number) {
        this.number = number;
    }

    public Integer getDiastolic() {
        return diastolic;
    }

    public void setDiastolic(Integer diastolic) {
        this.diastolic = diastolic;
    }

    public Integer getSystolic() {
        return systolic;
    }

    public void setSystolic(Integer systolic) {
        this.systolic = systolic;
    }

    public Integer getData() {
        return data;
    }

    public void setData(Integer data) {
        this.data = data;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getAlarm() {
        return alarm;
    }

    public void setAlarm(Integer alarm) {
        this.alarm = alarm;
    }

    @Override
    public String toString() {
        return "WatchDto{" +
                "time='" + time + '\'' +
                ", hash='" + hash + '\'' +
                ", action='" + action + '\'' +
                ", imei='" + imei + '\'' +
                ", volt=" + volt +
                ", step=" + step +
                ", reversal=" + reversal +
                ", number=" + number +
                ", diastolic=" + diastolic +
                ", systolic=" + systolic +
                ", data=" + data +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", location='" + location + '\'' +
                ", type='" + type + '\'' +
                ", alarm='" + alarm + '\'' +
                '}';
    }
}

