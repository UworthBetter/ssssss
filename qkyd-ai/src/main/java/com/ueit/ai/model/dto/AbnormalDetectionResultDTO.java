package com.ueit.ai.model.dto;

import java.io.Serializable;

/**
 * Result DTO from Python Abnormal Detection
 */
public class AbnormalDetectionResultDTO implements Serializable {
    private Boolean is_abnormal;
    private String type;
    private Double diff;
    private String msg;

    // Statistical specific
    private Double z_score;
    private Double mean;
    private Double std;

    public Boolean getIs_abnormal() {
        return is_abnormal;
    }

    public void setIs_abnormal(Boolean is_abnormal) {
        this.is_abnormal = is_abnormal;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getDiff() {
        return diff;
    }

    public void setDiff(Double diff) {
        this.diff = diff;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Double getZ_score() {
        return z_score;
    }

    public void setZ_score(Double z_score) {
        this.z_score = z_score;
    }

    public Double getMean() {
        return mean;
    }

    public void setMean(Double mean) {
        this.mean = mean;
    }

    public Double getStd() {
        return std;
    }

    public void setStd(Double std) {
        this.std = std;
    }
}
