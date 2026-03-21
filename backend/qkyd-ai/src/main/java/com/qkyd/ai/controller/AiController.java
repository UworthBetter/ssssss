package com.qkyd.ai.controller;

import com.qkyd.ai.service.IAiService;
import com.qkyd.common.core.domain.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * AI鎺у埗鍣?
 *
 * @author ueit
 */
@Tag(name = "AI鏈嶅姟", description = "AI鐩稿叧鎺ュ彛")
@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private IAiService aiService;

    /**
     * AI瀵硅瘽
     */
    @Operation(summary = "AI瀵硅瘽", description = "涓嶢I杩涜鍗曡疆瀵硅瘽")
    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody String message) {
        try {
            String response = aiService.chat(message);
            if (response == null || response.isBlank()) {
                response = "AI服务暂时未返回文本内容，请稍后重试";
            }
            return AjaxResult.success((Object) response);
        } catch (Exception e) {
            return AjaxResult.success((Object) ("AI对话失败：" + e.getMessage()));
        }
    }

    /**
     * 娴嬭瘯AI鏈嶅姟
     */
    @Operation(summary = "娴嬭瘯AI鏈嶅姟", description = "娴嬭瘯AI鏈嶅姟鏄惁姝ｅ父")
    @GetMapping("/test")
    public AjaxResult test() {
        try {
            String response = aiService.chat("浣犲ソ锛岃鑷垜浠嬬粛涓€涓?);
            return AjaxResult.success((Object) response);
        } catch (Exception e) {
            return AjaxResult.error("娴嬭瘯澶辫触锛? + e.getMessage());
        }
    }

    /**
     * 鍢夊簡绠楁硶-璺屽€掓娴?
     */
    @Operation(summary = "璺屽€掓娴?, description = "鍩轰簬浼犳劅鍣ㄦ暟鎹殑AI璺屽€掓娴?)
    @PostMapping("/detect/fall")
    public AjaxResult detectFall(@RequestBody com.qkyd.ai.domain.FallDetectionRequest request) {
        try {
            String result = aiService.detectFall(request);
            return AjaxResult.success((Object) result);
        } catch (Exception e) {
            return AjaxResult.error("妫€娴嬪け璐ワ細" + e.getMessage());
        }
    }
}

