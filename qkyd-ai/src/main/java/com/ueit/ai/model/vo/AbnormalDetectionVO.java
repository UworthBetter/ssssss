package com.ueit.ai.model.vo;

import java.io.Serializable;

/**
 * VO for Frontend Display
 */
public class AbnormalDetectionVO implements Serializable {
    private Boolean abnormal;
    private String message;
    private String detail;

    public Boolean getAbnormal() {
        return abnormal;
    }

    public void setAbnormal(Boolean abnormal) {
        this.abnormal = abnormal;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
