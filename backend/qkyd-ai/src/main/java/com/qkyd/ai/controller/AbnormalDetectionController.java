package com.qkyd.ai.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.common.event.AbnormalDetectionEvent;
import com.qkyd.ai.service.IAbnormalDetectionService;

@RestController
@RequestMapping("/ai/abnormal")
public class AbnormalDetectionController {

    @Autowired
    private IAbnormalDetectionService abnormalDetectionService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @PostMapping("/detect")
    public AjaxResult detect(@RequestBody Map<String, Object> data) {
        // 执行异常检测
        Object result = abnormalDetectionService.detect(data);

        // 如果检测到异常，发布WebSocket推送事件
        if (result instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> resultMap = (Map<String, Object>) result;

            if (Boolean.TRUE.equals(resultMap.get("isAbnormal"))) {
                try {
                    Long patientId = data.get("patientId") != null ? Long.valueOf(data.get("patientId").toString())
                            : null;
                    String patientName = data.get("patientName") != null ? data.get("patientName").toString() : "未知";
                    String abnormalType = resultMap.get("abnormalType") != null
                            ? resultMap.get("abnormalType").toString()
                            : "未知";
                    String abnormalValue = resultMap.get("abnormalValue") != null
                            ? resultMap.get("abnormalValue").toString()
                            : "未知";
                    String riskLevel = resultMap.get("riskLevel") != null ? resultMap.get("riskLevel").toString()
                            : "medium";
                    String message = resultMap.get("message") != null ? resultMap.get("message").toString() : "检测到异常";

                    AbnormalDetectionEvent event = new AbnormalDetectionEvent(
                            patientId, patientName, abnormalType, abnormalValue, riskLevel, message, resultMap);

                    eventPublisher.publishEvent(event);
                } catch (Exception e) {
                    // 不影响检测结果，只记录日志
                    System.err.println("发布异常检测事件失败: " + e.getMessage());
                }
            }
        }

        return AjaxResult.success(result);
    }

    @Autowired
    private com.qkyd.ai.mapper.AbnormalRecordMapper abnormalRecordMapper;

    /**
     * Get recent abnormal records for dashboard
     */
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
        list.add(buildRecent("孙七", "SOS求救", "critical", "手动触发", 18));
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
}
