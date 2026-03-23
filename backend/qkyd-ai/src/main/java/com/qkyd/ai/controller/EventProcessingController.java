package com.qkyd.ai.controller;

import com.qkyd.ai.service.IEventProcessingPipelineService;
import com.qkyd.ai.service.IOperationAuditService;
import com.qkyd.common.core.domain.AjaxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/event-processing")
public class EventProcessingController {

    private static final Logger log = LoggerFactory.getLogger(EventProcessingController.class);

    @Autowired
    private IEventProcessingPipelineService pipelineService;

    @Autowired
    private IOperationAuditService auditService;

    @PostMapping("/start/{eventId}")
    public AjaxResult startPipeline(@PathVariable Long eventId,
                                    @RequestBody(required = false) Map<String, Object> abnormalData) {
        pipelineService.startPipeline(eventId, abnormalData);
        return AjaxResult.success(pipelineService.getPipelineStatus(eventId));
    }

    @GetMapping("/status/{eventId}")
    public AjaxResult getStatus(@PathVariable Long eventId) {
        try {
            Map<String, Object> status = pipelineService.getPipelineStatus(eventId);
            if (status == null || status.isEmpty()) {
                return AjaxResult.success(emptyStatus(eventId));
            }
            return AjaxResult.success(status);
        } catch (Exception e) {
            log.warn("failed to fetch processing status for event {}", eventId, e);
            return AjaxResult.success(emptyStatus(eventId));
        }
    }

    @GetMapping("/audit-trail/{eventId}")
    public AjaxResult getAuditTrail(@PathVariable Long eventId) {
        return AjaxResult.success(auditService.getEventAuditTrail(eventId));
    }

    @PostMapping("/feedback/{eventId}")
    public AjaxResult recordFeedback(@PathVariable Long eventId,
                                     @RequestBody Map<String, Object> feedback) {
        String actualOutcome = (String) feedback.get("actualOutcome");
        Integer feedbackScore = (Integer) feedback.get("feedbackScore");
        pipelineService.recordFeedback(eventId, actualOutcome, feedbackScore);
        return AjaxResult.success("反馈已记录");
    }

    private Map<String, Object> emptyStatus(Long eventId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event_id", eventId);
        payload.put("auditTrail", List.of());
        payload.put("recentInsightSnapshots", List.of());
        payload.put("agentTrace", List.of());
        payload.put("stages", List.of());
        payload.put("totalDuration", 0);
        return payload;
    }
}
