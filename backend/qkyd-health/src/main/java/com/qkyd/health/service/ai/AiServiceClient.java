package com.qkyd.health.service.ai;

import com.qkyd.health.config.AiServiceProperties;
import com.qkyd.health.domain.dto.ai.AiHealthCheckRequest;
import com.qkyd.health.domain.dto.ai.AiHealthCheckResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * AI算法服务客户端
 * 负责与 Python ueit-ai-service 进行 HTTP 通信
 *
 * @author ueit
 * @date 2026-01-29
 */
@Component
public class AiServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AiServiceClient.class);

    private final RestTemplate restTemplate;
    private final AiServiceProperties aiServiceProperties;

    public AiServiceClient(
            @Qualifier("aiRestTemplate") RestTemplate restTemplate,
            AiServiceProperties aiServiceProperties) {
        this.restTemplate = restTemplate;
        this.aiServiceProperties = aiServiceProperties;
    }

    /**
     * 调用健康检查接口
     *
     * @param request 健康检查请求
     * @return 健康检查响应，调用失败时返回 null
     */
    public AiHealthCheckResponse healthCheck(AiHealthCheckRequest request) {
        String url = aiServiceProperties.getHealthCheckUrl();
        log.info("[AiServiceClient] 调用健康检查接口: {}", url);
        log.debug("[AiServiceClient] 请求数据点数量: {}",
                request.getData() != null ? request.getData().size() : 0);

        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<AiHealthCheckRequest> entity = new HttpEntity<>(request, headers);

            // 发送 POST 请求
            ResponseEntity<AiHealthCheckResponse> responseEntity = restTemplate.postForEntity(
                    url,
                    entity,
                    AiHealthCheckResponse.class);

            AiHealthCheckResponse response = responseEntity.getBody();
            if (response != null && response.isSuccess()) {
                log.info("[AiServiceClient] 健康检查成功, 风险等级: {}, 风险评分: {}",
                        response.getRiskLevel(), response.getRiskScore());
            } else {
                log.warn("[AiServiceClient] 健康检查响应异常: {}", response);
            }

            return response;

        } catch (RestClientException e) {
            log.error("[AiServiceClient] 调用AI服务失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 检查AI服务是否可用
     *
     * @return 服务是否可用
     */
    public boolean isServiceAvailable() {
        String url = aiServiceProperties.getServiceUrl() + "/health";
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("[AiServiceClient] AI服务不可用: {}", e.getMessage());
            return false;
        }
    }
}
