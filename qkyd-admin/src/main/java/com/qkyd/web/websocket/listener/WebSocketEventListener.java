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
 * WebSocket事件监听器
 * 
 * 监听AI模块发布的事件，并通过WebSocket推送到前端
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
     * 监听异常检测事件
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
            
            log.info("异常检测事件已通过WebSocket推送: {}", event);
        } catch (Exception e) {
            log.error("处理异常检测事件失败: {}", event, e);
        }
    }

    /**
     * 监听风险评分更新事件
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
            
            log.info("风险评分更新事件已通过WebSocket推送: {}", event);
        } catch (Exception e) {
            log.error("处理风险评分更新事件失败: {}", event, e);
        }
    }

    /**
     * 监听健康数据更新事件
     */
    @EventListener
    @Async
    public void handleHealthDataUpdate(HealthDataUpdateEvent event) {
        try {
            webSocketMessageService.pushHealthData(event.getPatientId(), event.getHealthData());
            
            log.debug("健康数据更新事件已通过WebSocket推送: {}", event);
        } catch (Exception e) {
            log.error("处理健康数据更新事件失败: {}", event, e);
        }
    }
}
