package com.ueit.ai.service.impl;

import com.ueit.ai.config.PythonIntegrationConfig;
import com.ueit.ai.service.IRiskScoreService;
import com.ueit.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Risk Assessment Service
 */
@Service
public class RiskScoreServiceImpl implements IRiskScoreService {

    @Autowired
    private PythonIntegrationConfig pythonConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public AjaxResult assessRisk(Map<String, Object> data) {
        String url = pythonConfig.getUrl() + "/api/risk/assess";
        try {
            Map<String, Object> response = restTemplate.postForObject(url, data, Map.class);
            if (response != null && response.containsKey("data")) {
                return AjaxResult.success(response.get("data"));
            }
            return AjaxResult.error("Failed to assess risk");
        } catch (Exception e) {
            return AjaxResult.error("Error connecting to AI service: " + e.getMessage());
        }
    }
}
