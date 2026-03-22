package com.qkyd.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qkyd.ai.mapper.EventInsightSnapshotMapper;
import com.qkyd.ai.model.dto.EventInsightResultDTO;
import com.qkyd.ai.model.dto.EventInsightSnapshotSummaryDTO;
import com.qkyd.ai.model.entity.EventInsightSnapshotRecord;
import com.qkyd.ai.service.IEventInsightService;
import com.qkyd.health.domain.HealthSubject;
import com.qkyd.health.domain.UeitException;
import com.qkyd.health.domain.dto.ai.EventInsightHealthContext;
import com.qkyd.health.service.IHealthSubjectService;
import com.qkyd.health.service.IUeitExceptionService;
import com.qkyd.health.service.ai.IEventInsightHealthContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Default implementation for the multi-agent event insight flow.
 */
@Service
public class EventInsightServiceImpl implements IEventInsightService {
    private static final long SNAPSHOT_MAX_AGE_MILLIS = 12L * 60L * 60L * 1000L;
    private static final long FRESHNESS_WARNING_AGE_MILLIS = 6L * 60L * 60L * 1000L;

    private static final List<String> FALLBACK_TYPES = Arrays.asList(
            "心率异常",
            "血氧偏低",
            "体温偏高",
            "围栏越界",
            "SOS急救"
    );

    @Autowired
    private IUeitExceptionService ueitExceptionService;

    @Autowired
    private IHealthSubjectService healthSubjectService;

    @Autowired
    private EventInsightSnapshotMapper eventInsightSnapshotMapper;

    @Autowired
    private IEventInsightHealthContextService eventInsightHealthContextService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public EventInsightResultDTO buildInsight(Long eventId) {
        return buildInsight(eventId, false);
    }

    @Override
    public EventInsightResultDTO buildInsight(Long eventId, boolean refresh) {
        UeitException source = loadException(eventId);
        boolean fallbackEvent = source == null;
        if (fallbackEvent) {
            source = createFallbackException(eventId);
        }

        EventInsightResultDTO.ParsedEvent parsedEvent = buildParsedEvent(eventId, source);
        EventInsightHealthContext realContext = loadRealContext(
                parsedEvent.getUserId(),
                parsedEvent.getDeviceId(),
                parsedEvent.getAbnormalType()
        );
        EventInsightSnapshotRecord snapshot = loadSnapshotRecord(eventId);
        EventInsightResultDTO cachedInsight = readSnapshot(snapshot);
        if (cachedInsight != null
                && !shouldRefreshSnapshot(snapshot, cachedInsight, realContext, refresh, fallbackEvent)) {
            return enrichFreshness(cachedInsight);
        }

        HealthSubject subject = loadSubject(source.getUserId());
        List<UeitException> history = loadHistory(source.getUserId());

        EventInsightResultDTO.ContextSnapshot context = buildContext(
                source,
                subject,
                history,
                parsedEvent,
                realContext,
                fallbackEvent
        );
        EventInsightResultDTO.RiskAssessment risk = buildRisk(parsedEvent, context, history);
        EventInsightResultDTO.Advice advice = buildAdvice(parsedEvent, context, risk);
        EventInsightResultDTO.Trace trace = buildTrace(
                parsedEvent,
                context,
                risk,
                advice,
                fallbackEvent
        );

        EventInsightResultDTO result = new EventInsightResultDTO();
        result.setEventId(eventId);
        result.setGeneratedAt(new Date());
        result.setParsedEvent(parsedEvent);
        result.setContext(context);
        result.setRisk(risk);
        result.setAdvice(advice);
        result.setTrace(trace);
        result.setSummary(buildSummary(source, subject, parsedEvent, risk));
        enrichFreshness(result);
        saveSnapshot(result);
        return result;
    }

