package com.qkyd.health.domain.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * 健康检查请求 DTO
 * 与 Python 端 ueit-ai-service 的 HealthCheckRequest 结构对齐
 *
 * @author ueit
 * @date 2026-01-29
 */
public class AiHealthCheckRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 体征数据列表
     */
    @JsonProperty("data")
    private List<VitalSignData> data;

    public AiHealthCheckRequest() {
    }

    public AiHealthCheckRequest(List<VitalSignData> data) {
        this.data = data;
    }

    public List<VitalSignData> getData() {
        return data;
    }

    public void setData(List<VitalSignData> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "AiHealthCheckRequest{" +
                "data=" + data +
                '}';
    }
}
