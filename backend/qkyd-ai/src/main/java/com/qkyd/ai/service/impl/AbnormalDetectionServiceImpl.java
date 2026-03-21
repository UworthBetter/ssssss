package com.qkyd.ai.service.impl;

import com.qkyd.ai.config.PythonIntegrationConfig;
import com.qkyd.ai.model.dto.AbnormalDetectionResultDTO;
import com.qkyd.ai.model.vo.AbnormalDetectionVO;
import com.qkyd.ai.service.IAbnormalDetectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AbnormalDetectionServiceImpl implements IAbnormalDetectionService {

    private static final Logger log = LoggerFactory.getLogger(AbnormalDetectionServiceImpl.class);

    @Autowired
    private PythonIntegrationConfig pythonConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private com.qkyd.ai.mapper.AbnormalRecordMapper abnormalRecordMapper;

    @Override
    public Object detect(Map<String, Object> data) {
        String url = pythonConfig.getPythonServiceUrl() + "/api/abnormal/detect";
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, data, Map.class);

            if (response == null || !response.containsKey("data")) {
                throw new RuntimeException("Invalid response from Python service");
            }

            ObjectMapper mapper = new ObjectMapper();
            AbnormalDetectionResultDTO dto = mapper.convertValue(response.get("data"),
                    AbnormalDetectionResultDTO.class);

            // Save record if abnormal
            if (Boolean.TRUE.equals(dto.getIs_abnormal())) {
                saveAbnormalRecord(data, dto);
            }

            AbnormalDetectionVO vo = new AbnormalDetectionVO();
            vo.setAbnormal(dto.getIs_abnormal());
            vo.setMessage(dto.getMsg());
            vo.setDetail(dto.getType() != null ? "Type: " + dto.getType() : "Z-Score: " + dto.getZ_score());

            return vo;

        } catch (Exception e) {
            log.error("Error calling Python service", e);
            throw new RuntimeException("Abnormal detection failed: " + e.getMessage());
        }
    }

    private void saveAbnormalRecord(Map<String, Object> data, AbnormalDetectionResultDTO dto) {
        try {
            com.qkyd.ai.model.entity.AbnormalRecord record = new com.qkyd.ai.model.entity.AbnormalRecord();
            if (data.get("userId") != null) {
                record.setUserId(Long.valueOf(String.valueOf(data.get("userId"))));
            }
            record.setDeviceId((String) data.getOrDefault("deviceId", "unknown"));
            record.setMetricType((String) data.get("metricType"));
            record.setAbnormalValue(String.valueOf(data.get("value")));
            record.setAbnormalType(dto.getType());
            // Default risk level for now, can be refined later
            record.setRiskLevel("warning");
            record.setDetectionMethod("statistical");
            record.setDetectedTime(new java.util.Date());
            record.setCreateTime(new java.util.Date());

            abnormalRecordMapper.insert(record);
        } catch (Exception e) {
            log.error("Failed to save abnormal record", e);
        }
    }
}
