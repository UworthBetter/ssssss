package com.qkyd.ai.agent.impl;

import com.qkyd.ai.agent.EventPipelineAgent;
import com.qkyd.ai.agent.EventPipelineAgentContext;
import com.qkyd.ai.agent.EventPipelineAgentResult;
import com.qkyd.health.domain.HealthSubject;
import com.qkyd.health.domain.UeitException;
import com.qkyd.health.service.IHealthSubjectService;
import com.qkyd.health.service.IUeitExceptionService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PerceptionEventPipelineAgent implements EventPipelineAgent {
    private final IUeitExceptionService exceptionService;
    private final IHealthSubjectService healthSubjectService;

    public PerceptionEventPipelineAgent(IUeitExceptionService exceptionService,
                                        IHealthSubjectService healthSubjectService) {
        this.exceptionService = exceptionService;
        this.healthSubjectService = healthSubjectService;
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public String getAgentKey() {
        return "perception";
    }

    @Override
    public String getAgentName() {
        return "感知 Agent";
    }

    @Override
    public EventPipelineAgentResult execute(EventPipelineAgentContext context) {
        UeitException event = exceptionService.selectUeitExceptionById(context.getEventId());
        if (event == null) {
            return EventPipelineAgentResult.failed("未找到原始异常事件", "感知 Agent 无法加载事件源数据", Map.of());
        }

        Map<String, Object> workingData = context.getWorkingData();
        workingData.putIfAbsent("userId", event.getUserId());
        workingData.putIfAbsent("deviceId", event.getDeviceId());
        workingData.putIfAbsent("abnormalType", event.getType());
        workingData.putIfAbsent("abnormalValue", event.getValue());
        workingData.put("location", event.getLocation());
        workingData.put("occurredAt", event.getCreateTime());

        HealthSubject subject = event.getUserId() == null
                ? null
                : healthSubjectService.selectHealthSubjectBySubjectId(event.getUserId());
        if (subject != null && subject.getAge() != null) {
            workingData.putIfAbsent("age", subject.getAge());
            workingData.put("subjectName", subject.getNickName());
        } else if (event.getNickName() != null) {
            workingData.put("subjectName", event.getNickName());
        }

        int integrityFields = 0;
        if (workingData.get("userId") != null) {
            integrityFields++;
        }
        if (workingData.get("deviceId") != null) {
            integrityFields++;
        }
        if (workingData.get("abnormalType") != null) {
            integrityFields++;
        }
        if (workingData.get("abnormalValue") != null) {
            integrityFields++;
        }
        if (workingData.get("location") != null) {
            integrityFields++;
        }

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("subjectId", workingData.get("userId"));
        output.put("subjectName", workingData.get("subjectName"));
        output.put("deviceId", workingData.get("deviceId"));
        output.put("abnormalType", workingData.get("abnormalType"));
        output.put("abnormalValue", workingData.get("abnormalValue"));
        output.put("location", workingData.get("location"));
        output.put("integrity", integrityFields + "/5");

        return EventPipelineAgentResult.completed(
                "已装配实时事件上下文",
                "感知 Agent 已汇聚对象、设备、异常指标和位置等基础感知信息",
                "warning",
                output
        );
    }
}