    @Override
    public List<EventInsightSnapshotSummaryDTO> listInsightSnapshots(Long eventId, int limit) {
        if (eventId == null) {
            return Collections.emptyList();
        }
        int resolvedLimit = limit <= 0 ? 10 : Math.min(limit, 20);
        try {
            List<EventInsightSnapshotSummaryDTO> rows =
                    eventInsightSnapshotMapper.selectRecentByEventId(eventId, resolvedLimit);
            if (rows == null || rows.isEmpty()) {
                return Collections.emptyList();
            }
            for (EventInsightSnapshotSummaryDTO row : rows) {
                enrichSnapshotSummaryFreshness(row);
            }
            return rows;
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    @Override
    public EventInsightResultDTO getInsightSnapshot(Long eventId, Long snapshotId) {
        if (eventId == null || snapshotId == null) {
            return null;
        }
        try {
            EventInsightSnapshotRecord snapshot =
                    eventInsightSnapshotMapper.selectByIdAndEventId(eventId, snapshotId);
            return enrichFreshness(readSnapshot(snapshot));
        } catch (Exception ignored) {
            return null;
        }
    }

    private EventInsightSnapshotRecord loadSnapshotRecord(Long eventId) {
        if (eventId == null) {
            return null;
        }
        try {
            return eventInsightSnapshotMapper.selectLatestByEventId(eventId);
        } catch (Exception ignored) {
            return null;
        }
    }

    private EventInsightResultDTO readSnapshot(EventInsightSnapshotRecord snapshot) {
        if (snapshot == null || isBlank(snapshot.getPayloadJson())) {
            return null;
        }
        try {
            return objectMapper.readValue(snapshot.getPayloadJson(), EventInsightResultDTO.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private EventInsightResultDTO enrichFreshness(EventInsightResultDTO result) {
        if (result == null) {
            return null;
        }
        result.setFreshness(buildFreshness(result));
        return result;
    }

    private EventInsightSnapshotSummaryDTO enrichSnapshotSummaryFreshness(EventInsightSnapshotSummaryDTO summary) {
        if (summary == null) {
            return null;
        }
        EventInsightResultDTO.Freshness freshness = buildFreshness(summary.getGeneratedAt(), false);
        summary.setFreshnessState(freshness.getState());
        summary.setFreshnessTone(freshness.getTone());
        summary.setFreshnessNote(freshness.getNote());
        return summary;
    }

    private EventInsightResultDTO.Freshness buildFreshness(EventInsightResultDTO result) {
        boolean fallbackUsed = result.getTrace() != null && Boolean.TRUE.equals(result.getTrace().getFallbackUsed());
        return buildFreshness(result.getGeneratedAt(), fallbackUsed);
    }

    private EventInsightResultDTO.Freshness buildFreshness(Date generatedAt, boolean fallbackUsed) {
        EventInsightResultDTO.Freshness freshness = new EventInsightResultDTO.Freshness();
        if (fallbackUsed) {
            freshness.setState("fallback");
            freshness.setTone("warning");
            freshness.setNote("当前为兜底研判结果，建议在后端可用时重新刷新。");
            return freshness;
        }
        if (generatedAt == null) {
            freshness.setState("unknown");
            freshness.setTone("neutral");
            freshness.setNote("");
            return freshness;
        }

        long ageMillis = Math.max(0L, System.currentTimeMillis() - generatedAt.getTime());
        String ageLabel = formatAgeLabel(ageMillis);
        if (ageMillis >= FRESHNESS_WARNING_AGE_MILLIS) {
            freshness.setState("aging");
            freshness.setTone("warning");
            freshness.setNote("当前研判生成于 " + ageLabel + "，如果事件上下文刚发生变化，建议手动刷新。");
            return freshness;
        }

        freshness.setState("fresh");
        freshness.setTone("neutral");
        freshness.setNote("当前研判生成于 " + ageLabel + "。");
        return freshness;
    }

    private String formatAgeLabel(long ageMillis) {
        long minutes = Math.max(1L, ageMillis / (60L * 1000L));
        if (minutes < 60L) {
            return minutes + " 分钟前";
        }
        long hours = minutes / 60L;
        if (hours < 24L) {
            return hours + " 小时前";
        }
        long days = hours / 24L;
        return days + " 天前";
    }

    private boolean shouldRefreshSnapshot(
            EventInsightSnapshotRecord snapshot,
            EventInsightResultDTO cachedInsight,
            EventInsightHealthContext realContext,
            boolean refresh,
            boolean fallbackEvent) {
        if (refresh) {
            return true;
        }
        if (snapshot == null || cachedInsight == null) {
            return true;
        }
        Date snapshotGeneratedAt = snapshot.getGeneratedAt() != null
                ? snapshot.getGeneratedAt()
                : cachedInsight.getGeneratedAt();
        if (snapshotGeneratedAt == null) {
            return true;
        }
        if (isSnapshotExpired(snapshotGeneratedAt)) {
            return true;
        }
        if (Boolean.TRUE.equals(snapshot.getFallbackUsed()) && !fallbackEvent && realContext != null) {
            return true;
        }
        return hasNewerRealContext(realContext, snapshotGeneratedAt);
    }

    private boolean isSnapshotExpired(Date snapshotGeneratedAt) {
        return snapshotGeneratedAt != null
                && System.currentTimeMillis() - snapshotGeneratedAt.getTime() >= SNAPSHOT_MAX_AGE_MILLIS;
    }

    private boolean hasNewerRealContext(
            EventInsightHealthContext realContext,
            Date snapshotGeneratedAt) {
        if (realContext == null || snapshotGeneratedAt == null) {
            return false;
        }
        if (realContext.getLastLocationTime() != null
                && realContext.getLastLocationTime().after(snapshotGeneratedAt)) {
            return true;
        }
        if (realContext.getBindingTime() != null
                && realContext.getBindingTime().after(snapshotGeneratedAt)) {
            return true;
        }
        return realContext.getLatestReportDate() != null
                && realContext.getLatestReportDate().after(snapshotGeneratedAt);
    }

    private void saveSnapshot(EventInsightResultDTO result) {
        if (result == null || result.getEventId() == null) {
            return;
        }
        try {
            EventInsightSnapshotRecord snapshot = new EventInsightSnapshotRecord();
            snapshot.setEventId(result.getEventId());
            snapshot.setSummary(result.getSummary());
            snapshot.setRiskLevel(result.getRisk() != null ? result.getRisk().getRiskLevel() : null);
            snapshot.setRiskScore(result.getRisk() != null ? result.getRisk().getRiskScore() : null);
            snapshot.setOrchestratorVersion(result.getTrace() != null
                    ? result.getTrace().getOrchestratorVersion()
                    : null);
            snapshot.setFallbackUsed(result.getTrace() != null
                    ? result.getTrace().getFallbackUsed()
                    : null);
            snapshot.setPayloadJson(writePayload(result));
            snapshot.setGeneratedAt(result.getGeneratedAt());
            snapshot.setCreateTime(new Date());
            eventInsightSnapshotMapper.insert(snapshot);
        } catch (Exception ignored) {
            // Keep the operator flow available even if snapshot persistence fails.
        }
    }

    private String writePayload(EventInsightResultDTO result) throws JsonProcessingException {
        return objectMapper.writeValueAsString(result);
    }

    private UeitException loadException(Long eventId) {
        if (eventId == null) {
            return null;
        }
        try {
            return ueitExceptionService.selectUeitExceptionById(eventId);
        } catch (Exception ignored) {
            return null;
        }
    }

    private HealthSubject loadSubject(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            return healthSubjectService.selectHealthSubjectBySubjectId(userId);
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<UeitException> loadHistory(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        try {
            List<UeitException> rows =
                    ueitExceptionService.selectUeitExceptionListByUserId(userId.intValue());
            return rows != null ? rows : Collections.emptyList();
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private EventInsightHealthContext loadRealContext(
            Long userId,
            Long deviceId,
            String abnormalType) {
        try {
            return eventInsightHealthContextService.loadContext(userId, deviceId, abnormalType);
        } catch (Exception ignored) {
            return null;
        }
    }

    private EventInsightResultDTO.ParsedEvent buildParsedEvent(Long eventId, UeitException source) {
        EventInsightResultDTO.ParsedEvent parsedEvent = new EventInsightResultDTO.ParsedEvent();
        parsedEvent.setUserId(source.getUserId());
        parsedEvent.setDeviceId(source.getDeviceId());
        parsedEvent.setAbnormalType(defaultString(source.getType(), deriveType(eventId)));
        parsedEvent.setMetricName(deriveMetricName(parsedEvent.getAbnormalType()));
        parsedEvent.setMetricValue(defaultString(
                source.getValue(),
                deriveMetricValue(parsedEvent.getAbnormalType(), eventId)
        ));
        parsedEvent.setOccurredAt(source.getCreateTime() != null
                ? source.getCreateTime()
                : source.getReadTime());
        parsedEvent.setLocation(defaultString(source.getLocation(), "系统模拟位置"));
        parsedEvent.setSource(source.getId() != null ? "health.exception" : "fallback.mock");
        return parsedEvent;
    }

    private EventInsightResultDTO.ContextSnapshot buildContext(
            UeitException source,
            HealthSubject subject,
            List<UeitException> history,
            EventInsightResultDTO.ParsedEvent parsedEvent,
            EventInsightHealthContext realContext,
            boolean fallbackEvent) {
        EventInsightResultDTO.ContextSnapshot context =
                new EventInsightResultDTO.ContextSnapshot();
        int age = realContext != null && realContext.getAge() != null
                ? realContext.getAge()
                : source.getAge() > 0
                ? source.getAge()
                : subject != null && subject.getAge() != null
                ? subject.getAge()
                : deriveAge(source.getUserId());
        context.setAge(age);
        context.setChronicDiseases(inferChronicDiseases(source, subject, parsedEvent));
        context.setRecentHealthTrend(resolveRecentHealthTrend(realContext, history, parsedEvent));
        context.setHistoricalAbnormalCount(resolveHistoricalAbnormalCount(realContext, history));
        context.setRecentSameTypeCount(resolveRecentSameTypeCount(realContext, history, parsedEvent));
        context.setDeviceStatus(resolveDeviceStatus(realContext, source, history, parsedEvent));
        context.setDeviceStatusReason(resolveDeviceStatusReason(
                realContext,
                source,
                history,
                parsedEvent,
                fallbackEvent
        ));
        context.setLastKnownLocation(resolveLastKnownLocation(realContext, source));
        context.setDataConfidence(resolveDataConfidence(realContext, fallbackEvent));
        return context;
    }

    private String resolveRecentHealthTrend(
            EventInsightHealthContext realContext,
            List<UeitException> history,
            EventInsightResultDTO.ParsedEvent parsedEvent) {
        if (realContext != null) {
            if (!isBlank(realContext.getLatestReportSummary())) {
                return realContext.getLatestReportSummary();
            }
            if (!isBlank(realContext.getLatestReportRiskLevel())) {
                return "latest report risk level: " + realContext.getLatestReportRiskLevel();
            }
        }
        return inferTrend(history, parsedEvent);
    }

    private Integer resolveHistoricalAbnormalCount(
            EventInsightHealthContext realContext,
            List<UeitException> history) {
        if (realContext != null && realContext.getHistoricalAbnormalCount() != null) {
            return realContext.getHistoricalAbnormalCount();
        }
        return history.size();
    }

    private Integer resolveRecentSameTypeCount(
            EventInsightHealthContext realContext,
            List<UeitException> history,
            EventInsightResultDTO.ParsedEvent parsedEvent) {
        if (realContext != null && realContext.getRecentSameTypeCount() != null) {
            return realContext.getRecentSameTypeCount();
        }
        return countSameType(history, parsedEvent.getAbnormalType());
    }

    private String resolveDeviceStatus(
            EventInsightHealthContext realContext,
            UeitException source,
            List<UeitException> history,
            EventInsightResultDTO.ParsedEvent parsedEvent) {
        if (realContext != null && !isBlank(realContext.getDeviceSignalStatus())) {
            return mapDeviceSignalStatus(realContext.getDeviceSignalStatus());
        }
        return inferDeviceStatus(source, history, parsedEvent);
    }

    private String resolveDeviceStatusReason(
            EventInsightHealthContext realContext,
            UeitException source,
            List<UeitException> history,
            EventInsightResultDTO.ParsedEvent parsedEvent,
            boolean fallbackEvent) {
        if (realContext != null && !isBlank(realContext.getDeviceSignalReason())) {
            return realContext.getDeviceSignalReason();
        }
        return inferDeviceStatusReason(source, history, parsedEvent, fallbackEvent);
    }

    private String resolveLastKnownLocation(
            EventInsightHealthContext realContext,
            UeitException source) {
        if (realContext != null && !isBlank(realContext.getLastKnownLocation())) {
            return realContext.getLastKnownLocation();
        }
        return defaultString(source.getLocation(), "暂无定位信息");
    }

    private String resolveDataConfidence(
            EventInsightHealthContext realContext,
            boolean fallbackEvent) {
        if (realContext != null && hasAnyRealContext(realContext)) {
            return "real_context";
        }
        return fallbackEvent ? "derived_fallback" : "real_event";
    }

    private boolean hasAnyRealContext(EventInsightHealthContext realContext) {
        return realContext.getAge() != null
                || !isBlank(realContext.getLastKnownLocation())
                || realContext.getHistoricalAbnormalCount() != null
                || !isBlank(realContext.getLatestReportSummary())
                || !isBlank(realContext.getDeviceSignalStatus());
    }

    private String mapDeviceSignalStatus(String signalStatus) {
        if ("active".equals(signalStatus)) {
            return "normal";
        }
        if ("stale".equals(signalStatus) || "bound_without_signal".equals(signalStatus)) {
            return "unstable";
        }
        if ("offline".equals(signalStatus) || "unbound".equals(signalStatus)) {
            return "offline";
        }
        return "unstable";
    }

    private EventInsightResultDTO.RiskAssessment buildRisk(
            EventInsightResultDTO.ParsedEvent parsedEvent,
            EventInsightResultDTO.ContextSnapshot context,
            List<UeitException> history) {
        int score = 0;
        List<String> ruleHits = new ArrayList<>();

        String type = parsedEvent.getAbnormalType();
        if (containsAny(type, "SOS", "急救", "跌倒")) {
            score += 35;
            ruleHits.add("TYPE_SOS");
        } else if (containsAny(type, "围栏", "越界")) {
            score += 22;
            ruleHits.add("TYPE_GEOFENCE");
        } else if (containsAny(type, "血氧", "spo2")) {
            score += 20;
            ruleHits.add("TYPE_SPO2");
        } else if (containsAny(type, "体温", "temp", "发热")) {
            score += 18;
            ruleHits.add("TYPE_TEMP");
        } else if (containsAny(type, "心率", "heart")) {
            score += 18;
            ruleHits.add("TYPE_HEART_RATE");
        } else {
            score += 12;
            ruleHits.add("TYPE_GENERAL");
        }

        Integer age = context.getAge();
        if (age != null && age >= 80) {
            score += 18;
            ruleHits.add("AGE_80_PLUS");
        } else if (age != null && age >= 65) {
            score += 10;
            ruleHits.add("AGE_65_PLUS");
        }

        if (context.getChronicDiseases() != null && !context.getChronicDiseases().isEmpty()) {
            score += 12;
            ruleHits.add("CHRONIC_DISEASE");
        }

        if (context.getRecentSameTypeCount() != null && context.getRecentSameTypeCount() >= 3) {
            score += 16;
            ruleHits.add("REPEATED_EVENTS");
        } else if (context.getRecentSameTypeCount() != null
                && context.getRecentSameTypeCount() >= 1) {
            score += 8;
            ruleHits.add("RECENT_REPEATS");
        }

        if ("offline".equals(context.getDeviceStatus())) {
            score += 20;
            ruleHits.add("DEVICE_OFFLINE");
        } else if ("unstable".equals(context.getDeviceStatus())) {
            score += 10;
            ruleHits.add("DEVICE_UNSTABLE");
        }

        if (history.size() >= 5) {
            score += 10;
            ruleHits.add("LONG_HISTORY");
        }

        score = Math.min(score, 100);

        String riskLevel;
        boolean immediateAction;
        if (score >= 80) {
            riskLevel = "danger";
            immediateAction = true;
        } else if (score >= 55) {
            riskLevel = "warning";
            immediateAction = true;
        } else {
            riskLevel = "normal";
            immediateAction = false;
        }

        EventInsightResultDTO.RiskAssessment risk =
                new EventInsightResultDTO.RiskAssessment();
        risk.setRiskLevel(riskLevel);
        risk.setRiskScore(score);
        risk.setImmediateAction(immediateAction);
        risk.setPossibleCauses(buildPossibleCauses(type, context, parsedEvent));
        risk.setAnalysisReasons(buildAnalysisReasons(parsedEvent, context, history, ruleHits, score));
        risk.setRuleHits(ruleHits);
        return risk;
    }

    private EventInsightResultDTO.Advice buildAdvice(
            EventInsightResultDTO.ParsedEvent parsedEvent,
            EventInsightResultDTO.ContextSnapshot context,
            EventInsightResultDTO.RiskAssessment risk) {
        boolean highRisk = "danger".equals(risk.getRiskLevel())
                || Boolean.TRUE.equals(risk.getImmediateAction());
        boolean contactFamily = highRisk || (context.getAge() != null && context.getAge() >= 75);
        boolean contactOrganization = highRisk
                && containsAny(parsedEvent.getAbnormalType(), "SOS", "急救", "围栏");

        EventInsightResultDTO.Advice advice = new EventInsightResultDTO.Advice();
        advice.setNotifyWho(buildNotifyTargets(highRisk, contactFamily, contactOrganization));
        advice.setSuggestedActions(buildSuggestedActions(parsedEvent, context, risk));
        advice.setOfflineCheck(highRisk || "offline".equals(context.getDeviceStatus()));
        advice.setContactFamily(contactFamily);
        advice.setContactOrganization(contactOrganization);
        advice.setOperatorNote(buildOperatorNote(parsedEvent, context, risk));
        return advice;
    }

    private EventInsightResultDTO.Trace buildTrace(
            EventInsightResultDTO.ParsedEvent parsedEvent,
            EventInsightResultDTO.ContextSnapshot context,
            EventInsightResultDTO.RiskAssessment risk,
            EventInsightResultDTO.Advice advice,
            boolean fallbackEvent) {
        EventInsightResultDTO.Trace trace = new EventInsightResultDTO.Trace();
        trace.setOrchestratorVersion("event-insight-orchestrator-v1");
        trace.setFallbackUsed(fallbackEvent);
        trace.setFallbackReason(fallbackEvent ? "未查询到原始异常事件，已使用兜底规则生成结果" : null);
        trace.setMissingFields(buildMissingFields(parsedEvent, context, risk, advice));
        trace.setSteps(buildTraceSteps(parsedEvent, context, risk, advice, fallbackEvent));
        return trace;
    }

    private List<EventInsightResultDTO.TraceStep> buildTraceSteps(
            EventInsightResultDTO.ParsedEvent parsedEvent,
            EventInsightResultDTO.ContextSnapshot context,
            EventInsightResultDTO.RiskAssessment risk,
            EventInsightResultDTO.Advice advice,
            boolean fallbackEvent) {
        List<EventInsightResultDTO.TraceStep> steps = new ArrayList<>();

        int parserResolved = countNonBlank(
                parsedEvent.getAbnormalType(),
                parsedEvent.getOccurredAt(),
                parsedEvent.getUserId(),
                parsedEvent.getDeviceId(),
                parsedEvent.getMetricName(),
                parsedEvent.getMetricValue()
        );
        steps.add(buildTraceStep(
                "parser",
                "Agent 1 事件解析",
                fallbackEvent ? "fallback" : resolveStatus(parserResolved, 6, false),
                parserResolved,
                6,
                defaultString(parsedEvent.getAbnormalType(), "待识别异常类型"),
                fallbackEvent
                        ? "原始事件缺失，解析结果来自兜底规则。"
                        : "已从异常事件中提取类型、时间、对象、设备和指标值。"
        ));

        int contextResolved = countNonBlank(
                context.getAge(),
                context.getChronicDiseases(),
                context.getRecentHealthTrend(),
                context.getHistoricalAbnormalCount(),
                context.getRecentSameTypeCount(),
                context.getDeviceStatus(),
                context.getDeviceStatusReason(),
                context.getLastKnownLocation()
        );
        steps.add(buildTraceStep(
                "context",
                "Agent 2 上下文补充",
                resolveStatus(contextResolved, 8, fallbackEvent),
                contextResolved,
                8,
                joinSummary(
                        context.getAge() != null ? context.getAge() + " 岁" : null,
                        context.getDeviceStatus()
                ),
                fallbackEvent
                        ? "事件来自兜底路径，上下文按安全默认值补全。"
                        : "已补充年龄、基础疾病、历史异常、趋势和设备状态。"
        ));

        int riskResolved = countNonBlank(
                risk.getRiskLevel(),
                risk.getRiskScore(),
                risk.getImmediateAction(),
                risk.getPossibleCauses(),
                risk.getAnalysisReasons(),
                risk.getRuleHits()
        );
        steps.add(buildTraceStep(
                "risk",
                "Agent 3 风险评估",
                resolveStatus(riskResolved, 6, fallbackEvent),
                riskResolved,
                6,
                joinSummary(
                        risk.getRiskLevel(),
                        Boolean.TRUE.equals(risk.getImmediateAction())
                                ? "建议立即处理"
                                : "可继续观察"
                ),
                "已结合异常类型、对象背景、重复次数和设备状态完成风险判断。"
        ));

        int adviceResolved = countNonBlank(
                advice.getNotifyWho(),
                advice.getSuggestedActions(),
                advice.getOfflineCheck(),
                advice.getContactFamily(),
                advice.getContactOrganization(),
                advice.getOperatorNote()
        );
        steps.add(buildTraceStep(
                "advice",
                "Agent 4 处置建议",
                resolveStatus(adviceResolved, 6, fallbackEvent),
                adviceResolved,
                6,
                advice.getNotifyWho() == null || advice.getNotifyWho().isEmpty()
                        ? "待生成通知对象"
                        : "通知 " + String.join("、", advice.getNotifyWho()),
                "已根据风险等级输出通知对象、建议动作和线下核查建议。"
        ));

        return steps;
    }

    private EventInsightResultDTO.TraceStep buildTraceStep(
            String agentKey,
            String agentName,
            String status,
            int resolvedCount,
            int targetCount,
            String summary,
            String detail) {
        EventInsightResultDTO.TraceStep step = new EventInsightResultDTO.TraceStep();
        step.setAgentKey(agentKey);
        step.setAgentName(agentName);
        step.setStatus(status);
        step.setResolvedCount(resolvedCount);
        step.setTargetCount(targetCount);
        step.setSummary(defaultString(summary, "待补充"));
        step.setDetail(defaultString(detail, "暂无详细信息"));
        return step;
    }

    private List<String> buildMissingFields(
            EventInsightResultDTO.ParsedEvent parsedEvent,
            EventInsightResultDTO.ContextSnapshot context,
            EventInsightResultDTO.RiskAssessment risk,
            EventInsightResultDTO.Advice advice) {
        List<String> missingFields = new ArrayList<>();

        addMissing(missingFields, "parsedEvent.abnormalType", parsedEvent.getAbnormalType());
        addMissing(missingFields, "parsedEvent.occurredAt", parsedEvent.getOccurredAt());
        addMissing(missingFields, "parsedEvent.userId", parsedEvent.getUserId());
        addMissing(missingFields, "parsedEvent.deviceId", parsedEvent.getDeviceId());
        addMissing(missingFields, "parsedEvent.metricName", parsedEvent.getMetricName());
        addMissing(missingFields, "parsedEvent.metricValue", parsedEvent.getMetricValue());

        addMissing(missingFields, "context.age", context.getAge());
        addMissing(missingFields, "context.chronicDiseases", context.getChronicDiseases());
        addMissing(missingFields, "context.recentHealthTrend", context.getRecentHealthTrend());
        addMissing(missingFields, "context.historicalAbnormalCount",
                context.getHistoricalAbnormalCount());
        addMissing(missingFields, "context.recentSameTypeCount",
                context.getRecentSameTypeCount());
        addMissing(missingFields, "context.deviceStatus", context.getDeviceStatus());
        addMissing(missingFields, "context.deviceStatusReason",
                context.getDeviceStatusReason());
        addMissing(missingFields, "context.lastKnownLocation", context.getLastKnownLocation());

        addMissing(missingFields, "risk.riskLevel", risk.getRiskLevel());
        addMissing(missingFields, "risk.riskScore", risk.getRiskScore());
        addMissing(missingFields, "risk.possibleCauses", risk.getPossibleCauses());
        addMissing(missingFields, "risk.analysisReasons", risk.getAnalysisReasons());

        addMissing(missingFields, "advice.notifyWho", advice.getNotifyWho());
        addMissing(missingFields, "advice.suggestedActions", advice.getSuggestedActions());
        addMissing(missingFields, "advice.operatorNote", advice.getOperatorNote());

        return missingFields;
    }

    private String buildSummary(
            UeitException source,
            HealthSubject subject,
            EventInsightResultDTO.ParsedEvent parsedEvent,
            EventInsightResultDTO.RiskAssessment risk) {
        String name = subject != null && subject.getNickName() != null
                ? subject.getNickName()
                : source.getNickName() != null
                ? source.getNickName()
                : "用户" + (parsedEvent.getUserId() != null ? parsedEvent.getUserId() : "未知");
        return String.format(
                "%s发生%s，指标值%s，当前风险为%s，建议尽快处理。",
                name,
                parsedEvent.getAbnormalType(),
                parsedEvent.getMetricValue(),
                risk.getRiskLevel()
        );
    }

    private List<String> inferChronicDiseases(
            UeitException source,
            HealthSubject subject,
            EventInsightResultDTO.ParsedEvent parsedEvent) {
        LinkedHashSet<String> diseases = new LinkedHashSet<>();
        Integer age = source.getAge() > 0
                ? source.getAge()
                : subject != null
                ? subject.getAge()
                : null;
        String type = parsedEvent.getAbnormalType();
        if (age != null && age >= 65) {
            diseases.add("老年慢病随访中");
        }
        if (containsAny(type, "心率", "血压")) {
            diseases.add("心血管相关风险");
        }
        if (containsAny(type, "血氧", "呼吸")) {
            diseases.add("呼吸系统相关风险");
        }
        if (containsAny(type, "体温")) {
            diseases.add("感染或炎症相关风险");
        }
        if (subject != null && subject.getRemark() != null && !subject.getRemark().isBlank()) {
            diseases.add(subject.getRemark());
        }
        if (diseases.isEmpty()) {
            diseases.add("暂无基础疾病信息");
        }
        return new ArrayList<>(diseases);
    }

    private String inferTrend(
            List<UeitException> history,
            EventInsightResultDTO.ParsedEvent parsedEvent) {
        int recentCount = countSameType(history, parsedEvent.getAbnormalType());
        if (recentCount >= 4) {
            return "近7天同类异常高频出现，趋势持续走高";
        }
        if (recentCount >= 2) {
            return "近7天出现多次同类异常，趋势偏上升";
        }
        if (recentCount == 1) {
            return "近期已有同类异常记录，需要继续观察";
        }
        return "近7天未见明显同类异常聚集";
    }

    private int countSameType(List<UeitException> history, String abnormalType) {
        if (history == null || history.isEmpty() || abnormalType == null) {
            return 0;
        }
        int count = 0;
        for (UeitException item : history) {
            if (item.getType() != null && item.getType().contains(abnormalType)) {
                count++;
            }
        }
        return count;
    }

    private String inferDeviceStatus(
            UeitException source,
            List<UeitException> history,
            EventInsightResultDTO.ParsedEvent parsedEvent) {
        if (source.getDeviceId() == null) {
            return "unknown";
        }
        if (containsAny(parsedEvent.getAbnormalType(), "SOS", "急救")) {
            return "unstable";
        }
        if (history.size() >= 5) {
            return "offline";
        }
        return source.getId() != null && source.getId() % 2 == 0 ? "online" : "unstable";
    }

    private String inferDeviceStatusReason(
            UeitException source,
            List<UeitException> history,
            EventInsightResultDTO.ParsedEvent parsedEvent,
            boolean fallbackEvent) {
        if (source.getDeviceId() == null) {
            return "未找到设备绑定信息";
        }
        if (fallbackEvent) {
            return "原始事件缺失，设备状态由兜底规则推导。";
        }
        if (containsAny(parsedEvent.getAbnormalType(), "SOS", "急救")) {
            return "事件类型为紧急求助，建议优先确认设备佩戴和现场情况。";
        }
        if (history.size() >= 5) {
            return "近7天异常较密集，设备可能存在持续离线或佩戴不稳。";
        }
        return "当前设备状态为启发式推断，后续可接入真实设备遥测数据。";
    }

    private List<String> buildPossibleCauses(
            String abnormalType,
            EventInsightResultDTO.ContextSnapshot context,
            EventInsightResultDTO.ParsedEvent parsedEvent) {
        List<String> causes = new ArrayList<>();
        if (containsAny(abnormalType, "心率")) {
            causes.add("活动后恢复期波动");
            causes.add("心律失常或基础心血管问题");
        } else if (containsAny(abnormalType, "血氧")) {
            causes.add("佩戴松动导致读数偏低");
            causes.add("呼吸功能下降或短时缺氧");
        } else if (containsAny(abnormalType, "体温")) {
            causes.add("环境温度升高或感染反应");
            causes.add("持续发热需要复测确认");
        } else if (containsAny(abnormalType, "围栏")) {
            causes.add("真实离开电子围栏范围");
            causes.add("定位漂移或设备短时丢星");
        } else if (containsAny(abnormalType, "SOS", "急救")) {
            causes.add("用户主动求助");
            causes.add("跌倒或突发不适需要人工确认");
        } else {
            causes.add("异常阈值触发");
        }

        if (context.getChronicDiseases() != null && !context.getChronicDiseases().isEmpty()) {
            causes.add("基础风险叠加，提高了事件敏感度");
        }
        if (parsedEvent.getMetricValue() != null) {
            causes.add("当前指标值为 " + parsedEvent.getMetricValue());
        }
        return causes;
    }

    private List<String> buildAnalysisReasons(
            EventInsightResultDTO.ParsedEvent parsedEvent,
            EventInsightResultDTO.ContextSnapshot context,
            List<UeitException> history,
            List<String> ruleHits,
            int score) {
        List<String> reasons = new ArrayList<>();
        reasons.add("异常类型: " + parsedEvent.getAbnormalType());
        reasons.add("年龄: " + context.getAge());
        reasons.add("同类历史异常数: " + context.getRecentSameTypeCount());
        reasons.add("近7天历史异常总数: " + context.getHistoricalAbnormalCount());
        reasons.add("设备状态: " + context.getDeviceStatus());
        reasons.add("风险评分: " + score);
        if (!history.isEmpty()) {
            reasons.add("已读取历史异常记录，用于趋势和重复性判断");
        }
        if (ruleHits != null && !ruleHits.isEmpty()) {
            reasons.add("命中规则: " + String.join(", ", ruleHits));
        }
        return reasons;
    }

    private List<String> buildNotifyTargets(
            boolean highRisk,
            boolean contactFamily,
            boolean contactOrganization) {
        List<String> targets = new ArrayList<>();
        targets.add("值班护士");
        if (highRisk) {
            targets.add("平台管理员");
        }
        if (contactFamily) {
            targets.add("家属");
        }
        if (contactOrganization) {
            targets.add("机构负责人");
        }
        return targets;
    }

    private List<String> buildSuggestedActions(
            EventInsightResultDTO.ParsedEvent parsedEvent,
            EventInsightResultDTO.ContextSnapshot context,
            EventInsightResultDTO.RiskAssessment risk) {
        List<String> actions = new ArrayList<>();
        actions.add("立即复核异常数据和设备佩戴状态");
        actions.add("联系对象进行电话确认或视频回访");
        if (Boolean.TRUE.equals(risk.getImmediateAction())) {
            actions.add("在10分钟内完成人工确认");
        }
        if ("offline".equals(context.getDeviceStatus())) {
            actions.add("检查设备在线情况和数据上传链路");
        }
        if (context.getAge() != null && context.getAge() >= 75) {
            actions.add("必要时联动家属和线下机构");
        }
        if (containsAny(parsedEvent.getAbnormalType(), "SOS", "急救", "围栏")) {
            actions.add("优先安排线下核查");
        }
        return actions;
    }

    private String buildOperatorNote(
            EventInsightResultDTO.ParsedEvent parsedEvent,
            EventInsightResultDTO.ContextSnapshot context,
            EventInsightResultDTO.RiskAssessment risk) {
        return String.format(
                "当前为%s级事件，建议围绕%s和设备状态优先处置。",
                risk.getRiskLevel(),
                parsedEvent.getAbnormalType() + " / " + context.getDeviceStatus()
        );
    }

    private UeitException createFallbackException(Long eventId) {
        UeitException fallback = new UeitException();
        int index = eventId == null ? 0 : Math.abs(eventId.intValue());
        String type = FALLBACK_TYPES.get(index % FALLBACK_TYPES.size());
        fallback.setId(eventId);
        fallback.setUserId(10000L + (index % 50));
        fallback.setDeviceId(3000L + (index % 20));
        fallback.setType(type);
        fallback.setValue(deriveMetricValue(type, eventId));
        fallback.setLocation("模拟事件中心");
        fallback.setState("0");
        fallback.setAge(65 + (index % 18));
        fallback.setNickName("用户" + fallback.getUserId());
        Date occurredAt = new Date(System.currentTimeMillis()
                - (index % 30 + 2) * 60L * 1000L);
        fallback.setCreateTime(occurredAt);
        fallback.setReadTime(occurredAt);
        return fallback;
    }

    private String deriveType(Long eventId) {
        if (eventId == null) {
            return FALLBACK_TYPES.get(0);
        }
        return FALLBACK_TYPES.get(Math.abs(eventId.intValue()) % FALLBACK_TYPES.size());
    }

    private String deriveMetricName(String abnormalType) {
        if (containsAny(abnormalType, "心率")) {
            return "heartRate";
        }
        if (containsAny(abnormalType, "血氧")) {
            return "spo2";
        }
        if (containsAny(abnormalType, "体温")) {
            return "temperature";
        }
        if (containsAny(abnormalType, "围栏")) {
            return "location";
        }
        if (containsAny(abnormalType, "SOS", "急救")) {
            return "manualAlert";
        }
        return "metric";
    }

    private String deriveMetricValue(String abnormalType, Long eventId) {
        int seed = eventId == null ? 0 : Math.abs(eventId.intValue());
        if (containsAny(abnormalType, "心率")) {
            return (110 + (seed % 32)) + " bpm";
        }
        if (containsAny(abnormalType, "血氧")) {
            return (88 + (seed % 6)) + "%";
        }
        if (containsAny(abnormalType, "体温")) {
            return String.valueOf(37.5 + (seed % 8) * 0.1).replace(".0", "") + " C";
        }
        if (containsAny(abnormalType, "围栏")) {
            return String.format("%.1f km", 1.5 + (seed % 6) * 0.2);
        }
        if (containsAny(abnormalType, "SOS", "急救")) {
            return "manual trigger";
        }
        return "n/a";
    }

    private int deriveAge(Long userId) {
        if (userId == null) {
            return 68;
        }
        return 62 + Math.abs(userId.intValue() % 18);
    }

    private int countNonBlank(Object... values) {
        int count = 0;
        for (Object value : values) {
            if (value instanceof String) {
                if (!((String) value).isBlank()) {
                    count++;
                }
            } else if (value instanceof List) {
                if (!((List<?>) value).isEmpty()) {
                    count++;
                }
            } else if (value != null) {
                count++;
            }
        }
        return count;
    }

    private void addMissing(List<String> missingFields, String fieldPath, Object value) {
        boolean missing = false;
        if (value == null) {
            missing = true;
        } else if (value instanceof String) {
            missing = ((String) value).isBlank();
        } else if (value instanceof List) {
            missing = ((List<?>) value).isEmpty();
        }
        if (missing) {
            missingFields.add(fieldPath);
        }
    }

    private String resolveStatus(int resolvedCount, int targetCount, boolean fallbackEvent) {
        if (fallbackEvent && resolvedCount > 0) {
            return "fallback";
        }
        if (resolvedCount >= targetCount) {
            return "done";
        }
        if (resolvedCount > 0) {
            return "partial";
        }
        return "waiting";
    }

    private String joinSummary(String... parts) {
        List<String> resolved = new ArrayList<>();
        for (String part : parts) {
            if (part != null && !part.isBlank()) {
                resolved.add(part);
            }
        }
        return resolved.isEmpty() ? "待补充" : String.join(" / ", resolved);
    }

    private boolean containsAny(String text, String... tokens) {
        if (text == null || tokens == null) {
            return false;
        }
        for (String token : tokens) {
            if (token != null && text.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private String defaultString(String primary, String fallback) {
        return primary != null && !primary.isBlank() ? primary : fallback;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
