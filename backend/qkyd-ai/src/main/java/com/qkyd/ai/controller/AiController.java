package com.qkyd.ai.controller;

import com.qkyd.ai.domain.FallDetectionRequest;
import com.qkyd.ai.service.IAiService;
import com.qkyd.common.core.domain.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AI controller.
 */
@Tag(name = "AI接口", description = "AI能力服务接口")
@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private IAiService aiService;

    @Operation(summary = "AI对话", description = "主对话能力，默认使用高能力模型")
    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody String message) {
        return invoke(() -> aiService.chat(message));
    }

    @Operation(summary = "AI快速对话", description = "快速响应能力，默认使用轻量模型")
    @PostMapping("/chat/fast")
    public AjaxResult chatFast(@RequestBody String message) {
        return invoke(() -> aiService.chatFast(message));
    }

    @Operation(summary = "AI报告生成", description = "报告和总结能力，默认使用长文本友好模型")
    @PostMapping("/chat/report")
    public AjaxResult chatReport(@RequestBody String message) {
        return invoke(() -> aiService.chatReport(message));
    }

    @Operation(summary = "AI模型能力", description = "返回当前后端暴露的模型能力入口")
    @GetMapping("/models")
    public AjaxResult models() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("chat", "/ai/chat");
        payload.put("fast", "/ai/chat/fast");
        payload.put("report", "/ai/chat/report");
        return AjaxResult.success(payload);
    }

    @Operation(summary = "AI测试", description = "测试 AI 服务连通性")
    @GetMapping("/test")
    public AjaxResult test() {
        return invoke(() -> aiService.chat("你好，请返回一条测试消息"));
    }

    @Operation(summary = "跌倒检测", description = "接收检测数据并返回 AI 识别结果")
    @PostMapping("/detect/fall")
    public AjaxResult detectFall(@RequestBody FallDetectionRequest request) {
        try {
            return AjaxResult.success(aiService.detectFall(request));
        } catch (Exception e) {
            return AjaxResult.error("跌倒检测失败: " + e.getMessage());
        }
    }

    private AjaxResult invoke(AiCall aiCall) {
        try {
            String response = aiCall.call();
            if (response == null || response.isBlank()) {
                response = "AI 服务暂时未返回内容，请稍后重试";
            }
            return AjaxResult.success(response);
        } catch (Exception e) {
            return AjaxResult.error("AI 对话失败: " + e.getMessage());
        }
    }

    @FunctionalInterface
    private interface AiCall {
        String call();
    }
}
