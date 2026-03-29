package com.qkyd.ai.agent;

public interface EventPipelineAgent {
    int getOrder();

    String getAgentKey();

    String getAgentName();

    EventPipelineAgentResult execute(EventPipelineAgentContext context);
}
