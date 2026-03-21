package com.qkyd.web.websocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import com.qkyd.web.websocket.handler.HealthDataWebSocketHandler;

/**
 * WebSocket配置类
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
        // 注册健康数据WebSocket端点
        registry.addHandler(healthDataWebSocketHandler, "/ws/health/data")
                .setAllowedOrigins("*");
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        // 设置最大文本消息缓冲区大小（10MB）
        container.setMaxTextMessageBufferSize(10 * 1024 * 1024);
        // 设置最大二进制消息缓冲区大小（10MB）
        container.setMaxBinaryMessageBufferSize(10 * 1024 * 1024);
        return container;
    }
}
