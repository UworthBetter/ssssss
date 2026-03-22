package com.qkyd.ai.controller;

import com.qkyd.ai.model.dto.EventInsightResultDTO;
import com.qkyd.ai.model.dto.EventInsightSnapshotSummaryDTO;
import com.qkyd.ai.service.IEventInsightService;
import com.qkyd.common.core.domain.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Event insight endpoint for the operator card.
 */
@Tag(name = "Event insight", description = "Returns one structured event insight card")
@RestController
@RequestMapping("/ai/event")
public class EventInsightController {
    @Autowired
    private IEventInsightService eventInsightService;

    @Operation(
            summary = "Get event insight",
            description = "Returns a structured event insight payload for one event"
    )
    @GetMapping("/insight/{eventId}")
    public AjaxResult getInsight(
            @PathVariable("eventId") Long eventId,
            @RequestParam(value = "refresh", required = false, defaultValue = "false")
            boolean refresh) {
        if (eventId == null) {
            return AjaxResult.error("eventId cannot be null");
        }
        EventInsightResultDTO insight = eventInsightService.buildInsight(eventId, refresh);
        return AjaxResult.success(insight);
    }

    @Operation(
            summary = "List event insight snapshots",
            description = "Returns recent event insight snapshot summaries for one event"
    )
    @GetMapping("/insight/{eventId}/snapshots")
    public AjaxResult listSnapshots(
            @PathVariable("eventId") Long eventId,
            @RequestParam(value = "limit", required = false, defaultValue = "10")
            int limit) {
        if (eventId == null) {
            return AjaxResult.error("eventId cannot be null");
        }
        List<EventInsightSnapshotSummaryDTO> snapshots =
                eventInsightService.listInsightSnapshots(eventId, limit);
        return AjaxResult.success(snapshots);
    }

    @Operation(
            summary = "Get one event insight snapshot",
            description = "Returns one historical event insight payload for one event snapshot"
    )
    @GetMapping("/insight/{eventId}/snapshots/{snapshotId}")
    public AjaxResult getSnapshot(
            @PathVariable("eventId") Long eventId,
            @PathVariable("snapshotId") Long snapshotId) {
        if (eventId == null || snapshotId == null) {
            return AjaxResult.error("eventId and snapshotId cannot be null");
        }
        EventInsightResultDTO insight = eventInsightService.getInsightSnapshot(eventId, snapshotId);
        if (insight == null) {
            return AjaxResult.error("snapshot not found");
        }
        return AjaxResult.success(insight);
    }
}
