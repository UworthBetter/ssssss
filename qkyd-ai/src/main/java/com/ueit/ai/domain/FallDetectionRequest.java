package com.ueit.ai.domain;

import java.io.Serializable;
import java.util.Map;

/**
 * 跌倒检测请求对象
 * 
 * @author ueit
 */
public class FallDetectionRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 加速度X */
    private Double acc_x;

    /** 加速度Y */
    private Double acc_y;

    /** 加速度Z */
    private Double acc_z;

    /** 用户年龄 */
    private Integer age;

    /** 地点 */
    private String location;

    /** 时间戳 */
    private Long timestamp;

    /** 其他上下文信息 */
    private Map<String, Object> extra;

    public Double getAcc_x() {
        return acc_x;
    }

    public void setAcc_x(Double acc_x) {
        this.acc_x = acc_x;
    }

    public Double getAcc_y() {
        return acc_y;
    }

    public void setAcc_y(Double acc_y) {
        this.acc_y = acc_y;
    }

    public Double getAcc_z() {
        return acc_z;
    }

    public void setAcc_z(Double acc_z) {
        this.acc_z = acc_z;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }
}
