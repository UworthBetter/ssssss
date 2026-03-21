package com.qkyd.health.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

//实时数据
public class RealTimeData {
    private Integer userId;
    private String nickName;
    private Integer sex;
    private Integer age;
    private Integer heartRateValue;
    private String spo2Value;
    private String temperatureValue;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date readTime;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getHeartRateValue() {
        return heartRateValue;
    }

    public void setHeartRateValue(Integer heartRateValue) {
        this.heartRateValue = heartRateValue;
    }

    public String getSpo2Value() {
        return spo2Value;
    }

    public void setSpo2Value(String spo2Value) {
        this.spo2Value = spo2Value;
    }

    public String getTemperatureValue() {
        return temperatureValue;
    }

    public void setTemperatureValue(String temperatureValue) {
        this.temperatureValue = temperatureValue;
    }

    public Date getReadTime() {
        return readTime;
    }

    public void setReadTime(Date readTime) {
        this.readTime = readTime;
    }

    @Override
    public String toString() {
        return "RealTimeData{" +
                "userId=" + userId +
                ", nickName='" + nickName + '\'' +
                ", sex=" + sex +
                ", age=" + age +
                ", heartRateValue='" + heartRateValue + '\'' +
                ", spo2Value='" + spo2Value + '\'' +
                ", temperatureValue='" + temperatureValue + '\'' +
                ", readTime=" + readTime +
                '}';
    }
}

