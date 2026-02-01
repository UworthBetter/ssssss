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
}
