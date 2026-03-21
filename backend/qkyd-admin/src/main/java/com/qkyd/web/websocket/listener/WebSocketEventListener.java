package com.qkyd.web.websocket.listener;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.qkyd.common.event.AbnormalDetectionEvent;
import com.qkyd.common.event.HealthDataUpdateEvent;
import com.qkyd.common.event.RiskScoreUpdateEvent;
import com.qkyd.web.websocket.service.WebSocketMessageService;

/**
 * WebSocket浜嬩欢鐩戝惉鍣?
 * 
 * 鐩戝惉AI妯″潡鍙戝竷鐨勪簨浠讹紝骞堕€氳繃WebSocket鎺ㄩ€佸埌鍓嶇
 * 
 * @author qkyd
 * @date 2026-02-02
 */
@Component
public class WebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final WebSocketMessageService webSocketMessageService;

    public WebSocketEventListener(WebSocketMessageService webSocketMessageService) {
        this.webSocketMessageService = webSocketMessageService;
    }

    /**
     * 鐩戝惉寮傚父妫€娴嬩簨浠?
     */
    @EventListener
    @Async
    public void handleAbnormalDetection(AbnormalDetectionEvent event) {
        try {
            Map<String, Object> alert = new HashMap<>();
            alert.put("patientId", event.getPatientId());
            alert.put("patientName", event.getPatientName());
            alert.put("abnormalType", event.getAbnormalType());
            alert.put("abnormalValue", event.getAbnormalValue());
            alert.put("riskLevel", event.getRiskLevel());
            alert.put("message", event.getMessage());
            alert.put("details", event.getDetails());
            alert.put("createTime", event.getTimestamp());
            
            webSocketMessageService.pushAbnormalAlert(alert);
            
            log.info("寮傚父妫€娴嬩簨浠跺凡閫氳繃WebSocket鎺ㄩ€? {}", event);
        } catch (Exception e) {
            log.error("澶勭悊寮傚父妫€娴嬩簨浠跺け璐? {}", event, e);
        }
    }

    /**
     * 鐩戝惉椋庨櫓璇勫垎鏇存柊浜嬩欢
     */
    @EventListener
    @Async
    public void handleRiskScoreUpdate(RiskScoreUpdateEvent event) {
        try {
            Map<String, Object> riskData = new HashMap<>();
            riskData.put("riskScore", event.getRiskScore());
            riskData.put("riskLevel", event.getRiskLevel());
            riskData.put("features", event.getFeatures());
            riskData.put("updateTime", event.getTimestamp());
            
            webSocketMessageService.pushRiskScore(event.getPatientId(), riskData);
            
            log.info("椋庨櫓璇勫垎鏇存柊浜嬩欢宸查€氳繃WebSocket鎺ㄩ€? {}", event);
        } catch (Exception e) {
            log.error("澶勭悊椋庨櫓璇勫垎鏇存柊浜嬩欢澶辫触: {}", event, e);
        }
    }

    /**
     * 鐩戝惉鍋ュ悍鏁版嵁鏇存柊浜嬩欢
     */
    @EventListener
    @Async
    public void handleHealthDataUpdate(HealthDataUpdateEvent event) {
        try {
            webSocketMessageService.pushHealthData(event.getPatientId(), event.getHealthData());
            
            log.debug("鍋ュ悍鏁版嵁鏇存柊浜嬩欢宸查€氳繃WebSocket鎺ㄩ€? {}", event);
        } catch (Exception e) {
            log.error("澶勭悊鍋ュ悍鏁版嵁鏇存柊浜嬩欢澶辫触: {}", event, e);
        }
    }
}
