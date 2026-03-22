package com.qkyd.ai.service;

import com.qkyd.ai.model.dto.EventInsightResultDTO;
import com.qkyd.ai.model.dto.EventInsightSnapshotSummaryDTO;

import java.util.List;

/**
 * Builds a structured insight card for an abnormal event.
 */
public interface IEventInsightService {
    EventInsightResultDTO buildInsight(Long eventId);

    EventInsightResultDTO buildInsight(Long eventId, boolean refresh);

    EventInsightResultDTO getInsightSnapshot(Long eventId, Long snapshotId);

    List<EventInsightSnapshotSummaryDTO> listInsightSnapshots(Long eventId, int limit);
}
