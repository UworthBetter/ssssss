package com.qkyd.health.service.ai;

import com.alibaba.fastjson2.JSON;
import com.qkyd.health.config.AiServiceProperties;
import com.qkyd.health.domain.AiHealthRecord;
import com.qkyd.health.domain.HealthSubject;
import com.qkyd.health.domain.UeitBlood;
import com.qkyd.health.domain.UeitDeviceInfo;
import com.qkyd.health.domain.UeitException;
import com.qkyd.health.domain.UeitHeartRate;
import com.qkyd.health.domain.dto.ai.AiHealthCheckRequest;
import com.qkyd.health.domain.dto.ai.AiHealthCheckResponse;
import com.qkyd.health.domain.dto.ai.HeartRateAnomaly;
import com.qkyd.health.domain.dto.ai.VitalSignData;
import com.qkyd.health.mapper.AiHealthRecordMapper;
import com.qkyd.health.service.IHealthSubjectService;
import com.qkyd.health.service.IUeitBloodService;
import com.qkyd.health.service.IUeitDeviceInfoService;
import com.qkyd.health.service.IUeitExceptionService;
import com.qkyd.health.service.IUeitHeartRateService;
import com.qkyd.health.service.IUeitSpo2Service;
import com.qkyd.health.service.IUeitTempService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@Service
public class HealthDataService {

    private static final Logger log = LoggerFactory.getLogger(HealthDataService.class);

    private final AiServiceClient aiServiceClient;
    private final AiServiceProperties aiServiceProperties;
    private final AiHealthRecordMapper aiHealthRecordMapper;
    private final IUeitHeartRateService heartRateService;
    private final IUeitBloodService bloodService;
    private final IUeitSpo2Service spo2Service;
    private final IUeitTempService tempService;
    private final IUeitDeviceInfoService deviceInfoService;
    private final IHealthSubjectService subjectService;
    private final IUeitExceptionService exceptionService;

    public HealthDataService(
            AiServiceClient aiServiceClient,
            AiServiceProperties aiServiceProperties,
            AiHealthRecordMapper aiHealthRecordMapper,
            IUeitHeartRateService heartRateService,
            IUeitBloodService bloodService,
            IUeitSpo2Service spo2Service,
            IUeitTempService tempService,
            IUeitDeviceInfoService deviceInfoService,
            IHealthSubjectService subjectService,
            IUeitExceptionService exceptionService) {
        this.aiServiceClient = aiServiceClient;
        this.aiServiceProperties = aiServiceProperties;
        this.aiHealthRecordMapper = aiHealthRecordMapper;
        this.heartRateService = heartRateService;
        this.bloodService = bloodService;
        this.spo2Service = spo2Service;
        this.tempService = tempService;
        this.deviceInfoService = deviceInfoService;
        this.subjectService = subjectService;
        this.exceptionService = exceptionService;
    }

    public AiHealthCheckResponse processRawData(String deviceId, Long userId, List<VitalSignData> dataList) {
        log.info("[HealthDataService] 开始处理体征数据, 设备: {}, 数据点数: {}", deviceId, dataList.size());

        Long finalUserId = resolveUserId(userId);

        AiHealthCheckRequest request = new AiHealthCheckRequest(dataList);
        AiHealthCheckResponse response = aiServiceClient.healthCheck(request);

        if (response != null && response.isSuccess()) {
            saveHealthRecord(deviceId, finalUserId, dataList, response);
        }

        persistVitalSigns(deviceId, finalUserId, dataList);

        if (response != null && response.isSuccess()) {
            UeitException generatedEvent = createExceptionEventIfNeeded(deviceId, finalUserId, dataList, response);
            if (generatedEvent != null) {
                response.setGeneratedEventId(generatedEvent.getId());
                response.setGeneratedEventType(generatedEvent.getType());
            }
        }

        return response;
    }

