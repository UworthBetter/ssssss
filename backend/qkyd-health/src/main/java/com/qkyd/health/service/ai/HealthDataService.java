package com.qkyd.health.service.ai;

import com.alibaba.fastjson2.JSON;
import com.qkyd.common.event.AbnormalDetectionEvent;
import com.qkyd.common.event.HealthDataUpdateEvent;
import com.qkyd.common.event.RiskScoreUpdateEvent;
import com.qkyd.health.config.AiServiceProperties;
import com.qkyd.health.domain.AiHealthRecord;
import com.qkyd.health.domain.HealthSubject;
import com.qkyd.health.domain.UeitBlood;
import com.qkyd.health.domain.UeitDeviceInfo;
import com.qkyd.health.domain.UeitDeviceInfoExtend;
import com.qkyd.health.domain.UeitException;
import com.qkyd.health.domain.UeitHeartRate;
import com.qkyd.health.domain.UeitLocation;
import com.qkyd.health.domain.UeitSpo2;
import com.qkyd.health.domain.UeitSteps;
import com.qkyd.health.domain.UeitTemp;
import com.qkyd.health.domain.dto.ai.AiHealthCheckRequest;
import com.qkyd.health.domain.dto.ai.AiHealthCheckResponse;
import com.qkyd.health.domain.dto.ai.HeartRateAnomaly;
import com.qkyd.health.domain.dto.ai.VitalSignData;
import com.qkyd.health.mapper.AiHealthRecordMapper;
import com.qkyd.health.service.IHealthSubjectService;
import com.qkyd.health.service.IUeitBloodService;
import com.qkyd.health.service.IUeitDeviceInfoExtendService;
import com.qkyd.health.service.IUeitDeviceInfoService;
import com.qkyd.health.service.IUeitExceptionService;
import com.qkyd.health.service.IUeitHeartRateService;
import com.qkyd.health.service.IUeitLocationService;
import com.qkyd.health.service.IUeitSpo2Service;
import com.qkyd.health.service.IUeitStepsService;
import com.qkyd.health.service.IUeitTempService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
public class HealthDataService {

    private static final Logger log = LoggerFactory.getLogger(HealthDataService.class);
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();
    private static final String SIM_DETECTION_METHOD = "SIM_ENGINE";
    private static final String MOCK_DETECTION_METHOD = "MOCK_INGEST";
    private static final String HISTORY_RESOLVE_NOTE = "Historical simulator alert auto-closed";
    private static final String ALERT_HEART_RATE = "\u5fc3\u7387\u5f02\u5e38";
    private static final String ALERT_SPO2 = "\u8840\u6c27\u5f02\u5e38";
    private static final String ALERT_BLOOD_PRESSURE = "\u8840\u538b\u5f02\u5e38";
    private static final String ALERT_TEMPERATURE = "\u4f53\u6e29\u5f02\u5e38";
    private static final String ALERT_FENCE = "\u56f4\u680f\u8d8a\u754c";
    private static final String ALERT_SOS = "SOS\u6c42\u6551";
    private static final String ALERT_DEVICE_OFFLINE = "\u8bbe\u5907\u79bb\u7ebf";
    private static final String ALERT_ACTIVITY = "\u6d3b\u52a8\u91cf\u5f02\u5e38";
    private static final String ALERT_DEVICE_SIGNAL = "\u8bbe\u5907\u4fe1\u53f7\u5f02\u5e38";
    private static final String ALERT_HEALTH_RISK = "\u5065\u5eb7\u98ce\u9669\u9884\u8b66";

    private final AiServiceClient aiServiceClient;
    private final AiServiceProperties aiServiceProperties;
    private final AiHealthRecordMapper aiHealthRecordMapper;
    private final IUeitHeartRateService heartRateService;
    private final IUeitBloodService bloodService;
    private final IUeitSpo2Service spo2Service;
    private final IUeitTempService tempService;
    private final IUeitDeviceInfoService deviceInfoService;
    private final IUeitDeviceInfoExtendService deviceInfoExtendService;
    private final IUeitLocationService locationService;
    private final IUeitStepsService stepsService;
    private final IHealthSubjectService subjectService;
    private final IUeitExceptionService exceptionService;
    private final ApplicationEventPublisher eventPublisher;
    private final JdbcTemplate jdbcTemplate;

    public HealthDataService(
            AiServiceClient aiServiceClient,
            AiServiceProperties aiServiceProperties,
            AiHealthRecordMapper aiHealthRecordMapper,
            IUeitHeartRateService heartRateService,
            IUeitBloodService bloodService,
            IUeitSpo2Service spo2Service,
            IUeitTempService tempService,
            IUeitDeviceInfoService deviceInfoService,
            IUeitDeviceInfoExtendService deviceInfoExtendService,
            IUeitLocationService locationService,
            IUeitStepsService stepsService,
            IHealthSubjectService subjectService,
            IUeitExceptionService exceptionService,
            ApplicationEventPublisher eventPublisher,
            JdbcTemplate jdbcTemplate) {
        this.aiServiceClient = aiServiceClient;
        this.aiServiceProperties = aiServiceProperties;
        this.aiHealthRecordMapper = aiHealthRecordMapper;
        this.heartRateService = heartRateService;
        this.bloodService = bloodService;
        this.spo2Service = spo2Service;
        this.tempService = tempService;
        this.deviceInfoService = deviceInfoService;
        this.deviceInfoExtendService = deviceInfoExtendService;
        this.locationService = locationService;
        this.stepsService = stepsService;
        this.subjectService = subjectService;
        this.exceptionService = exceptionService;
        this.eventPublisher = eventPublisher;
        this.jdbcTemplate = jdbcTemplate;
    }

