package com.qkyd.ai.agent.impl;

import com.qkyd.ai.agent.EventPipelineAgent;
import com.qkyd.ai.agent.EventPipelineAgentContext;
import com.qkyd.ai.agent.EventPipelineAgentResult;
import com.qkyd.ai.service.IDispositionRuleEngine;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ExecuteEventPipelineAgent implements EventPipelineAgent {
    private final IDispositionRuleEngine dispositionRuleEngine;

    public ExecuteEventPipelineAgent(IDispositionRuleEngine dispositionRuleEngine) {
        this.dispositionRuleEngine = dispositionRuleEngine;
    }

    @Override
    public int getOrder() {
        return 50;
    }

    @Override
    public String getAgentKey() {
        return "execute";
    }

    @Override
    public String getAgentName() {
        return "执行 Agent";
    }

    @Override
    public EventPipelineAgentResult execute(EventPipelineAgentContext context) {
        String abnormalType = context.getAbnormalType();
        int riskScore = context.getRiskScore();
        String disposition = dispositionRuleEngine.generateDisposition(abnormalType, riskScore);
        String notificationLevel = dispositionRuleEngine.getNotificationLevel(abnormalType, riskScore);
        boolean autoExecute = dispositionRuleEngine.shouldAutoExecute(abnormalType, riskScore);

        context.setDisposition(disposition);
        context.setNotificationLevel(notificationLevel);
        context.setAutoExecute(autoExecute);

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("disposition", disposition);
        output.put("notificationLevel", notificationLevel);
        output.put("autoExecute", autoExecute);
        output.put("riskScore", riskScore);
        output.put("riskLevel", context.getRiskLevel());

        return EventPipelineAgentResult.completed(
                autoExecute ? "已触发自动处置" : "待人工确认处置",
                "执行 Agent 已生成通知级别和处置动作，并推动闭环执行",
                null,
                output
        );
    }
}
