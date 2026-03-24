package com.qkyd.ai.service;

import org.springframework.ai.chat.model.ChatResponse;

/**
 * AI service contract.
 */
public interface IAiService {

    /**
     * Main conversational capability. Defaults to the high-capability model.
     */
    String chat(String message);

    /**
     * Fast response capability for lightweight prompts.
     */
    String chatFast(String message);

    /**
     * Report or summary generation capability.
     */
    String chatReport(String message);

    /**
     * Multi-message conversational capability.
     */
    String chat(String[] messages);

    /**
     * Returns the underlying chat response for the main model.
     */
    ChatResponse chatWithResponse(String message);

    /**
     * Fall detection algorithm bridge.
     */
    String detectFall(com.qkyd.ai.domain.FallDetectionRequest request);
}