    public AiHealthCheckResponse processRawData(String deviceId, Long userId, List<VitalSignData> dataList) {
        return ingest(deviceId, userId, dataList, new IngestionOptions(false, true, false));
    }

    public AiHealthCheckResponse processSimulatedData(
            String deviceId,
            Long userId,
            List<VitalSignData> dataList,
            boolean emitRealtimeEvents,
            boolean resolveGeneratedAlert) {
        return ingest(deviceId, userId, dataList, new IngestionOptions(true, emitRealtimeEvents, resolveGeneratedAlert));
    }

    @Async
    public CompletableFuture<AiHealthCheckResponse> processRawDataAsync(
            String deviceId, Long userId, List<VitalSignData> dataList) {
        try {
            return CompletableFuture.completedFuture(processRawData(deviceId, userId, dataList));
        } catch (Exception e) {
            log.error("[HealthDataService] async process failed, device={}", deviceId, e);
            return CompletableFuture.completedFuture(null);
        }
    }

    public AiHealthRecord getLatestRecord(String deviceId) {
        return aiHealthRecordMapper.selectLatestByDeviceId(deviceId);
    }

    public List<AiHealthRecord> listRecords(AiHealthRecord record) {
        return aiHealthRecordMapper.selectAiHealthRecordList(record);
    }

    public boolean isAiServiceAvailable() {
        return aiServiceClient.isServiceAvailable();
    }

    private AiHealthCheckResponse ingest(
            String deviceId,
            Long userId,
            List<VitalSignData> dataList,
            IngestionOptions options) {
        if (deviceId == null || deviceId.isBlank() || dataList == null || dataList.isEmpty()) {
            throw new IllegalArgumentException("deviceId and dataList are required");
        }

        Long finalUserId = resolveUserId(userId);
        UeitDeviceInfo deviceInfo = ensureDevice(deviceId, finalUserId);
        UeitDeviceInfoExtend deviceExtend = ensureDeviceExtend(deviceInfo.getId());
        HealthSubject subject = finalUserId != null ? subjectService.selectHealthSubjectBySubjectId(finalUserId) : null;
        VitalSignData latest = dataList.get(dataList.size() - 1);

        AiHealthCheckResponse aiResponse = options.ruleOnly ? null : invokeAiSafely(dataList);
        RuleAlert alert = resolveRuleAlert(latest, aiResponse, options.ruleOnly);

        persistSamples(deviceInfo, deviceExtend, finalUserId, dataList);

        UeitException generatedEvent = null;
        if (alert != null) {
            generatedEvent = createException(deviceInfo, deviceExtend, subject, finalUserId, latest, alert, options.resolveGeneratedAlert);
            persistAbnormalRecord(finalUserId, deviceId, latest, alert, options.ruleOnly);
        }

        AiHealthCheckResponse response = mergeResponse(aiResponse, alert, dataList, latest);
        if (generatedEvent != null) {
            response.setGeneratedEventId(generatedEvent.getId());
            response.setGeneratedEventType(generatedEvent.getType());
        }
        saveHealthRecord(deviceId, finalUserId, dataList, response);

        if (options.emitRealtimeEvents) {
            publishHealthUpdate(finalUserId, deviceInfo, deviceExtend, subject, latest, response, alert);
            publishRiskUpdate(finalUserId, latest, response, alert);
            if (generatedEvent != null && !options.resolveGeneratedAlert) {
                publishAbnormalUpdate(finalUserId, subject, latest, alert, generatedEvent);
            }
        }
        return response;
    }

    private AiHealthCheckResponse invokeAiSafely(List<VitalSignData> dataList) {
        try {
            if (aiServiceProperties.getServiceUrl() == null || aiServiceProperties.getServiceUrl().isBlank() || !aiServiceClient.isServiceAvailable()) {
                return null;
            }
            AiHealthCheckResponse response = aiServiceClient.healthCheck(new AiHealthCheckRequest(dataList));
            return response != null && response.isSuccess() ? response : null;
        } catch (Exception e) {
            log.warn("[HealthDataService] AI health check unavailable, falling back to local rules: {}", e.getMessage());
            return null;
        }
    }

    private Long resolveUserId(Long userId) {
        if (userId != null && userId > 1L) {
            return userId;
        }
        List<HealthSubject> subjects = subjectService.selectHealthSubjectList(new HealthSubject());
        if (subjects != null && !subjects.isEmpty()) {
            return subjects.get(0).getSubjectId();
        }
        return userId;
    }

