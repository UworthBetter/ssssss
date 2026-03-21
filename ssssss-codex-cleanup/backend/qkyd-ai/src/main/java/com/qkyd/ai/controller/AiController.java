package com.qkyd.ai.controller;

import com.qkyd.ai.service.IAiService;
import com.qkyd.common.core.domain.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * AI控制器
 *
 * @author ueit
 */
@Tag(name = "AI服务", description = "AI相关接口")
@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private IAiService aiService;

    /**
     * AI对话
     */
    @Operation(summary = "AI对话", description = "与AI进行单轮对话")
    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody String message) {
        try {
            String response = aiService.chat(message);
            return AjaxResult.success(response);
        } catch (Exception e) {
            return AjaxResult.error("AI对话失败：" + e.getMessage());
        }
    }

    /**
     * 测试AI服务
     */
    @Operation(summary = "测试AI服务", description = "测试AI服务是否正常")
    @GetMapping("/test")
    public AjaxResult test() {
        try {
            String response = aiService.chat("你好，请自我介绍一下");
            return AjaxResult.success(response);
        } catch (Exception e) {
            return AjaxResult.error("测试失败：" + e.getMessage());
        }
    }

    /**
     * 嘉庆算法-跌倒检测
     */
    @Operation(summary = "跌倒检测", description = "基于传感器数据的AI跌倒检测")
    @PostMapping("/detect/fall")
    public AjaxResult detectFall(@RequestBody com.qkyd.ai.domain.FallDetectionRequest request) {
        try {
            String result = aiService.detectFall(request);
            return AjaxResult.success(result);
        } catch (Exception e) {
            return AjaxResult.error("检测失败：" + e.getMessage());
        }
    }
}
