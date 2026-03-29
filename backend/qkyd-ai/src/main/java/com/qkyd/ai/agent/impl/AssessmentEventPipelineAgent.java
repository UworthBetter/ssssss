package com.qkyd.ai.agent.impl;

import com.qkyd.ai.agent.EventPipelineAgent;
import com.qkyd.ai.agent.EventPipelineAgentContext;
import com.qkyd.ai.agent.EventPipelineAgentResult;
import com.qkyd.ai.service.IRiskScoreService;
import com.qkyd.common.core.domain.AjaxResult;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AssessmentEventPipelineAgent implements EventPipelineAgent {
    private final IRiskScoreService riskScoreService;

    public AssessmentEventPipelineAgent(IRiskScoreService riskScoreService) {
        this.riskScoreService = riskScoreService;
    }

    @Override
    public int getOrder() {
        return 30;
    }

    @Override
    public String getAgentKey() {
        return "assess";
    }

    @Override
    public String getAgentName() {
        return "评估 Agent";
    }

    @Override
    public EventPipelineAgentResult execute(EventPipelineAgentContext context) {
        AjaxResult result = riskScoreService.assessRisk(context.copyWorkingData());
        Map<String, Object> payload = new LinkedHashMap<>();
        if (result != null && result.isSuccess() && result.get("data") instanceof Map<?, ?> data) {
            data.forEach((key, value) -> payload.put(String.valueOf(key), value));
        }

        int riskScore = intValue(payload.get("riskScore"), intValue(payload.get("risk_score"), context.getPriority()));
        String riskLevel = stringValue(payload.containsKey("riskLevel") ? payload.get("riskLevel") : payload.get("risk_level"));
        context.setRiskPayload(payload);
        context.setRiskScore(riskScore);
        context.setRiskLevel(riskLevel);

        Map<String, Object> output = new LinkedHashMap<>(payload);
        output.put("riskScore", riskScore);
        output.put("riskLevel", riskLevel);

        return EventPipelineAgentResult.completed(
                "风险评分 " + riskScore + "，等级 " + riskLevel,
                "评估 Agent 已结合异常事件和对象背景完成风险量化",
                "decision",
                output
        );
    }

    private int intValue(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