    private UeitDeviceInfo ensureDevice(String deviceId, Long userId) {
        UeitDeviceInfo deviceInfo = deviceInfoService.selectUeitDeviceInfoByImei(deviceId);
        if (deviceInfo != null) {
            boolean needsUpdate = !Objects.equals(deviceInfo.getUserId(), userId)
                    || !"SMART_WATCH".equals(deviceInfo.getType());
            if (needsUpdate) {
                deviceInfo.setUserId(userId);
                deviceInfo.setType("SMART_WATCH");
                if (deviceInfo.getName() == null || deviceInfo.getName().isBlank()) {
                    deviceInfo.setName("Sim Watch " + deviceId);
                }
                deviceInfoService.updateUeitDeviceInfo(deviceInfo);
            }
            return deviceInfo;
        }

        UeitDeviceInfo created = new UeitDeviceInfo();
        created.setUserId(userId);
        created.setImei(deviceId);
        created.setName("Sim Watch " + deviceId);
        created.setType("SMART_WATCH");
        created.setCreateTime(new Date());
        deviceInfoService.insertUeitDeviceInfo(created);
        return deviceInfoService.selectUeitDeviceInfoByImei(deviceId);
    }

    private UeitDeviceInfoExtend ensureDeviceExtend(Long deviceId) {
        UeitDeviceInfoExtend extend = deviceInfoExtendService.selectUeitDeviceInfoExtendByDeviceId(deviceId);
        if (extend != null) {
            return extend;
        }
        extend = new UeitDeviceInfoExtend();
        extend.setDeviceId(deviceId);
        deviceInfoExtendService.insertUeitDeviceInfoExtend(extend);
        return deviceInfoExtendService.selectUeitDeviceInfoExtendByDeviceId(deviceId);
    }

    private void persistSamples(
            UeitDeviceInfo deviceInfo,
            UeitDeviceInfoExtend deviceExtend,
            Long userId,
            List<VitalSignData> dataList) {
        for (VitalSignData data : dataList) {
            Date readTime = resolveReadTime(data);
            if (data.getHeartRate() != null && data.getHeartRate() > 0) {
                UeitHeartRate heartRate = new UeitHeartRate();
                heartRate.setUserId(userId);
                heartRate.setDeviceId(deviceInfo.getId());
                heartRate.setValue(data.getHeartRate().floatValue());
                heartRate.setCreateTime(readTime);
                heartRateService.insertUeitHeartRate(heartRate);
                deviceExtend.setHeartRate(data.getHeartRate().floatValue());
                deviceExtend.setHeartRateTime(readTime);
            }

            int[] bloodPressure = parseBloodPressure(data.getBloodPressure());
            if (bloodPressure != null) {
                UeitBlood blood = new UeitBlood();
                blood.setUserId(userId);
                blood.setDeviceId(deviceInfo.getId());
                blood.setSystolic(bloodPressure[0]);
                blood.setDiastolic(bloodPressure[1]);
                blood.setCreateTime(readTime);
                bloodService.insertUeitBlood(blood);
                deviceExtend.setBloodSystolic(bloodPressure[0]);
                deviceExtend.setBloodDiastolic(bloodPressure[1]);
                deviceExtend.setBloodTime(readTime);
            }

            if (data.getSpo2() != null && data.getSpo2() > 0) {
                UeitSpo2 spo2 = new UeitSpo2();
                spo2.setUserId(userId);
                spo2.setDeviceId(deviceInfo.getId());
                spo2.setValue(data.getSpo2().floatValue());
                spo2.setCreateTime(readTime);
                spo2Service.insertUeitSpo2(spo2);
                deviceExtend.setSpo2(data.getSpo2().floatValue());
                deviceExtend.setSpo2Time(readTime);
            }

            if (data.getTemperature() != null && data.getTemperature() > 0) {
                UeitTemp temp = new UeitTemp();
                temp.setUserId(userId);
                temp.setDeviceId(deviceInfo.getId());
                temp.setValue(data.getTemperature().floatValue());
                temp.setCreateTime(readTime);
                tempService.insertUeitTemp(temp);
                deviceExtend.setTemp(data.getTemperature().floatValue());
                deviceExtend.setTempTime(readTime);
            }

            if (data.getSteps() != null && data.getSteps() >= 0) {
                upsertDailySteps(userId, deviceInfo.getId(), data.getSteps(), readTime);
                deviceExtend.setStep(data.getSteps());
            }

            if (data.getBatteryLevel() != null) {
                deviceExtend.setBatteryLevel(data.getBatteryLevel());
            }

            if (hasLocation(data)) {
                UeitLocation location = new UeitLocation();
                location.setUserId(userId);
                location.setDeviceId(deviceInfo.getId());
                location.setLongitude(formatCoordinate(data.getLongitude()));
                location.setLatitude(formatCoordinate(data.getLatitude()));
                location.setLocation(data.getLocation());
                location.setType(defaultText(data.getLocationType(), "GPS"));
                location.setCreateTime(readTime);
                location.setReadTime(readTime);
                locationService.insertUeitLocation(location);

                if (data.getLongitude() != null) deviceExtend.setLongitude(scaleCoordinate(data.getLongitude()));
                if (data.getLatitude() != null) deviceExtend.setLatitude(scaleCoordinate(data.getLatitude()));
                deviceExtend.setLocation(data.getLocation());
                deviceExtend.setType(defaultText(data.getLocationType(), "GPS"));
                deviceExtend.setPositioningTime(readTime);
            }

            if (!"offline".equalsIgnoreCase(defaultText(data.getDeviceStatus(), ""))) {
                deviceExtend.setLastCommunicationTime(readTime);
            }
            if (data.getAbnormalType() != null && !data.getAbnormalType().isBlank()) {
                deviceExtend.setAlarmContent(data.getAbnormalType());
                deviceExtend.setAlarmTime(readTime);
            }
        }

        if (deviceInfoExtendService.selectUeitDeviceInfoExtendByDeviceId(deviceInfo.getId()) == null) {
            deviceInfoExtendService.insertUeitDeviceInfoExtend(deviceExtend);
        } else {
            deviceInfoExtendService.updateUeitDeviceInfoExtend(deviceExtend);
        }
    }

