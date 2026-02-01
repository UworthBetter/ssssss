package com.qkyd.health.service.ai;

import com.alibaba.fastjson2.JSON;
import com.qkyd.health.config.AiServiceProperties;
import com.qkyd.health.domain.AiHealthRecord;
import com.qkyd.health.domain.dto.ai.*;
import com.qkyd.health.mapper.AiHealthRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
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

    public HealthDataService(
            AiServiceClient aiServiceClient,
            AiServiceProperties aiServiceProperties,
            AiHealthRecordMapper aiHealthRecordMapper) {
        this.aiServiceClient = aiServiceClient;
        this.aiServiceProperties = aiServiceProperties;
        this.aiHealthRecordMapper = aiHealthRecordMapper;
    }

    /**
     * 处理原始体征数据（同步方式）
     *
     * @param deviceId 设备ID
     * @param userId   用户ID (可选)
     * @param dataList 体征数据列表
     * @return AI健康检查响应
     */
    public AiHealthCheckResponse processRawData(String deviceId, Long userId, List<VitalSignData> dataList) {
        log.info("[HealthDataService] 开始处理体征数据, 设备: {}, 数据点数: {}", deviceId, dataList.size());

        // 1. 构建请求
        AiHealthCheckRequest request = new AiHealthCheckRequest(dataList);

        // 2. 调用AI服务
        AiHealthCheckResponse response = aiServiceClient.healthCheck(request);

        // 3. 保存结果到数据库
        if (response != null && response.isSuccess()) {
            saveHealthRecord(deviceId, userId, dataList, response);
        } else {
            log.warn("[HealthDataService] AI服务调用失败或响应异常, 设备: {}", deviceId);
        }

        return response;
    }

    /**
     * 异步处理原始体征数据
     *
     * @param deviceId 设备ID
     * @param userId   用户ID (可选)
     * @param dataList 体征数据列表
     * @return CompletableFuture 包含处理结果
     */
    @Async
    public CompletableFuture<AiHealthCheckResponse> processRawDataAsync(
            String deviceId, Long userId, List<VitalSignData> dataList) {
        log.info("[HealthDataService] 异步处理体征数据开始, 设备: {}", deviceId);

        try {
            AiHealthCheckResponse response = processRawData(deviceId, userId, dataList);
            log.info("[HealthDataService] 异步处理完成, 设备: {}, 风险等级: {}",
                    deviceId, response != null ? response.getRiskLevel() : "N/A");
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("[HealthDataService] 异步处理失败, 设备: {}, 错误: {}", deviceId, e.getMessage(), e);
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * 保存健康分析记录到数据库
     */
    private void saveHealthRecord(String deviceId, Long userId,
            List<VitalSignData> dataList, AiHealthCheckResponse response) {
        try {
            if (response == null || response.getRiskLevel() == null) {
                log.warn("[HealthDataService] 响应内容不完整，跳过数据库保存");
                return;
            }

            AiHealthRecord record = new AiHealthRecord();
            record.setDeviceId(deviceId);
            record.setUserId(userId);
            record.setRiskLevel(response.getRiskLevel());
            // 增加空值保护
            double score = response.getRiskScore() != null ? response.getRiskScore() : 0.0;
            record.setRiskScore(BigDecimal.valueOf(score));
            record.setAnomalyCount(response.getAnomalyCount() != null ? response.getAnomalyCount() : 0);
            record.setRiskFactors(JSON.toJSONString(response.getRiskFactors()));
            record.setRawData(JSON.toJSONString(dataList));
            record.setDataPoints(response.getDataPointsAnalyzed() != null ? response.getDataPointsAnalyzed() : 0);

            log.info("[HealthDataService] 准备保存健康记录, 设备: {}", deviceId);
            int rows = aiHealthRecordMapper.insertAiHealthRecord(record);
            log.info("[HealthDataService] 健康记录保存成功, ID: {}, 影响行数: {}", record.getId(), rows);

        } catch (Exception e) {
            log.error("[HealthDataService] 保存健康记录失败: {}, 错误详情: {}", e.getMessage(), e);
            // 这里抛出异常是为了让调用者（Controller）能感知到数据库错误
            throw new RuntimeException("数据库保存失败: " + e.getMessage());
        }
    }

    /**
     * 查询设备最新的健康分析记录
     *
     * @param deviceId 设备ID
     * @return 最新的健康分析记录
     */
    public AiHealthRecord getLatestRecord(String deviceId) {
        return aiHealthRecordMapper.selectLatestByDeviceId(deviceId);
    }

    /**
     * 查询健康分析记录列表
     *
     * @param record 查询条件
     * @return 健康分析记录列表
     */
    public List<AiHealthRecord> listRecords(AiHealthRecord record) {
        return aiHealthRecordMapper.selectAiHealthRecordList(record);
    }

    /**
     * 检查AI服务是否可用
     */
    public boolean isAiServiceAvailable() {
        return aiServiceClient.isServiceAvailable();
    }
}
