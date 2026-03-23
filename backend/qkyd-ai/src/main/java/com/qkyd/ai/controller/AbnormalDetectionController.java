package com.qkyd.ai.controller;

import com.qkyd.ai.mapper.AbnormalRecordMapper;
import com.qkyd.ai.model.vo.AbnormalDetectionVO;
import com.qkyd.ai.service.IAbnormalDetectionService;
import com.qkyd.ai.service.IEventProcessingPipelineService;
import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.common.event.AbnormalDetectionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/abnormal")
public class AbnormalDetectionController {

    @Autowired
    private IAbnormalDetectionService abnormalDetectionService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private IEventProcessingPipelineService pipelineService;

    @Autowired
    private AbnormalRecordMapper abnormalRecordMapper;

    @PostMapping("/detect")
    public AjaxResult detect(@RequestBody Map<String, Object> data) {
        Object result = abnormalDetectionService.detect(data);

        if (result instanceof AbnormalDetectionVO vo && Boolean.TRUE.equals(vo.getAbnormal())) {
            try {
                Long eventId = parseLong(data.get("eventId"));
                Map<String, Object> pipelineData = new HashMap<>(data);
                pipelineData.put("abnormalType", String.valueOf(data.getOrDefault("metricType", "健康异常")));
                pipelineData.put("abnormalValue", String.valueOf(data.getOrDefault("value", vo.getDetail())));
                pipelineData.put("message", vo.getMessage());

                if (eventId != null) {
                    pipelineService.startPipeline(eventId, pipelineData);
                }

                Long patientId = parseLong(data.get("patientId"));
                String patientName = String.valueOf(data.getOrDefault("patientName", "未知"));
                String abnormalType = String.valueOf(pipelineData.get("abnormalType"));
                String abnormalValue = String.valueOf(pipelineData.get("abnormalValue"));
                String riskLevel = String.valueOf(data.getOrDefault("riskLevel", "warning"));
                String message = vo.getMessage() != null ? vo.getMessage() : "检测到异常";

                AbnormalDetectionEvent event = new AbnormalDetectionEvent(
                        patientId,
                        patientName,
                        abnormalType,
                        abnormalValue,
                        riskLevel,
                        message,
                        pipelineData);
                eventPublisher.publishEvent(event);
            } catch (Exception e) {
                System.err.println("处理异常事件失败: " + e.getMessage());
            }
        }

        return AjaxResult.success(result);
    }

    @GetMapping("/recent")
    public AjaxResult getRecentAbnormals(@RequestParam(name = "limit", defaultValue = "10") int limit) {
        List<?> rows;
        try {
            rows = abnormalRecordMapper.selectRecent(limit);
        } catch (Exception e) {
            rows = Collections.emptyList();
        }
        if (rows == null || rows.isEmpty()) {
            return AjaxResult.success(mockRecentAbnormal(limit));
        }
        return AjaxResult.success(rows);
    }

    private List<Map<String, Object>> mockRecentAbnormal(int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(buildRecent("张三", "心率异常", "high", "132 bpm", 2));
        list.add(buildRecent("李四", "体温偏高", "medium", "38.6 ℃", 5));
        list.add(buildRecent("王五", "血氧偏低", "high", "89%", 9));
        list.add(buildRecent("赵六", "围栏越界", "medium", "2.1 km", 13));
        list.add(buildRecent("孙七", "SOS求助", "critical", "手动触发", 18));
        if (limit <= 0 || limit >= list.size()) {
            return list;
        }
        return list.subList(0, limit);
    }

    private Map<String, Object> buildRecent(String patientName, String abnormalType, String riskLevel,
                                            String abnormalValue, int minutesAgo) {
        Map<String, Object> item = new HashMap<>();
        item.put("patientName", patientName);
        item.put("abnormalType", abnormalType);
        item.put("riskLevel", riskLevel);
        item.put("abnormalValue", abnormalValue);
        item.put("detectedTime", new Date(System.currentTimeMillis() - minutesAgo * 60L * 1000L));
        return item;
    }

    private Long parseLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value != null) {
            try {
                return Long.parseLong(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
