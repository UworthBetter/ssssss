package com.qkyd.ai.controller;

import com.qkyd.ai.model.dto.EventInsightResultDTO;
import com.qkyd.ai.service.IEventInsightService;
import com.qkyd.common.core.domain.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Event insight endpoint for the operator card.
 */
@Tag(name = "事件研判", description = "为异常事件生成结构化研判卡片")
@RestController
@RequestMapping("/ai/event")
public class EventInsightController {
    @Autowired
    private IEventInsightService eventInsightService;

    @Operation(summary = "获取事件研判卡", description = "根据异常事件ID返回结构化的研判结果")
    @GetMapping("/insight/{eventId}")
    public AjaxResult getInsight(@PathVariable("eventId") Long eventId) {
        if (eventId == null) {
            return AjaxResult.error("eventId不能为空");
        }
        EventInsightResultDTO insight = eventInsightService.buildInsight(eventId);
        return AjaxResult.success(insight);
    }
}
