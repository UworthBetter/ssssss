package com.qkyd.web.websocket.handler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.alibaba.fastjson2.JSON;

/**
 * 健康数据WebSocket处理器
 * 
 * 功能：
 * 1. 管理WebSocket连接（支持按服务对象ID分组）
 * 2. 接收客户端订阅请求
 * 3. 广播健康数据更新
 * 4. 推送异常告警
 * 
 * @author qkyd
 * @date 2026-02-02
 */
@Component
public class HealthDataWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(HealthDataWebSocketHandler.class);

    // 存储所有WebSocket会话
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // 存储服务对象ID与会话ID的映射关系（用于推送特定服务对象的数据）
    private static final Map<String, String> patientSessionMap = new ConcurrentHashMap<>();

    // 存储会话ID与订阅的服务对象列表的映射
    private static final Map<String, Long> sessionPatientMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        log.info("WebSocket连接建立: sessionId={}", sessionId);
        
        // 发送连接成功消息
        sendMessage(sessionId, JSON.toJSONString(Map.of(
            "type", "connected",
            "message", "WebSocket连接成功",
            "sessionId", sessionId
        )));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        String payload = message.getPayload();
        
        log.debug("收到WebSocket消息: sessionId={}, message={}", sessionId, payload);
        
        try {
            // 解析客户端消息
            Map<String, Object> data = JSON.parseObject(payload, Map.class);
            String type = (String) data.get("type");
            
            switch (type) {
                case "subscribe":
                    // 订阅服务对象数据
                    handleSubscribe(sessionId, data);
                    break;
                    
                case "unsubscribe":
                    // 取消订阅
                    handleUnsubscribe(sessionId);
                    break;
                    
                case "heartbeat":
                    // 心跳检测
                    handleHeartbeat(sessionId);
                    break;
                    
                default:
                    log.warn("未知的消息类型: type={}", type);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败: sessionId={}, message={}", sessionId, payload, e);
            sendMessage(sessionId, JSON.toJSONString(Map.of(
                "type", "error",
                "message", "消息处理失败: " + e.getMessage()
            )));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        
        // 清理订阅关系
        Long patientId = sessionPatientMap.remove(sessionId);
        if (patientId != null) {
            patientSessionMap.remove(String.valueOf(patientId));
        }
        
        log.info("WebSocket连接关闭: sessionId={}, status={}", sessionId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        log.error("WebSocket传输错误: sessionId={}", sessionId, exception);
        
        // 清理会话
        sessions.remove(sessionId);
        Long patientId = sessionPatientMap.remove(sessionId);
        if (patientId != null) {
            patientSessionMap.remove(String.valueOf(patientId));
        }
    }

    /**
     * 处理订阅请求
     */
    private void handleSubscribe(String sessionId, Map<String, Object> data) {
        Long patientId = Long.valueOf(data.get("patientId").toString());
        
        // 更新订阅关系
        Long oldPatientId = sessionPatientMap.put(sessionId, patientId);
        if (oldPatientId != null) {
            patientSessionMap.remove(String.valueOf(oldPatientId));
        }
        patientSessionMap.put(String.valueOf(patientId), sessionId);
        
        log.info("客户端订阅服务对象数据: sessionId={}, patientId={}", sessionId, patientId);
        
        // 发送订阅成功消息
        sendMessage(sessionId, JSON.toJSONString(Map.of(
            "type", "subscribed",
            "message", "订阅成功",
            "patientId", patientId
        )));
    }

    /**
     * 处理取消订阅
     */
    private void handleUnsubscribe(String sessionId) {
        Long patientId = sessionPatientMap.remove(sessionId);
        if (patientId != null) {
            patientSessionMap.remove(String.valueOf(patientId));
        }
        
        log.info("客户端取消订阅: sessionId={}", sessionId);
        
        sendMessage(sessionId, JSON.toJSONString(Map.of(
            "type", "unsubscribed",
            "message", "取消订阅成功"
        )));
    }

    /**
     * 处理心跳检测
     */
    private void handleHeartbeat(String sessionId) {
        sendMessage(sessionId, JSON.toJSONString(Map.of(
            "type", "heartbeat",
            "timestamp", System.currentTimeMillis()
        )));
    }

    /**
     * 发送消息到指定会话
     */
    public boolean sendMessage(String sessionId, String message) {
        WebSocketSession session = sessions.get(sessionId);
        if (session == null || !session.isOpen()) {
            log.warn("会话不存在或已关闭: sessionId={}", sessionId);
            return false;
        }
        
        try {
            session.sendMessage(new TextMessage(message));
            return true;
        } catch (IOException e) {
            log.error("发送WebSocket消息失败: sessionId={}", sessionId, e);
            return false;
        }
    }

    /**
     * 广播消息到所有会话
     */
    public void broadcast(String message) {
        sessions.forEach((sessionId, session) -> {
            if (session.isOpen()) {
                sendMessage(sessionId, message);
            }
        });
    }

    /**
     * 推送健康数据到订阅的服务对象
     */
    public void pushHealthData(Long patientId, Map<String, Object> data) {
        String sessionId = patientSessionMap.get(String.valueOf(patientId));
        if (sessionId == null) {
            log.debug("没有客户端订阅服务对象: patientId={}", patientId);
            return;
        }
        
        Map<String, Object> message = Map.of(
            "type", "healthData",
            "patientId", patientId,
            "data", data,
            "timestamp", System.currentTimeMillis()
        );
        
        sendMessage(sessionId, JSON.toJSONString(message));
    }

    /**
     * 推送异常告警
     */
    public void pushAbnormalAlert(Map<String, Object> alert) {
        Map<String, Object> message = Map.of(
            "type", "abnormalAlert",
            "data", alert,
            "timestamp", System.currentTimeMillis()
        );
        
        // 广播告警消息
        broadcast(JSON.toJSONString(message));
    }

    /**
     * 推送风险评分更新
     */
    public void pushRiskScore(Long patientId, Map<String, Object> riskData) {
        String sessionId = patientSessionMap.get(String.valueOf(patientId));
        if (sessionId == null) {
            log.debug("没有客户端订阅服务对象: patientId={}", patientId);
            return;
        }
        
        Map<String, Object> message = Map.of(
            "type", "riskScore",
            "patientId", patientId,
            "data", riskData,
            "timestamp", System.currentTimeMillis()
        );
        
        sendMessage(sessionId, JSON.toJSONString(message));
    }

    /**
     * 获取当前连接数
     */
    public int getConnectionCount() {
        return sessions.size();
    }

    /**
     * 获取订阅的服务对象数
     */
    public int getSubscriptionCount() {
        return patientSessionMap.size();
    }
}
