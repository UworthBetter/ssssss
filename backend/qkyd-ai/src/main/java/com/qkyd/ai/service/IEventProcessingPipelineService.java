package com.qkyd.ai.service;

import com.qkyd.ai.model.dto.EventInsightResultDTO;
import java.util.Map;

public interface IEventProcessingPipelineService {
    /**
     * 启动完整的事件处理管道
     * 自动执行: 异常检测 → 事件洞察 → 风险评估 → 处置建议 → 执行
     */
    void startPipeline(Long eventId, Map<String, Object> abnormalData);

    /**
     * 获取事件处理进度
     */
    Map<String, Object> getPipelineStatus(Long eventId);

    /**
     * 记录处置反馈(用于模型优化)
     */
    void recordFeedback(Long pipelineId, String actualOutcome, Integer feedbackScore);
}