    private void upsertDailySteps(Long userId, Long deviceId, Integer steps, Date readTime) {
        UeitSteps probe = new UeitSteps();
        probe.setUserId(userId);
        probe.setDeviceId(deviceId);
        probe.setDate(startOfDay(readTime));
        UeitSteps existing = stepsService.selectUeitSteps(probe);

        UeitSteps current = new UeitSteps();
        current.setUserId(userId);
        current.setDeviceId(deviceId);
        current.setDate(startOfDay(readTime));
        current.setValue(steps);
        current.setCalories(Math.max(100L, steps.longValue() / 24L));
        current.setDateTime(readTime);
        if (existing == null) {
            stepsService.insertUeitSteps(current);
        } else {
            current.setId(existing.getId());
            stepsService.updateUeitSteps(current);
        }
    }

    private RuleAlert resolveRuleAlert(VitalSignData latest, AiHealthCheckResponse aiResponse, boolean explicitOnly) {
        if (latest == null) return alertFromAi(latest, aiResponse);

        if (latest.getAbnormalType() != null && !latest.getAbnormalType().isBlank()) {
            String metricType = defaultText(latest.getMetricType(), normalizeMetricType(latest.getAbnormalType()));
            String abnormalType = canonicalAbnormalType(metricType, latest.getAbnormalType());
            int riskScore = latest.getRiskScore() != null ? latest.getRiskScore() : defaultRiskScore(metricType);
            String abnormalValue = defaultText(latest.getAbnormalValue(), resolveAbnormalValue(latest, metricType));
            return new RuleAlert(metricType, abnormalType, abnormalValue,
                    defaultText(latest.getNormalRange(), defaultNormalRange(metricType)),
                    resolveRiskLevel(latest.getRiskLevel(), riskScore), riskScore,
                    defaultText(latest.getMessage(), defaultAlertMessage(abnormalType)));
        }

        if (explicitOnly) {
            return null;
        }

        if ("offline".equalsIgnoreCase(defaultText(latest.getDeviceStatus(), ""))) {
            return new RuleAlert("device_offline", ALERT_DEVICE_OFFLINE, "\u8fde\u7eed\u7f3a\u62a5", "\u8bbe\u5907\u5728\u7ebf", "warning", 62, "\u8bbe\u5907\u72b6\u6001\u5f02\u5e38");
        }
        if (latest.getHeartRate() != null && (latest.getHeartRate() > 110 || latest.getHeartRate() < 55)) {
            int score = latest.getHeartRate() > 125 ? 86 : 72;
            return new RuleAlert("heart_rate", ALERT_HEART_RATE, latest.getHeartRate() + " bpm", "60-100 bpm",
                    latest.getHeartRate() > 125 ? "high" : "warning", score, "\u5fc3\u7387\u8d85\u51fa\u6b63\u5e38\u8303\u56f4");
        }
        if (latest.getSpo2() != null && latest.getSpo2() < 95) {
            int score = latest.getSpo2() < 90 ? 88 : 76;
            return new RuleAlert("spo2", ALERT_SPO2, latest.getSpo2() + "%", "95-99%",
                    latest.getSpo2() < 90 ? "high" : "warning", score, "\u8840\u6c27\u4f4e\u4e8e\u6b63\u5e38\u8303\u56f4");
        }
        int[] bloodPressure = parseBloodPressure(latest.getBloodPressure());
        if (bloodPressure != null && (bloodPressure[0] > 140 || bloodPressure[0] < 90 || bloodPressure[1] > 90 || bloodPressure[1] < 60)) {
            int score = bloodPressure[0] > 155 ? 82 : 68;
            return new RuleAlert("blood_pressure", ALERT_BLOOD_PRESSURE, bloodPressure[0] + "/" + bloodPressure[1],
                    "90-140 / 60-90", bloodPressure[0] > 155 ? "high" : "warning", score, "\u8840\u538b\u8d85\u51fa\u6b63\u5e38\u8303\u56f4");
        }
        if (latest.getTemperature() != null && (latest.getTemperature() >= 37.4 || latest.getTemperature() <= 35.5)) {
            int score = latest.getTemperature() >= 38.3 ? 78 : 64;
            return new RuleAlert("temperature", ALERT_TEMPERATURE, round(latest.getTemperature()) + "C", "36.0-37.3C",
                    latest.getTemperature() >= 38.3 ? "high" : "warning", score, "\u4f53\u6e29\u8d85\u51fa\u6b63\u5e38\u8303\u56f4");
        }
        return alertFromAi(latest, aiResponse);
    }

