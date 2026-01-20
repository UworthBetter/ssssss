package com.ueit.health.domain.dto;

import java.math.BigDecimal;

/**
 * 5C-BNB02Y类型的手表数据实体类
 */
public class WatchBNDto {
    /**
     * 数据类型
     */
    private String action;
    // 设备唯一编码
    private String imei;
    /**
     * 经度
     */
    private BigDecimal Longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;
    //位置描述
    private String location;
    private Integer signal;
    //定位方式
    private Integer locationType;
    // 步数
    private Integer step;
    // 电量
    private Integer volt;
    private String etemp;
    // 体温
    private Float temp;
    // 数据值
    private Integer data;
    // 收缩压
    private Integer systolic;
    // 舒张压
    private Integer diastolic;
    // 心率
    private Integer heart;
    private Integer avg_step;
    private Double calorie;
    private Integer distance;
    private Long time;
    private String hash;

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getSignal() {
        return signal;
    }

    public void setSignal(Integer signal) {
        this.signal = signal;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public Integer getVolt() {
        return volt;
    }

    public void setVolt(Integer volt) {
        this.volt = volt;
    }

    public String getEtemp() {
        return etemp;
    }

    public void setEtemp(String etemp) {
        this.etemp = etemp;
    }

    public Float getTemp() {
        return temp;
    }

    public void setTemp(Float temp) {
        this.temp = temp;
    }

    public Integer getData() {
        return data;
    }

    public void setData(Integer data) {
        this.data = data;
    }

    public Integer getSystolic() {
        return systolic;
    }

    public void setSystolic(Integer systolic) {
        this.systolic = systolic;
    }

    public Integer getDiastolic() {
        return diastolic;
    }

    public void setDiastolic(Integer diastolic) {
        this.diastolic = diastolic;
    }

    public Integer getHeart() {
        return heart;
    }

    public void setHeart(Integer heart) {
        this.heart = heart;
    }

    public Integer getAvg_step() {
        return avg_step;
    }

    public void setAvg_step(Integer avg_step) {
        this.avg_step = avg_step;
    }

    public Double getCalorie() {
        return calorie;
    }

    public void setCalorie(Double calorie) {
        this.calorie = calorie;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public BigDecimal getLongitude() {
        return Longitude;
    }

    public void setLongitude(BigDecimal Longitude) {
        this.Longitude = Longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public Integer getLocationType() {
        return locationType;
    }

    public void setLocationType(Integer locationType) {
        this.locationType = locationType;
    }

    @Override
    public String toString() {
        return "WatchBNDto{" +
                "action='" + action + '\'' +
                ", imei='" + imei + '\'' +
                ", Longitude=" + Longitude +
                ", latitude=" + latitude +
                ", location='" + location + '\'' +
                ", signal=" + signal +
                ", locationType=" + locationType +
                ", step=" + step +
                ", volt=" + volt +
                ", etemp='" + etemp + '\'' +
                ", temp='" + temp + '\'' +
                ", data=" + data +
                ", systolic=" + systolic +
                ", diastolic=" + diastolic +
                ", heart=" + heart +
                ", avg_step=" + avg_step +
                ", calorie=" + calorie +
                ", distance=" + distance +
                ", time=" + time +
                ", hash='" + hash + '\'' +
                '}';
    }
}
