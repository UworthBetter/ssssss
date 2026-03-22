package com.qkyd.health.service.ai;

import com.qkyd.health.domain.dto.ai.EventInsightHealthContext;

/**
 * Aggregates real health-domain context for event insight enrichment.
 */
public interface IEventInsightHealthContextService {
    EventInsightHealthContext loadContext(Long userId, Long deviceId, String abnormalType);
}