    private RuleAlert alertFromAi(VitalSignData latest, AiHealthCheckResponse aiResponse) {
        if (aiResponse == null || !aiResponse.isSuccess()) return null;
        String riskLevel = normalizeText(aiResponse.getRiskLevel());
        boolean highRisk = "high".equals(riskLevel) || "critical".equals(riskLevel)
                || (aiResponse.getAnomalyCount() != null && aiResponse.getAnomalyCount() > 0);
        if (!highRisk) return null;
        String metricType = latest != null ? detectMetricType(latest) : "health_risk";
        String abnormalType = aiResponse.getAnomalyCount() != null && aiResponse.getAnomalyCount() > 0
                ? fallbackAbnormalType(metricType) : ALERT_HEALTH_RISK;
        String abnormalValue = latest != null ? resolveAbnormalValue(latest, metricType) : "\u98ce\u9669\u8bc4\u4f30";
        int riskScore = aiResponse.getRiskScore() != null
                ? Math.max(0, Math.min(100, (int) Math.round(aiResponse.getRiskScore() * 100)))
                : 70;
        return new RuleAlert("health_risk", abnormalType, abnormalValue, "-", resolveRiskLevel(aiResponse.getRiskLevel(), riskScore),
                riskScore, defaultText(aiResponse.getMessage(), defaultAlertMessage(abnormalType)));
    }

    private UeitException createException(
            UeitDeviceInfo deviceInfo,
            UeitDeviceInfoExtend deviceExtend,
            HealthSubject subject,
            Long userId,
            VitalSignData latest,
            RuleAlert alert,
            boolean resolvedImmediately) {
        UeitException event = new UeitException();
        event.setUserId(userId);
        event.setDeviceId(deviceInfo.getId());
        event.setType(alert.abnormalType);
        event.setValue(alert.abnormalValue);
        event.setState(resolvedImmediately ? "1" : "0");
        event.setLocation(defaultText(latest.getLocation(), deviceExtend.getLocation()));
        event.setLongitude(resolveLongitude(latest, deviceExtend));
        event.setLatitude(resolveLatitude(latest, deviceExtend));
        Date eventTime = resolveReadTime(latest);
        event.setReadTime(eventTime);
        event.setCreateTime(eventTime);
        if (resolvedImmediately) {
            event.setUpdateBy(SIM_DETECTION_METHOD);
            event.setUpdateTime(eventTime);
            event.setUpdateContent(HISTORY_RESOLVE_NOTE);
        }
        if (subject != null) {
            event.setNickName(subject.getNickName());
            event.setAge(subject.getAge() != null ? subject.getAge() : 0);
            event.setPhone(subject.getPhonenumber());
            event.setSex(subject.getSex());
        }
        exceptionService.insertUeitException(event);
        return event;
    }

    private void persistAbnormalRecord(Long userId, String deviceId, VitalSignData latest, RuleAlert alert, boolean simulated) {
        jdbcTemplate.update(
                "INSERT INTO ai_abnormal_record (user_id, device_id, metric_type, abnormal_value, normal_range, abnormal_type, risk_level, detection_method, detected_time, create_time) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                userId, deviceId, alert.metricType, alert.abnormalValue, alert.normalRange, alert.abnormalType, alert.riskLevel,
                simulated ? SIM_DETECTION_METHOD : MOCK_DETECTION_METHOD, resolveReadTime(latest), resolveReadTime(latest)
        );
    }

    private void publishHealthUpdate(Long userId, UeitDeviceInfo deviceInfo, UeitDeviceInfoExtend deviceExtend, HealthSubject subject,
                                     VitalSignData latest, AiHealthCheckResponse response, RuleAlert alert) {
        if (userId == null) return;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("patientId", userId);
        payload.put("patientName", resolvePatientName(userId, subject));
        payload.put("deviceId", deviceInfo.getId());
        payload.put("deviceImei", deviceInfo.getImei());
        payload.put("deviceName", deviceInfo.getName());
        payload.put("heartRate", latest.getHeartRate());
        payload.put("bloodPressure", latest.getBloodPressure());
        payload.put("spo2", latest.getSpo2());
        payload.put("temperature", latest.getTemperature());
        payload.put("steps", latest.getSteps());
        payload.put("batteryLevel", latest.getBatteryLevel());
        payload.put("signalQuality", latest.getSignalQuality());
        payload.put("deviceStatus", latest.getDeviceStatus());
        payload.put("longitude", resolveLongitude(latest, deviceExtend));
        payload.put("latitude", resolveLatitude(latest, deviceExtend));
        payload.put("location", defaultText(latest.getLocation(), deviceExtend.getLocation()));
        payload.put("riskScore", response.getRiskScore());
        payload.put("riskLevel", response.getRiskLevel());
        payload.put("abnormalType", alert != null ? alert.abnormalType : null);
        payload.put("sampleTime", resolveReadTime(latest).getTime());
        eventPublisher.publishEvent(new HealthDataUpdateEvent(userId, payload));
    }

