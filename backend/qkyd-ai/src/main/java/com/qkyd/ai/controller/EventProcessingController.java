package com.qkyd.ai.controller;

import com.qkyd.ai.service.IEventProcessingPipelineService;
import com.qkyd.ai.service.IOperationAuditService;
import com.qkyd.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/ai/event-processing")
public class EventProcessingController {

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
        Map<String, Object> status = pipelineService.getPipelineStatus(eventId);
        if (status == null || status.isEmpty()) {
            try {
                pipelineService.startPipeline(eventId, null);
                status = pipelineService.getPipelineStatus(eventId);
            } catch (Exception e) {
                return AjaxResult.error("事件流水线初始化失败: " + e.getMessage());
            }
        }
        if (status == null || status.isEmpty()) {
            return AjaxResult.error("未找到对应事件流水线");
        }
        return AjaxResult.success(status);
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
}
