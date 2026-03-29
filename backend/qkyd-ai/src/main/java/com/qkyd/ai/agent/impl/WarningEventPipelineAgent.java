package com.qkyd.ai.agent.impl;

import com.qkyd.ai.agent.EventPipelineAgent;
import com.qkyd.ai.agent.EventPipelineAgentContext;
import com.qkyd.ai.agent.EventPipelineAgentResult;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class WarningEventPipelineAgent implements EventPipelineAgent {
    @Override
    public int getOrder() {
        return 20;
    }

    @Override
    public String getAgentKey() {
        return "warning";
    }

    @Override
    public String getAgentName() {
        return "预警 Agent";
    }

    @Override
    public EventPipelineAgentResult execute(EventPipelineAgentContext context) {
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("abnormalType", context.getAbnormalType());
        output.put("priority", context.getPriority());

        if (context.getPriority() < 30) {
            String reason = "低优先级事件保留留痕，不触发后续协同处置";
            context.stopPipeline(reason);
            output.put("decision", "archive");
            return EventPipelineAgentResult.skipped("低优先级事件已归档", reason, output);
        }

        output.put("decision", "dispatch");
        return EventPipelineAgentResult.completed(
                "异常已转化为可处理风险事件",
                "预警 Agent 已完成异常标准化与优先级判定，准备移交评估 Agent",
                "assess",
                output
        );
    }
}