    private void publishRiskUpdate(Long userId, VitalSignData latest, AiHealthCheckResponse response, RuleAlert alert) {
        if (userId == null) return;
        int riskScore = response.getRiskScore() != null ? Math.max(0, Math.min(100, (int) Math.round(response.getRiskScore())))
                : (alert != null ? alert.riskScore : 24);
        Map<String, Object> features = new LinkedHashMap<>();
        features.put("heartRate", latest.getHeartRate());
        features.put("bloodPressure", latest.getBloodPressure());
        features.put("spo2", latest.getSpo2());
        features.put("temperature", latest.getTemperature());
        features.put("steps", latest.getSteps());
        features.put("batteryLevel", latest.getBatteryLevel());
        features.put("signalQuality", latest.getSignalQuality());
        features.put("deviceStatus", latest.getDeviceStatus());
        if (alert != null) {
            features.put("metricType", alert.metricType);
            features.put("abnormalType", alert.abnormalType);
        }
        eventPublisher.publishEvent(new RiskScoreUpdateEvent(userId, riskScore, resolveRiskLevel(response.getRiskLevel(), riskScore), features));
    }

    private void publishAbnormalUpdate(Long userId, HealthSubject subject, VitalSignData latest, RuleAlert alert, UeitException generatedEvent) {
        if (userId == null || alert == null) return;
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("metricType", alert.metricType);
        details.put("eventId", generatedEvent.getId());
        details.put("location", generatedEvent.getLocation());
        details.put("deviceStatus", latest.getDeviceStatus());
        details.put("signalQuality", latest.getSignalQuality());
        eventPublisher.publishEvent(new AbnormalDetectionEvent(
                userId, resolvePatientName(userId, subject), alert.abnormalType, alert.abnormalValue, alert.riskLevel, alert.message, details
        ));
    }

    private AiHealthCheckResponse mergeResponse(AiHealthCheckResponse aiResponse, RuleAlert alert,
                                                List<VitalSignData> dataList, VitalSignData latest) {
        AiHealthCheckResponse response = aiResponse != null ? aiResponse : new AiHealthCheckResponse();
        response.setCode(200);
        response.setDataPointsAnalyzed(dataList != null ? dataList.size() : 0);
        if (alert != null) {
            response.setAnomalyCount(1);
            response.setRiskLevel(alert.riskLevel);
            response.setRiskScore((double) alert.riskScore);
            response.setRiskFactors(Collections.singletonList(alert.abnormalType));
            response.setMessage(defaultText(response.getMessage(), alert.message));
            if ("heart_rate".equals(alert.metricType) && latest != null && latest.getHeartRate() != null) {
                HeartRateAnomaly anomaly = new HeartRateAnomaly();
                anomaly.setTimestamp(resolveReadTime(latest).getTime());
                anomaly.setHeartRate(latest.getHeartRate());
                anomaly.setWindowMean(82D);
                anomaly.setDeviationPercent(Math.abs(latest.getHeartRate() - 82D) / 82D * 100D);
                anomaly.setMessage(alert.message);
                response.setAnomalies(Collections.singletonList(anomaly));
            } else if (response.getAnomalies() == null) {
                response.setAnomalies(new ArrayList<>());
            }
            return response;
        }
        if (response.getAnomalyCount() == null) response.setAnomalyCount(0);
        if (response.getRiskLevel() == null || response.getRiskLevel().isBlank()) response.setRiskLevel("low");
        if (response.getRiskScore() == null) response.setRiskScore(18D);
        if (response.getRiskFactors() == null) response.setRiskFactors(Collections.emptyList());
        if (response.getAnomalies() == null) response.setAnomalies(Collections.emptyList());
        if (response.getMessage() == null || response.getMessage().isBlank()) response.setMessage("Data ingested successfully");
        return response;
    }

