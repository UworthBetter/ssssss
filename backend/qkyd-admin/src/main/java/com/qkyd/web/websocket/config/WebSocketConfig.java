package com.qkyd.web.websocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import com.qkyd.web.websocket.handler.HealthDataWebSocketHandler;

/**
 * WebSocket閰嶇疆绫?
 * 
 * @author qkyd
 * @date 2026-02-02
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final HealthDataWebSocketHandler healthDataWebSocketHandler;

    public WebSocketConfig(HealthDataWebSocketHandler healthDataWebSocketHandler) {
        this.healthDataWebSocketHandler = healthDataWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 娉ㄥ唽鍋ュ悍鏁版嵁WebSocket绔偣
        registry.addHandler(healthDataWebSocketHandler, "/ws/health/data")
                .setAllowedOrigins("*");
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        // 璁剧疆鏈€澶ф枃鏈秷鎭紦鍐插尯澶у皬锛?0MB锛?
        container.setMaxTextMessageBufferSize(10 * 1024 * 1024);
        // 璁剧疆鏈€澶т簩杩涘埗娑堟伅缂撳啿鍖哄ぇ灏忥紙10MB锛?
        container.setMaxBinaryMessageBufferSize(10 * 1024 * 1024);
        return container;
    }
}
