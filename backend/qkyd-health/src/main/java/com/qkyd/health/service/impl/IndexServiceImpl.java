package com.qkyd.health.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.qkyd.health.domain.UeitException;
import com.qkyd.health.domain.dto.AgeSexGroupCountDto;
import com.qkyd.health.domain.dto.RealTimeData;
import com.qkyd.health.mapper.UeitBloodMapper;
import com.qkyd.health.mapper.UeitDeviceInfoExtendMapper;
import com.qkyd.health.mapper.UeitExceptionMapper;
import com.qkyd.health.service.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class IndexServiceImpl implements IndexService {

    private static final Logger log = LoggerFactory.getLogger(IndexServiceImpl.class);
    private static final int DASHBOARD_EVENT_LIMIT = 12;
    private static final long DASHBOARD_PENDING_WINDOW_MINUTES = 360L;
    private static final long DASHBOARD_RESOLVED_WINDOW_MINUTES = 120L;
    private static final Map<String, List<String>> ALERT_KEYWORDS = createAlertKeywords();

    @Autowired
    private UeitBloodMapper bloodMapper;

    @Autowired
    private UeitExceptionMapper exceptionMapper;

    @Autowired
    private UeitDeviceInfoExtendMapper deviceInfoExtendMapper;

    @Override
    public AgeSexGroupCountDto getAgeSexGroupCount() {
        try {
            AgeSexGroupCountDto dto = bloodMapper.getAgeSexGroupCount();
            if (dto != null) {
                return dto;
            }
        } catch (Exception e) {
            log.warn("getAgeSexGroupCount failed, use mock data: {}", e.getMessage());
        }
        return mockAgeSexGroupCount();
    }

    @Override
    public JSONObject getExceptionData(String type, int pageNum) {
        JSONObject result = new JSONObject();
        List<UeitException> exceptionList = filterDashboardExceptions(loadDashboardExceptions(), type);
        int total = exceptionList.size();

        result.put("total", total);
        result.put("rows", exceptionList);
        result.put("code", 200);
        result.put("msg", "success");
        return result;
    }

    @Override
    public List<RealTimeData> realTimeData() {
        try {
            List<RealTimeData> dataList = deviceInfoExtendMapper.realTimeData();
            if (dataList != null && !dataList.isEmpty()) {
                return dataList;
            }
        } catch (Exception e) {
            log.warn("realTimeData failed, use mock data: {}", e.getMessage());
        }
        return mockRealtimeList();
    }

    @Override
    public Map<String, Integer> dashboardSummary() {
        Map<String, Integer> summary = new LinkedHashMap<>();
        List<UeitException> dashboardExceptions = loadDashboardExceptions();
        List<UeitException> pendingExceptions = dashboardExceptions.stream()
                .filter(this::isPendingException)
                .toList();
        List<RealTimeData> realtimeRows = loadRealtimeRows();

        summary.put("stepExceptionCount", countByCategory(pendingExceptions, "step"));
        summary.put("fenceExceptionCount", countByCategory(pendingExceptions, "fence"));
        summary.put("sosHelpCount", countByCategory(pendingExceptions, "sos"));
        summary.put("temperatureExceptionCount", countByCategory(pendingExceptions, "temperature"));
        summary.put("heartRateExceptionCount", countByCategory(pendingExceptions, "heart"));
        summary.put("spo2ExceptionCount", countByCategory(pendingExceptions, "oxygen"));
        summary.put("bloodPressureExceptionCount", countByCategory(pendingExceptions, "pressure"));
        summary.put("abnormalUserCount", countUniqueUsers(pendingExceptions));
        summary.put("todayExceptionUsers", countUniqueUsers(pendingExceptions));
        summary.put("highRiskCount", countHighRiskExceptions(pendingExceptions));
        summary.put("todayHighRisk", countHighRiskExceptions(pendingExceptions));
        summary.put("avgResponseTime", averagePendingMinutes(pendingExceptions));
        summary.put("closedRatio", closedRatioPercent(dashboardExceptions));
        summary.put("onlineDeviceCount", realtimeRows.isEmpty() ? mockRealtimeList().size() : realtimeRows.size());

        return summary;
    }

    @Override
    public List indexUserLocation() {
        return deviceInfoExtendMapper.indexUserLocation();
    }

    private List<UeitException> loadDashboardExceptions() {
        try {
            List<UeitException> queried = exceptionMapper.selectExceptionForDashboard();
            if (queried != null && !queried.isEmpty()) {
                return normalizeDashboardExceptions(queried);
            }
        } catch (Exception e) {
            log.warn("dashboardSummary query exceptions failed: {}", e.getMessage());
        }
        return normalizeDashboardExceptions(mockExceptionList("all"));
    }

    private List<RealTimeData> loadRealtimeRows() {
        try {
            List<RealTimeData> queried = deviceInfoExtendMapper.realTimeData();
            if (queried != null && !queried.isEmpty()) {
                return queried;
            }
        } catch (Exception e) {
            log.warn("dashboardSummary query online devices failed: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    private List<UeitException> normalizeDashboardExceptions(List<UeitException> source) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>();
        }

        long now = System.currentTimeMillis();
        Map<String, UeitException> latestByKey = new LinkedHashMap<>();
        for (UeitException item : source) {
            if (item == null) {
                continue;
            }
            UeitException normalized = normalizeDashboardException(item);
            if (!shouldDisplayOnDashboard(normalized, now)) {
                continue;
            }
            latestByKey.putIfAbsent(buildDashboardIdentity(normalized), normalized);
        }

        List<UeitException> result = new ArrayList<>(latestByKey.values());
        result.sort(Comparator.comparingLong(this::exceptionTimeMillis).reversed());
        if (result.size() > DASHBOARD_EVENT_LIMIT) {
            return new ArrayList<>(result.subList(0, DASHBOARD_EVENT_LIMIT));
        }
        return result;
    }

    private UeitException normalizeDashboardException(UeitException source) {
        if (source.getCreateTime() == null && source.getReadTime() != null) {
            source.setCreateTime(source.getReadTime());
        }
        if (source.getReadTime() == null && source.getCreateTime() != null) {
            source.setReadTime(source.getCreateTime());
        }
        if (source.getState() == null || source.getState().isBlank()) {
            source.setState("0");
        }
        return source;
    }

    private boolean shouldDisplayOnDashboard(UeitException item, long now) {
        long ageMinutes = Math.max(0L, (now - exceptionTimeMillis(item)) / (60L * 1000L));
        if (isPendingException(item)) {
            return ageMinutes <= DASHBOARD_PENDING_WINDOW_MINUTES;
        }
        return ageMinutes <= DASHBOARD_RESOLVED_WINDOW_MINUTES;
    }

    private List<UeitException> filterDashboardExceptions(List<UeitException> source, String type) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>();
        }
        String normalizedType = normalizeText(type);
        if (normalizedType.isBlank() || "all".equals(normalizedType)) {
            return source;
        }

        List<String> keywords = resolveCategoryKeywords(normalizedType);
        List<UeitException> filtered = new ArrayList<>();
        for (UeitException item : source) {
            if (matchesAnyKeyword(item.getType(), keywords)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private int countByCategory(List<UeitException> source, String category) {
        if (source == null || source.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (UeitException item : source) {
            if (matchesAnyKeyword(item.getType(), resolveCategoryKeywords(category))) {
                count++;
            }
        }
        return count;
    }

    private int countUniqueUsers(List<UeitException> source) {
        if (source == null || source.isEmpty()) {
            return 0;
        }

        Set<Long> seen = new LinkedHashSet<>();
        for (UeitException item : source) {
            if (item.getUserId() != null) {
                seen.add(item.getUserId());
            }
        }
        return seen.size();
    }

    private int countHighRiskExceptions(List<UeitException> source) {
        if (source == null || source.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (UeitException item : source) {
            if (matchesAnyKeyword(item.getType(), resolveCategoryKeywords("sos"))
                    || matchesAnyKeyword(item.getType(), resolveCategoryKeywords("heart"))
                    || matchesAnyKeyword(item.getType(), resolveCategoryKeywords("oxygen"))
                    || matchesAnyKeyword(item.getType(), resolveCategoryKeywords("pressure"))
                    || matchesAnyKeyword(item.getType(), resolveCategoryKeywords("temperature"))) {
                count++;
            }
        }
        return count;
    }

    private int averagePendingMinutes(List<UeitException> source) {
        if (source == null || source.isEmpty()) {
            return 0;
        }

        long now = System.currentTimeMillis();
        long totalMinutes = 0L;
        int count = 0;

        for (UeitException item : source) {
            if ("1".equals(String.valueOf(item.getState()))) {
                continue;
            }
            Date baseTime = item.getReadTime() != null ? item.getReadTime() : item.getCreateTime();
            if (baseTime == null) {
                continue;
            }
            totalMinutes += Math.max(0L, (now - baseTime.getTime()) / (60L * 1000L));
            count++;
        }

        if (count == 0) {
            return 0;
        }
        return (int) Math.round((double) totalMinutes / count);
    }

    private int closedRatioPercent(List<UeitException> source) {
        if (source == null || source.isEmpty()) {
            return 0;
        }

        int resolved = 0;
        for (UeitException item : source) {
            if ("1".equals(String.valueOf(item.getState()))) {
                resolved++;
            }
        }
        return (int) Math.round((resolved * 100.0) / source.size());
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT)
                .replace('_', ' ')
                .replace('-', ' ')
                .trim();
    }

    private boolean isPendingException(UeitException item) {
        return item != null && !"1".equals(String.valueOf(item.getState()));
    }

    private boolean matchesAnyKeyword(String value, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return false;
        }
        String normalized = normalizeText(value);
        if (normalized.isBlank()) {
            return false;
        }
        for (String keyword : keywords) {
            String normalizedKeyword = normalizeText(keyword);
            if (!normalizedKeyword.isBlank() && normalized.contains(normalizedKeyword)) {
                return true;
            }
        }
        return false;
    }

    private long exceptionTimeMillis(UeitException item) {
        if (item == null) {
            return 0L;
        }
        Date eventTime = item.getReadTime() != null ? item.getReadTime() : item.getCreateTime();
        if (eventTime == null) {
            eventTime = item.getUpdateTime();
        }
        return eventTime == null ? 0L : eventTime.getTime();
    }

    private String buildDashboardIdentity(UeitException item) {
        String subject = item.getUserId() != null
                ? "user:" + item.getUserId()
                : item.getDeviceId() != null
                ? "device:" + item.getDeviceId()
                : "nick:" + normalizeText(item.getNickName());
        return subject + "|" + resolveCategoryKey(item.getType());
    }

    private String resolveCategoryKey(String type) {
        String normalized = normalizeText(type);
        for (Map.Entry<String, List<String>> entry : ALERT_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                String normalizedKeyword = normalizeText(keyword);
                if (!normalizedKeyword.isBlank() && normalized.contains(normalizedKeyword)) {
                    return entry.getKey();
                }
            }
        }
        return normalized;
    }

    private List<String> resolveCategoryKeywords(String category) {
        return ALERT_KEYWORDS.getOrDefault(normalizeText(category), List.of(category));
    }

    private static Map<String, List<String>> createAlertKeywords() {
        Map<String, List<String>> mapping = new LinkedHashMap<>();
        mapping.put("step", List.of("step", "steps", "activity", "walk", "move", "步数", "活动"));
        mapping.put("fence", List.of("fence", "geofence", "breach", "zone", "围栏", "越界", "电子围栏"));
        mapping.put("sos", List.of("sos", "help request", "emergency", "求助", "紧急"));
        mapping.put("temperature", List.of("temperature", "temp", "fever", "体温", "发热"));
        mapping.put("heart", List.of("heart", "pulse", "heart rate", "心率", "心跳"));
        mapping.put("oxygen", List.of("spo2", "oxygen", "blood oxygen", "血氧"));
        mapping.put("pressure", List.of("blood pressure", "blood_pressure", "pressure", "血压"));
        return mapping;
    }

    private AgeSexGroupCountDto mockAgeSexGroupCount() {
        AgeSexGroupCountDto dto = new AgeSexGroupCountDto();
        dto.setA(2);
        dto.setB(4);
        dto.setC(10);
        dto.setD(21);
        dto.setE(30);
        dto.setMan(34);
        dto.setWoman(32);
        dto.setNono(1);
        return dto;
    }

    private List<UeitException> mockExceptionList(String type) {
        List<UeitException> list = new ArrayList<>();
        list.add(buildException(1001L, 9101L, 5001L, "心率异常", "132 bpm", "0",
                "上海-人民广场", 121.4737, 31.2304, 20));
        list.add(buildException(1002L, 9102L, 5002L, "围栏越界", "超出1.8公里", "0",
                "上海-南京东路", 121.4812, 31.2279, 16));
        list.add(buildException(1003L, 9103L, 5003L, "体温异常", "38.6 C", "1",
                "上海-南京西路", 121.4648, 31.2245, 12));
        list.add(buildException(1004L, 9104L, 5004L, "血氧异常", "89%", "0",
                "上海-四川北路", 121.4901, 31.2372, 8));
        list.add(buildException(1005L, 9105L, 5005L, "SOS求救", "手动触发", "0",
                "上海-长寿路", 121.4555, 31.2401, 5));
        list.add(buildException(1006L, 9106L, 5006L, "步数异常", "15400 steps/day", "1",
                "上海-五角场", 121.5010, 31.2456, 3));

        if (type == null || type.isBlank() || "all".equalsIgnoreCase(type)) {
            return list;
        }

        String normalizedType = normalizeText(type);
        List<UeitException> filtered = new ArrayList<>();
        for (UeitException item : list) {
            if (matchesAnyKeyword(item.getType(), resolveCategoryKeywords(normalizedType))) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private UeitException buildException(Long id, Long userId, Long deviceId, String type, String value,
                                         String state, String location, double longitude, double latitude,
                                         int minutesAgo) {
        UeitException exception = new UeitException();
        exception.setId(id);
        exception.setUserId(userId);
        exception.setDeviceId(deviceId);
        exception.setType(type);
        exception.setValue(value);
        exception.setState(state);
        exception.setLocation(location);
        exception.setLongitude(BigDecimal.valueOf(longitude));
        exception.setLatitude(BigDecimal.valueOf(latitude));
        exception.setReadTime(new Date(System.currentTimeMillis() - minutesAgo * 60L * 1000L));
        exception.setCreateTime(new Date(System.currentTimeMillis() - minutesAgo * 60L * 1000L));
        exception.setNickName("演示对象" + userId);
        return exception;
    }

    private List<RealTimeData> mockRealtimeList() {
        List<RealTimeData> list = new ArrayList<>();
        list.add(buildRealtime(9101, "演示对象A", 0, 72, 85, "97", "36.4"));
        list.add(buildRealtime(9102, "演示对象B", 1, 68, 92, "96", "36.8"));
        list.add(buildRealtime(9103, "演示对象C", 0, 75, 78, "98", "36.6"));
        return list;
    }

    private RealTimeData buildRealtime(int userId, String nickName, int sex, int age, int hr, String spo2, String temp) {
        RealTimeData data = new RealTimeData();
        data.setUserId(userId);
        data.setNickName(nickName);
        data.setSex(sex);
        data.setAge(age);
        data.setHeartRateValue(hr);
        data.setSpo2Value(spo2);
        data.setTemperatureValue(temp);
        data.setReadTime(new Date());
        return data;
    }
}
