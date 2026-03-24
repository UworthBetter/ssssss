package com.qkyd.web.demo;

import com.qkyd.ai.mapper.AbnormalRecordMapper;
import com.qkyd.ai.model.entity.AbnormalRecord;
import com.qkyd.ai.service.IEventProcessingPipelineService;
import com.qkyd.common.core.domain.entity.SysUser;
import com.qkyd.common.event.AbnormalDetectionEvent;
import com.qkyd.common.event.HealthDataUpdateEvent;
import com.qkyd.common.event.RiskScoreUpdateEvent;
import com.qkyd.common.utils.SecurityUtils;
import com.qkyd.health.domain.UeitBlood;
import com.qkyd.health.domain.UeitDeviceInfo;
import com.qkyd.health.domain.UeitDeviceInfoExtend;
import com.qkyd.health.domain.UeitException;
import com.qkyd.health.domain.UeitHeartRate;
import com.qkyd.health.domain.UeitLocation;
import com.qkyd.health.domain.UeitSpo2;
import com.qkyd.health.domain.UeitSteps;
import com.qkyd.health.domain.UeitTemp;
import com.qkyd.health.domain.dto.ai.AiHealthCheckResponse;
import com.qkyd.health.domain.dto.ai.VitalSignData;
import com.qkyd.health.service.IUeitBloodService;
import com.qkyd.health.service.IUeitDeviceInfoExtendService;
import com.qkyd.health.service.IUeitDeviceInfoService;
import com.qkyd.health.service.IUeitExceptionService;
import com.qkyd.health.service.IUeitHeartRateService;
import com.qkyd.health.service.IUeitLocationService;
import com.qkyd.health.service.IUeitSpo2Service;
import com.qkyd.health.service.IUeitStepsService;
import com.qkyd.health.service.IUeitTempService;
import com.qkyd.health.service.ai.HealthDataService;
import com.qkyd.system.service.ISysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class DemoDataEngineService {

    private static final Logger log = LoggerFactory.getLogger(DemoDataEngineService.class);

    private static final String DEMO_PASSWORD = "Demo@123456";
    private static final String DEMO_REMARK = "仿真健康数据自动生成";
    private static final String DEMO_CREATE_BY = "health_seed_engine";
    private static final String HEALTH_EMAIL_DOMAIN = "@health.local";
    private static final String LIVE_RESOLVE_NOTE = "仿真异常已自动闭环";
    private static final String HISTORY_RESOLVE_NOTE = "历史异常已按仿真规则自动归档";
    private static final long DEFAULT_DEPT_ID = 103L;
    private static final long DEMO_AUTO_RESOLVE_MINUTES = 45L;
    private static final int SAMPLE_INTERVAL_MINUTES = 5;
    private static final int HISTORY_SAMPLE_INTERVAL_MINUTES = 15;
    private static final int HISTORY_DAYS = 7;
    private static final int SCENARIO_BLOCK_TICKS = 9;
    private static final int MAX_SUPPORTED_SUBJECT_COUNT = 100;
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    private static final List<DemoProfileSeed> DEMO_SEEDS = List.of(
            new DemoProfileSeed(9101L, "王秀兰", "王秀兰", "13900000001", "0", 68,
                    "8681200019101", "康护腕表-01",
                    new BigDecimal("113.624930"), new BigDecimal("34.747250"), "郑州-二七广场",
                    ProfileArchetype.STABLE, "无明显慢病", "high", 67, 122, 76, 5800, "low", 96),
            new DemoProfileSeed(9102L, "李建国", "李建国", "13900000002", "1", 76,
                    "8681200019102", "康护腕表-02",
                    new BigDecimal("113.792520"), new BigDecimal("34.756630"), "郑州-郑州东站",
                    ProfileArchetype.CHRONIC, "高血压", "medium", 74, 146, 88, 3200, "medium", 91),
            new DemoProfileSeed(9103L, "陈桂英", "陈桂英", "13900000003", "0", 83,
                    "8681200019103", "康护腕表-03",
                    new BigDecimal("113.748880"), new BigDecimal("34.765110"), "郑州-郑东新区如意湖",
                    ProfileArchetype.FRAGILE, "高血压,冠心病", "low", 79, 152, 92, 1600, "high", 88),
            new DemoProfileSeed(9104L, "周德明", "周德明", "13900000004", "1", 79,
                    "8681200019104", "康护腕表-04",
                    new BigDecimal("113.686800"), new BigDecimal("34.759560"), "郑州-金水路省人民医院",
                    ProfileArchetype.ACUTE, "高血压,睡眠不规律", "medium", 73, 145, 86, 2600, "medium", 90),
            new DemoProfileSeed(9105L, "吴美琴", "吴美琴", "13900000005", "0", 72,
                    "8681200019105", "康护腕表-05",
                    new BigDecimal("113.640980"), new BigDecimal("34.756450"), "郑州-郑州大学一附院",
                    ProfileArchetype.DEVICE_ISSUE, "糖尿病", "medium", 72, 134, 82, 2800, "medium", 72),
            new DemoProfileSeed(9106L, "赵文华", "赵文华", "13900000006", "1", 74,
                    "8681200019106", "康护腕表-06",
                    new BigDecimal("113.721550"), new BigDecimal("34.739010"), "郑州-中原福塔",
                    ProfileArchetype.CHRONIC, "高血压,轻度心血管问题", "medium", 71, 138, 84, 3600, "medium", 89)
    );

    private final DemoDataEngineProperties properties;
    private final ISysUserService sysUserService;
    private final IUeitDeviceInfoService deviceInfoService;
    private final IUeitDeviceInfoExtendService deviceInfoExtendService;
    private final IUeitHeartRateService heartRateService;
    private final IUeitBloodService bloodService;
    private final IUeitSpo2Service spo2Service;
    private final IUeitTempService tempService;
    private final IUeitLocationService locationService;
    private final IUeitStepsService stepsService;
    private final IUeitExceptionService exceptionService;
    private final HealthDataService healthDataService;
    private final AbnormalRecordMapper abnormalRecordMapper;
    private final IEventProcessingPipelineService pipelineService;
    private final ApplicationEventPublisher eventPublisher;
    private final JdbcTemplate jdbcTemplate;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean datasetInitialized = new AtomicBoolean(false);
    private final AtomicBoolean legacyDataSanitized = new AtomicBoolean(false);
    private final AtomicLong lastGeneratedBucket = new AtomicLong(-1L);
    private final AtomicLong tickCounter = new AtomicLong(0L);
    private final AtomicLong runtimeSubjectCountOverride = new AtomicLong(-1L);
    private final AtomicReference<Boolean> runtimeEnabledOverride = new AtomicReference<>(null);
    private final Map<Long, DemoProfileState> profileCache = new ConcurrentHashMap<>();

    public DemoDataEngineService(
            DemoDataEngineProperties properties,
            ISysUserService sysUserService,
            IUeitDeviceInfoService deviceInfoService,
            IUeitDeviceInfoExtendService deviceInfoExtendService,
            IUeitHeartRateService heartRateService,
            IUeitBloodService bloodService,
            IUeitSpo2Service spo2Service,
            IUeitTempService tempService,
            IUeitLocationService locationService,
            IUeitStepsService stepsService,
            IUeitExceptionService exceptionService,
            HealthDataService healthDataService,
            AbnormalRecordMapper abnormalRecordMapper,
            IEventProcessingPipelineService pipelineService,
            ApplicationEventPublisher eventPublisher,
            JdbcTemplate jdbcTemplate) {
        this.properties = properties;
        this.sysUserService = sysUserService;
        this.deviceInfoService = deviceInfoService;
        this.deviceInfoExtendService = deviceInfoExtendService;
        this.heartRateService = heartRateService;
        this.bloodService = bloodService;
        this.spo2Service = spo2Service;
        this.tempService = tempService;
        this.locationService = locationService;
        this.stepsService = stepsService;
        this.exceptionService = exceptionService;
        this.healthDataService = healthDataService;
        this.abnormalRecordMapper = abnormalRecordMapper;
        this.pipelineService = pipelineService;
        this.eventPublisher = eventPublisher;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(
            fixedDelayString = "${qkyd.demo.engine.interval-ms:10000}",
            initialDelayString = "${qkyd.demo.engine.initial-delay-ms:5000}"
    )
    public void generateTick() {
        if (!isEngineEnabled() || !running.compareAndSet(false, true)) {
            return;
        }

        try {
            runScheduledTick();
        } catch (Exception e) {
            log.error("demo data engine tick failed", e);
        } finally {
            running.set(false);
        }
    }

    public Map<String, Object> getEngineStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        Boolean override = runtimeEnabledOverride.get();
        long subjectCountOverride = runtimeSubjectCountOverride.get();
        status.put("configuredEnabled", properties.isEnabled());
        status.put("effectiveEnabled", isEngineEnabled());
        status.put("runtimeEnabledOverride", override);
        status.put("configuredSubjectCount", properties.getSubjectCount());
        status.put("runtimeSubjectCountOverride", subjectCountOverride > 0 ? subjectCountOverride : null);
        status.put("effectiveSubjectCount", getEffectiveSubjectCount());
        status.put("maxSupportedSubjectCount", MAX_SUPPORTED_SUBJECT_COUNT);
        status.put("running", running.get());
        status.put("datasetInitialized", datasetInitialized.get());
        status.put("legacyDataSanitized", legacyDataSanitized.get());
        status.put("profileCacheSize", profileCache.size());
        status.put("tickCounter", tickCounter.get());
        status.put("lastGeneratedBucket", lastGeneratedBucket.get());
        return status;
    }

    public Map<String, Object> startRuntimeSimulation(Integer subjectCount) {
        runtimeEnabledOverride.set(Boolean.TRUE);
        if (subjectCount != null) {
            runtimeSubjectCountOverride.set(normalizeSubjectCount(subjectCount));
        }
        return getEngineStatus();
    }

    public Map<String, Object> stopRuntimeSimulation() {
        runtimeEnabledOverride.set(Boolean.FALSE);
        return getEngineStatus();
    }

    public Map<String, Object> resetRuntimeSimulation() {
        runtimeEnabledOverride.set(null);
        runtimeSubjectCountOverride.set(-1L);
        return getEngineStatus();
    }

    public Map<String, Object> setRuntimeSubjectCount(Integer subjectCount) {
        runtimeSubjectCountOverride.set(normalizeSubjectCount(subjectCount));
        return getEngineStatus();
    }

    public Map<String, Object> triggerManualTick(Integer subjectCount) {
        if (subjectCount != null) {
            runtimeSubjectCountOverride.set(normalizeSubjectCount(subjectCount));
        }
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("demo data engine is already running");
        }

        Map<String, Object> status;
        try {
            runManualTick();
            status = getEngineStatus();
        } finally {
            running.set(false);
        }
        status.put("running", false);
        return status;
    }

    private boolean isEngineEnabled() {
        Boolean override = runtimeEnabledOverride.get();
        return override != null ? override : properties.isEnabled();
    }

    private int getEffectiveSubjectCount() {
        long override = runtimeSubjectCountOverride.get();
        int requested = override > 0 ? (int) override : properties.getSubjectCount();
        return Math.max(1, Math.min(requested, MAX_SUPPORTED_SUBJECT_COUNT));
    }

    private long normalizeSubjectCount(Integer subjectCount) {
        if (subjectCount == null) {
            throw new IllegalArgumentException("subjectCount is required");
        }
        return Math.max(1, Math.min(subjectCount.longValue(), MAX_SUPPORTED_SUBJECT_COUNT));
    }

    private DemoProfileSeed resolveSeed(int index) {
        if (index < DEMO_SEEDS.size()) {
            return DEMO_SEEDS.get(index);
        }

        DemoProfileSeed template = DEMO_SEEDS.get(index % DEMO_SEEDS.size());
        int serial = index + 1;
        long userId = 9100L + serial;
        String userName = "sim_user_" + String.format("%03d", serial);
        String nickName = "Sim-" + String.format("%03d", serial);
        String phone = "139" + String.format("%08d", serial);
        String imei = "8681209" + String.format("%06d", serial);
        String deviceName = "Sim-Watch-" + String.format("%03d", serial);
        BigDecimal lng = template.baseLng().add(decimalOffset(((serial % 9) - 4) * 0.0032));
        BigDecimal lat = template.baseLat().add(decimalOffset((((serial / 3) % 9) - 4) * 0.0027));
        String location = template.baseLocation() + "-SIM-" + String.format("%03d", serial);
        int age = Math.max(60, template.age() + (serial % 7) - 3);
        int hr = clamp(template.baselineHeartRate() + (serial % 6) - 2, 58, 92);
        int sys = clamp(template.baselineSystolic() + (serial % 9) - 4, 110, 155);
        int dia = clamp(template.baselineDiastolic() + (serial % 7) - 3, 68, 95);
        int steps = clamp(template.dailyStepTarget() + ((serial % 7) - 3) * 280, 1600, 7600);
        int reliability = clamp(template.deviceReliability() - (serial % 8), 68, 98);

        return new DemoProfileSeed(
                userId,
                userName,
                nickName,
                phone,
                serial % 2 == 0 ? "1" : "0",
                age,
                imei,
                deviceName,
                lng,
                lat,
                location,
                template.archetype(),
                template.chronicTags(),
                template.mobilityLevel(),
                hr,
                sys,
                dia,
                steps,
                template.riskBaseline(),
                reliability
        );
    }

    private void runScheduledTick() {
        tickCounter.incrementAndGet();
        sanitizeLegacySeedData();
        List<DemoProfileState> profiles = ensureProfiles();
        if (profiles.isEmpty()) {
            return;
        }

        LocalDateTime now = alignToSampleWindow(LocalDateTime.now(ZONE_ID), SAMPLE_INTERVAL_MINUTES);
        initializeControlledDataset(profiles, now);
        autoResolveStaleAlerts(profiles, toDate(now));
        generateMissingLiveBuckets(profiles, now);
    }

    private void runManualTick() {
        tickCounter.incrementAndGet();
        sanitizeLegacySeedData();
        List<DemoProfileState> profiles = ensureProfiles();
        if (profiles.isEmpty()) {
            return;
        }

        LocalDateTime now = alignToSampleWindow(LocalDateTime.now(ZONE_ID), SAMPLE_INTERVAL_MINUTES);
        initializeControlledDataset(profiles, now);
        autoResolveStaleAlerts(profiles, toDate(now));
        generateControlledBucket(profiles, now, true);
        lastGeneratedBucket.set(resolveLiveBucket(now));
    }

    private List<DemoProfileState> ensureProfiles() {
        List<DemoProfileState> states = new ArrayList<>();
        int expectedCount = getEffectiveSubjectCount();

        for (int index = 0; index < expectedCount; index++) {
            DemoProfileSeed seed = resolveSeed(index);
            DemoProfileState cached = profileCache.get(seed.userId());
            if (cached != null) {
                ensureDeviceBinding(cached.userId(), cached.deviceId());
                states.add(cached);
                continue;
            }

            SysUser user = ensureSysUser(seed);
            ensureHealthSubject(seed, user.getUserId());
            UeitDeviceInfo device = ensureDevice(seed, user.getUserId());
            ensureDeviceExtend(seed, device.getId());
            ensureDeviceBinding(user.getUserId(), device.getId());

            DemoProfileState state = new DemoProfileState(seed, user.getUserId(), device.getId());
            profileCache.put(seed.userId(), state);
            states.add(state);
        }

        return states;
    }

    private void sanitizeLegacySeedData() {
        if (!legacyDataSanitized.compareAndSet(false, true)) {
            return;
        }
        purgeLegacyDemoProfiles();
        for (DemoProfileSeed seed : DEMO_SEEDS) {
            jdbcTemplate.update(
                    "UPDATE sys_user SET user_name = ?, nick_name = ?, email = ?, remark = ?, update_by = ? "
                            + "WHERE phonenumber = ?",
                    seed.userName(),
                    seed.nickName(),
                    seed.userName() + HEALTH_EMAIL_DOMAIN,
                    DEMO_REMARK,
                    DEMO_CREATE_BY,
                    seed.phone()
            );
            jdbcTemplate.update(
                    "UPDATE health_subject SET subject_name = ?, nick_name = ?, email = ?, remark = ?, update_by = ? "
                            + "WHERE phonenumber = ?",
                    seed.userName(),
                    seed.nickName(),
                    seed.userName() + HEALTH_EMAIL_DOMAIN,
                    buildSubjectRemark(seed),
                    DEMO_CREATE_BY,
                    seed.phone()
            );
            jdbcTemplate.update(
                    "UPDATE qkyd_device_info SET name = ?, imei = ?, type = 'SMART_WATCH' "
                            + "WHERE user_id IN (SELECT user_id FROM sys_user WHERE phonenumber = ?)",
                    seed.deviceName(),
                    seed.imei(),
                    seed.phone()
            );
        }
        jdbcTemplate.update("UPDATE qkyd_device_info SET type = 'SMART_WATCH' WHERE type = 'DEMO_WATCH'");
        jdbcTemplate.update("UPDATE ai_abnormal_record SET detection_method = 'SIM_ENGINE' WHERE detection_method = 'DEMO_ENGINE'");
    }

    private SysUser ensureSysUser(DemoProfileSeed seed) {
        SysUser existing = sysUserService.selectUserByUserName(seed.userName());
        if (existing != null) {
            boolean needsUpdate = !seed.nickName().equals(existing.getNickName())
                    || !seed.phone().equals(existing.getPhonenumber())
                    || !seed.sex().equals(existing.getSex())
                    || seed.age() != (existing.getAge() == null ? 0 : existing.getAge())
                    || existing.getDeptId() == null
                    || existing.getDeptId() == 0L
                    || !seed.userName().concat(HEALTH_EMAIL_DOMAIN).equals(existing.getEmail());
            if (needsUpdate) {
                existing.setDeptId(DEFAULT_DEPT_ID);
                existing.setNickName(seed.nickName());
                existing.setPhonenumber(seed.phone());
                existing.setSex(seed.sex());
                existing.setAge(seed.age());
                existing.setEmail(seed.userName() + HEALTH_EMAIL_DOMAIN);
                existing.setStatus("0");
                existing.setRemark(DEMO_REMARK);
                existing.setUpdateBy(DEMO_CREATE_BY);
                sysUserService.updateUserProfile(existing);
            }
            return existing;
        }

        SysUser user = new SysUser();
        user.setDeptId(DEFAULT_DEPT_ID);
        user.setUserName(seed.userName());
        user.setNickName(seed.nickName());
        user.setPhonenumber(seed.phone());
        user.setSex(seed.sex());
        user.setAge(seed.age());
        user.setEmail(seed.userName() + HEALTH_EMAIL_DOMAIN);
        user.setPassword(SecurityUtils.encryptPassword(DEMO_PASSWORD));
        user.setStatus("0");
        user.setUserType("00");
        user.setCreateBy(DEMO_CREATE_BY);
        user.setRemark(DEMO_REMARK);
        boolean registered = sysUserService.registerUser(user);
        if (!registered) {
            throw new IllegalStateException("Failed to register demo user: " + seed.userName());
        }

        SysUser created = sysUserService.selectUserByUserName(seed.userName());
        if (created == null || created.getUserId() == null) {
            throw new IllegalStateException("Demo user not found after registration: " + seed.userName());
        }
        return created;
    }

    private void ensureHealthSubject(DemoProfileSeed seed, Long subjectId) {
        jdbcTemplate.update(
                "INSERT INTO health_subject (subject_id, subject_name, nick_name, user_type, email, phonenumber, age, sex, status, del_flag, create_by, create_time, update_by, update_time, remark) "
                        + "VALUES (?, ?, ?, '00', ?, ?, ?, ?, '0', '0', ?, NOW(), ?, NOW(), ?) "
                        + "ON DUPLICATE KEY UPDATE subject_name = VALUES(subject_name), nick_name = VALUES(nick_name), "
                        + "email = VALUES(email), phonenumber = VALUES(phonenumber), age = VALUES(age), sex = VALUES(sex), "
                        + "status = '0', del_flag = '0', update_by = VALUES(update_by), update_time = NOW(), remark = VALUES(remark)",
                subjectId, seed.userName(), seed.nickName(), seed.userName() + HEALTH_EMAIL_DOMAIN, seed.phone(), seed.age(), seed.sex(),
                DEMO_CREATE_BY, DEMO_CREATE_BY, buildSubjectRemark(seed)
        );
    }

    private String buildSubjectRemark(DemoProfileSeed seed) {
        return "profile=" + seed.archetype().name().toLowerCase()
                + "; chronic=" + seed.chronicTags()
                + "; mobility=" + seed.mobilityLevel()
                + "; riskBaseline=" + seed.riskBaseline()
                + "; reliability=" + seed.deviceReliability();
    }

    private void initializeControlledDataset(List<DemoProfileState> profiles, LocalDateTime now) {
        if (!datasetInitialized.compareAndSet(false, true)) {
            return;
        }

        clearControlledSeedData(profiles);
        for (DemoProfileState profile : profiles) {
            ensureDeviceBinding(profile.userId(), profile.deviceId());
            ensureDeviceExtend(profile.seed(), profile.deviceId());
        }

        LocalDate startDate = now.toLocalDate().minusDays(HISTORY_DAYS - 1L);
        for (LocalDate date = startDate; !date.isAfter(now.toLocalDate()); date = date.plusDays(1)) {
            int intervalMinutes = intervalForDate(date, now.toLocalDate());
            LocalDateTime cursor = date.atStartOfDay();
            LocalDateTime limit = date.equals(now.toLocalDate())
                    ? now
                    : alignToSampleWindow(date.atTime(23, 59), intervalMinutes);

            while (!cursor.isAfter(limit)) {
                generateControlledBucket(profiles, cursor, false);
                cursor = cursor.plusMinutes(intervalMinutes);
            }
        }

        lastGeneratedBucket.set(resolveLiveBucket(now));
        log.info("controlled seed dataset initialized through {}", now);
    }

    private void generateMissingLiveBuckets(List<DemoProfileState> profiles, LocalDateTime now) {
        long currentBucket = resolveLiveBucket(now);
        long previousBucket = lastGeneratedBucket.get();
        if (previousBucket < 0) {
            lastGeneratedBucket.set(currentBucket);
            return;
        }

        for (long bucket = previousBucket + 1; bucket <= currentBucket; bucket++) {
            generateControlledBucket(profiles, resolveLiveBucketTime(bucket), true);
        }

        lastGeneratedBucket.set(currentBucket);
    }

    private void generateControlledBucket(List<DemoProfileState> profiles, LocalDateTime sampleTime, boolean emitRealtimeEvents) {
        Date sampleDate = toDate(sampleTime);
        for (DemoProfileState profile : profiles) {
            MetricSnapshot snapshot = buildControlledSnapshot(profile, sampleTime);
            AiHealthCheckResponse response = healthDataService.processSimulatedData(
                    profile.seed().imei(),
                    profile.userId(),
                    List.of(toVitalSignData(snapshot, sampleDate)),
                    emitRealtimeEvents,
                    !emitRealtimeEvents && snapshot.createAlert()
            );
            if (emitRealtimeEvents && response != null && response.getGeneratedEventId() != null) {
                triggerPipeline(response.getGeneratedEventId(), snapshot);
            }
        }
    }

    private VitalSignData toVitalSignData(MetricSnapshot snapshot, Date sampleDate) {
        VitalSignData data = new VitalSignData();
        data.setHeartRate(snapshot.heartRate());
        data.setBloodPressure(snapshot.systolic() + "/" + snapshot.diastolic());
        data.setSteps(snapshot.steps());
        data.setSpo2(snapshot.spo2());
        data.setTemperature(snapshot.temperature());
        data.setBatteryLevel(snapshot.batteryLevel());
        data.setSignalQuality(snapshot.signalQuality());
        data.setDeviceStatus(snapshot.deviceStatus());
        data.setLongitude(snapshot.longitude().doubleValue());
        data.setLatitude(snapshot.latitude().doubleValue());
        data.setLocation(snapshot.location());
        data.setLocationType("offline".equals(snapshot.deviceStatus()) ? "GPS-OFFLINE" : "GPS");
        data.setMetricType(snapshot.metricType());
        if (snapshot.createAlert()) {
            data.setAbnormalType(canonicalAlertType(snapshot.metricType(), snapshot.abnormalType()));
            data.setAbnormalValue(canonicalAlertValue(snapshot));
            data.setNormalRange(canonicalNormalRange(snapshot));
            data.setRiskLevel(snapshot.riskLevel());
            data.setRiskScore(snapshot.riskScore());
            data.setMessage(canonicalAlertMessage(snapshot));
        }
        data.setTimestamp(sampleDate.getTime());
        return data;
    }

    private String canonicalAlertType(String metricType, String fallback) {
        return switch (String.valueOf(metricType == null ? "" : metricType).trim().toLowerCase()) {
            case "heart_rate" -> "\u5fc3\u7387\u5f02\u5e38";
            case "spo2" -> "\u8840\u6c27\u5f02\u5e38";
            case "temperature" -> "\u4f53\u6e29\u5f02\u5e38";
            case "fence" -> "\u56f4\u680f\u8d8a\u754c";
            case "sos" -> "SOS\u6c42\u6551";
            case "blood_pressure" -> "\u8840\u538b\u5f02\u5e38";
            case "activity" -> "\u6d3b\u52a8\u91cf\u5f02\u5e38";
            case "device_signal" -> "\u8bbe\u5907\u4fe1\u53f7\u5f02\u5e38";
            case "device_offline" -> "\u8bbe\u5907\u79bb\u7ebf";
            default -> (fallback == null || fallback.isBlank()) ? "\u5065\u5eb7\u98ce\u9669\u9884\u8b66" : fallback;
        };
    }

    private String canonicalAlertValue(MetricSnapshot snapshot) {
        return switch (String.valueOf(snapshot.metricType() == null ? "" : snapshot.metricType()).trim().toLowerCase()) {
            case "heart_rate" -> snapshot.heartRate() + " bpm";
            case "spo2" -> snapshot.spo2() + "%";
            case "temperature" -> formatDecimal(snapshot.temperature()) + "\u00b0C";
            case "fence" -> "\u8d85\u51fa\u5b89\u5168\u533a\u57df";
            case "sos" -> "\u624b\u52a8\u89e6\u53d1";
            case "blood_pressure" -> snapshot.systolic() + "/" + snapshot.diastolic() + " mmHg";
            case "activity" -> snapshot.steps() + " \u6b65/\u65e5";
            case "device_signal" -> snapshot.signalQuality() + "%";
            case "device_offline" -> "\u8fde\u7eed\u7f3a\u62a5";
            default -> snapshot.abnormalValue();
        };
    }

    private String canonicalNormalRange(MetricSnapshot snapshot) {
        return switch (String.valueOf(snapshot.metricType() == null ? "" : snapshot.metricType()).trim().toLowerCase()) {
            case "heart_rate" -> "60-100 bpm";
            case "spo2" -> "95-99%";
            case "temperature" -> "36.0-37.3\u00b0C";
            case "fence" -> "\u4f4d\u4e8e\u5b89\u5168\u533a\u57df";
            case "sos" -> "\u672a\u89e6\u53d1";
            case "blood_pressure" -> "90-140/60-90 mmHg";
            case "activity" -> Math.round(profileTargetBaseline(snapshot) * 0.8) + "-" + Math.round(profileTargetBaseline(snapshot) * 1.15) + " \u6b65/\u65e5";
            case "device_signal" -> ">=85%";
            case "device_offline" -> "\u8bbe\u5907\u5728\u7ebf";
            default -> snapshot.normalRange();
        };
    }

    private int profileTargetBaseline(MetricSnapshot snapshot) {
        return Math.max(snapshot.steps(), 2000);
    }

    private String canonicalAlertMessage(MetricSnapshot snapshot) {
        return switch (String.valueOf(snapshot.metricType() == null ? "" : snapshot.metricType()).trim().toLowerCase()) {
            case "heart_rate" -> "\u8fde\u7eed\u5fc3\u7387\u504f\u9ad8\uff0c\u8bf7\u53ca\u65f6\u590d\u6838";
            case "spo2" -> "\u8840\u6c27\u6301\u7eed\u504f\u4f4e\uff0c\u8bf7\u5173\u6ce8\u547c\u5438\u72b6\u6001";
            case "temperature" -> "\u4f53\u6e29\u6301\u7eed\u504f\u9ad8\uff0c\u5efa\u8bae\u7ed3\u5408\u5176\u4ed6\u4f53\u5f81\u590d\u6838";
            case "fence" -> "\u8f68\u8ff9\u8131\u79bb\u5b89\u5168\u533a\u57df\uff0c\u8bf7\u5c3d\u5feb\u786e\u8ba4\u5916\u51fa\u72b6\u6001";
            case "sos" -> "\u89e6\u53d1 SOS \u6c42\u6551\uff0c\u9700\u4f18\u5148\u56de\u8bbf\u6838\u5b9e";
            case "blood_pressure" -> "\u51fa\u73b0\u9636\u6bb5\u6027\u8840\u538b\u5347\u9ad8\uff0c\u8bf7\u6301\u7eed\u89c2\u5bdf";
            case "activity" -> "\u5f53\u65e5\u6d3b\u52a8\u91cf\u660e\u663e\u504f\u4f4e";
            case "device_signal" -> "\u8bbe\u5907\u4fe1\u53f7\u8d28\u91cf\u660e\u663e\u4e0b\u964d";
            case "device_offline" -> "\u8bbe\u5907\u8fdb\u5165\u79bb\u7ebf\u72b6\u6001\uff0c\u8bf7\u4f18\u5148\u6392\u67e5";
            default -> snapshot.message();
        };
    }

    private void clearControlledSeedData(List<DemoProfileState> profiles) {
        List<Long> userIds = profiles.stream().map(DemoProfileState::userId).toList();
        List<Long> deviceIds = profiles.stream().map(DemoProfileState::deviceId).toList();
        List<String> imeis = profiles.stream().map(profile -> profile.seed().imei()).toList();

        String userPlaceholders = buildPlaceholders(userIds.size());
        String devicePlaceholders = buildPlaceholders(deviceIds.size());
        String imeiPlaceholders = buildPlaceholders(imeis.size());

        jdbcTemplate.update(
                "DELETE FROM ai_event_insight_snapshot WHERE event_id IN (SELECT id FROM qkyd_exception WHERE user_id IN (" + userPlaceholders + "))",
                userIds.toArray()
        );
        jdbcTemplate.update(
                "DELETE FROM ai_event_processing_pipeline WHERE event_id IN (SELECT id FROM qkyd_exception WHERE user_id IN (" + userPlaceholders + "))",
                userIds.toArray()
        );

        List<Object> abnormalParams = new ArrayList<>(userIds);
        abnormalParams.addAll(imeis);
        jdbcTemplate.update(
                "DELETE FROM ai_abnormal_record WHERE user_id IN (" + userPlaceholders + ") "
                        + "OR device_id IN (" + imeiPlaceholders + ") "
                        + "OR detection_method IN ('SIM_ENGINE', 'DEMO_ENGINE') "
                        + "OR device_id LIKE 'DEMO-%'",
                abnormalParams.toArray()
        );

        List<Object> userAndDeviceParams = new ArrayList<>(userIds);
        userAndDeviceParams.addAll(deviceIds);
        deleteSeedRows("qkyd_exception", userPlaceholders, devicePlaceholders, userAndDeviceParams);
        deleteSeedRows("qkyd_location", userPlaceholders, devicePlaceholders, userAndDeviceParams);
        deleteSeedRows("qkyd_heart_rate", userPlaceholders, devicePlaceholders, userAndDeviceParams);
        deleteSeedRows("qkyd_blood", userPlaceholders, devicePlaceholders, userAndDeviceParams);
        deleteSeedRows("qkyd_spo2", userPlaceholders, devicePlaceholders, userAndDeviceParams);
        deleteSeedRows("qkyd_temp", userPlaceholders, devicePlaceholders, userAndDeviceParams);
        deleteSeedRows("qkyd_steps", userPlaceholders, devicePlaceholders, userAndDeviceParams);

        List<Object> bindingParams = new ArrayList<>(userIds);
        bindingParams.addAll(deviceIds);
        jdbcTemplate.update(
                "DELETE FROM health_device_binding WHERE subject_id IN (" + userPlaceholders + ") OR device_id IN (" + devicePlaceholders + ")",
                bindingParams.toArray()
        );
        jdbcTemplate.update(
                "DELETE FROM qkyd_device_info_extend WHERE device_id IN (" + devicePlaceholders + ")",
                deviceIds.toArray()
        );
    }

    private void purgeLegacyDemoProfiles() {
        List<Long> legacyUserIds = jdbcTemplate.queryForList(
                "SELECT user_id FROM sys_user WHERE user_name LIKE 'demo_%' OR email LIKE 'demo%@qkyd.local'",
                Long.class
        );
        List<Long> legacyDeviceIds;
        List<String> legacyImeis;
        if (legacyUserIds.isEmpty()) {
            legacyDeviceIds = jdbcTemplate.queryForList(
                    "SELECT id FROM qkyd_device_info WHERE imei LIKE 'IMEI910%' OR name LIKE '腕表-A%'",
                    Long.class
            );
            legacyImeis = jdbcTemplate.queryForList(
                    "SELECT imei FROM qkyd_device_info WHERE imei LIKE 'IMEI910%' OR name LIKE '腕表-A%'",
                    String.class
            ).stream().filter(imei -> imei != null && !imei.isBlank()).toList();
        } else {
            String userPlaceholders = buildPlaceholders(legacyUserIds.size());
            legacyDeviceIds = jdbcTemplate.queryForList(
                    "SELECT id FROM qkyd_device_info WHERE user_id IN (" + userPlaceholders + ") OR imei LIKE 'IMEI910%' OR name LIKE '腕表-A%'",
                    Long.class,
                    legacyUserIds.toArray()
            );
            legacyImeis = jdbcTemplate.queryForList(
                    "SELECT imei FROM qkyd_device_info WHERE user_id IN (" + userPlaceholders + ") OR imei LIKE 'IMEI910%' OR name LIKE '腕表-A%'",
                    String.class,
                    legacyUserIds.toArray()
            ).stream().filter(imei -> imei != null && !imei.isBlank()).toList();
        }

        if (legacyUserIds.isEmpty() && legacyDeviceIds.isEmpty()) {
            return;
        }

        if (!legacyUserIds.isEmpty()) {
            String userPlaceholders = buildPlaceholders(legacyUserIds.size());
            jdbcTemplate.update(
                    "DELETE FROM ai_event_insight_snapshot WHERE event_id IN (SELECT id FROM qkyd_exception WHERE user_id IN (" + userPlaceholders + "))",
                    legacyUserIds.toArray()
            );
            jdbcTemplate.update(
                    "DELETE FROM ai_event_processing_pipeline WHERE event_id IN (SELECT id FROM qkyd_exception WHERE user_id IN (" + userPlaceholders + "))",
                    legacyUserIds.toArray()
            );
            deleteRowsByUserIds("qkyd_exception", userPlaceholders, legacyUserIds);
            deleteRowsByUserIds("qkyd_location", userPlaceholders, legacyUserIds);
            deleteRowsByUserIds("qkyd_heart_rate", userPlaceholders, legacyUserIds);
            deleteRowsByUserIds("qkyd_blood", userPlaceholders, legacyUserIds);
            deleteRowsByUserIds("qkyd_spo2", userPlaceholders, legacyUserIds);
            deleteRowsByUserIds("qkyd_temp", userPlaceholders, legacyUserIds);
            deleteRowsByUserIds("qkyd_steps", userPlaceholders, legacyUserIds);
            jdbcTemplate.update(
                    "DELETE FROM health_subject WHERE subject_id IN (" + userPlaceholders + ") OR email LIKE 'demo%@qkyd.local' OR subject_name LIKE 'demo_%'",
                    legacyUserIds.toArray()
            );
            jdbcTemplate.update(
                    "DELETE FROM sys_user_role WHERE user_id IN (" + userPlaceholders + ")",
                    legacyUserIds.toArray()
            );
            jdbcTemplate.update(
                    "DELETE FROM sys_user_post WHERE user_id IN (" + userPlaceholders + ")",
                    legacyUserIds.toArray()
            );
            jdbcTemplate.update(
                    "DELETE FROM sys_user WHERE user_id IN (" + userPlaceholders + ")",
                    legacyUserIds.toArray()
            );
        }

        List<Object> abnormalParams = new ArrayList<>();
        StringBuilder abnormalSql = new StringBuilder("DELETE FROM ai_abnormal_record WHERE detection_method = 'DEMO_ENGINE'");
        if (!legacyUserIds.isEmpty()) {
            abnormalParams.addAll(legacyUserIds);
            abnormalSql.append(" OR user_id IN (").append(buildPlaceholders(legacyUserIds.size())).append(")");
        }
        if (!legacyImeis.isEmpty()) {
            abnormalParams.addAll(legacyImeis);
            abnormalSql.append(" OR device_id IN (").append(buildPlaceholders(legacyImeis.size())).append(")");
        }
        jdbcTemplate.update(abnormalSql.toString(), abnormalParams.toArray());

        if (!legacyDeviceIds.isEmpty()) {
            String devicePlaceholders = buildPlaceholders(legacyDeviceIds.size());
            jdbcTemplate.update(
                    "DELETE FROM health_device_binding WHERE device_id IN (" + devicePlaceholders + ")",
                    legacyDeviceIds.toArray()
            );
            jdbcTemplate.update(
                    "DELETE FROM qkyd_device_info_extend WHERE device_id IN (" + devicePlaceholders + ")",
                    legacyDeviceIds.toArray()
            );
            jdbcTemplate.update(
                    "DELETE FROM qkyd_device_info WHERE id IN (" + devicePlaceholders + ")",
                    legacyDeviceIds.toArray()
            );
        }

        log.info("purged {} legacy demo users and {} legacy devices", legacyUserIds.size(), legacyDeviceIds.size());
    }

    private void deleteSeedRows(String tableName,
                                String userPlaceholders,
                                String devicePlaceholders,
                                List<Object> params) {
        jdbcTemplate.update(
                "DELETE FROM " + tableName + " WHERE user_id IN (" + userPlaceholders + ") OR device_id IN (" + devicePlaceholders + ")",
                params.toArray()
        );
    }

    private void deleteRowsByUserIds(String tableName, String userPlaceholders, List<Long> userIds) {
        jdbcTemplate.update(
                "DELETE FROM " + tableName + " WHERE user_id IN (" + userPlaceholders + ")",
                userIds.toArray()
        );
    }

    private String buildPlaceholders(int size) {
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(index -> "?")
                .collect(Collectors.joining(","));
    }

    private MetricSnapshot buildControlledSnapshot(DemoProfileState profile, LocalDateTime sampleTime) {
        DemoProfileSeed seed = profile.seed();
        DemoScenario scenario = resolveControlledScenario(profile, sampleTime);
        ActivitySegment segment = resolveActivitySegment(sampleTime.toLocalTime());

        int heartRate = clamp(seed.baselineHeartRate()
                + segment.hrOffset()
                + stableInt(profile, sampleTime, "hr-base", -3, 3), 50, 128);
        int spo2 = clamp(baseSpo2(seed) + stableInt(profile, sampleTime, "spo2-base", -1, 1), 92, 99);
        double temperature = round(36.4 + segment.tempOffset() + stableInt(profile, sampleTime, "temp-base", -2, 2) / 10.0);
        int systolic = clamp(seed.baselineSystolic()
                + segment.systolicOffset()
                + morningPeak(sampleTime.toLocalTime())
                + stableInt(profile, sampleTime, "sys-base", -4, 4), 98, 175);
        int diastolic = clamp(seed.baselineDiastolic()
                + segment.diastolicOffset()
                + morningPeak(sampleTime.toLocalTime()) / 3
                + stableInt(profile, sampleTime, "dia-base", -3, 3), 60, 110);
        int steps = computeControlledDailySteps(profile, sampleTime);
        int stepIncrement = computeControlledStepIncrement(profile, sampleTime, steps, scenario);
        int batteryLevel = clamp(seed.deviceReliability()
                - stableInt(profile, sampleTime.toLocalDate().atStartOfDay(), "battery-drift", 3, 16)
                + stableInt(profile, sampleTime, "battery-noise", -2, 1), 28, 100);
        int signalQuality = clamp(seed.deviceReliability() + stableInt(profile, sampleTime, "signal-base", -3, 3), 35, 100);
        String deviceStatus = "online";
        BigDecimal lng = jitterCoordinateStable(seed.baseLng(), movementVariance(seed.mobilityLevel()),
                stableDouble(profile, sampleTime, "lng-jitter", -1.0, 1.0));
        BigDecimal lat = jitterCoordinateStable(seed.baseLat(), movementVariance(seed.mobilityLevel()) * 0.85,
                stableDouble(profile, sampleTime, "lat-jitter", -1.0, 1.0));
        String location = seed.baseLocation();
        String metricType = "";
        String abnormalType = "";
        String abnormalValue = "";
        String normalRange = "";
        String message = seed.nickName() + "当前生命体征与日常作息匹配，波动维持在个体基线附近。";
        int riskScore = clamp(baseRisk(seed) + riskByTime(segment) + stableInt(profile, sampleTime, "risk-base", -2, 3), 20, 72);

        switch (scenario) {
            case HEART_RATE -> {
                heartRate = clamp(seed.baselineHeartRate() + 22 + stableInt(profile, sampleTime, "hr-alert", 4, 12), 88, 132);
                systolic = clamp(systolic + stableInt(profile, sampleTime, "hr-alert-sys", 6, 12), 112, 170);
                metricType = "heart_rate";
                abnormalType = "心率异常";
                abnormalValue = heartRate + " 次/分";
                normalRange = "60-100 次/分";
                message = seed.nickName() + "在连续窗口内心率持续偏高，符合短时心率异常特征。";
                riskScore = clamp(baseRisk(seed) + 30 + stableInt(profile, sampleTime, "risk-hr", 0, 6), 74, 92);
            }
            case SPO2 -> {
                spo2 = clamp(baseSpo2(seed) - stableInt(profile, sampleTime, "spo2-alert", 3, 6), 89, 95);
                heartRate = clamp(heartRate + stableInt(profile, sampleTime, "spo2-hr", 4, 10), 64, 118);
                metricType = "spo2";
                abnormalType = "血氧异常";
                abnormalValue = spo2 + "%";
                normalRange = "95-99%";
                message = seed.nickName() + "血氧在连续片段内偏低，并伴随心率补偿性上扬。";
                riskScore = clamp(baseRisk(seed) + 24 + stableInt(profile, sampleTime, "risk-spo2", 0, 6), 68, 90);
            }
            case TEMPERATURE -> {
                temperature = round(37.3 + stableInt(profile, sampleTime, "temp-alert", 0, 7) / 10.0);
                metricType = "temperature";
                abnormalType = "体温异常";
                abnormalValue = formatDecimal(temperature) + "℃";
                normalRange = "36.0-37.3℃";
                message = seed.nickName() + "体温在连续监测窗口内偏高，建议结合其他体征复核。";
                riskScore = clamp(baseRisk(seed) + 18 + stableInt(profile, sampleTime, "risk-temp", 0, 4), 62, 82);
            }
            case FENCE -> {
                lng = seed.baseLng().add(new BigDecimal("0.016500"));
                lat = seed.baseLat().add(new BigDecimal("0.012200"));
                location = seed.baseLocation() + "-围栏外延0.8公里";
                metricType = "fence";
                abnormalType = "围栏越界";
                abnormalValue = "超出安全区域";
                normalRange = "安全区域内";
                message = seed.nickName() + "移动轨迹脱离常驻范围，需要人工确认外出状态。";
                riskScore = clamp(baseRisk(seed) + 22 + stableInt(profile, sampleTime, "risk-fence", 0, 4), 66, 86);
            }
            case SOS -> {
                heartRate = clamp(seed.baselineHeartRate() + 28 + stableInt(profile, sampleTime, "sos-hr", 6, 12), 96, 138);
                systolic = clamp(seed.baselineSystolic() + 18 + stableInt(profile, sampleTime, "sos-sys", 4, 10), 126, 180);
                diastolic = clamp(seed.baselineDiastolic() + 10 + stableInt(profile, sampleTime, "sos-dia", 2, 6), 86, 110);
                metricType = "sos";
                abnormalType = "SOS求助";
                abnormalValue = "手动触发";
                normalRange = "无";
                message = seed.nickName() + "触发 SOS 求助，需优先电话回访并核实现场情况。";
                riskScore = 96;
            }
            case BLOOD_PRESSURE -> {
                systolic = clamp(seed.baselineSystolic() + stableInt(profile, sampleTime, "bp-sys", 14, 24), 142, 176);
                diastolic = clamp(seed.baselineDiastolic() + stableInt(profile, sampleTime, "bp-dia", 8, 14), 90, 108);
                metricType = "blood_pressure";
                abnormalType = "血压异常";
                abnormalValue = systolic + "/" + diastolic + " mmHg";
                normalRange = "90-140/60-90 mmHg";
                message = seed.nickName() + "在晨间监测窗口出现阶段性高血压峰值，随后应逐步回落。";
                riskScore = clamp(baseRisk(seed) + 26 + stableInt(profile, sampleTime, "risk-bp", 0, 6), 72, 92);
            }
            case LOW_ACTIVITY -> {
                stepIncrement = Math.min(stepIncrement, 6);
                metricType = "activity";
                abnormalType = "活动量异常";
                abnormalValue = steps + " 步/日";
                normalRange = Math.round(seed.dailyStepTarget() * 0.8) + "-" + Math.round(seed.dailyStepTarget() * 1.15) + " 步/日";
                message = seed.nickName() + "当日累计步数明显低于个人常态目标，符合低活动量预警。";
                riskScore = clamp(baseRisk(seed) + 16 + stableInt(profile, sampleTime, "risk-activity", 0, 4), 58, 80);
            }
            case DEVICE_UNSTABLE -> {
                signalQuality = clamp(seed.deviceReliability() - stableInt(profile, sampleTime, "signal-alert", 18, 34), 28, 68);
                deviceStatus = "unstable";
                metricType = "device_signal";
                abnormalType = "设备信号异常";
                abnormalValue = signalQuality + "%";
                normalRange = ">=85%";
                message = seed.nickName() + "设备信号质量明显下降，数据可信度需结合上下文判断。";
                riskScore = clamp(baseRisk(seed) + 10 + stableInt(profile, sampleTime, "risk-signal", 0, 3), 54, 74);
            }
            case DEVICE_OFFLINE -> {
                signalQuality = clamp(seed.deviceReliability() - stableInt(profile, sampleTime, "offline-signal", 34, 54), 5, 30);
                deviceStatus = "offline";
                metricType = "device_offline";
                abnormalType = "设备离线";
                abnormalValue = "连续缺报";
                normalRange = "在线";
                message = seed.nickName() + "设备进入离线窗口，需要优先排查佩戴、供电和网络连接。";
                riskScore = clamp(baseRisk(seed) + 8 + stableInt(profile, sampleTime, "risk-offline", 0, 2), 52, 70);
            }
            case NORMAL -> {
            }
        }

        return new MetricSnapshot(
                heartRate, spo2, round(temperature), systolic, diastolic, steps, stepIncrement,
                batteryLevel, signalQuality, deviceStatus, lng, lat, location, riskScore,
                resolveRiskLevel(riskScore), metricType, abnormalType, abnormalValue, normalRange, message,
                scenario, shouldCreateControlledAlert(profile, sampleTime, scenario)
        );
    }

    private DemoScenario resolveControlledScenario(DemoProfileState profile, LocalDateTime sampleTime) {
        DemoProfileSeed seed = profile.seed();
        int intervalMinutes = intervalForDate(sampleTime.toLocalDate(), LocalDate.now(ZONE_ID));
        long slot = sampleTime.atZone(ZONE_ID).toEpochSecond() / 60L / Math.max(1, intervalMinutes);
        int selector = Math.floorMod((int) (slot + seed.userId() + seed.archetype().ordinal() * 3L), 24);

        return switch (seed.archetype()) {
            case STABLE -> switch (selector) {
                case 4 -> DemoScenario.BLOOD_PRESSURE;
                case 15 -> DemoScenario.LOW_ACTIVITY;
                default -> DemoScenario.NORMAL;
            };
            case CHRONIC -> switch (selector) {
                case 2, 3 -> DemoScenario.BLOOD_PRESSURE;
                case 9 -> DemoScenario.SPO2;
                case 16 -> DemoScenario.TEMPERATURE;
                case 20 -> DemoScenario.LOW_ACTIVITY;
                default -> DemoScenario.NORMAL;
            };
            case FRAGILE -> switch (selector) {
                case 1, 2 -> DemoScenario.SPO2;
                case 8 -> DemoScenario.HEART_RATE;
                case 14 -> DemoScenario.TEMPERATURE;
                case 19 -> DemoScenario.LOW_ACTIVITY;
                default -> DemoScenario.NORMAL;
            };
            case ACUTE -> switch (selector) {
                case 5, 21 -> DemoScenario.HEART_RATE;
                case 11 -> DemoScenario.FENCE;
                case 17 -> DemoScenario.SOS;
                default -> DemoScenario.NORMAL;
            };
            case DEVICE_ISSUE -> switch (selector) {
                case 6 -> DemoScenario.DEVICE_UNSTABLE;
                case 12 -> DemoScenario.DEVICE_OFFLINE;
                default -> DemoScenario.NORMAL;
            };
        };
    }

    private boolean shouldCreateControlledAlert(DemoProfileState profile, LocalDateTime sampleTime, DemoScenario scenario) {
        if (scenario == DemoScenario.NORMAL) {
            return false;
        }
        int intervalMinutes = intervalForDate(sampleTime.toLocalDate(), LocalDate.now(ZONE_ID));
        DemoScenario previous = resolveControlledScenario(profile, sampleTime.minusMinutes(intervalMinutes));
        return previous != scenario;
    }

    private boolean isWindow(LocalTime time, LocalTime startInclusive, LocalTime endInclusive) {
        return !time.isBefore(startInclusive) && !time.isAfter(endInclusive);
    }

    private int cycleCode(DemoProfileSeed seed, LocalDate date, String salt) {
        long value = stableSeed(seed.userId(), date.toEpochDay(), salt.hashCode());
        return Math.floorMod((int) value, 7);
    }

    private LocalDateTime alignToSampleWindow(LocalDateTime time, int intervalMinutes) {
        int minute = (time.getMinute() / intervalMinutes) * intervalMinutes;
        return time.withMinute(minute).withSecond(0).withNano(0);
    }

    private int intervalForDate(LocalDate sampleDate, LocalDate currentDate) {
        return sampleDate.equals(currentDate) ? SAMPLE_INTERVAL_MINUTES : HISTORY_SAMPLE_INTERVAL_MINUTES;
    }

    private long resolveLiveBucket(LocalDateTime time) {
        long epochMinutes = time.atZone(ZONE_ID).toEpochSecond() / 60L;
        return epochMinutes / SAMPLE_INTERVAL_MINUTES;
    }

    private LocalDateTime resolveLiveBucketTime(long bucket) {
        long epochMinutes = bucket * SAMPLE_INTERVAL_MINUTES;
        return LocalDateTime.ofEpochSecond(epochMinutes * 60L, 0, ZONE_ID.getRules().getOffset(LocalDateTime.now(ZONE_ID)));
    }

    private Date toDate(LocalDateTime value) {
        return Date.from(value.atZone(ZONE_ID).toInstant());
    }

    private int dailyTargetForDate(DemoProfileState profile, LocalDate date) {
        DemoProfileSeed seed = profile.seed();
        int activityDay = cycleCode(seed, date, "activity-day");
        double baseFactor = switch (seed.archetype()) {
            case STABLE -> activityDay == 5 ? 0.72 : 1.00;
            case CHRONIC -> activityDay == 6 ? 0.58 : 0.82;
            case FRAGILE -> activityDay == 4 ? 0.48 : 0.62;
            case ACUTE -> activityDay == 1 ? 0.68 : 0.86;
            case DEVICE_ISSUE -> activityDay == 3 ? 0.52 : 0.74;
        };
        double dayDrift = stableInt(profile, date.atStartOfDay(), "daily-target-drift", -4, 5) / 100.0;
        return (int) Math.round(seed.dailyStepTarget() * Math.max(0.38, baseFactor + dayDrift));
    }

    private int computeControlledDailySteps(DemoProfileState profile, LocalDateTime sampleTime) {
        int dailyTarget = dailyTargetForDate(profile, sampleTime.toLocalDate());
        return clamp((int) Math.round(dailyTarget * stepProgressRatio(sampleTime.toLocalTime())),
                0, (int) Math.round(profile.seed().dailyStepTarget() * 1.15));
    }

    private int computeControlledStepIncrement(DemoProfileState profile,
                                               LocalDateTime sampleTime,
                                               int steps,
                                               DemoScenario scenario) {
        int intervalMinutes = intervalForDate(sampleTime.toLocalDate(), LocalDate.now(ZONE_ID));
        LocalDateTime previousTime = sampleTime.minusMinutes(intervalMinutes);
        int previousSteps = previousTime.toLocalDate().equals(sampleTime.toLocalDate())
                ? computeControlledDailySteps(profile, previousTime)
                : 0;
        int increment = Math.max(0, steps - previousSteps);
        if (scenario == DemoScenario.LOW_ACTIVITY) {
            return Math.min(increment, 6);
        }
        if (scenario == DemoScenario.DEVICE_OFFLINE) {
            return 0;
        }
        return increment;
    }

    private double stepProgressRatio(LocalTime time) {
        int minuteOfDay = time.getHour() * 60 + time.getMinute();
        if (minuteOfDay <= 330) {
            return minuteOfDay / 330.0 * 0.02;
        }
        if (minuteOfDay <= 480) {
            return 0.02 + (minuteOfDay - 330) / 150.0 * 0.23;
        }
        if (minuteOfDay <= 690) {
            return 0.25 + (minuteOfDay - 480) / 210.0 * 0.18;
        }
        if (minuteOfDay <= 840) {
            return 0.43 + (minuteOfDay - 690) / 150.0 * 0.09;
        }
        if (minuteOfDay <= 1080) {
            return 0.52 + (minuteOfDay - 840) / 240.0 * 0.33;
        }
        if (minuteOfDay <= 1320) {
            return 0.85 + (minuteOfDay - 1080) / 240.0 * 0.12;
        }
        return Math.min(1.0, 0.97 + (minuteOfDay - 1320) / 120.0 * 0.03);
    }

    private int stableInt(DemoProfileState profile,
                          LocalDateTime sampleTime,
                          String salt,
                          int minInclusive,
                          int maxInclusive) {
        if (maxInclusive <= minInclusive) {
            return minInclusive;
        }
        long base = stableSeed(profile.seed().userId(), sampleTime.atZone(ZONE_ID).toEpochSecond(), salt.hashCode());
        long range = (long) maxInclusive - minInclusive + 1L;
        return minInclusive + (int) Math.floorMod(base, range);
    }

    private double stableDouble(DemoProfileState profile,
                                LocalDateTime sampleTime,
                                String salt,
                                double minInclusive,
                                double maxInclusive) {
        long base = stableSeed(profile.seed().userId(), sampleTime.atZone(ZONE_ID).toEpochSecond(), salt.hashCode());
        double normalized = Math.floorMod(base, 10000L) / 9999.0;
        return minInclusive + (maxInclusive - minInclusive) * normalized;
    }

    private long stableSeed(long subjectSeed, long timeSeed, int saltSeed) {
        long value = subjectSeed * 1103515245L;
        value ^= timeSeed * 2654435761L;
        value ^= saltSeed * 2246822519L;
        value ^= (value >>> 29);
        return value;
    }

    private BigDecimal jitterCoordinateStable(BigDecimal value, double variance, double normalizedOffset) {
        return value.add(decimalOffset(variance * normalizedOffset));
    }

    private DemoScenario resolveScenario(long tick, int profileIndex, DemoProfileState profile) {
        long block = (tick - 1) / SCENARIO_BLOCK_TICKS;
        int localPhase = Math.floorMod((int) (block + profileIndex + profile.seed().userId()), 12);
        int normalThreshold = switch (profile.seed().archetype()) {
            case STABLE -> 10;
            case CHRONIC -> 8;
            case FRAGILE -> 7;
            case ACUTE -> 8;
            case DEVICE_ISSUE -> 7;
        };
        if (localPhase < normalThreshold) {
            return DemoScenario.NORMAL;
        }
        LocalTime virtualTime = resolveVirtualTime(tick, profileIndex).toLocalTime();
        int selector = Math.floorMod((int) (block + profile.seed().userId() + profileIndex), 4);
        return switch (profile.seed().archetype()) {
            case STABLE -> selector % 2 == 0 ? DemoScenario.LOW_ACTIVITY : DemoScenario.BLOOD_PRESSURE;
            case CHRONIC -> isMorningWindow(virtualTime)
                    ? DemoScenario.BLOOD_PRESSURE
                    : selector % 2 == 0 ? DemoScenario.LOW_ACTIVITY : DemoScenario.SPO2;
            case FRAGILE -> selector == 0 ? DemoScenario.HEART_RATE : selector == 1 ? DemoScenario.SPO2 : DemoScenario.LOW_ACTIVITY;
            case ACUTE -> isEveningWindow(virtualTime)
                    ? selector % 3 == 0 ? DemoScenario.SOS : selector % 3 == 1 ? DemoScenario.FENCE : DemoScenario.HEART_RATE
                    : DemoScenario.HEART_RATE;
            case DEVICE_ISSUE -> selector % 2 == 0 ? DemoScenario.DEVICE_UNSTABLE : DemoScenario.DEVICE_OFFLINE;
        };
    }

    private void autoResolveStaleAlerts(List<DemoProfileState> profiles, Date now) {
        if (profiles == null || profiles.isEmpty()) {
            return;
        }

        Date expireBefore = new Date(now.getTime() - DEMO_AUTO_RESOLVE_MINUTES * 60L * 1000L);
        String placeholders = profiles.stream()
                .map(profile -> "?")
                .collect(Collectors.joining(","));
        if (placeholders.isBlank()) {
            return;
        }

        List<Object> params = new ArrayList<>();
        params.add(DEMO_CREATE_BY);
        params.add(now);
        params.add(LIVE_RESOLVE_NOTE);
        params.add(expireBefore);
        profiles.stream()
                .map(DemoProfileState::userId)
                .forEach(params::add);

        jdbcTemplate.update(
                "UPDATE qkyd_exception SET state = '1', update_by = ?, update_time = ?, "
                        + "update_content = COALESCE(update_content, ?) "
                        + "WHERE state <> '1' AND create_time < ? AND user_id IN (" + placeholders + ")",
                params.toArray()
        );
    }

    private UeitDeviceInfo ensureDevice(DemoProfileSeed seed, Long userId) {
        UeitDeviceInfo device = deviceInfoService.selectUeitDeviceInfoByImei(seed.imei());
        if (device == null) {
            UeitDeviceInfo insert = new UeitDeviceInfo();
            insert.setUserId(userId);
            insert.setName(seed.deviceName());
            insert.setImei(seed.imei());
            insert.setType("SMART_WATCH");
            deviceInfoService.insertUeitDeviceInfo(insert);
            return deviceInfoService.selectUeitDeviceInfoByImei(seed.imei());
        }

        boolean needsUpdate = !userId.equals(device.getUserId())
                || !seed.deviceName().equals(device.getName())
                || !"SMART_WATCH".equals(device.getType())
                || !seed.imei().equals(device.getImei());
        if (needsUpdate) {
            device.setUserId(userId);
            device.setName(seed.deviceName());
            device.setImei(seed.imei());
            device.setType("SMART_WATCH");
            deviceInfoService.updateUeitDeviceInfo(device);
        }
        return device;
    }

    private void ensureDeviceExtend(DemoProfileSeed seed, Long deviceId) {
        if (deviceInfoExtendService.selectUeitDeviceInfoExtendByDeviceId(deviceId) != null) {
            return;
        }

        UeitDeviceInfoExtend insert = new UeitDeviceInfoExtend();
        insert.setDeviceId(deviceId);
        insert.setBatteryLevel(Math.max(48, seed.deviceReliability()));
        insert.setStep((int) Math.round(seed.dailyStepTarget() * 0.55));
        insert.setTemp(36.5F);
        insert.setHeartRate((float) seed.baselineHeartRate());
        insert.setBloodSystolic(seed.baselineSystolic());
        insert.setBloodDiastolic(seed.baselineDiastolic());
        insert.setSpo2((float) baseSpo2(seed));
        insert.setLongitude(seed.baseLng());
        insert.setLatitude(seed.baseLat());
        insert.setLocation(seed.baseLocation());
        insert.setType("GPS");
        insert.setPositioningTime(new Date());
        insert.setLastCommunicationTime(new Date());
        insert.setAlarmContent("normal");
        deviceInfoExtendService.insertUeitDeviceInfoExtend(insert);
    }

    private void ensureDeviceBinding(Long subjectId, Long deviceId) {
        jdbcTemplate.update("DELETE FROM health_device_binding WHERE subject_id = ? OR device_id = ?", subjectId, deviceId);
        jdbcTemplate.update(
                "INSERT INTO health_device_binding (subject_id, device_id, bind_time, status, bind_by, create_time, update_by, update_time) "
                        + "VALUES (?, ?, NOW(), '1', ?, NOW(), ?, NOW())",
                subjectId, deviceId, DEMO_CREATE_BY, DEMO_CREATE_BY
        );
    }

    private MetricSnapshot buildSnapshot(DemoProfileState profile, long tick, DemoScenario scenario) {
        DemoProfileSeed seed = profile.seed();
        LocalDateTime virtualTime = resolveVirtualTime(tick, (int) (seed.userId() % 5));
        ActivitySegment segment = resolveActivitySegment(virtualTime.toLocalTime());
        int stageTick = (int) ((tick - 1) % SCENARIO_BLOCK_TICKS);

        int heartRate = clamp(seed.baselineHeartRate() + segment.hrOffset() + randomBetween(-segment.hrNoise(), segment.hrNoise()), 52, 138);
        int spo2 = clamp(baseSpo2(seed) + randomBetween(-1, 1), 92, 99);
        double temperature = round(36.4 + segment.tempOffset() + randomBetween(-2, 2) / 10.0);
        int systolic = clamp(seed.baselineSystolic() + segment.systolicOffset() + morningPeak(virtualTime.toLocalTime()) + randomBetween(-segment.bpNoise(), segment.bpNoise()), 98, 178);
        int diastolic = clamp(seed.baselineDiastolic() + segment.diastolicOffset() + morningPeak(virtualTime.toLocalTime()) / 3 + randomBetween(-segment.bpNoise(), segment.bpNoise()), 60, 110);
        int steps = computeDailySteps(seed, virtualTime.toLocalTime(), segment, tick);
        int stepIncrement = computeStepIncrement(seed, segment, tick);
        int batteryLevel = clamp(Math.max(30, seed.deviceReliability() - (int) ((tick + seed.userId()) % 18)), 24, 100);
        int signalQuality = clamp(seed.deviceReliability() + randomBetween(-4, 4), 35, 100);
        String deviceStatus = "online";
        BigDecimal lng = jitterCoordinate(seed.baseLng(), movementVariance(seed.mobilityLevel()));
        BigDecimal lat = jitterCoordinate(seed.baseLat(), movementVariance(seed.mobilityLevel()) * 0.85);
        String location = seed.baseLocation();
        String metricType = "";
        String abnormalType = "";
        String abnormalValue = "";
        String normalRange = "";
        String message = seed.nickName() + "当前处于" + segment.label() + "阶段，生命体征围绕画像基线平稳波动。";
        int riskScore = clamp(baseRisk(seed) + riskByTime(segment) + randomBetween(-2, 3), 20, 72);

        switch (scenario) {
            case HEART_RATE -> {
                heartRate = clamp(seed.baselineHeartRate() + scenarioWave(stageTick, 14, 34, 12), 82, 132);
                systolic = clamp(systolic + scenarioWave(stageTick, 4, 10, 4), 108, 170);
                metricType = "heart_rate";
                abnormalType = "心率异常";
                abnormalValue = heartRate + " 次/分";
                normalRange = "60-100 次/分";
                message = seed.nickName() + "在连续时间窗内心率逐步抬升并缓慢回落。";
                riskScore = clamp(baseRisk(seed) + 28 + stageTick, 72, 92);
            }
            case SPO2 -> {
                spo2 = clamp(baseSpo2(seed) - scenarioWave(stageTick, 2, 5, 2), 89, 95);
                heartRate = clamp(heartRate + scenarioWave(stageTick, 3, 9, 3), 64, 118);
                metricType = "spo2";
                abnormalType = "血氧异常";
                abnormalValue = spo2 + "%";
                normalRange = "95-99%";
                message = seed.nickName() + "血氧在一个连续片段内偏低，心率伴随联动变化。";
                riskScore = clamp(baseRisk(seed) + 24 + stageTick, 68, 90);
            }
            case TEMPERATURE -> {
                temperature = round(37.3 + scenarioWave(stageTick, 0.2, 0.8, 0.3));
                heartRate = clamp(heartRate + 4 + stageTick / 2, 68, 118);
                metricType = "temperature";
                abnormalType = "体温异常";
                abnormalValue = formatDecimal(temperature) + "℃";
                normalRange = "36.0-37.3℃";
                message = seed.nickName() + "体温在连续观测窗口内偏高。";
                riskScore = clamp(baseRisk(seed) + 18 + stageTick, 62, 82);
            }
            case FENCE -> {
                lng = seed.baseLng().add(new BigDecimal("0.016500"));
                lat = seed.baseLat().add(new BigDecimal("0.012200"));
                location = seed.baseLocation() + "-超出安全区域约1.6公里";
                metricType = "fence";
                abnormalType = "围栏越界";
                abnormalValue = "超出安全区域";
                normalRange = "安全区域内";
                message = seed.nickName() + "移动轨迹偏离常用活动区域。";
                riskScore = clamp(baseRisk(seed) + 20 + stageTick, 64, 84);
            }
            case SOS -> {
                heartRate = clamp(heartRate + 24 + stageTick * 2, 96, 138);
                systolic = clamp(systolic + 16, 122, 178);
                metricType = "sos";
                abnormalType = "SOS求救";
                abnormalValue = "手动触发";
                normalRange = "无";
                message = seed.nickName() + "触发 SOS 求救，需要优先人工确认。";
                riskScore = 96;
            }
            case BLOOD_PRESSURE -> {
                systolic = clamp(seed.baselineSystolic() + scenarioWave(stageTick, 10, 22, 10), 142, 175);
                diastolic = clamp(seed.baselineDiastolic() + scenarioWave(stageTick, 6, 12, 6), 90, 108);
                metricType = "blood_pressure";
                abnormalType = "血压异常";
                abnormalValue = systolic + "/" + diastolic + " mmHg";
                normalRange = "90-140/60-90 mmHg";
                message = seed.nickName() + "血压在连续时间片中出现晨峰式抬升后缓慢回落。";
                riskScore = clamp(baseRisk(seed) + 26 + stageTick, 72, 92);
            }
            case LOW_ACTIVITY -> {
                steps = Math.max(steps / 4, (int) Math.round(seed.dailyStepTarget() * 0.22));
                stepIncrement = Math.min(stepIncrement, 3);
                heartRate = clamp(heartRate + 4, 58, 108);
                metricType = "activity";
                abnormalType = "活动量异常";
                abnormalValue = steps + " 步/日";
                normalRange = Math.round(seed.dailyStepTarget() * 0.8) + "-" + Math.round(seed.dailyStepTarget() * 1.2) + " 步/日";
                message = seed.nickName() + "当日累计步数显著低于其日常目标。";
                riskScore = clamp(baseRisk(seed) + 14 + stageTick, 58, 80);
            }
            case DEVICE_UNSTABLE -> {
                signalQuality = clamp(seed.deviceReliability() - scenarioWave(stageTick, 12, 30, 12), 28, 68);
                deviceStatus = "unstable";
                metricType = "device_signal";
                abnormalType = "设备信号异常";
                abnormalValue = signalQuality + "%";
                normalRange = ">=85%";
                message = seed.nickName() + "设备信号质量下降，数据可信度不足。";
                riskScore = clamp(baseRisk(seed) + 10 + stageTick, 54, 74);
            }
            case DEVICE_OFFLINE -> {
                signalQuality = clamp(seed.deviceReliability() - scenarioWave(stageTick, 25, 48, 18), 5, 35);
                deviceStatus = "offline";
                metricType = "device_offline";
                abnormalType = "设备离线";
                abnormalValue = "连续缺报";
                normalRange = "在线";
                message = seed.nickName() + "设备连续缺报，需要先排查连接和供电状态。";
                riskScore = clamp(baseRisk(seed) + 8 + stageTick, 52, 70);
            }
            case NORMAL -> {
            }
        }

        String riskLevel = resolveRiskLevel(riskScore);
        return new MetricSnapshot(
                heartRate, spo2, round(temperature), systolic, diastolic, steps, stepIncrement,
                batteryLevel, signalQuality, deviceStatus, lng, lat, location, riskScore, riskLevel,
                metricType, abnormalType, abnormalValue, normalRange, message, scenario, stageTick == 0 && scenario != DemoScenario.NORMAL
        );
    }

    private void persistRealtimeData(DemoProfileState profile, MetricSnapshot snapshot, Date now) {
        if (!"offline".equals(snapshot.deviceStatus())) {
            UeitHeartRate heartRate = new UeitHeartRate();
            heartRate.setUserId(profile.userId());
            heartRate.setDeviceId(profile.deviceId());
            heartRate.setValue((float) snapshot.heartRate());
            heartRateService.insertUeitHeartRate(heartRate);

            UeitSpo2 spo2 = new UeitSpo2();
            spo2.setUserId(profile.userId());
            spo2.setDeviceId(profile.deviceId());
            spo2.setValue((float) snapshot.spo2());
            spo2Service.insertUeitSpo2(spo2);

            UeitTemp temp = new UeitTemp();
            temp.setUserId(profile.userId());
            temp.setDeviceId(profile.deviceId());
            temp.setValue((float) snapshot.temperature());
            tempService.insertUeitTemp(temp);

            if (tickCounter.get() % 2 == 0) {
                UeitBlood blood = new UeitBlood();
                blood.setUserId(profile.userId());
                blood.setDeviceId(profile.deviceId());
                blood.setSystolic(snapshot.systolic());
                blood.setDiastolic(snapshot.diastolic());
                bloodService.insertUeitBlood(blood);
            }

            UeitLocation location = new UeitLocation();
            location.setUserId(profile.userId());
            location.setDeviceId(profile.deviceId());
            location.setLongitude(snapshot.longitude().toPlainString());
            location.setLatitude(snapshot.latitude().toPlainString());
            location.setLocation(snapshot.location());
            location.setType("GPS");
            location.setReadTime(now);
            locationService.insertUeitLocation(location);
        }

        UeitSteps dailySteps = new UeitSteps();
        dailySteps.setUserId(profile.userId());
        dailySteps.setDeviceId(profile.deviceId());
        dailySteps.setDate(startOfDay(now));
        UeitSteps existingSteps = stepsService.selectUeitSteps(dailySteps);
        dailySteps.setValue(snapshot.steps());
        dailySteps.setCalories((long) Math.max(120, snapshot.steps() / 24));
        dailySteps.setDateTime(now);
        if (existingSteps == null) {
            stepsService.insertUeitSteps(dailySteps);
        } else {
            dailySteps.setId(existingSteps.getId());
            stepsService.updateUeitSteps(dailySteps);
        }

        UeitDeviceInfoExtend extend = deviceInfoExtendService.selectUeitDeviceInfoExtendByDeviceId(profile.deviceId());
        if (extend == null) {
            extend = new UeitDeviceInfoExtend();
            extend.setDeviceId(profile.deviceId());
        }
        extend.setBatteryLevel(snapshot.batteryLevel());
        extend.setStep(snapshot.steps());
        extend.setAlarmContent(snapshot.abnormalType().isBlank() ? snapshot.deviceStatus() : snapshot.abnormalType());
        extend.setAlarmTime(snapshot.abnormalType().isBlank() ? null : now);
        extend.setTemp((float) snapshot.temperature());
        extend.setTempTime(now);
        extend.setHeartRate((float) snapshot.heartRate());
        extend.setHeartRateTime(now);
        extend.setBloodSystolic(snapshot.systolic());
        extend.setBloodDiastolic(snapshot.diastolic());
        extend.setBloodTime(now);
        extend.setSpo2((float) snapshot.spo2());
        extend.setSpo2Time(now);
        extend.setLongitude(snapshot.longitude());
        extend.setLatitude(snapshot.latitude());
        extend.setLocation(snapshot.location());
        extend.setType("offline".equals(snapshot.deviceStatus()) ? "GPS-OFFLINE" : "GPS");
        if (!"offline".equals(snapshot.deviceStatus())) {
            extend.setPositioningTime(now);
            extend.setLastCommunicationTime(now);
        }

        if (deviceInfoExtendService.selectUeitDeviceInfoExtendByDeviceId(profile.deviceId()) == null) {
            deviceInfoExtendService.insertUeitDeviceInfoExtend(extend);
        } else {
            deviceInfoExtendService.updateUeitDeviceInfoExtend(extend);
        }
    }

    private AbnormalRecord persistAiAbnormal(DemoProfileState profile, MetricSnapshot snapshot, Date now) {
        AbnormalRecord record = new AbnormalRecord();
        record.setUserId(profile.userId());
        record.setDeviceId(profile.seed().imei());
        record.setMetricType(snapshot.metricType());
        record.setAbnormalValue(snapshot.abnormalValue());
        record.setNormalRange(snapshot.normalRange());
        record.setAbnormalType(snapshot.abnormalType());
        record.setRiskLevel(snapshot.riskLevel());
        record.setDetectionMethod("SIM_ENGINE");
        record.setDetectedTime(now);
        record.setCreateTime(now);
        abnormalRecordMapper.insert(record);
        return record;
    }

    private UeitException persistException(DemoProfileState profile, MetricSnapshot snapshot, Date now) {
        return persistException(profile, snapshot, now, false);
    }

    private UeitException persistException(DemoProfileState profile,
                                           MetricSnapshot snapshot,
                                           Date now,
                                           boolean historicalAlert) {
        UeitException exception = new UeitException();
        exception.setUserId(profile.userId());
        exception.setDeviceId(profile.deviceId());
        exception.setType(snapshot.abnormalType());
        exception.setValue(snapshot.abnormalValue());
        exception.setLongitude(snapshot.longitude());
        exception.setLatitude(snapshot.latitude());
        exception.setState(historicalAlert ? "1" : "0");
        exception.setLocation(snapshot.location());
        exception.setReadTime(now);
        exception.setCreateTime(now);
        if (historicalAlert) {
            exception.setUpdateBy(DEMO_CREATE_BY);
            exception.setUpdateTime(now);
            exception.setUpdateContent(HISTORY_RESOLVE_NOTE);
        }
        exceptionService.insertUeitException(exception);
        return exception;
    }

    private void publishHealthUpdate(DemoProfileState profile, MetricSnapshot snapshot) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("patientId", profile.userId());
        payload.put("patientName", profile.seed().nickName());
        payload.put("deviceId", profile.deviceId());
        payload.put("deviceName", profile.seed().deviceName());
        payload.put("heartRate", snapshot.heartRate());
        payload.put("systolic", snapshot.systolic());
        payload.put("diastolic", snapshot.diastolic());
        payload.put("spo2", snapshot.spo2());
        payload.put("temperature", snapshot.temperature());
        payload.put("steps", snapshot.steps());
        payload.put("stepsIncrement", snapshot.stepIncrement());
        payload.put("dailyStepTarget", profile.seed().dailyStepTarget());
        payload.put("batteryLevel", snapshot.batteryLevel());
        payload.put("signalQuality", snapshot.signalQuality());
        payload.put("deviceStatus", snapshot.deviceStatus());
        payload.put("profileType", profile.seed().archetype().name().toLowerCase());
        payload.put("chronicTags", profile.seed().chronicTags());
        payload.put("mobilityLevel", profile.seed().mobilityLevel());
        payload.put("sampleIntervalMinutes", SAMPLE_INTERVAL_MINUTES);
        payload.put("longitude", snapshot.longitude());
        payload.put("latitude", snapshot.latitude());
        payload.put("location", snapshot.location());
        payload.put("riskScore", snapshot.riskScore());
        payload.put("riskLevel", snapshot.riskLevel());
        payload.put("abnormalType", snapshot.abnormalType());
        eventPublisher.publishEvent(new HealthDataUpdateEvent(profile.userId(), payload));
    }

    private void publishAbnormal(DemoProfileState profile, MetricSnapshot snapshot, AbnormalRecord record) {
        Map<String, Object> details = new HashMap<>();
        details.put("metricType", snapshot.metricType());
        details.put("riskScore", snapshot.riskScore());
        details.put("deviceId", profile.deviceId());
        details.put("recordId", record.getId());
        details.put("location", snapshot.location());
        details.put("signalQuality", snapshot.signalQuality());
        details.put("deviceStatus", snapshot.deviceStatus());
        details.put("stepsIncrement", snapshot.stepIncrement());
        details.put("profileType", profile.seed().archetype().name().toLowerCase());

        eventPublisher.publishEvent(new AbnormalDetectionEvent(
                profile.userId(),
                profile.seed().nickName(),
                snapshot.abnormalType(),
                snapshot.abnormalValue(),
                snapshot.riskLevel(),
                snapshot.message(),
                details
        ));
    }

    private void publishRisk(DemoProfileState profile, MetricSnapshot snapshot) {
        Map<String, Object> features = new LinkedHashMap<>();
        features.put("heartRate", snapshot.heartRate());
        features.put("spo2", snapshot.spo2());
        features.put("temperature", snapshot.temperature());
        features.put("bloodPressure", snapshot.systolic() + "/" + snapshot.diastolic());
        features.put("steps", snapshot.steps());
        features.put("stepsIncrement", snapshot.stepIncrement());
        features.put("signalQuality", snapshot.signalQuality());
        features.put("deviceStatus", snapshot.deviceStatus());
        features.put("scenario", snapshot.metricType());
        eventPublisher.publishEvent(new RiskScoreUpdateEvent(
                profile.userId(),
                snapshot.riskScore(),
                snapshot.riskLevel(),
                features
        ));
    }

    private void triggerPipeline(Long eventId, MetricSnapshot snapshot) {
        if (eventId == null) {
            return;
        }
        try {
            Map<String, Object> abnormalData = new LinkedHashMap<>();
            abnormalData.put("abnormalId", eventId);
            abnormalData.put("abnormalType", canonicalAlertType(snapshot.metricType(), snapshot.abnormalType()));
            abnormalData.put("abnormalValue", canonicalAlertValue(snapshot));
            abnormalData.put("riskLevel", snapshot.riskLevel());
            abnormalData.put("riskScore", snapshot.riskScore());
            abnormalData.put("metricType", snapshot.metricType());
            abnormalData.put("message", canonicalAlertMessage(snapshot));
            abnormalData.put("signalQuality", snapshot.signalQuality());
            abnormalData.put("deviceStatus", snapshot.deviceStatus());
            pipelineService.startPipeline(eventId, abnormalData);
        } catch (Exception e) {
            log.warn("failed to trigger demo pipeline for event {}", eventId, e);
        }
    }

    private LocalDateTime resolveVirtualTime(long tick, int offset) {
        LocalDateTime base = LocalDateTime.of(LocalDateTime.now(ZONE_ID).toLocalDate().minusDays(1), LocalTime.MIDNIGHT);
        return base.plusMinutes((tick * SAMPLE_INTERVAL_MINUTES) + ((long) offset * SAMPLE_INTERVAL_MINUTES));
    }

    private ActivitySegment resolveActivitySegment(LocalTime time) {
        if (!time.isAfter(LocalTime.of(5, 30))) {
            return ActivitySegment.SLEEP;
        }
        if (time.isBefore(LocalTime.of(8, 0))) {
            return ActivitySegment.MORNING;
        }
        if (time.isBefore(LocalTime.of(11, 30))) {
            return ActivitySegment.STEADY;
        }
        if (time.isBefore(LocalTime.of(14, 0))) {
            return ActivitySegment.REST;
        }
        if (time.isBefore(LocalTime.of(18, 0))) {
            return ActivitySegment.PEAK;
        }
        if (time.isBefore(LocalTime.of(22, 0))) {
            return ActivitySegment.RELAX;
        }
        return ActivitySegment.PRESLEEP;
    }

    private boolean isMorningWindow(LocalTime time) {
        return !time.isBefore(LocalTime.of(6, 0)) && time.isBefore(LocalTime.of(9, 0));
    }

    private boolean isEveningWindow(LocalTime time) {
        return !time.isBefore(LocalTime.of(19, 0)) && time.isBefore(LocalTime.of(21, 30));
    }

    private int baseSpo2(DemoProfileSeed seed) {
        return switch (seed.archetype()) {
            case FRAGILE -> 95;
            case CHRONIC, DEVICE_ISSUE -> 96;
            default -> 98;
        };
    }

    private int baseRisk(DemoProfileSeed seed) {
        return switch (seed.riskBaseline()) {
            case "high" -> 52;
            case "medium" -> 38;
            default -> 26;
        };
    }

    private int riskByTime(ActivitySegment segment) {
        return switch (segment) {
            case PEAK -> 4;
            case MORNING -> 3;
            case REST, SLEEP -> -1;
            default -> 1;
        };
    }

    private int morningPeak(LocalTime time) {
        if (isMorningWindow(time)) {
            return 4;
        }
        if (time.isAfter(LocalTime.of(23, 0)) || time.isBefore(LocalTime.of(5, 30))) {
            return -3;
        }
        return 0;
    }

    private int computeDailySteps(DemoProfileSeed seed, LocalTime time, ActivitySegment segment, long tick) {
        double progress = Math.max(0.08, (time.getHour() * 60D + time.getMinute()) / 1440D);
        double targetRatio = switch (seed.archetype()) {
            case STABLE -> 1.0;
            case CHRONIC -> 0.82;
            case FRAGILE -> 0.62;
            case ACUTE -> 0.86;
            case DEVICE_ISSUE -> 0.74;
        };
        int total = (int) Math.round(seed.dailyStepTarget() * progress * targetRatio);
        int pulse = switch (segment) {
            case SLEEP -> 0;
            case MORNING -> randomBetween(10, 36);
            case STEADY -> randomBetween(0, 18);
            case REST -> randomBetween(0, 8);
            case PEAK -> randomBetween(24, 110);
            case RELAX -> randomBetween(0, 14);
            case PRESLEEP -> randomBetween(0, 5);
        };
        int mobilityAdjusted = (int) Math.round(pulse * movementFactor(seed.mobilityLevel()));
        return clamp(total + mobilityAdjusted + (int) ((tick + seed.userId()) % 60), 0, (int) Math.round(seed.dailyStepTarget() * 1.35));
    }

    private int computeStepIncrement(DemoProfileSeed seed, ActivitySegment segment, long tick) {
        int increment = switch (segment) {
            case SLEEP -> 0;
            case MORNING -> randomBetween(6, 38);
            case STEADY -> randomBetween(0, 18);
            case REST -> randomBetween(0, 8);
            case PEAK -> randomBetween(22, 96);
            case RELAX -> randomBetween(0, 15);
            case PRESLEEP -> randomBetween(0, 6);
        };
        return clamp((int) Math.round(increment * movementFactor(seed.mobilityLevel()) + ((tick + seed.userId()) % 4)), 0, 130);
    }

    private double movementFactor(String mobilityLevel) {
        return switch (mobilityLevel) {
            case "high" -> 1.15;
            case "low" -> 0.55;
            default -> 0.82;
        };
    }

    private double movementVariance(String mobilityLevel) {
        return switch (mobilityLevel) {
            case "high" -> 0.0042;
            case "low" -> 0.0012;
            default -> 0.0024;
        };
    }

    private int scenarioWave(int stageTick, int warmup, int peak, int recovery) {
        if (stageTick < 2) {
            return warmup;
        }
        if (stageTick < 6) {
            return peak;
        }
        return recovery;
    }

    private double scenarioWave(int stageTick, double warmup, double peak, double recovery) {
        if (stageTick < 2) {
            return warmup;
        }
        if (stageTick < 6) {
            return peak;
        }
        return recovery;
    }

    private String resolveRiskLevel(int riskScore) {
        if (riskScore >= 90) {
            return "critical";
        }
        if (riskScore >= 74) {
            return "high";
        }
        if (riskScore >= 56) {
            return "warning";
        }
        return "low";
    }

    private Date startOfDay(Date value) {
        LocalDateTime time = LocalDateTime.ofInstant(value.toInstant(), ZONE_ID);
        return Date.from(time.toLocalDate().atStartOfDay(ZONE_ID).toInstant());
    }

    private BigDecimal jitterCoordinate(BigDecimal value, double variance) {
        return value.add(decimalOffset(ThreadLocalRandom.current().nextDouble(-variance, variance)));
    }

    private int randomBetween(int minInclusive, int maxInclusive) {
        if (maxInclusive <= minInclusive) {
            return minInclusive;
        }
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private BigDecimal decimalOffset(double value) {
        return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP);
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private String formatDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).toPlainString();
    }

    private record DemoProfileSeed(
            Long userId,
            String userName,
            String nickName,
            String phone,
            String sex,
            int age,
            String imei,
            String deviceName,
            BigDecimal baseLng,
            BigDecimal baseLat,
            String baseLocation,
            ProfileArchetype archetype,
            String chronicTags,
            String mobilityLevel,
            int baselineHeartRate,
            int baselineSystolic,
            int baselineDiastolic,
            int dailyStepTarget,
            String riskBaseline,
            int deviceReliability
    ) {
    }

    private record DemoProfileState(DemoProfileSeed seed, Long userId, Long deviceId) {
    }

    private record MetricSnapshot(
            int heartRate,
            int spo2,
            double temperature,
            int systolic,
            int diastolic,
            int steps,
            int stepIncrement,
            int batteryLevel,
            int signalQuality,
            String deviceStatus,
            BigDecimal longitude,
            BigDecimal latitude,
            String location,
            int riskScore,
            String riskLevel,
            String metricType,
            String abnormalType,
            String abnormalValue,
            String normalRange,
            String message,
            DemoScenario scenario,
            boolean createAlert
    ) {
    }

    private enum DemoScenario {
        NORMAL,
        HEART_RATE,
        SPO2,
        TEMPERATURE,
        FENCE,
        SOS,
        BLOOD_PRESSURE,
        LOW_ACTIVITY,
        DEVICE_UNSTABLE,
        DEVICE_OFFLINE
    }

    private enum ProfileArchetype {
        STABLE,
        CHRONIC,
        FRAGILE,
        ACUTE,
        DEVICE_ISSUE
    }

    private enum ActivitySegment {
        SLEEP("睡眠", -7, -5, -3, 2, 0.0),
        MORNING("晨起活动", 3, 4, 2, 3, 0.1),
        STEADY("白天平稳", 1, 2, 1, 3, 0.0),
        REST("午休低活动", -2, -1, -1, 2, -0.1),
        PEAK("活动高峰", 14, 8, 5, 5, 0.2),
        RELAX("晚间放松", 2, 1, 1, 3, 0.1),
        PRESLEEP("入睡前", -3, -2, -1, 2, -0.1);

        private final String label;
        private final int hrOffset;
        private final int systolicOffset;
        private final int diastolicOffset;
        private final int bpNoise;
        private final double tempOffset;

        ActivitySegment(String label, int hrOffset, int systolicOffset, int diastolicOffset, int bpNoise, double tempOffset) {
            this.label = label;
            this.hrOffset = hrOffset;
            this.systolicOffset = systolicOffset;
            this.diastolicOffset = diastolicOffset;
            this.bpNoise = bpNoise;
            this.tempOffset = tempOffset;
        }

        private String label() {
            return label;
        }

        private int hrOffset() {
            return hrOffset;
        }

        private int systolicOffset() {
            return systolicOffset;
        }

        private int diastolicOffset() {
            return diastolicOffset;
        }

        private int bpNoise() {
            return bpNoise;
        }

        private int hrNoise() {
            return Math.max(2, bpNoise - 1);
        }

        private double tempOffset() {
            return tempOffset;
        }
    }
}

