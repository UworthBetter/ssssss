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
 * 鍋ュ悍鏁版嵁WebSocket澶勭悊鍣?
 * 
 * 鍔熻兘锛?
 * 1. 绠＄悊WebSocket杩炴帴锛堟敮鎸佹寜鏈嶅姟瀵硅薄ID鍒嗙粍锛?
 * 2. 鎺ユ敹瀹㈡埛绔闃呰姹?
 * 3. 骞挎挱鍋ュ悍鏁版嵁鏇存柊
 * 4. 鎺ㄩ€佸紓甯稿憡璀?
 * 
 * @author qkyd
 * @date 2026-02-02
 */
@Component
public class HealthDataWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(HealthDataWebSocketHandler.class);

    // 瀛樺偍鎵€鏈塛ebSocket浼氳瘽
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // 瀛樺偍鏈嶅姟瀵硅薄ID涓庝細璇滻D鐨勬槧灏勫叧绯伙紙鐢ㄤ簬鎺ㄩ€佺壒瀹氭湇鍔″璞＄殑鏁版嵁锛?
    private static final Map<String, String> patientSessionMap = new ConcurrentHashMap<>();

    // 瀛樺偍浼氳瘽ID涓庤闃呯殑鏈嶅姟瀵硅薄鍒楄〃鐨勬槧灏?
    private static final Map<String, Long> sessionPatientMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        log.info("WebSocket杩炴帴寤虹珛: sessionId={}", sessionId);
        
        // 鍙戦€佽繛鎺ユ垚鍔熸秷鎭?
        sendMessage(sessionId, JSON.toJSONString(Map.of(
            "type", "connected",
            "message", "WebSocket杩炴帴鎴愬姛",
            "sessionId", sessionId
        )));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        String payload = message.getPayload();
        
        log.debug("鏀跺埌WebSocket娑堟伅: sessionId={}, message={}", sessionId, payload);
        
        try {
            // 瑙ｆ瀽瀹㈡埛绔秷鎭?
            Map<String, Object> data = JSON.parseObject(payload, Map.class);
            String type = (String) data.get("type");
            
            switch (type) {
                case "subscribe":
                    // 璁㈤槄鏈嶅姟瀵硅薄鏁版嵁
                    handleSubscribe(sessionId, data);
                    break;
                    
                case "unsubscribe":
                    // 鍙栨秷璁㈤槄
                    handleUnsubscribe(sessionId);
                    break;
                    
                case "heartbeat":
                    // 蹇冭烦妫€娴?
                    handleHeartbeat(sessionId);
                    break;
                    
                default:
                    log.warn("鏈煡鐨勬秷鎭被鍨? type={}", type);
            }
        } catch (Exception e) {
            log.error("澶勭悊WebSocket娑堟伅澶辫触: sessionId={}, message={}", sessionId, payload, e);
            sendMessage(sessionId, JSON.toJSONString(Map.of(
                "type", "error",
                "message", "娑堟伅澶勭悊澶辫触: " + e.getMessage()
            )));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        
        // 娓呯悊璁㈤槄鍏崇郴
        Long patientId = sessionPatientMap.remove(sessionId);
        if (patientId != null) {
            patientSessionMap.remove(String.valueOf(patientId));
        }
        
        log.info("WebSocket杩炴帴鍏抽棴: sessionId={}, status={}", sessionId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        log.error("WebSocket浼犺緭閿欒: sessionId={}", sessionId, exception);
        
        // 娓呯悊浼氳瘽
        sessions.remove(sessionId);
        Long patientId = sessionPatientMap.remove(sessionId);
        if (patientId != null) {
            patientSessionMap.remove(String.valueOf(patientId));
        }
    }

    /**
     * 澶勭悊璁㈤槄璇锋眰
     */
    private void handleSubscribe(String sessionId, Map<String, Object> data) {
        Long patientId = Long.valueOf(data.get("patientId").toString());
        
        // 鏇存柊璁㈤槄鍏崇郴
        Long oldPatientId = sessionPatientMap.put(sessionId, patientId);
        if (oldPatientId != null) {
            patientSessionMap.remove(String.valueOf(oldPatientId));
        }
        patientSessionMap.put(String.valueOf(patientId), sessionId);
        
        log.info("瀹㈡埛绔闃呮湇鍔″璞℃暟鎹? sessionId={}, patientId={}", sessionId, patientId);
        
        // 鍙戦€佽闃呮垚鍔熸秷鎭?
        sendMessage(sessionId, JSON.toJSONString(Map.of(
            "type", "subscribed",
            "message", "璁㈤槄鎴愬姛",
            "patientId", patientId
        )));
    }

    /**
     * 澶勭悊鍙栨秷璁㈤槄
     */
    private void handleUnsubscribe(String sessionId) {
        Long patientId = sessionPatientMap.remove(sessionId);
        if (patientId != null) {
            patientSessionMap.remove(String.valueOf(patientId));
        }
        
        log.info("瀹㈡埛绔彇娑堣闃? sessionId={}", sessionId);
        
        sendMessage(sessionId, JSON.toJSONString(Map.of(
            "type", "unsubscribed",
            "message", "鍙栨秷璁㈤槄鎴愬姛"
        )));
    }

    /**
     * 澶勭悊蹇冭烦妫€娴?
     */
    private void handleHeartbeat(String sessionId) {
        sendMessage(sessionId, JSON.toJSONString(Map.of(
            "type", "heartbeat",
            "timestamp", System.currentTimeMillis()
        )));
    }

    /**
     * 鍙戦€佹秷鎭埌鎸囧畾浼氳瘽
     */
    public boolean sendMessage(String sessionId, String message) {
        WebSocketSession session = sessions.get(sessionId);
        if (session == null || !session.isOpen()) {
            log.warn("浼氳瘽涓嶅瓨鍦ㄦ垨宸插叧闂? sessionId={}", sessionId);
            return false;
        }
        
        try {
            session.sendMessage(new TextMessage(message));
            return true;
        } catch (IOException e) {
            log.error("鍙戦€乄ebSocket娑堟伅澶辫触: sessionId={}", sessionId, e);
            return false;
        }
    }

    /**
     * 骞挎挱娑堟伅鍒版墍鏈変細璇?
     */
    public void broadcast(String message) {
        sessions.forEach((sessionId, session) -> {
            if (session.isOpen()) {
                sendMessage(sessionId, message);
            }
        });
    }

    /**
     * 鎺ㄩ€佸仴搴锋暟鎹埌璁㈤槄鐨勬湇鍔″璞?
     */
    public void pushHealthData(Long patientId, Map<String, Object> data) {
        String sessionId = patientSessionMap.get(String.valueOf(patientId));
        if (sessionId == null) {
            log.debug("娌℃湁瀹㈡埛绔闃呮湇鍔″璞? patientId={}", patientId);
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
     * 鎺ㄩ€佸紓甯稿憡璀?
     */
    public void pushAbnormalAlert(Map<String, Object> alert) {
        Map<String, Object> message = Map.of(
            "type", "abnormalAlert",
            "data", alert,
            "timestamp", System.currentTimeMillis()
        );
        
        // 骞挎挱鍛婅娑堟伅
        broadcast(JSON.toJSONString(message));
    }

    /**
     * 鎺ㄩ€侀闄╄瘎鍒嗘洿鏂?
     */
    public void pushRiskScore(Long patientId, Map<String, Object> riskData) {
        String sessionId = patientSessionMap.get(String.valueOf(patientId));
        if (sessionId == null) {
            log.debug("娌℃湁瀹㈡埛绔闃呮湇鍔″璞? patientId={}", patientId);
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
     * 鑾峰彇褰撳墠杩炴帴鏁?
     */
    public int getConnectionCount() {
        return sessions.size();
    }

    /**
     * 鑾峰彇璁㈤槄鐨勬湇鍔″璞℃暟
     */
    public int getSubscriptionCount() {
        return patientSessionMap.size();
    }
}
