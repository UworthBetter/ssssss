package com.qkyd.web.websocket.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.qkyd.web.websocket.handler.HealthDataWebSocketHandler;

/**
 * WebSocket消息推送服务
 * 
 * 提供统一的消息推送接口，供其他模块（如AI模块）调用
 * 
 * @author qkyd
 * @date 2026-02-02
 */
@Service
public class WebSocketMessageService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketMessageService.class);

    private final HealthDataWebSocketHandler webSocketHandler;

    public WebSocketMessageService(HealthDataWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    /**
     * 推送健康数据更新
     * 
     * @param patientId 服务对象ID
     * @param heartRate 心率
     * @param systolic 收缩压
     * @param diastolic 舒张压
     * @param spo2 血氧
     * @param temperature 体温
     * @param steps 步数
     */
    public void pushHealthData(Long patientId, Integer heartRate, Integer systolic, Integer diastropic, 
                               Integer spo2, Double temperature, Integer steps) {
        Map<String, Object> data = new HashMap<>();
        data.put("heartRate", heartRate);
        data.put("systolic", systolic);
        data.put("diastolic", diastropic);
        data.put("spo2", spo2);
        data.put("temperature", temperature);
        data.put("steps", steps);
        
        webSocketHandler.pushHealthData(patientId, data);
        log.debug("推送健康数据: patientId={}, data={}", patientId, data);
    }

    /**
     * 推送健康数据（使用Map）
     * 
     * @param patientId 服务对象ID
     * @param data 健康数据Map
     */
    public void pushHealthData(Long patientId, Map<String, Object> data) {
        webSocketHandler.pushHealthData(patientId, data);
        log.debug("推送健康数据: patientId={}, data={}", patientId, data);
    }

    /**
     * 推送异常告警
     * 
     * @param alertId 告警ID
     * @param patientId 服务对象ID
     * @param patientName 服务对象姓名
     * @param abnormalType 异常类型（心率异常/血压异常/血氧异常/体温异常）
     * @param abnormalValue 异常值
     * @param riskLevel 风险等级（high/medium/low）
     * @param message 告警消息
     */
    public void pushAbnormalAlert(Long alertId, Long patientId, String patientName, String abnormalType,
                                  String abnormalValue, String riskLevel, String message) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("alertId", alertId);
        alert.put("patientId", patientId);
        alert.put("patientName", patientName);
        alert.put("abnormalType", abnormalType);
        alert.put("abnormalValue", abnormalValue);
        alert.put("riskLevel", riskLevel);
        alert.put("message", message);
        alert.put("createTime", System.currentTimeMillis());
        
        webSocketHandler.pushAbnormalAlert(alert);
        log.info("推送异常告警: alertId={}, patientId={}, type={}, level={}", 
                 alertId, patientId, abnormalType, riskLevel);
    }

    /**
     * 推送异常告警（使用Map）
     * 
     * @param alert 告警数据Map
     */
    public void pushAbnormalAlert(Map<String, Object> alert) {
        webSocketHandler.pushAbnormalAlert(alert);
        log.info("推送异常告警: alert={}", alert);
    }

    /**
     * 推送风险评分更新
     * 
     * @param patientId 服务对象ID
     * @param riskScore 风险评分（0-100）
     * @param riskLevel 风险等级（low/medium/high）
     * @param features 特征数据
     */
    public void pushRiskScore(Long patientId, Integer riskScore, String riskLevel, Map<String, Object> features) {
        Map<String, Object> riskData = new HashMap<>();
        riskData.put("riskScore", riskScore);
        riskData.put("riskLevel", riskLevel);
        riskData.put("features", features);
        riskData.put("updateTime", System.currentTimeMillis());
        
        webSocketHandler.pushRiskScore(patientId, riskData);
        log.info("推送风险评分: patientId={}, score={}, level={}", patientId, riskScore, riskLevel);
    }

    /**
     * 推送风险评分（使用Map）
     * 
     * @param patientId 服务对象ID
     * @param riskData 风险数据Map
     */
    public void pushRiskScore(Long patientId, Map<String, Object> riskData) {
        webSocketHandler.pushRiskScore(patientId, riskData);
        log.info("推送风险评分: patientId={}, data={}", patientId, riskData);
    }

    /**
     * 推送跌倒检测告警
     * 
     * @param patientId 服务对象ID
     * @param patientName 服务对象姓名
     * @param location 位置信息
     * @param probability 跌倒概率
     */
    public void pushFallAlert(Long patientId, String patientName, String location, Double probability) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("type", "fall");
        alert.put("patientId", patientId);
        alert.put("patientName", patientName);
        alert.put("location", location);
        alert.put("probability", probability);
        alert.put("message", String.format("检测到%s可能发生跌倒（概率: %.2f%%）", patientName, probability * 100));
        alert.put("createTime", System.currentTimeMillis());
        
        webSocketHandler.pushAbnormalAlert(alert);
        log.warn("推送跌倒告警: patientId={}, probability={}", patientId, probability);
    }

    /**
     * 推送算法分析结果
     * 
     * @param patientId 服务对象ID
     * @param algorithmType 算法类型（abnormal_detection/risk_assessment/trend_analysis）
     * @param result 分析结果
     */
    public void pushAlgorithmResult(Long patientId, String algorithmType, Map<String, Object> result) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "algorithmResult");
        message.put("patientId", patientId);
        message.put("algorithmType", algorithmType);
        message.put("result", result);
        message.put("timestamp", System.currentTimeMillis());
        
        webSocketHandler.pushHealthData(patientId, message);
        log.debug("推送算法结果: patientId={}, algorithmType={}", patientId, algorithmType);
    }

    /**
     * 广播系统通知
     * 
     * @param level 通知级别（info/warning/error）
     * @param message 通知内容
     */
    public void broadcastNotification(String level, String message) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "notification");
        notification.put("level", level);
        notification.put("message", message);
        notification.put("timestamp", System.currentTimeMillis());
        
        webSocketHandler.broadcast(com.alibaba.fastjson2.JSON.toJSONString(notification));
        log.info("广播系统通知: level={}, message={}", level, message);
    }

    /**
     * 获取WebSocket连接状态
     * 
     * @return 连接状态信息
     */
    public Map<String, Object> getConnectionStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("connectionCount", webSocketHandler.getConnectionCount());
        status.put("subscriptionCount", webSocketHandler.getSubscriptionCount());
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }
}
