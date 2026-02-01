package com.qkyd.ai.service.impl;

import com.qkyd.ai.config.PythonIntegrationConfig;
import com.qkyd.ai.mapper.FallAlarmRecordMapper;
import com.qkyd.ai.model.dto.FallDetectionResultDTO;
import com.qkyd.ai.model.entity.FallAlarmRecord;
import com.qkyd.ai.model.vo.FallDetectionVO;
import com.qkyd.ai.service.IFallDetectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Service
public class FallDetectionServiceImpl implements IFallDetectionService {

    private static final Logger log = LoggerFactory.getLogger(FallDetectionServiceImpl.class);

    @Autowired
    private PythonIntegrationConfig pythonConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FallAlarmRecordMapper fallAlarmRecordMapper;

    @Override
    public FallDetectionVO detectFall(Map<String, Object> sensorData) {
        String url = pythonConfig.getPythonServiceUrl() + "/api/algorithms/detect_fall";

        try {
            // 1. Call Python API
            // Python returns: {"code": 200, "msg": "success", "data": {...}}
            // We need to unwrap it or just map the structure.
            // The Python code returns dict with "data" key containing the actual ResultDTO
            // structure.

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, sensorData, Map.class);

            if (response == null || !response.containsKey("data")) {
                throw new RuntimeException("Invalid response from Python service");
            }

            // Convert 'data' part to DTO
            // Using a simple object mapper or manual mapping would be safer if Jackson is
            // not pre-configured for this
            // but RestTemplate usually handles it.
            // Let's assume we can map the 'data' object.

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            FallDetectionResultDTO result = mapper.convertValue(response.get("data"), FallDetectionResultDTO.class);

            // 2. Process Result
            FallDetectionVO vo = new FallDetectionVO();
            vo.setDetected(result.getIs_fall());
            vo.setConfidence(BigDecimal.valueOf(result.getConfidence()));

            if (result.getEnhanced_features() != null) {
                vo.setAnalysis(result.getEnhanced_features().getReasoning());
                vo.setAdvice(result.getEnhanced_features().getRecommendation());
                vo.setRiskLevel(result.getEnhanced_features().getSeverity());
            }

            // 3. Save Record if Fall Detected
            if (Boolean.TRUE.equals(result.getIs_fall())) {
                FallAlarmRecord record = new FallAlarmRecord();
                // Assuming userId is in sensorData or context. For now, hardcode or extract.
                Object userIdObj = sensorData.get("userId");
                record.setUserId(userIdObj != null ? Long.valueOf(userIdObj.toString()) : 1L); // Default/Mock ID

                record.setIsValid(true);
                record.setValidationReason(vo.getAnalysis());
                record.setAccelerationPeak(BigDecimal.valueOf(25.5)); // Mock or extract from sensorData
                record.setValidationTime(new Date());
                record.setCreateTime(new Date());

                fallAlarmRecordMapper.insertFallAlarmRecord(record);
            }

            return vo;

        } catch (Exception e) {
            log.error("Error calling Python service", e);
            throw new RuntimeException("Fall detection failed: " + e.getMessage());
        }
    }
}
