package com.qkyd.web.controller.platform;

import com.qkyd.common.core.controller.BaseController;
import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.health.domain.HealthSubject;
import com.qkyd.health.domain.UeitDeviceInfo;
import com.qkyd.health.domain.UeitException;
import com.qkyd.health.service.IHealthSubjectService;
import com.qkyd.health.service.IUeitDeviceInfoService;
import com.qkyd.health.service.IUeitExceptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/platform")
public class PlatformSearchController extends BaseController {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 30;

    @Autowired
    private IHealthSubjectService healthSubjectService;

    @Autowired
    private IUeitDeviceInfoService ueitDeviceInfoService;

    @Autowired
    private IUeitExceptionService ueitExceptionService;

    @GetMapping("/search")
    public AjaxResult search(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "center", required = false, defaultValue = "workbench") String center,
            @RequestParam(value = "limit", required = false, defaultValue = "" + DEFAULT_LIMIT) Integer limit) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        if (normalizedKeyword.isEmpty()) {
            return AjaxResult.success(buildPayload(normalizedKeyword, List.of()));
        }

        int resolvedLimit = limit == null ? DEFAULT_LIMIT : Math.max(1, Math.min(limit, MAX_LIMIT));
        List<Map<String, Object>> results = new ArrayList<>();
        appendSubjectResults(results, normalizedKeyword, center);
        appendDeviceResults(results, normalizedKeyword, center);
        appendEventResults(results, normalizedKeyword, center);

        results.sort(Comparator.comparingInt(item -> -toInt(item.get("score"))));
        if (results.size() > resolvedLimit) {
            results = new ArrayList<>(results.subList(0, resolvedLimit));
        }

        return AjaxResult.success(buildPayload(normalizedKeyword, results));
    }

    private Map<String, Object> buildPayload(String keyword, List<Map<String, Object>> results) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("keyword", keyword);
        payload.put("results", results);
        return payload;
    }

    private void appendSubjectResults(List<Map<String, Object>> results, String keyword, String center) {
        List<HealthSubject> subjects;
        try {
            subjects = healthSubjectService.selectHealthSubjectList(new HealthSubject());
        } catch (Exception ex) {
            subjects = List.of();
        }

        for (HealthSubject subject : subjects) {
            if (!matchesSubject(subject, keyword)) {
                continue;
            }

            String phone = safeString(subject.getPhonenumber());
            String displayName = firstNonBlank(subject.getNickName(), subject.getSubjectName(), "对象");
            String subjectName = firstNonBlank(subject.getSubjectName(), displayName);

            results.add(buildResult(
                    "subject",
                    "subject:" + firstNonBlank(String.valueOf(subject.getSubjectId()), displayName),
                    displayName,
                    "对象中心 · " + subjectName,
                    phone.isEmpty() ? "服务对象档案" : "手机号 " + phone,
                    buildSubjectQuery(subject, keyword),
                    computeScore(center, "subject", keyword,
                            subject.getSubjectName(), subject.getNickName(), subject.getPhonenumber(), subject.getSubjectId())
            ));
        }
    }

    private void appendDeviceResults(List<Map<String, Object>> results, String keyword, String center) {
        List<UeitDeviceInfo> devices;
        try {
            devices = ueitDeviceInfoService.selectUeitDeviceInfoList(new UeitDeviceInfo());
        } catch (Exception ex) {
            devices = List.of();
        }

        for (UeitDeviceInfo device : devices) {
            if (!matchesDevice(device, keyword)) {
                continue;
            }

            String imei = safeString(device.getImei());
            String title = firstNonBlank(device.getName(), "设备");
            String description = device.getUserId() == null
                    ? firstNonBlank(device.getType(), "设备档案")
                    : "绑定对象 #" + device.getUserId();

            results.add(buildResult(
                    "device",
                    "device:" + firstNonBlank(String.valueOf(device.getId()), imei, title),
                    title,
                    "设备中心 · IMEI " + firstNonBlank(imei, "-"),
                    description,
                    buildDeviceQuery(device, keyword),
                    computeScore(center, "device", keyword,
                            device.getName(), device.getImei(), device.getUserId(), device.getType(), device.getId())
            ));
        }
    }

    private void appendEventResults(List<Map<String, Object>> results, String keyword, String center) {
        List<UeitException> events;
        try {
            events = ueitExceptionService.selectUeitExceptionList(new UeitException());
        } catch (Exception ex) {
            events = List.of();
        }

        for (UeitException event : events) {
            if (!matchesEvent(event, keyword)) {
                continue;
            }

            String title = firstNonBlank(event.getType(), "事件");
            String description = firstNonBlank(event.getLocation(), event.getValue(), "异常事件");

            results.add(buildResult(
                    "event",
                    "event:" + firstNonBlank(String.valueOf(event.getId()), title),
                    title,
                    "事件中心 · 用户 " + firstNonBlank(String.valueOf(event.getUserId()), "-")
                            + " / 设备 " + firstNonBlank(String.valueOf(event.getDeviceId()), "-"),
                    description,
                    buildEventQuery(event, keyword),
                    computeScore(center, "event", keyword,
                            event.getType(), event.getValue(), event.getLocation(),
                            event.getUserId(), event.getDeviceId(), event.getId())
            ));
        }
    }

    private Map<String, String> buildSubjectQuery(HealthSubject subject, String keyword) {
        Map<String, String> query = new LinkedHashMap<>();
        if (isExact(subject.getPhonenumber(), keyword)) {
            query.put("phonenumber", safeString(subject.getPhonenumber()));
        } else if (subject.getSubjectId() != null && isExact(subject.getSubjectId(), keyword)) {
            query.put("subjectName", firstNonBlank(subject.getSubjectName(), subject.getNickName(), keyword));
        } else {
            query.put("subjectName", firstNonBlank(subject.getSubjectName(), subject.getNickName(), keyword));
        }
        return query;
    }

    private Map<String, String> buildDeviceQuery(UeitDeviceInfo device, String keyword) {
        Map<String, String> query = new LinkedHashMap<>();
        if (isExact(device.getImei(), keyword)) {
            query.put("imei", safeString(device.getImei()));
        } else if (device.getUserId() != null && isExact(device.getUserId(), keyword)) {
            query.put("userId", String.valueOf(device.getUserId()));
        } else if (device.getId() != null && isExact(device.getId(), keyword)) {
            query.put("name", firstNonBlank(device.getName(), keyword));
        } else {
            query.put("name", firstNonBlank(device.getName(), keyword));
        }
        return query;
    }

    private Map<String, String> buildEventQuery(UeitException event, String keyword) {
        Map<String, String> query = new LinkedHashMap<>();
        if (event.getUserId() != null && isExact(event.getUserId(), keyword)) {
            query.put("userId", String.valueOf(event.getUserId()));
            query.put("type", firstNonBlank(event.getType(), ""));
        } else {
            query.put("type", firstNonBlank(event.getType(), keyword));
        }
        return query;
    }

    private Map<String, Object> buildResult(
            String kind,
            String id,
            String title,
            String subtitle,
            String description,
            Map<String, String> query,
            int score) {
        Map<String, Object> target = new LinkedHashMap<>();
        target.put("path", "/" + kind);
        target.put("query", query);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", id);
        item.put("kind", kind);
        item.put("title", title);
        item.put("subtitle", subtitle);
        item.put("description", description);
        item.put("score", score);
        item.put("target", target);
        return item;
    }

    private boolean matchesSubject(HealthSubject subject, String keyword) {
        return matchesAny(keyword, subject.getSubjectName(), subject.getNickName(), subject.getPhonenumber(), subject.getSubjectId());
    }

    private boolean matchesDevice(UeitDeviceInfo device, String keyword) {
        return matchesAny(keyword, device.getName(), device.getImei(), device.getType(), device.getUserId(), device.getId());
    }

    private boolean matchesEvent(UeitException event, String keyword) {
        return matchesAny(keyword, event.getType(), event.getValue(), event.getLocation(), event.getUserId(), event.getDeviceId(), event.getId());
    }

    private boolean matchesAny(String keyword, Object... values) {
        for (Object value : values) {
            if (contains(value, keyword)) {
                return true;
            }
        }
        return false;
    }

    private int computeScore(String center, String kind, String keyword, Object... values) {
        int score = 66;
        for (Object value : values) {
            if (isExact(value, keyword)) {
                score = 100;
                break;
            }
            if (contains(value, keyword)) {
                score = Math.max(score, 78);
            }
        }
        if (!"workbench".equalsIgnoreCase(center) && !"ai".equalsIgnoreCase(center)
                && kind.equalsIgnoreCase(safeString(center))) {
            score += 8;
        }
        return score;
    }

    private boolean contains(Object value, String keyword) {
        return normalize(value).contains(normalize(keyword));
    }

    private boolean isExact(Object value, String keyword) {
        String normalizedValue = normalize(value);
        String normalizedKeyword = normalize(keyword);
        return !normalizedValue.isEmpty() && normalizedValue.equals(normalizedKeyword);
    }

    private String normalize(Object value) {
        return safeString(value).trim().toLowerCase(Locale.ROOT);
    }

    private String safeString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ex) {
            return 0;
        }
    }
}
