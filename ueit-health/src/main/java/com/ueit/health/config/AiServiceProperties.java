package com.ueit.health.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI算法服务配置属性
 *
 * @author ueit
 * @date 2026-01-29
 */
@Configuration
@ConfigurationProperties(prefix = "ueit.ai")
public class AiServiceProperties {

    /**
     * Python算法服务地址
     */
    private String serviceUrl = "http://localhost:8001";

    /**
     * 连接超时时间 (毫秒)
     */
    private Integer connectTimeout = 5000;

    /**
     * 读取超时时间 (毫秒)
     */
    private Integer readTimeout = 30000;

    /**
     * 是否启用异步处理
     */
    private Boolean asyncEnabled = true;

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Boolean getAsyncEnabled() {
        return asyncEnabled;
    }

    public void setAsyncEnabled(Boolean asyncEnabled) {
        this.asyncEnabled = asyncEnabled;
    }

    /**
     * 获取健康检查接口完整URL
     */
    public String getHealthCheckUrl() {
        return serviceUrl + "/algo/v1/health-check";
    }
}