    private void saveHealthRecord(String deviceId, Long userId, List<VitalSignData> dataList, AiHealthCheckResponse response) {
        try {
            AiHealthRecord record = new AiHealthRecord();
            record.setDeviceId(deviceId);
            record.setUserId(userId);
            record.setRiskLevel(response.getRiskLevel());
            record.setRiskScore(BigDecimal.valueOf(response.getRiskScore() != null ? response.getRiskScore() : 0D));
            record.setAnomalyCount(response.getAnomalyCount() != null ? response.getAnomalyCount() : 0);
            record.setRiskFactors(JSON.toJSONString(response.getRiskFactors()));
            record.setRawData(JSON.toJSONString(dataList));
            record.setDataPoints(response.getDataPointsAnalyzed() != null ? response.getDataPointsAnalyzed() : 0);
            aiHealthRecordMapper.insertAiHealthRecord(record);
        } catch (Exception e) {
            log.warn("[HealthDataService] failed to save AI health record: {}", e.getMessage());
        }
    }

    private Date resolveReadTime(VitalSignData data) {
        return data != null && data.getTimestamp() != null && data.getTimestamp() > 0 ? new Date(data.getTimestamp()) : new Date();
    }

    private int[] parseBloodPressure(String value) {
        if (value == null || !value.contains("/")) return null;
        String[] parts = value.split("/");
        if (parts.length != 2) return null;
        try {
            return new int[]{Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim())};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean hasLocation(VitalSignData data) {
        return data.getLongitude() != null || data.getLatitude() != null || (data.getLocation() != null && !data.getLocation().isBlank());
    }

    private String formatCoordinate(Double value) { return value == null ? null : scaleCoordinate(value).toPlainString(); }
    private BigDecimal scaleCoordinate(Double value) { return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP); }
    private BigDecimal resolveLongitude(VitalSignData latest, UeitDeviceInfoExtend extend) { return latest != null && latest.getLongitude() != null ? scaleCoordinate(latest.getLongitude()) : extend.getLongitude(); }
    private BigDecimal resolveLatitude(VitalSignData latest, UeitDeviceInfoExtend extend) { return latest != null && latest.getLatitude() != null ? scaleCoordinate(latest.getLatitude()) : extend.getLatitude(); }
    private String resolvePatientName(Long userId, HealthSubject subject) { return subject != null && subject.getNickName() != null && !subject.getNickName().isBlank() ? subject.getNickName() : (userId != null ? "Patient-" + userId : "Unknown"); }

    private String resolveAbnormalValue(VitalSignData latest, String metricType) {
        if (latest == null) return "-";
        return switch (normalizeMetricType(metricType)) {
            case "heart_rate" -> latest.getHeartRate() != null ? latest.getHeartRate() + " bpm" : "-";
            case "spo2" -> latest.getSpo2() != null ? latest.getSpo2() + "%" : "-";
            case "temperature" -> latest.getTemperature() != null ? round(latest.getTemperature()) + "C" : "-";
            case "blood_pressure" -> defaultText(latest.getBloodPressure(), "-");
            case "fence" -> defaultText(latest.getAbnormalValue(), "\u8d85\u51fa\u5b89\u5168\u533a\u57df");
            case "sos" -> defaultText(latest.getAbnormalValue(), "\u624b\u52a8\u89e6\u53d1");
            case "device_offline" -> defaultText(latest.getAbnormalValue(), "\u8fde\u7eed\u7f3a\u62a5");
            case "activity" -> defaultText(latest.getAbnormalValue(), latest.getSteps() != null ? latest.getSteps() + " \u6b65/\u65e5" : "-");
            default -> defaultText(latest.getAbnormalValue(), "-");
        };
    }

    private String defaultNormalRange(String metricType) {
        return switch (normalizeMetricType(metricType)) {
            case "heart_rate" -> "60-100 bpm";
            case "spo2" -> "95-99%";
            case "temperature" -> "36.0-37.3C";
            case "blood_pressure" -> "90-140 / 60-90";
            case "sos" -> "\u672a\u89e6\u53d1";
            case "fence" -> "\u4f4d\u4e8e\u5b89\u5168\u533a\u57df";
            case "device_offline" -> "\u8bbe\u5907\u5728\u7ebf";
            case "activity" -> "\u7b26\u5408\u65e5\u5e38\u6d3b\u52a8\u8303\u56f4";
            default -> "-";
        };
    }

    private int defaultRiskScore(String metricType) {
        return switch (normalizeMetricType(metricType)) {
            case "sos" -> 96;
            case "heart_rate", "spo2" -> 84;
            case "blood_pressure", "temperature", "fence" -> 72;
            default -> 62;
        };
    }

    private String resolveRiskLevel(String preferredRiskLevel, int riskScore) {
        String normalized = normalizeText(preferredRiskLevel);
        if (!normalized.isBlank()) return normalized;
        if (riskScore >= 90) return "critical";
        if (riskScore >= 74) return "high";
        if (riskScore >= 56) return "warning";
        return "low";
    }

    private String defaultText(String value, String fallback) { return value != null && !value.isBlank() ? value : fallback; }
    private String normalizeText(String value) { return value == null ? "" : value.trim().toLowerCase(Locale.ROOT); }

