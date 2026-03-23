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
import com.qkyd.health.service.IUeitBloodService;
import com.qkyd.health.service.IUeitDeviceInfoExtendService;
import com.qkyd.health.service.IUeitDeviceInfoService;
import com.qkyd.health.service.IUeitExceptionService;
import com.qkyd.health.service.IUeitHeartRateService;
import com.qkyd.health.service.IUeitLocationService;
import com.qkyd.health.service.IUeitSpo2Service;
import com.qkyd.health.service.IUeitStepsService;
import com.qkyd.health.service.IUeitTempService;
import com.qkyd.system.service.ISysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

@Service
public class DemoDataEngineService {

    private static final Logger log = LoggerFactory.getLogger(DemoDataEngineService.class);

    private static final String DEMO_PASSWORD = "Demo@123456";
    private static final String DEMO_REMARK = "演示数据引擎自动生成";
    private static final String DEMO_CREATE_BY = "demo_engine";
    private static final long DEFAULT_DEPT_ID = 103L;
    private static final int DEFAULT_ACTIVE_ABNORMALS = 3;
    private static final long DEMO_AUTO_RESOLVE_MINUTES = 3L;
    private static final DemoScenario[] DEMO_SCENARIO_ROTATION = {
            DemoScenario.HEART_RATE,
            DemoScenario.SPO2,
            DemoScenario.TEMPERATURE,
            DemoScenario.FENCE,
            DemoScenario.BLOOD_PRESSURE,
            DemoScenario.STEP,
            DemoScenario.SOS
    };

    private static final List<DemoProfileSeed> DEMO_SEEDS = List.of(
            new DemoProfileSeed(9101L, "demo_watch_01", "演示对象A", "13900000001", "0", 72,
                    "DEMO-IMEI-9101", "演示手表-A01",
                    new BigDecimal("121.473700"), new BigDecimal("31.230400"), "上海-人民广场"),
            new DemoProfileSeed(9102L, "demo_watch_02", "演示对象B", "13900000002", "1", 68,
                    "DEMO-IMEI-9102", "演示手表-A02",
                    new BigDecimal("121.481200"), new BigDecimal("31.227900"), "上海-南京东路"),
            new DemoProfileSeed(9103L, "demo_watch_03", "演示对象C", "13900000003", "0", 81,
                    "DEMO-IMEI-9103", "演示手表-A03",
                    new BigDecimal("121.464800"), new BigDecimal("31.224500"), "上海-南京西路"),
            new DemoProfileSeed(9104L, "demo_watch_04", "演示对象D", "13900000004", "1", 76,
                    "DEMO-IMEI-9104", "演示手表-A04",
                    new BigDecimal("121.490100"), new BigDecimal("31.237200"), "上海-四川北路"),
            new DemoProfileSeed(9105L, "demo_watch_05", "演示对象E", "13900000005", "0", 64,
                    "DEMO-IMEI-9105", "演示手表-A05",
                    new BigDecimal("121.455500"), new BigDecimal("31.240100"), "上海-长寿路"),
            new DemoProfileSeed(9106L, "demo_watch_06", "演示对象F", "13900000006", "1", 74,
                    "DEMO-IMEI-9106", "演示手表-A06",
                    new BigDecimal("121.501000"), new BigDecimal("31.245600"), "上海-五角场")
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
    private final AbnormalRecordMapper abnormalRecordMapper;
    private final IEventProcessingPipelineService pipelineService;
    private final ApplicationEventPublisher eventPublisher;
    private final JdbcTemplate jdbcTemplate;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong tickCounter = new AtomicLong(0L);
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
        if (!properties.isEnabled() || !running.compareAndSet(false, true)) {
            return;
        }

        try {
            List<DemoProfileState> profiles = ensureProfiles();
            if (profiles.isEmpty()) {
                return;
            }

            long tick = tickCounter.incrementAndGet();
            Date now = new Date();
            int activeAbnormalCount = Math.min(Math.max(2, DEFAULT_ACTIVE_ABNORMALS), profiles.size());
            autoResolveStaleAlerts(profiles, now);

            for (int index = 0; index < profiles.size(); index++) {
                DemoProfileState profile = profiles.get(index);
                DemoScenario scenario = resolveScenario(tick, index, profiles.size(), activeAbnormalCount);
                MetricSnapshot snapshot = buildSnapshot(profile, tick, scenario);
                persistRealtimeData(profile, snapshot, now);
                publishHealthUpdate(profile, snapshot);

                if (scenario != DemoScenario.NORMAL) {
                    AbnormalRecord abnormalRecord = persistAiAbnormal(profile, snapshot, now);
                    UeitException exception = persistException(profile, snapshot, now);
                    publishAbnormal(profile, snapshot, abnormalRecord);
                    publishRisk(profile, snapshot);
                    triggerPipeline(exception, snapshot);
                }
            }
        } catch (Exception e) {
            log.error("demo data engine tick failed", e);
        } finally {
            running.set(false);
        }
    }

