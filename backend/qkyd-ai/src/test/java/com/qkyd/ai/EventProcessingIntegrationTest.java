package com.qkyd.ai;

import com.qkyd.ai.service.IEventProcessingPipelineService;
import com.qkyd.ai.service.IOperationAuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 事件处理管道集成测试
 */
@SpringBootTest
public class EventProcessingIntegrationTest {

    @Autowired
    private IEventProcessingPipelineService pipelineService;

    @Autowired
    private IOperationAuditService auditService;

    @Test
    public void testHighRiskEventProcessing() {
        // 模拟高风险异常
        Long eventId = 1L;
        Map<String, Object> abnormalData = new HashMap<>();
        abnormalData.put("abnormalType", "心率异常");
        abnormalData.put("abnormalValue", "150");
        abnormalData.put("riskLevel", "danger");
        abnormalData.put("patientId", 1L);

        // 启动管道
        pipelineService.startPipeline(eventId, abnormalData);

        // 验证操作日志
        var trail = auditService.getEventAuditTrail(eventId);
        assertNotNull(trail);
        assertTrue(trail.size() > 0);
        assertTrue(trail.stream().anyMatch(log -> "DETECT".equals(log.get("operation_type"))));
        assertTrue(trail.stream().anyMatch(log -> "RISK_ASSESS".equals(log.get("operation_type"))));
    }

    @Test
    public void testLowPriorityEventSkipped() {
        // 模拟低优先级异常
        Long eventId = 2L;
        Map<String, Object> abnormalData = new HashMap<>();
        abnormalData.put("abnormalType", "轻微波动");
        abnormalData.put("riskLevel", "normal");

        pipelineService.startPipeline(eventId, abnormalData);

        // 验证被跳过
        var trail = auditService.getEventAuditTrail(eventId);
        assertTrue(trail.stream().anyMatch(log -> "SKIP".equals(log.get("operation_type"))));
    }
}