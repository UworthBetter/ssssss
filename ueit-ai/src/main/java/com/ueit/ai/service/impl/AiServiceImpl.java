package com.ueit.ai.service.impl;

import com.ueit.ai.service.IAiService;
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
}


