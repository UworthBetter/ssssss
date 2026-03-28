package com.qkyd.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.qkyd.ai.service.IAiService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;

/**
 * AI service implementation.
 */
@Service
public class AiServiceImpl implements IAiService {

    /**
     * AI 系统 Prompt，定义 AI 助手的角色和职责
     */
    private static final String SYSTEM_PROMPT =
        "你是专业的智慧养老健康数据分析助手。你的职责是基于提供的健康监护数据，" +
        "输出有针对性、有数据支撑的分析结论。要求：1. 必须引用具体数据而非泛泛而谈；" +
        "2. 根据不同报告类型调整分析侧重点；3. 给出可执行的处置建议；" +
        "4. 使用简洁专业的中文表述。";

    private static final String DEFAULT_BASE_URL = "https://open.bigmodel.cn/api/paas/v4";
    private static final String DEFAULT_CHAT_MODEL = "glm-4.7-flash";
    private static final String DEFAULT_FAST_MODEL = "glm-4.7-flash";
    private static final String DEFAULT_REPORT_MODEL = "glm-4.7-flash";
    private static final String PLACEHOLDER_API_KEY = "sk-placeholder-key-for-startup-fix";

    private final ChatClient chatClient;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:" + DEFAULT_BASE_URL + "}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model:" + DEFAULT_CHAT_MODEL + "}")
    private String model;

    @Value("${qkyd.ai.models.chat:" + DEFAULT_CHAT_MODEL + "}")
    private String chatModel;

    @Value("${qkyd.ai.models.fast:" + DEFAULT_FAST_MODEL + "}")
    private String fastModel;

    @Value("${qkyd.ai.models.report:" + DEFAULT_REPORT_MODEL + "}")
    private String reportModel;

    @Autowired
    public AiServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);
        requestFactory.setReadTimeout(60000);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    @Override
    public String chat(String message) {
        return chatWithModel(message, normalizeModel(chatModel));
    }

    @Override
    public String chatFast(String message) {
        return chatWithModel(message, normalizeModel(fastModel));
    }

    @Override
    public String chatReport(String message) {
        return chatWithModel(message, normalizeModel(reportModel));
    }

    @Override
    public String chat(String[] messages) {
        StringBuilder builder = new StringBuilder();
        for (String msg : messages) {
            builder.append(msg).append("\n");
        }
        return chat(builder.toString());
    }

    @Override
    public ChatResponse chatWithResponse(String message) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(message)
                .call()
                .chatResponse();
    }

    @Override
    public String detectFall(com.qkyd.ai.domain.FallDetectionRequest request) {
        try {
            RestTemplate pythonRestTemplate = new RestTemplate();
            String pythonServiceUrl = "http://localhost:8011/api/algorithms/detect_fall";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<com.qkyd.ai.domain.FallDetectionRequest> entity = new HttpEntity<>(request, headers);
            return pythonRestTemplate.postForObject(pythonServiceUrl, entity, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"跌倒检测调用失败: " + e.getMessage() + "\"}";
        }
    }

    private String chatWithModel(String message, String selectedModel) {
        if (shouldUseCompatibleGateway()) {
            return callCompatibleGateway(message, selectedModel);
        }
        try {
            return chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(message)
                    .call()
                    .content();
        } catch (Exception ex) {
            return callCompatibleGateway(message, selectedModel);
        }
    }

    private String callCompatibleGateway(String message, String selectedModel) {
        try {
            validateGatewayConfig();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("model", selectedModel);
            payload.put("stream", false);

            // 构造 messages 数组，先添加 system 消息，再添加 user 消息
            ArrayNode messages = payload.putArray("messages");
            // system message
            messages.addObject()
                    .put("role", "system")
                    .put("content", SYSTEM_PROMPT);
            // user message
            messages.addObject()
                    .put("role", "user")
                    .put("content", message);

            String endpoint = normalizeBaseUrl(baseUrl) + "/chat/completions";
            String response = restTemplate.postForObject(
                    endpoint,
                    new HttpEntity<>(payload.toString(), headers),
                    String.class
            );

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
            throw new RuntimeException("AI对话调用失败: " + e.getMessage(), e);
        }
    }

    private void validateGatewayConfig() {
        if (apiKey == null || apiKey.isBlank() || PLACEHOLDER_API_KEY.equals(apiKey)) {
            throw new IllegalStateException("AI_API_KEY 未配置，请在服务环境或配置文件中设置智谱 API Key");
        }

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("AI_BASE_URL 未配置，建议使用 " + DEFAULT_BASE_URL);
        }
    }

    private String normalizeBaseUrl(String rawBaseUrl) {
        String normalizedBaseUrl = rawBaseUrl.endsWith("/")
                ? rawBaseUrl.substring(0, rawBaseUrl.length() - 1)
                : rawBaseUrl;

        if (!normalizedBaseUrl.endsWith("/v1") && !normalizedBaseUrl.endsWith("/v4")) {
            normalizedBaseUrl = normalizedBaseUrl + "/v1";
        }

        return normalizedBaseUrl;
    }

    private String normalizeModel(String rawModel) {
        if (rawModel == null || rawModel.isBlank()) {
            return normalizeModel(model);
        }
        return rawModel.trim().toLowerCase(Locale.ROOT);
    }

    private boolean shouldUseCompatibleGateway() {
        if (baseUrl == null || baseUrl.isBlank()) {
            return false;
        }
        String normalized = baseUrl.toLowerCase(Locale.ROOT);
        return normalized.contains("bigmodel.cn") || normalized.contains("/api/paas/") || normalized.contains("/api/coding/paas/");
    }
}
