package com.ueit.health.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate 配置类
 *
 * @author ueit
 * @date 2026-01-29
 */
@Configuration
public class RestTemplateConfig {

    private final AiServiceProperties aiServiceProperties;

    public RestTemplateConfig(AiServiceProperties aiServiceProperties) {
        this.aiServiceProperties = aiServiceProperties;
    }

    /**
     * 创建用于调用AI服务的RestTemplate
     */
    @Bean(name = "aiRestTemplate")
    public RestTemplate aiRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(aiServiceProperties.getConnectTimeout()))
                .setReadTimeout(Duration.ofMillis(aiServiceProperties.getReadTimeout()))
                .build();
    }
}
