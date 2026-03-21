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

/**
 * AI控制器
 *
 * @author qkyd
 */
@Tag(name = "AI接口", description = "AI能力服务接口")
@RestController
@RequestMapping("/ai")
public class AiController
{
    @Autowired
    private IAiService aiService;

    /**
     * AI对话
     */
    @Operation(summary = "AI对话", description = "调用AI服务进行智能对话")
    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody String message)
    {
        try
        {
            String response = aiService.chat(message);
            if (response == null || response.isBlank())
            {
                response = "AI服务暂时未返回内容，请稍后重试";
            }
            return AjaxResult.success((Object) response);
        }
        catch (Exception e)
        {
            return AjaxResult.error("AI对话失败：" + e.getMessage());
        }
    }

    /**
     * AI连通性测试
     */
    @Operation(summary = "AI测试", description = "测试AI服务连通性")
    @GetMapping("/test")
    public AjaxResult test()
    {
        try
        {
            String response = aiService.chat("你好，请返回一条测试消息");
            return AjaxResult.success((Object) response);
        }
        catch (Exception e)
        {
            return AjaxResult.error("测试失败：" + e.getMessage());
        }
    }

    /**
     * 跌倒检测
     */
    @Operation(summary = "跌倒检测", description = "接收检测数据并返回AI识别结果")
    @PostMapping("/detect/fall")
    public AjaxResult detectFall(@RequestBody FallDetectionRequest request)
    {
        try
        {
            String result = aiService.detectFall(request);
            return AjaxResult.success((Object) result);
        }
        catch (Exception e)
        {
            return AjaxResult.error("跌倒检测失败：" + e.getMessage());
        }
    }
}