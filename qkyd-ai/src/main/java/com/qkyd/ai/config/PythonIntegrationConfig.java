package com.qkyd.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;

@Configuration
public class PythonIntegrationConfig {

    @Value("${ai.python.url:http://localhost:8000}")
    private String pythonServiceUrl;

    public String getPythonServiceUrl() {
        return pythonServiceUrl;
    }

    public String getUrl() {
        return pythonServiceUrl;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
