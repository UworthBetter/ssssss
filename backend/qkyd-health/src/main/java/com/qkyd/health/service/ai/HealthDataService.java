package com.qkyd.health.service.ai;

import com.alibaba.fastjson2.JSON;
import com.qkyd.health.config.AiServiceProperties;
import com.qkyd.health.domain.*;
import com.qkyd.health.domain.dto.ai.*;
import com.qkyd.health.mapper.AiHealthRecordMapper;
import com.qkyd.health.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 健康数据处理服务
 * 负责接收原始体征数据，调用AI服务进行分析，并将结果存储到数据库
 *
 * @author ueit
 * @date 2026-01-29
 */
@Service
public class HealthDataService {

    private static final Logger log = LoggerFactory.getLogger(HealthDataService.class);

    private final AiServiceClient aiServiceClient;
    private final AiServiceProperties aiServiceProperties;
    private final AiHealthRecordMapper aiHealthRecordMapper;

    // Inject vital sign services
    private final IUeitHeartRateService heartRateService;
    private final IUeitBloodService bloodService;
    private final IUeitSpo2Service spo2Service;
    private final IUeitTempService tempService;
    private final IUeitDeviceInfoService deviceInfoService;
    private final IHealthSubjectService subjectService;

    public HealthDataService(
            AiServiceClient aiServiceClient,
            AiServiceProperties aiServiceProperties,
            AiHealthRecordMapper aiHealthRecordMapper,
            IUeitHeartRateService heartRateService,
            IUeitBloodService bloodService,
            IUeitSpo2Service spo2Service,
            IUeitTempService tempService,
            IUeitDeviceInfoService deviceInfoService,
            IHealthSubjectService subjectService) {
        this.aiServiceClient = aiServiceClient;
        this.aiServiceProperties = aiServiceProperties;
        this.aiHealthRecordMapper = aiHealthRecordMapper;
        this.heartRateService = heartRateService;
        this.bloodService = bloodService;
        this.spo2Service = spo2Service;
        this.tempService = tempService;
        this.deviceInfoService = deviceInfoService;
        this.subjectService = subjectService;
    }

    /**
     * 处理原始体征数据（同步方式）
     */
    public AiHealthCheckResponse processRawData(String deviceId, Long userId, List<VitalSignData> dataList) {
        log.info("[HealthDataService] 开始处理体征数据, 设备: {}, 数据点数: {}", deviceId, dataList.size());

        // 1. 实现用户绑定自动兜底
        Long finalUserId = userId;
        if (finalUserId == null || finalUserId == 1L) {
            // 如果传入的是默认ID或空，尝试查找第一个受试者
            List<HealthSubject> subjects = subjectService.selectHealthSubjectList(new HealthSubject());
            if (subjects != null && !subjects.isEmpty()) {
                finalUserId = subjects.get(0).getSubjectId();
                log.info("[HealthDataService] 自动匹配受试者: {}", finalUserId);
            }
        }

        // 2. 构建请求并调用AI服务
        AiHealthCheckRequest request = new AiHealthCheckRequest(dataList);
        AiHealthCheckResponse response = aiServiceClient.healthCheck(request);

        // 3. 保存AI分析结果
        if (response != null && response.isSuccess()) {
            saveHealthRecord(deviceId, finalUserId, dataList, response);
        }

        // 4. 持久化具体的体征指标到业务表（确保大屏能看到数据）
        persistVitalSigns(deviceId, finalUserId, dataList);

        // 5. 注入调试信息到响应中（方便用户在大屏调试）
        if (response != null) {
            Map<String, Object> meta = new HashMap<>();
            meta.put("matchedUserId", finalUserId);
            meta.put("targetDeviceId", deviceId);
            meta.put("timestamp", System.currentTimeMillis());
            response.setRiskFactors(Collections.singletonList("Backend Debug: Matched User " + finalUserId));
        }

        return response;
    }

    /**
     * 持久化具体的体征指标到真实业务表
     */
    private void persistVitalSigns(String deviceImei, Long userId, List<VitalSignData> dataList) {
        try {
            // 解析设备ID (Long)
            UeitDeviceInfo deviceInfo = deviceInfoService.selectUeitDeviceInfoByImei(deviceImei);
            Long dbDeviceId = (deviceInfo != null) ? deviceInfo.getId() : 0L;

            log.info("[HealthDataService] 准备持久化体征, 受试者ID: {}, 设备IMEI: {}, 数据库设备ID: {}",
                    userId, deviceImei, dbDeviceId);

            for (VitalSignData data : dataList) {
                Date readTime = data.getTimestamp() != null ? new Date(data.getTimestamp()) : new Date();

                // 1. 保存心率
                if (data.getHeartRate() != null && data.getHeartRate() > 0) {
                    UeitHeartRate hr = new UeitHeartRate();
                    hr.setUserId(userId);
                    hr.setDeviceId(dbDeviceId);
                    hr.setValue(data.getHeartRate().floatValue());
                    // 这里虽然 Service 可能会重置时间，但我们尽量先设好
                    hr.setCreateTime(readTime);
                    heartRateService.insertUeitHeartRate(hr);
                    log.info("[HealthDataService] 心率入库成功: {} bpm", data.getHeartRate());
                }

                // 2. 保存血压
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
                        log.info("[HealthDataService] 血压入库成功: {}/{}", bp[0], bp[1]);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[HealthDataService] 业务指标持久化过程发生严重错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 异步处理原始体征数据
     */
    @Async
    public CompletableFuture<AiHealthCheckResponse> processRawDataAsync(
            String deviceId, Long userId, List<VitalSignData> dataList) {
        log.info("[HealthDataService] 异步处理体征数据开始, 设备: {}", deviceId);
        try {
            AiHealthCheckResponse response = processRawData(deviceId, userId, dataList);
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("[HealthDataService] 异步处理失败, 设备: {}, 错误: {}", deviceId, e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * 保存健康分析记录到数据库
     */
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
            log.error("[HealthDataService] 保存健康记录记录失败: {}", e.getMessage());
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
}
