package com.qkyd.ai.agent;

import java.util.LinkedHashMap;
import java.util.Map;

public class EventPipelineAgentResult {
    private final String status;
    private final String summary;
    private final String detail;
    private final String handoffTo;
    private final Map<String, Object> outputPayload;

    private EventPipelineAgentResult(String status,
                                     String summary,
                                     String detail,
                                     String handoffTo,
                                     Map<String, Object> outputPayload) {
        this.status = status;
        this.summary = summary;
        this.detail = detail;
        this.handoffTo = handoffTo;
        this.outputPayload = outputPayload == null ? new LinkedHashMap<>() : new LinkedHashMap<>(outputPayload);
    }

    public static EventPipelineAgentResult completed(String summary,
                                                     String detail,
                                                     String handoffTo,
                                                     Map<String, Object> outputPayload) {
        return new EventPipelineAgentResult("completed", summary, detail, handoffTo, outputPayload);
    }

    public static EventPipelineAgentResult skipped(String summary,
                                                   String detail,
                                                   Map<String, Object> outputPayload) {
        return new EventPipelineAgentResult("skipped", summary, detail, null, outputPayload);
    }

    public static EventPipelineAgentResult failed(String summary,
                                                  String detail,
                                                  Map<String, Object> outputPayload) {
        return new EventPipelineAgentResult("failed", summary, detail, null, outputPayload);
    }

    public String getStatus() {
        return status;
    }

    public String getSummary() {
        return summary;
    }

    public String getDetail() {
        return detail;
    }

    public String getHandoffTo() {
        return handoffTo;
    }

    public Map<String, Object> getOutputPayload() {
        return outputPayload;
    }
}
