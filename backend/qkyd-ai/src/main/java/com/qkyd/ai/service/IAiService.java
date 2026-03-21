package com.qkyd.ai.service;

import org.springframework.ai.chat.model.ChatResponse;

import java.util.List;

/**
 * AI鏈嶅姟鎺ュ彛
 *
 * @author ueit
 */
public interface IAiService {

    /**
     * 鑱婂ぉ瀵硅瘽
     *
     * @param message 鐢ㄦ埛娑堟伅
     * @return AI鍥炲
     */
    String chat(String message);

    /**
     * 鎵归噺瀵硅瘽
     *
     * @param messages 娑堟伅鍒楄〃
     * @return AI鍥炲
     */
    String chat(String[] messages);

    /**
     * 鑾峰彇瀹屾暣鍝嶅簲瀵硅薄
     *
     * @param message 鐢ㄦ埛娑堟伅
     * @return 鍝嶅簲瀵硅薄
     */
    ChatResponse chatWithResponse(String message);

    /**
     * 璺屽€掓娴?(Jiaqing Algorithm)
     * 
     * @param request 妫€娴嬭姹傚弬鏁?
     * @return 妫€娴嬬粨鏋?JSON瀛楃涓?
     */
    String detectFall(com.qkyd.ai.domain.FallDetectionRequest request);
}