    @Async
    public CompletableFuture<AiHealthCheckResponse> processRawDataAsync(
            String deviceId, Long userId, List<VitalSignData> dataList) {
        log.info("[HealthDataService] 异步处理体征数据开始, 设备: {}", deviceId);
        try {
            return CompletableFuture.completedFuture(processRawData(deviceId, userId, dataList));
        } catch (Exception e) {
            log.error("[HealthDataService] 异步处理失败, 设备: {}, 错误: {}", deviceId, e.getMessage(), e);
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

    private Long resolveUserId(Long userId) {
        Long resolved = userId;
        if (resolved == null || resolved == 1L) {
            List<HealthSubject> subjects = subjectService.selectHealthSubjectList(new HealthSubject());
            if (subjects != null && !subjects.isEmpty()) {
                resolved = subjects.get(0).getSubjectId();
                log.info("[HealthDataService] 自动匹配受试者 {}", resolved);
            }
        }
        return resolved;
    }

    private void persistVitalSigns(String deviceImei, Long userId, List<VitalSignData> dataList) {
        try {
            UeitDeviceInfo deviceInfo = deviceInfoService.selectUeitDeviceInfoByImei(deviceImei);
            Long dbDeviceId = deviceInfo != null ? deviceInfo.getId() : 0L;

            for (VitalSignData data : dataList) {
                Date readTime = data.getTimestamp() != null ? new Date(data.getTimestamp()) : new Date();

                if (data.getHeartRate() != null && data.getHeartRate() > 0) {
                    UeitHeartRate hr = new UeitHeartRate();
                    hr.setUserId(userId);
                    hr.setDeviceId(dbDeviceId);
                    hr.setValue(data.getHeartRate().floatValue());
                    hr.setCreateTime(readTime);
                    heartRateService.insertUeitHeartRate(hr);
                }

                if (data.getBloodPressure() != null && data.getBloodPressure().contains("/")) {
                    String[] bp = data.getBloodPressure().split("/");
                    if (bp.length == 2) {
                        UeitBlood blood = new UeitBlood();
                        blood.setUserId(userId);
                        blood.setDeviceId(dbDeviceId);
                        blood.setSystolic(Integer.parseInt(bp[0]));
                        blood.setDiastolic(Integer.parseInt(bp[1]));
                        blood.setCreateTime(readTime);
                        bloodService.insertUeitBlood(blood);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[HealthDataService] 业务指标持久化失败: {}", e.getMessage(), e);
        }
    }

    private void saveHealthRecord(String deviceId, Long userId,
                                  List<VitalSignData> dataList, AiHealthCheckResponse response) {
        try {
            AiHealthRecord record = new AiHealthRecord();
            record.setDeviceId(deviceId);
            record.setUserId(userId);
            record.setRiskLevel(response.getRiskLevel());
            double score = response.getRiskScore() != null ? response.getRiskScore() : 0.0;
            record.setRiskScore(BigDecimal.valueOf(score));
            record.setAnomalyCount(response.getAnomalyCount() != null ? response.getAnomalyCount() : 0);
            record.setRiskFactors(JSON.toJSONString(response.getRiskFactors()));
            record.setRawData(JSON.toJSONString(dataList));
            record.setDataPoints(response.getDataPointsAnalyzed() != null ? response.getDataPointsAnalyzed() : 0);

            aiHealthRecordMapper.insertAiHealthRecord(record);
            log.info("[HealthDataService] 健康分析结果已记录, ID: {}", record.getId());
        } catch (Exception e) {
            log.error("[HealthDataService] 保存健康记录失败: {}", e.getMessage(), e);
        }
    }

    private UeitException createExceptionEventIfNeeded(String deviceId, Long userId,
                                                       List<VitalSignData> dataList,
                                                       AiHealthCheckResponse response) {
        if (!shouldCreateEvent(response)) {
            return null;
        }

        try {
            HealthSubject subject = userId != null ? subjectService.selectHealthSubjectBySubjectId(userId) : null;
            UeitDeviceInfo deviceInfo = deviceInfoService.selectUeitDeviceInfoByImei(deviceId);
            VitalSignData latest = dataList != null && !dataList.isEmpty() ? dataList.get(dataList.size() - 1) : null;

            UeitException event = new UeitException();
            event.setUserId(userId);
            event.setDeviceId(deviceInfo != null ? deviceInfo.getId() : null);
            event.setType(resolveEventType(response));
            event.setValue(resolveEventValue(response, latest));
            event.setState("0");
            event.setLocation("设备 " + deviceId + " 自动上报");

            Date eventTime = latest != null && latest.getTimestamp() != null ? new Date(latest.getTimestamp()) : new Date();
            event.setReadTime(eventTime);
            event.setCreateTime(eventTime);

            if (subject != null) {
                event.setNickName(subject.getNickName());
                event.setAge(subject.getAge() != null ? subject.getAge() : 0);
                event.setPhone(subject.getPhonenumber());
                event.setSex(subject.getSex());
            }

            exceptionService.insertUeitException(event);
            log.info("[HealthDataService] 自动生成异常事件成功, eventId={}, type={}", event.getId(), event.getType());
            return event;
        } catch (Exception e) {
            log.error("[HealthDataService] 自动生成异常事件失败: {}", e.getMessage(), e);
            return null;
        }
    }

    private boolean shouldCreateEvent(AiHealthCheckResponse response) {
        if (response == null || !response.isSuccess()) {
            return false;
        }
        if (response.getAnomalyCount() != null && response.getAnomalyCount() > 0) {
            return true;
        }
        String riskLevel = response.getRiskLevel() != null ? response.getRiskLevel().toLowerCase(Locale.ROOT) : "";
        return "high".equals(riskLevel) || "critical".equals(riskLevel);
    }

    private String resolveEventType(AiHealthCheckResponse response) {
        if (response.getAnomalyCount() != null && response.getAnomalyCount() > 0) {
            return "心率异常";
        }
        if (response.getRiskFactors() != null) {
            for (String factor : response.getRiskFactors()) {
                if (factor == null) {
                    continue;
                }
                if (factor.contains("血压")) {
                    return "血压异常";
                }
                if (factor.contains("活动")) {
                    return "活动量异常";
                }
            }
        }
        return "健康风险预警";
    }

    private String resolveEventValue(AiHealthCheckResponse response, VitalSignData latest) {
        if (response.getAnomalies() != null && !response.getAnomalies().isEmpty()) {
            HeartRateAnomaly anomaly = response.getAnomalies().get(0);
            return anomaly.getHeartRate() + " bpm";
        }
        if (latest != null && latest.getBloodPressure() != null) {
            return latest.getBloodPressure();
        }
        return "risk=" + response.getRiskScore();
    }
}
