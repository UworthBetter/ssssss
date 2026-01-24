package com.ueit.ai.service;

import org.springframework.ai.chat.model.ChatResponse;

import java.util.List;

/**
 * AI服务接口
 *
 * @author ueit
 */
public interface IAiService {

    /**
     * 聊天对话
     *
     * @param message 用户消息
     * @return AI回复
     */
    String chat(String message);

    /**
     * 批量对话
     *
     * @param messages 消息列表
     * @return AI回复
     */
    String chat(String[] messages);

    /**
     * 获取完整响应对象
     *
     * @param message 用户消息
     * @return 响应对象
     */
    ChatResponse chatWithResponse(String message);

    /**
     * 跌倒检测 (Jiaqing Algorithm)
     * 
     * @param request 检测请求参数
     * @return 检测结果 JSON字符串
     */
    String detectFall(com.ueit.ai.domain.FallDetectionRequest request);
}
