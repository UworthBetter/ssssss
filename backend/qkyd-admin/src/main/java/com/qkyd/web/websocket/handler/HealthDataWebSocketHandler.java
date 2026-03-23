package com.qkyd.web.websocket.handler;

import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Health data websocket handler.
 */
@Component
public class HealthDataWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(HealthDataWebSocketHandler.class);

    private static final Map<String, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();
    private static final Map<Long, Set<String>> PATIENT_SESSION_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Set<Long>> SESSION_PATIENT_MAP = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        SESSIONS.put(sessionId, session);
        log.info("WebSocket connected: {}", sessionId);

        Map<String, Object> response = new ConcurrentHashMap<>();
        response.put("type", "connected");
        response.put("sessionId", sessionId);
        sendMessage(session, JSON.toJSONString(response));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        String payload = message.getPayload();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = JSON.parseObject(payload, Map.class);
            if (data == null) {
                return;
            }

            Object typeValue = data.get("type");
            String type = typeValue == null ? "" : typeValue.toString();

            switch (type) {
                case "subscribe":
                    handleSubscribe(sessionId, data);
                    break;
                case "unsubscribe":
                    handleUnsubscribe(sessionId, data);
                    break;
                case "heartbeat":
                    Map<String, Object> heartbeat = new ConcurrentHashMap<>();
                    heartbeat.put("type", "heartbeat_ack");
                    heartbeat.put("timestamp", System.currentTimeMillis());
                    sendMessage(session, JSON.toJSONString(heartbeat));
                    break;
                default:
                    log.warn("Unknown WebSocket message type: {}", type);
                    break;
            }
        } catch (Exception e) {
            log.error("Failed to handle WebSocket message: {}", payload, e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        SESSIONS.remove(sessionId);
        cleanupSessionSubscriptions(sessionId);
        log.info("WebSocket closed: {}", sessionId);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error: {}", session.getId(), exception);
        if (session.isOpen()) {
            session.close();
        }
        SESSIONS.remove(session.getId());
        cleanupSessionSubscriptions(session.getId());
    }

    public void pushHealthData(Long patientId, Object healthData) {
        if (patientId == null) {
            return;
        }
        Set<String> sessionIds = PATIENT_SESSION_MAP.getOrDefault(patientId, Collections.emptySet());
        if (sessionIds.isEmpty()) {
            return;
        }

        Map<String, Object> message = new ConcurrentHashMap<>();
        message.put("type", "healthData");
        message.put("patientId", patientId);
        message.put("data", healthData);

        String json = JSON.toJSONString(message);
        for (String sessionId : sessionIds) {
            WebSocketSession session = SESSIONS.get(sessionId);
            if (session != null && session.isOpen()) {
                sendMessage(session, json);
            }
        }
    }

    public void pushAbnormalAlert(Object alert) {
        Map<String, Object> message = new ConcurrentHashMap<>();
        message.put("type", "abnormalAlert");
        message.put("data", alert);

        String json = JSON.toJSONString(message);
        for (WebSocketSession session : SESSIONS.values()) {
            if (session.isOpen()) {
                sendMessage(session, json);
            }
        }
    }

    public void pushRiskScore(Long patientId, Object riskData) {
        if (patientId == null) {
            return;
        }
        Set<String> sessionIds = PATIENT_SESSION_MAP.getOrDefault(patientId, Collections.emptySet());
        if (sessionIds.isEmpty()) {
            return;
        }

        Map<String, Object> message = new ConcurrentHashMap<>();
        message.put("type", "riskScore");
        message.put("patientId", patientId);
        message.put("data", riskData);

        String json = JSON.toJSONString(message);
        for (String sessionId : sessionIds) {
            WebSocketSession session = SESSIONS.get(sessionId);
            if (session != null && session.isOpen()) {
                sendMessage(session, json);
            }
        }
    }

    public void broadcast(String message) {
        for (WebSocketSession session : SESSIONS.values()) {
            if (session.isOpen()) {
                sendMessage(session, message);
            }
        }
    }

    public int getConnectionCount() {
        return SESSIONS.size();
    }

    public int getSubscriptionCount() {
        return SESSION_PATIENT_MAP.values().stream().mapToInt(Set::size).sum();
    }

    private void handleSubscribe(String sessionId, Map<String, Object> data) {
        Set<Long> patientIds = parsePatientIds(data);
        if (patientIds.isEmpty()) {
            return;
        }

        for (Long patientId : patientIds) {
            SESSION_PATIENT_MAP.computeIfAbsent(sessionId, key -> ConcurrentHashMap.newKeySet()).add(patientId);
            PATIENT_SESSION_MAP.computeIfAbsent(patientId, key -> ConcurrentHashMap.newKeySet()).add(sessionId);
        }

        WebSocketSession session = SESSIONS.get(sessionId);
        if (session != null) {
            Map<String, Object> response = new ConcurrentHashMap<>();
            response.put("type", "subscribed");
            response.put("patientIds", patientIds);
            sendMessage(session, JSON.toJSONString(response));
        }

        log.info("WebSocket session {} subscribed patients {}", sessionId, patientIds);
    }

    private void handleUnsubscribe(String sessionId, Map<String, Object> data) {
        Set<Long> patientIds = parsePatientIds(data);

        if (patientIds.isEmpty()) {
            cleanupSessionSubscriptions(sessionId);
        } else {
            for (Long patientId : patientIds) {
                removeSessionSubscription(sessionId, patientId);
            }
        }

        WebSocketSession session = SESSIONS.get(sessionId);
        if (session != null) {
            Map<String, Object> response = new ConcurrentHashMap<>();
            response.put("type", "unsubscribed");
            response.put("patientIds", patientIds);
            sendMessage(session, JSON.toJSONString(response));
        }

        log.info("WebSocket session {} unsubscribed patients {}", sessionId, patientIds);
    }

    private Set<Long> parsePatientIds(Map<String, Object> data) {
        Set<Long> patientIds = new LinkedHashSet<>();

        Long patientId = parseLong(data.get("patientId"));
        if (patientId != null) {
            patientIds.add(patientId);
        }

        Object patientIdsValue = data.get("patientIds");
        if (patientIdsValue instanceof List<?>) {
            for (Object item : (List<?>) patientIdsValue) {
                Long parsed = parseLong(item);
                if (parsed != null) {
                    patientIds.add(parsed);
                }
            }
        } else if (patientIdsValue instanceof Object[]) {
            for (Object item : (Object[]) patientIdsValue) {
                Long parsed = parseLong(item);
                if (parsed != null) {
                    patientIds.add(parsed);
                }
            }
        } else if (patientIdsValue instanceof String) {
            String[] values = ((String) patientIdsValue).split(",");
            for (String value : values) {
                Long parsed = parseLong(value);
                if (parsed != null) {
                    patientIds.add(parsed);
                }
            }
        }

        return patientIds;
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }

        try {
            return Long.parseLong(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void cleanupSessionSubscriptions(String sessionId) {
        Set<Long> patientIds = SESSION_PATIENT_MAP.remove(sessionId);
        if (patientIds == null || patientIds.isEmpty()) {
            return;
        }

        for (Long patientId : patientIds) {
            Set<String> sessions = PATIENT_SESSION_MAP.get(patientId);
            if (sessions == null) {
                continue;
            }
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                PATIENT_SESSION_MAP.remove(patientId);
            }
        }
    }

    private void removeSessionSubscription(String sessionId, Long patientId) {
        Set<Long> patientIds = SESSION_PATIENT_MAP.get(sessionId);
        if (patientIds != null) {
            patientIds.remove(patientId);
            if (patientIds.isEmpty()) {
                SESSION_PATIENT_MAP.remove(sessionId);
            }
        }

        Set<String> sessions = PATIENT_SESSION_MAP.get(patientId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                PATIENT_SESSION_MAP.remove(patientId);
            }
        }
    }

    private void sendMessage(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (IOException e) {
            log.error("Failed to send WebSocket message to session {}", session.getId(), e);
        }
    }
}