    private List<DemoProfileState> ensureProfiles() {
        List<DemoProfileState> states = new ArrayList<>();
        int expectedCount = Math.max(1, Math.min(properties.getSubjectCount(), DEMO_SEEDS.size()));

        for (int index = 0; index < expectedCount; index++) {
            DemoProfileSeed seed = DEMO_SEEDS.get(index);
            DemoProfileState cached = profileCache.get(seed.userId());
            if (cached != null) {
                states.add(cached);
                continue;
            }

            SysUser user = ensureSysUser(seed);
            ensureHealthSubject(seed, user.getUserId());
            UeitDeviceInfo device = ensureDevice(seed, user.getUserId());
            ensureDeviceExtend(seed, device.getId());

            DemoProfileState state = new DemoProfileState(seed, user.getUserId(), device.getId());
            profileCache.put(seed.userId(), state);
            states.add(state);
        }

        return states;
    }

    private SysUser ensureSysUser(DemoProfileSeed seed) {
        SysUser existing = sysUserService.selectUserByUserName(seed.userName());
        if (existing != null) {
            boolean needsUpdate = !seed.nickName().equals(existing.getNickName())
                    || !seed.phone().equals(existing.getPhonenumber())
                    || !seed.sex().equals(existing.getSex())
                    || seed.age() != (existing.getAge() == null ? 0 : existing.getAge())
                    || existing.getDeptId() == null
                    || existing.getDeptId() == 0L;
            if (needsUpdate) {
                existing.setDeptId(DEFAULT_DEPT_ID);
                existing.setNickName(seed.nickName());
                existing.setPhonenumber(seed.phone());
                existing.setSex(seed.sex());
                existing.setAge(seed.age());
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
        user.setEmail(seed.userName() + "@demo.local");
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
                subjectId, seed.userName(), seed.nickName(), seed.userName() + "@demo.local", seed.phone(), seed.age(), seed.sex(),
                DEMO_CREATE_BY, DEMO_CREATE_BY, DEMO_REMARK
        );
    }

    private DemoScenario resolveScenario(long tick, int profileIndex, int profileCount, int abnormalCount) {
        if (profileIndex >= abnormalCount) {
            return DemoScenario.NORMAL;
        }

        int rotationBase = (int) ((tick - 1) % DEMO_SCENARIO_ROTATION.length);
        int scenarioIndex = (rotationBase + profileIndex) % DEMO_SCENARIO_ROTATION.length;
        return DEMO_SCENARIO_ROTATION[scenarioIndex];
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
        params.add("Auto resolved by demo engine");
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
            insert.setType("DEMO_WATCH");
            insert.setCreateTime(new Date());
            deviceInfoService.insertUeitDeviceInfo(insert);
            return deviceInfoService.selectUeitDeviceInfoByImei(seed.imei());
        }

        boolean needsUpdate = !userId.equals(device.getUserId())
                || !seed.deviceName().equals(device.getName())
                || !"DEMO_WATCH".equals(device.getType());
        if (needsUpdate) {
            device.setUserId(userId);
            device.setName(seed.deviceName());
            device.setType("DEMO_WATCH");
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
        insert.setBatteryLevel(96);
        insert.setStep(3200);
        insert.setTemp(36.5F);
        insert.setHeartRate(72F);
        insert.setBloodSystolic(124);
        insert.setBloodDiastolic(78);
        insert.setSpo2(98F);
        insert.setLongitude(seed.baseLng());
        insert.setLatitude(seed.baseLat());
        insert.setLocation(seed.baseLocation());
        insert.setType("GPS");
        insert.setPositioningTime(new Date());
        insert.setLastCommunicationTime(new Date());
        deviceInfoExtendService.insertUeitDeviceInfoExtend(insert);
    }

    private MetricSnapshot buildSnapshot(DemoProfileState profile, long tick, DemoScenario scenario) {
        DemoProfileSeed seed = profile.seed();
        double phase = (tick + (seed.userId() % 5)) / 3.0;
        int baseHeartRate = 68 + (int) Math.round(Math.sin(phase) * 6);
        int baseSpo2 = 97 + (int) Math.round(Math.cos(phase) * 1.2);
        double baseTemp = 36.4 + Math.sin(phase / 2.0) * 0.25;
        int baseSystolic = 122 + (int) Math.round(Math.cos(phase / 2.0) * 6);
        int baseDiastolic = 78 + (int) Math.round(Math.sin(phase / 2.0) * 5);
        int baseSteps = 2600 + (int) ((tick * 135 + seed.userId()) % 6000);
        int batteryLevel = 38 + (int) ((tick + seed.userId()) % 58);
        BigDecimal lng = seed.baseLng().add(decimalOffset(Math.sin(phase) * 0.0042));
        BigDecimal lat = seed.baseLat().add(decimalOffset(Math.cos(phase) * 0.0036));
        String location = seed.baseLocation();

        String metricType = "";
        String abnormalType = "";
        String abnormalValue = "";
        String normalRange = "";
        String riskLevel = "low";
        int riskScore = 22 + (int) ((tick + seed.userId()) % 18);
        String message = "生命体征平稳。";

        switch (scenario) {
            case HEART_RATE -> {
                baseHeartRate = 128 + ThreadLocalRandom.current().nextInt(18);
                metricType = "heart_rate";
                abnormalType = "心率异常";
                abnormalValue = baseHeartRate + " bpm";
                normalRange = "60-100 bpm";
                riskLevel = "high";
                riskScore = 86;
                message = seed.nickName() + "出现持续心率升高。";
            }
            case SPO2 -> {
                baseSpo2 = 88 + ThreadLocalRandom.current().nextInt(4);
                metricType = "spo2";
                abnormalType = "血氧异常";
                abnormalValue = baseSpo2 + "%";
                normalRange = "95-100%";
                riskLevel = "high";
                riskScore = 84;
                message = seed.nickName() + "出现血氧偏低。";
            }
            case TEMPERATURE -> {
                baseTemp = 38.1 + ThreadLocalRandom.current().nextDouble(1.0);
                metricType = "temperature";
                abnormalType = "体温异常";
                abnormalValue = formatDecimal(baseTemp) + " C";
                normalRange = "36.0-37.3 C";
                riskLevel = "warning";
                riskScore = 74;
                message = seed.nickName() + "体温持续偏高。";
            }
            case FENCE -> {
                lng = seed.baseLng().add(new BigDecimal("0.0180"));
                lat = seed.baseLat().add(new BigDecimal("0.0130"));
                location = seed.baseLocation() + "-超出安全区域1.8公里";
                metricType = "fence";
                abnormalType = "围栏越界";
                abnormalValue = "超出1.8公里";
                normalRange = "安全区域内";
                riskLevel = "warning";
                riskScore = 79;
                message = seed.nickName() + "离开了安全活动区域。";
            }
            case SOS -> {
                metricType = "sos";
                abnormalType = "SOS求救";
                abnormalValue = "手动触发";
                normalRange = "n/a";
                riskLevel = "critical";
                riskScore = 96;
                message = seed.nickName() + "触发了SOS求救。";
            }
            case BLOOD_PRESSURE -> {
                baseSystolic = 150 + ThreadLocalRandom.current().nextInt(18);
                baseDiastolic = 96 + ThreadLocalRandom.current().nextInt(10);
                metricType = "blood_pressure";
                abnormalType = "血压异常";
                abnormalValue = baseSystolic + "/" + baseDiastolic + " mmHg";
                normalRange = "90-140/60-90 mmHg";
                riskLevel = "high";
                riskScore = 88;
                message = seed.nickName() + "出现血压偏高。";
            }
            case STEP -> {
                baseSteps = 15000 + ThreadLocalRandom.current().nextInt(2500);
                metricType = "step";
                abnormalType = "步数异常";
                abnormalValue = baseSteps + " steps/day";
                normalRange = "2000-12000 steps/day";
                riskLevel = "warning";
                riskScore = 70;
                message = seed.nickName() + "活动量出现异常波动。";
            }
            case NORMAL -> {
            }
        }

        return new MetricSnapshot(baseHeartRate, baseSpo2, round(baseTemp), baseSystolic, baseDiastolic, baseSteps,
                batteryLevel, lng, lat, location, riskScore, riskLevel, metricType, abnormalType,
                abnormalValue, normalRange, message, scenario);
    }

    private void persistRealtimeData(DemoProfileState profile, MetricSnapshot snapshot, Date now) {
        UeitHeartRate heartRate = new UeitHeartRate();
        heartRate.setUserId(profile.userId());
        heartRate.setDeviceId(profile.deviceId());
        heartRate.setValue((float) snapshot.heartRate());
        heartRate.setCreateTime(now);
        heartRateService.insertUeitHeartRate(heartRate);

        UeitSpo2 spo2 = new UeitSpo2();
        spo2.setUserId(profile.userId());
        spo2.setDeviceId(profile.deviceId());
        spo2.setValue((float) snapshot.spo2());
        spo2.setCreateTime(now);
        spo2Service.insertUeitSpo2(spo2);

        UeitTemp temp = new UeitTemp();
        temp.setUserId(profile.userId());
        temp.setDeviceId(profile.deviceId());
        temp.setValue((float) snapshot.temperature());
        temp.setCreateTime(now);
        tempService.insertUeitTemp(temp);

        if (tickCounter.get() % 2 == 0) {
            UeitBlood blood = new UeitBlood();
            blood.setUserId(profile.userId());
            blood.setDeviceId(profile.deviceId());
            blood.setSystolic(snapshot.systolic());
            blood.setDiastolic(snapshot.diastolic());
            blood.setCreateTime(now);
            bloodService.insertUeitBlood(blood);
        }

        UeitLocation location = new UeitLocation();
        location.setUserId(profile.userId());
        location.setDeviceId(profile.deviceId());
        location.setLongitude(snapshot.longitude().toPlainString());
        location.setLatitude(snapshot.latitude().toPlainString());
        location.setLocation(snapshot.location());
        location.setType("GPS");
        location.setCreateTime(now);
        location.setReadTime(now);
        locationService.insertUeitLocation(location);

        UeitSteps dailySteps = new UeitSteps();
        dailySteps.setUserId(profile.userId());
        dailySteps.setDeviceId(profile.deviceId());
        dailySteps.setDate(now);
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
        extend.setAlarmContent(snapshot.abnormalType().isBlank() ? "normal" : snapshot.abnormalType());
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
        extend.setType("GPS");
        extend.setPositioningTime(now);
        extend.setLastCommunicationTime(now);

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
        record.setDetectionMethod("DEMO_ENGINE");
        record.setDetectedTime(now);
        record.setCreateTime(now);
        abnormalRecordMapper.insert(record);
        return record;
    }

    private UeitException persistException(DemoProfileState profile, MetricSnapshot snapshot, Date now) {
        UeitException exception = new UeitException();
        exception.setUserId(profile.userId());
        exception.setDeviceId(profile.deviceId());
        exception.setType(snapshot.abnormalType());
        exception.setValue(snapshot.abnormalValue());
        exception.setLongitude(snapshot.longitude());
        exception.setLatitude(snapshot.latitude());
        exception.setState(snapshot.scenario() == DemoScenario.STEP ? "1" : "0");
        exception.setUpdateContent("1".equals(exception.getState()) ? "Auto closed by demo engine" : null);
        exception.setLocation(snapshot.location());
        exception.setReadTime(now);
        exception.setCreateTime(now);
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
        payload.put("batteryLevel", snapshot.batteryLevel());
        payload.put("longitude", snapshot.longitude());
        payload.put("latitude", snapshot.latitude());
        payload.put("location", snapshot.location());
        payload.put("riskScore", snapshot.riskScore());
        payload.put("riskLevel", snapshot.riskLevel());
        eventPublisher.publishEvent(new HealthDataUpdateEvent(profile.userId(), payload));
    }

    private void publishAbnormal(DemoProfileState profile, MetricSnapshot snapshot, AbnormalRecord record) {
        Map<String, Object> details = new HashMap<>();
        details.put("metricType", snapshot.metricType());
        details.put("riskScore", snapshot.riskScore());
        details.put("deviceId", profile.deviceId());
        details.put("recordId", record.getId());
        details.put("location", snapshot.location());

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
        features.put("scenario", snapshot.metricType());
        eventPublisher.publishEvent(new RiskScoreUpdateEvent(
                profile.userId(),
                snapshot.riskScore(),
                snapshot.riskLevel(),
                features
        ));
    }

    private void triggerPipeline(UeitException exception, MetricSnapshot snapshot) {
        if (exception == null || exception.getId() == null) {
            return;
        }
        try {
            Map<String, Object> abnormalData = new LinkedHashMap<>();
            abnormalData.put("abnormalType", snapshot.abnormalType());
            abnormalData.put("abnormalValue", snapshot.abnormalValue());
            abnormalData.put("riskLevel", snapshot.riskLevel());
            abnormalData.put("riskScore", snapshot.riskScore());
            abnormalData.put("metricType", snapshot.metricType());
            abnormalData.put("message", snapshot.message());
            pipelineService.startPipeline(exception.getId(), abnormalData);
        } catch (Exception e) {
            log.warn("failed to trigger demo pipeline for event {}", exception.getId(), e);
        }
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
            String baseLocation
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
            int batteryLevel,
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
            DemoScenario scenario
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
        STEP
    }
}
