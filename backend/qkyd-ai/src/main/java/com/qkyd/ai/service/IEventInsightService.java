package com.qkyd.ai.service;

import com.qkyd.ai.model.dto.EventInsightResultDTO;

/**
 * Builds a structured insight card for an abnormal event.
 */
public interface IEventInsightService {
    EventInsightResultDTO buildInsight(Long eventId);
}
