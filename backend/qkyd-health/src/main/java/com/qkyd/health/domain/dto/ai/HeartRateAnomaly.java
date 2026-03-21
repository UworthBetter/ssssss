package com.qkyd.health.domain.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * 心率异常记录 DTO
 * 与 Python 端 ueit-ai-service 的 HeartRateAnomaly 结构对齐
 *
 * @author ueit
 * @date 2026-01-29
 */
public class HeartRateAnomaly implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 异常发生时的时间戳
     */
    @JsonProperty("timestamp")
    private Long timestamp;

    /**
     * 异常心率值
     */
    @JsonProperty("heart_rate")
    private Integer heartRate;

    /**
     * 滑动窗口均值
     */
    @JsonProperty("window_mean")
    private Double windowMean;

    /**
     * 偏离百分比
     */
    @JsonProperty("deviation_percent")
    private Double deviationPercent;

    /**
     * 预警信息
     */
    @JsonProperty("message")
    private String message;

    public HeartRateAnomaly() {
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }

    public Double getWindowMean() {
        return windowMean;
    }

    public void setWindowMean(Double windowMean) {
        this.windowMean = windowMean;
    }

    public Double getDeviationPercent() {
        return deviationPercent;
    }

    public void setDeviationPercent(Double deviationPercent) {
        this.deviationPercent = deviationPercent;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "HeartRateAnomaly{" +
                "timestamp=" + timestamp +
                ", heartRate=" + heartRate +
                ", windowMean=" + windowMean +
                ", deviationPercent=" + deviationPercent +
                ", message='" + message + '\'' +
                '}';
    }
}