    private String normalizeMetricType(String metricType) {
        String normalized = normalizeText(metricType).replace(' ', '_');
        if (normalized.contains("\u5fc3\u7387")) return "heart_rate";
        if (normalized.contains("\u8840\u538b")) return "blood_pressure";
        if (normalized.contains("\u8840\u6c27")) return "spo2";
        if (normalized.contains("\u4f53\u6e29")) return "temperature";
        if (normalized.contains("\u56f4\u680f") || normalized.contains("\u8d8a\u754c")) return "fence";
        if (normalized.contains("sos") || normalized.contains("\u6c42\u6551") || normalized.contains("\u6c42\u52a9")) return "sos";
        if (normalized.contains("\u79bb\u7ebf")) return "device_offline";
        if (normalized.contains("\u6d3b\u52a8") || normalized.contains("\u6b65\u6570")) return "activity";
        if (normalized.contains("\u4fe1\u53f7")) return "device_signal";
        if (normalized.contains("heart")) return "heart_rate";
        if (normalized.contains("pressure") || normalized.contains("blood")) return "blood_pressure";
        if (normalized.contains("spo2") || normalized.contains("oxygen")) return "spo2";
        if (normalized.contains("temp")) return "temperature";
        if (normalized.contains("fence")) return "fence";
        if (normalized.contains("sos")) return "sos";
        if (normalized.contains("offline")) return "device_offline";
        if (normalized.contains("activity") || normalized.contains("step")) return "activity";
        if (normalized.contains("signal")) return "device_signal";
        return normalized;
    }

    private String canonicalAbnormalType(String metricType, String abnormalType) {
        String normalized = normalizeMetricType(defaultText(metricType, abnormalType));
        if (!normalized.isBlank()) {
            return fallbackAbnormalType(normalized);
        }
        return defaultText(abnormalType, ALERT_HEALTH_RISK);
    }

    private String fallbackAbnormalType(String metricType) {
        return switch (normalizeMetricType(metricType)) {
            case "heart_rate" -> ALERT_HEART_RATE;
            case "spo2" -> ALERT_SPO2;
            case "blood_pressure" -> ALERT_BLOOD_PRESSURE;
            case "temperature" -> ALERT_TEMPERATURE;
            case "fence" -> ALERT_FENCE;
            case "sos" -> ALERT_SOS;
            case "device_offline" -> ALERT_DEVICE_OFFLINE;
            case "activity" -> ALERT_ACTIVITY;
            case "device_signal" -> ALERT_DEVICE_SIGNAL;
            default -> ALERT_HEALTH_RISK;
        };
    }

    private String defaultAlertMessage(String abnormalType) {
        return defaultText(abnormalType, ALERT_HEALTH_RISK) + "\uff0c\u8bf7\u53ca\u65f6\u5173\u6ce8";
    }

    private String detectMetricType(VitalSignData latest) {
        if (latest == null) return "health_risk";
        if (latest.getAbnormalType() != null && !latest.getAbnormalType().isBlank()) {
            return normalizeMetricType(latest.getAbnormalType());
        }
        if (latest.getHeartRate() != null && (latest.getHeartRate() > 110 || latest.getHeartRate() < 55)) {
            return "heart_rate";
        }
        if (latest.getSpo2() != null && latest.getSpo2() < 95) {
            return "spo2";
        }
        int[] bloodPressure = parseBloodPressure(latest.getBloodPressure());
        if (bloodPressure != null && (bloodPressure[0] > 140 || bloodPressure[0] < 90 || bloodPressure[1] > 90 || bloodPressure[1] < 60)) {
            return "blood_pressure";
        }
        if (latest.getTemperature() != null && (latest.getTemperature() >= 37.4 || latest.getTemperature() <= 35.5)) {
            return "temperature";
        }
        if ("offline".equalsIgnoreCase(defaultText(latest.getDeviceStatus(), ""))) {
            return "device_offline";
        }
        return normalizeMetricType(defaultText(latest.getMetricType(), "health_risk"));
    }

    private Date startOfDay(Date value) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(value.toInstant(), ZONE_ID).toLocalDate().atStartOfDay();
        return Date.from(localDateTime.atZone(ZONE_ID).toInstant());
    }

    private String round(Double value) { return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).toPlainString(); }

    private static class IngestionOptions {
        final boolean ruleOnly;
        final boolean emitRealtimeEvents;
        final boolean resolveGeneratedAlert;
        IngestionOptions(boolean ruleOnly, boolean emitRealtimeEvents, boolean resolveGeneratedAlert) {
            this.ruleOnly = ruleOnly;
            this.emitRealtimeEvents = emitRealtimeEvents;
            this.resolveGeneratedAlert = resolveGeneratedAlert;
        }
    }

    private static class RuleAlert {
        final String metricType;
        final String abnormalType;
        final String abnormalValue;
        final String normalRange;
        final String riskLevel;
        final int riskScore;
        final String message;
        RuleAlert(String metricType, String abnormalType, String abnormalValue, String normalRange, String riskLevel, int riskScore, String message) {
            this.metricType = metricType;
            this.abnormalType = abnormalType;
            this.abnormalValue = abnormalValue;
            this.normalRange = normalRange;
            this.riskLevel = riskLevel;
            this.riskScore = riskScore;
            this.message = message;
        }
    }
}
