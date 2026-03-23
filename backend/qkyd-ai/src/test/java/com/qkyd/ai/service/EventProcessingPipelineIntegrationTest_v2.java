package com.qkyd.ai.service;

import com.qkyd.ai.service.impl.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 事件处理管道集成测试 (v2.0)
 * 测试完整的异常检测结果流程
 */
@SpringBootTest
@ActiveProfiles("test")
public class EventProcessingPipelineIntegrationTest {

    @Autowired
    private IEventProcessingPipelineService pipelineService;

    @Autowired
    private IRiskScoreService riskScoreService;

    @Autowired
    private IDispositionRuleEngine dispositionRuleEngine;

    @Autowired
    private IOperationAuditService auditService;

    /**
     * 测试完整的异常检测流程
     */
    @Test
    public void testCompleteAnomalyDetectionPipeline() {
        // 1. 准备测试数据
        Long eventId = 123L;
        AbnormalData abnormalData = createTestAbnormalData();

        // 2. 启动管道
        pipelineService.startPipeline(eventId, abnormalData);

        // 3. 验证处理结果
        PipelineStatus status = pipelineService.getPipelineStatus(eventId);
        assertNotNull(status);
        assertEquals("COMPLETED", status.getStage());
        assertNotNull(status.getRiskScore());
        assertNotNull(status.getDetectedAnomalies());
        assertNotNull(status.getTrendAnalysis());
        assertNotNull(status.getBaselineComparison());
        assertNotNull(status.getModelConfidence());
        assertNotNull(status.getAlgorithmVersion());

        // 4. 验证异常检测结果
        assertTrue(status.getDetectedAnomalies().contains("heart_rate_spike"));
        assertTrue(status.getModelConfidence() > 0.8);

        // 5. 验证审计日志
        List<AuditLog> logs = auditService.getAuditTrail(eventId);
        assertFalse(logs.isEmpty());
        assertTrue(logs.stream().anyMatch(l -> "RISK_ASSESS".equals(l.getOperationType())));
    }

    /**
     * 测试多项异常检测
     */
    @Test
    public void testMultipleAnomaliesDetection() {
        Long eventId = 124L;
        AbnormalData abnormalData = createTestAbnormalData();

        // 启动管道
        pipelineService.startPipeline(eventId, abnormalData);

        // 获取状态
        PipelineStatus status = pipelineService.getPipelineStatus(eventId);

        // 验证多项异常
        int anomalyCount = countAnomalies(status.getDetectedAnomalies());
        assertTrue(anomalyCount >= 1, "应该检测到至少1项异常");

        // 验证处置建议
        assertNotNull(status.getDispositionSuggestion());
        if (anomalyCount > 1) {
            assertTrue(status.getDispositionSuggestion().contains("异常"));
        }
    }

    /**
     * 测试趋势分析优化
     */
    @Test
    public void testTrendAnalysisOptimization() {
        Long eventId = 125L;
        AbnormalData abnormalData = createTestAbnormalData();

        pipelineService.startPipeline(eventId, abnormalData);
        PipelineStatus status = pipelineService.getPipelineStatus(eventId);

        // 验证趋势分析
        assertNotNull(status.getTrendAnalysis());
        assertTrue(status.getTrendAnalysis().contains("trend"));

        // 如果趋势是恶化,应该提升通知级别
        if (status.getTrendAnalysis().contains("deteriorating")) {
            assertTrue(status.getDispositionSuggestion().contains("恶化") ||
                      status.getDispositionSuggestion().contains("加强"));
        }
    }

    /**
     * 测试置信度影响决策
     */
    @Test
    public void testConfidenceImpactOnDecision() {
        Long eventId = 126L;
        AbnormalData abnormalData = createTestAbnormalData();

        pipelineService.startPipeline(eventId, abnormalData);
        PipelineStatus status = pipelineService.getPipelineStatus(eventId);

        // 验证置信度
        assertNotNull(status.getModelConfidence());
        assertTrue(status.getModelConfidence() >= 0 && status.getModelConfidence() <= 1);

        // 低置信度应该建议人工复核
        if (status.getModelConfidence() < 0.7) {
            assertTrue(status.getDispositionSuggestion().contains("复核") ||
                      !status.isAutoExecute());
        }

        // 高置信度可以自动执行
        if (status.getModelConfidence() >= 0.9 && status.getRiskScore() >= 70) {
            assertTrue(status.isAutoExecute());
        }
    }

    /**
     * 测试基线对比优化
     */
    @Test
    public void testBaselineComparisonOptimization() {
        Long eventId = 127L;
        AbnormalData abnormalData = createTestAbnormalData();

        pipelineService.startPipeline(eventId, abnormalData);
        PipelineStatus status = pipelineService.getPipelineStatus(eventId);

        // 验证基线对比
        assertNotNull(status.getBaselineComparison());
        assertTrue(status.getBaselineComparison().contains("deviation"));

        // 极其异常应该提升通知级别
        if (status.getBaselineComparison().contains("3.0") ||
            status.getBaselineComparison().contains("3.5")) {
            assertTrue(status.getDispositionSuggestion().contains("异常") ||
                      status.getDispositionSuggestion().contains("建议"));
        }
    }

    /**
     * 测试反馈记录
     */
    @Test
    public void testFeedbackRecording() {
        Long eventId = 128L;
        AbnormalData abnormalData = createTestAbnormalData();

        // 启动管道
        pipelineService.startPipeline(eventId, abnormalData);

        // 记录反馈
        pipelineService.recordFeedback(eventId, "RESOLVED", 5);

        // 验证反馈被记录
        PipelineStatus status = pipelineService.getPipelineStatus(eventId);
        assertNotNull(status);
    }

    /**
     * 创建测试异常数据
     */
    private AbnormalData createTestAbnormalData() {
        AbnormalData data = new AbnormalData();
        data.setPatientId(1001L);
        data.setAbnormalType("heart_rate_spike");
        data.setPriority(80);
        data.setHeartRate(120);
        data.setOxygenSaturation(95);
        data.setTemperature(37.5);
        return data;
    }

    /**
     * 计算异常数量
     */
    private int countAnomalies(String detectedAnomaliesJson) {
        if (detectedAnomaliesJson == null || detectedAnomaliesJson.isEmpty()) {
            return 0;
        }
        return (int) detectedAnomaliesJson.split("\"type\":").length - 1;
    }
}

/**
 * 测试用的异常数据类
 */
class AbnormalData {
    private Long patientId;
    private String abnormalType;
    private Integer priority;
    private Integer heartRate;
    private Integer oxygenSaturation;
    private Double temperature;

    // Getters and Setters
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getAbnormalType() { return abnormalType; }
    public void setAbnormalType(String abnormalType) { this.abnormalType = abnormalType; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }

    public Integer getOxygenSaturation() { return oxygenSaturation; }
    public void setOxygenSaturation(Integer oxygenSaturation) { this.oxygenSaturation = oxygenSaturation; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
}
