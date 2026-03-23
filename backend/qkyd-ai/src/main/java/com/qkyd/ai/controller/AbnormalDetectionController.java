package com.qkyd.ai.controller;

import com.qkyd.ai.mapper.AbnormalRecordMapper;
import com.qkyd.ai.model.entity.AbnormalRecord;
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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
                pipelineData.put("abnormalType", String.valueOf(data.getOrDefault("metricType", "health_abnormal")));
                pipelineData.put("abnormalValue", String.valueOf(data.getOrDefault("value", vo.getDetail())));
                pipelineData.put("message", vo.getMessage());

                if (eventId != null) {
                    pipelineService.startPipeline(eventId, pipelineData);
                }

                Long patientId = parseLong(data.get("patientId"));
                String patientName = String.valueOf(data.getOrDefault("patientName", "Unknown"));
                String abnormalType = String.valueOf(pipelineData.get("abnormalType"));
                String abnormalValue = String.valueOf(pipelineData.get("abnormalValue"));
                String riskLevel = String.valueOf(data.getOrDefault("riskLevel", "warning"));
                String message = vo.getMessage() != null ? vo.getMessage() : "Abnormal condition detected";

                eventPublisher.publishEvent(new AbnormalDetectionEvent(
                        patientId,
                        patientName,
                        abnormalType,
                        abnormalValue,
                        riskLevel,
                        message,
                        pipelineData
                ));
            } catch (Exception e) {
                System.err.println("Failed to publish abnormal event: " + e.getMessage());
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

        List<Map<String, Object>> normalizedRows = normalizeRecentRows(rows, limit);
        if (normalizedRows.isEmpty()) {
            return AjaxResult.success(mockRecentAbnormal(limit));
        }
        return AjaxResult.success(normalizedRows);
    }

    @GetMapping("/trend")
    public AjaxResult getAbnormalTrend(
            @RequestParam(name = "metricType", required = false) String metricType,
            @RequestParam(name = "hours", defaultValue = "24") int hours) {

        int safeHours = Math.max(6, Math.min(hours, 72));
        String normalizedMetricType = normalizeMetricType(metricType);
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime currentHour = LocalDateTime.now(zoneId).truncatedTo(ChronoUnit.HOURS);
        LocalDateTime startHour = currentHour.minusHours(safeHours - 1L);
        DateTimeFormatter formatter = safeHours > 24
                ? DateTimeFormatter.ofPattern("MM-dd HH:00")
                : DateTimeFormatter.ofPattern("HH:00");

        Map<String, Integer> counts = new LinkedHashMap<>();
        for (int i = 0; i < safeHours; i++) {
            LocalDateTime slot = startHour.plusHours(i);
            counts.put(slot.format(formatter), 0);
        }

        try {
            Date startDate = Date.from(startHour.atZone(zoneId).toInstant());
            List<AbnormalRecord> records = abnormalRecordMapper.selectSince(startDate, normalizedMetricType);
            for (AbnormalRecord record : records) {
                Date detectedTime = record.getDetectedTime() != null ? record.getDetectedTime() : record.getCreateTime();
                if (detectedTime == null) {
                    continue;
                }
                String slotKey = LocalDateTime.ofInstant(detectedTime.toInstant(), zoneId)
                        .truncatedTo(ChronoUnit.HOURS)
                        .format(formatter);
                if (counts.containsKey(slotKey)) {
                    counts.put(slotKey, counts.get(slotKey) + 1);
                }
            }
        } catch (Exception ignored) {
            // Keep the zero-filled slots when the table is still empty or temporarily unavailable.
        }

        List<Map<String, Object>> trend = new ArrayList<>();
        counts.forEach((label, value) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("label", label);
            item.put("value", value);
            trend.add(item);
        });
        return AjaxResult.success(trend);
    }

    private List<Map<String, Object>> mockRecentAbnormal(int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(buildRecent(9101L, "演示对象A", "心率异常", "high", "132 bpm", 96, "演示手表-A01", "AI_RULE_V2", 2));
        list.add(buildRecent(9102L, "演示对象B", "体温异常", "medium", "38.6 C", 88, "演示手表-A02", "AI_RULE_V2", 5));
        list.add(buildRecent(9103L, "演示对象C", "血氧异常", "high", "89%", 93, "演示手表-A03", "VITAL_FUSION", 9));
        list.add(buildRecent(9104L, "演示对象D", "围栏越界", "medium", "超出1.8公里", 82, "演示手表-A04", "GEOFENCE_GUARD", 13));
        list.add(buildRecent(9105L, "演示对象E", "SOS求救", "critical", "手动触发", 99, "演示手表-A05", "EMERGENCY_AGENT", 18));
        list.add(buildRecent(9106L, "演示对象F", "步数异常", "low", "15400 steps/day", 79, "演示手表-A06", "ACTIVITY_BASELINE", 24));
        if (limit <= 0 || limit >= list.size()) {
            return list;
        }
        return list.subList(0, limit);
    }

    private Map<String, Object> buildRecent(Long userId, String patientName, String abnormalType, String riskLevel,
                                            String abnormalValue, int confidence, String deviceName,
                                            String source, int minutesAgo) {
        Map<String, Object> item = new HashMap<>();
        Date detectedTime = new Date(System.currentTimeMillis() - minutesAgo * 60L * 1000L);
        item.put("id", userId);
        item.put("userId", userId);
        item.put("patientName", patientName);
        item.put("abnormalType", abnormalType);
        item.put("riskLevel", riskLevel);
        item.put("abnormalValue", abnormalValue);
        item.put("confidence", confidence);
        item.put("deviceName", deviceName);
        item.put("source", source);
        item.put("detectedTime", detectedTime);
        item.put("createTime", detectedTime);
        item.put("readTime", detectedTime);
        return item;
    }

    private List<Map<String, Object>> normalizeRecentRows(List<?> rows, int limit) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> normalized = new ArrayList<>();
        for (Object row : rows) {
            Map<String, Object> item = normalizeRecentRow(row);
            if (item != null) {
                normalized.add(item);
            }
            if (limit > 0 && normalized.size() >= limit) {
                break;
            }
        }
        return normalized;
    }

    private Map<String, Object> normalizeRecentRow(Object row) {
        if (row instanceof AbnormalRecord record) {
            if (isBlank(record.getAbnormalType()) && isBlank(record.getAbnormalValue()) && record.getDetectedTime() == null) {
                return null;
            }

            Map<String, Object> item = new HashMap<>();
            Long userId = record.getUserId();
            Date detectedTime = record.getDetectedTime() != null ? record.getDetectedTime() : record.getCreateTime();
            item.put("id", record.getId());
            item.put("userId", userId);
            item.put("patientName", userId == null ? "Demo patient" : "Patient-" + userId);
            item.put("abnormalType", defaultText(record.getAbnormalType(), defaultText(record.getMetricType(), "Health abnormal")));
            item.put("riskLevel", defaultText(record.getRiskLevel(), "medium"));
            item.put("abnormalValue", defaultText(record.getAbnormalValue(), "-"));
            item.put("confidence", confidenceFromRisk(record.getRiskLevel()));
            item.put("deviceName", defaultText(record.getDeviceId(), "Smart device"));
            item.put("source", defaultText(record.getDetectionMethod(), "AI_RULE_V2"));
            item.put("detectedTime", detectedTime);
            item.put("createTime", record.getCreateTime() != null ? record.getCreateTime() : detectedTime);
            item.put("readTime", detectedTime);
            item.put("metricType", record.getMetricType());
            return item;
        }

        if (row instanceof Map<?, ?> rawMap) {
            Map<String, Object> source = new HashMap<>();
            rawMap.forEach((key, value) -> source.put(String.valueOf(key), value));
            if (isBlank(source.get("abnormalType")) && isBlank(source.get("abnormalValue")) && source.get("detectedTime") == null) {
                return null;
            }
            source.putIfAbsent("patientName", source.getOrDefault("nickName", "Demo patient"));
            source.putIfAbsent("riskLevel", "medium");
            source.putIfAbsent("confidence", confidenceFromRisk(String.valueOf(source.get("riskLevel"))));
            source.putIfAbsent("deviceName", "Smart device");
            source.putIfAbsent("source", "AI_RULE_V2");
            source.putIfAbsent("createTime", source.get("detectedTime"));
            source.putIfAbsent("readTime", source.get("detectedTime"));
            return source;
        }

        return null;
    }

    private String normalizeMetricType(String metricType) {
        if (metricType == null || metricType.isBlank()) {
            return null;
        }

        String normalized = metricType.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
        return switch (normalized) {
            case "heart", "heart_rate" -> "heart_rate";
            case "oxygen", "spo2", "blood_oxygen" -> "spo2";
            case "temperature", "temp" -> "temperature";
            case "pressure", "blood_pressure", "bp" -> "blood_pressure";
            case "fence", "geofence" -> "fence";
            case "step", "steps" -> "step";
            case "sos" -> "sos";
            default -> normalized;
        };
    }

    private int confidenceFromRisk(String riskLevel) {
        String normalized = String.valueOf(riskLevel).toLowerCase(Locale.ROOT);
        if (normalized.contains("critical") || normalized.contains("danger")) {
            return 99;
        }
        if (normalized.contains("high")) {
            return 92;
        }
        if (normalized.contains("low")) {
            return 78;
        }
        return 85;
    }

    private String defaultText(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private boolean isBlank(Object value) {
        return value == null || String.valueOf(value).trim().isEmpty() || "null".equalsIgnoreCase(String.valueOf(value).trim());
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
