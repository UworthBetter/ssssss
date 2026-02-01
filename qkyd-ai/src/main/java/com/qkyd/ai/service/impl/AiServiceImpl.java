package com.qkyd.ai.service.impl;

import com.qkyd.ai.service.IAiService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * AI服务实现
 *
 * @author ueit
 */
@Service
public class AiServiceImpl implements IAiService {

    private final ChatClient chatClient;

    @Autowired
    public AiServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String chat(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    @Override
    public String chat(String[] messages) {
        // 将数组转换为多轮对话
        StringBuilder builder = new StringBuilder();
        for (String msg : messages) {
            builder.append(msg).append("\n");
        }
        return chatClient.prompt()
                .user(builder.toString())
                .call()
                .content();
    }

    @Override
    public ChatResponse chatWithResponse(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .chatResponse();
    }

    @Override
    public String detectFall(com.qkyd.ai.domain.FallDetectionRequest request) {
        try {
            // 调用 Python 算法服务
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            String pythonServiceUrl = "http://localhost:8000/api/algorithms/detect_fall";

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            org.springframework.http.HttpEntity<com.qkyd.ai.domain.FallDetectionRequest> entity = new org.springframework.http.HttpEntity<>(
                    request, headers);

            String response = restTemplate.postForObject(pythonServiceUrl, entity, String.class);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"算法服务调用失败: " + e.getMessage() + "\"}";
        }
    }
}
