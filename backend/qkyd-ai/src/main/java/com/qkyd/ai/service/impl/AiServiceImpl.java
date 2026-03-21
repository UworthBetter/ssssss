package com.qkyd.ai.service.impl;

import com.qkyd.ai.service.IAiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * AI鏈嶅姟瀹炵幇
 *
 * @author ueit
 */
@Service
public class AiServiceImpl implements IAiService {

    private final ChatClient chatClient;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:https://api.openai.com}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model:gpt-3.5-turbo}")
    private String model;

    @Autowired
    public AiServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String chat(String message) {
        try {
            return chatClient.prompt()
                    .user(message)
                    .call()
                    .content();
        } catch (Exception ex) {
            // Fallback for OpenAI-compatible gateways that do not support Spring AI default /v1 path.
            return callCompatibleGateway(message);
        }
    }

    @Override
    public String chat(String[] messages) {
        // 灏嗘暟缁勮浆鎹负澶氳疆瀵硅瘽
        StringBuilder builder = new StringBuilder();
        for (String msg : messages) {
            builder.append(msg).append("\n");
        }
        return chat(builder.toString());
    }

    @Override
    public ChatResponse chatWithResponse(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .chatResponse();
    }

    private String callCompatibleGateway(String message) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            String body = objectMapper.createObjectNode()
                    .put("model", model)
                    .putArray("messages")
                    .addObject()
                    .put("role", "user")
                    .put("content", message)
                    .toString();

            String endpoint = baseUrl.endsWith("/")
                    ? baseUrl + "chat/completions"
                    : baseUrl + "/chat/completions";

            String response = restTemplate.postForObject(endpoint, new HttpEntity<>(body, headers), String.class);
            if (response == null) {
                return "AI service returned empty response";
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (!content.isMissingNode() && !content.isNull()) {
                return content.asText();
            }
            return response;
        } catch (Exception e) {
            throw new RuntimeException("AI瀵硅瘽澶辫触: " + e.getMessage(), e);
        }
    }

    @Override
    public String detectFall(com.qkyd.ai.domain.FallDetectionRequest request) {
        try {
            // 璋冪敤 Python 绠楁硶鏈嶅姟
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            String pythonServiceUrl = "http://localhost:8011/api/algorithms/detect_fall";

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            org.springframework.http.HttpEntity<com.qkyd.ai.domain.FallDetectionRequest> entity = new org.springframework.http.HttpEntity<>(
                    request, headers);

            String response = restTemplate.postForObject(pythonServiceUrl, entity, String.class);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"绠楁硶鏈嶅姟璋冪敤澶辫触: " + e.getMessage() + "\"}";
        }
    }
}
