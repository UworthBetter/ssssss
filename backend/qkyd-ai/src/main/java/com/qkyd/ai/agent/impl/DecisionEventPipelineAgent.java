package com.qkyd.ai.agent.impl;

import com.qkyd.ai.agent.EventPipelineAgent;
import com.qkyd.ai.agent.EventPipelineAgentContext;
import com.qkyd.ai.agent.EventPipelineAgentResult;
import com.qkyd.ai.model.dto.EventInsightResultDTO;
import com.qkyd.ai.service.IEventInsightService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DecisionEventPipelineAgent implements EventPipelineAgent {
    private final IEventInsightService eventInsightService;

    public DecisionEventPipelineAgent(IEventInsightService eventInsightService) {
        this.eventInsightService = eventInsightService;
    }

    @Override
    public int getOrder() {
        return 40;
    }

    @Override
    public String getAgentKey() {
        return "decision";
    }

    @Override
    public String getAgentName() {
        return "决策 Agent";
    }

    @Override
    public EventPipelineAgentResult execute(EventPipelineAgentContext context) {
        EventInsightResultDTO insight = eventInsightService.buildInsight(context.getEventId(), false);
        context.setInsight(insight);

        Map<String, Object> output = new LinkedHashMap<>();
        if (insight != null) {
            output.put("summary", insight.getSummary());
            output.put("freshness", insight.getFreshness());
            if (insight.getAdvice() != null) {
                output.put("suggestedActions", insight.getAdvice().getSuggestedActions());
                output.put("notifyWho", insight.getAdvice().getNotifyWho());
                output.put("operatorNote", insight.getAdvice().getOperatorNote());
            }
        }

        return EventPipelineAgentResult.completed(
                insight != null && insight.getSummary() != null ? insight.getSummary() : "已生成医学语义洞察",
                "决策 Agent 已输出结构化医学洞察与干预建议",
                "execute",
                output
        );
    }
}
