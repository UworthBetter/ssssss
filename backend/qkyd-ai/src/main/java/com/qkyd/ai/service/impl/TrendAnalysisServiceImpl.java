package com.qkyd.ai.service.impl;

import com.qkyd.ai.config.PythonIntegrationConfig;
import com.qkyd.ai.service.ITrendAnalysisService;
import com.qkyd.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Trend Analysis Service Implementation
 */
@Service
public class TrendAnalysisServiceImpl implements ITrendAnalysisService {

    @Autowired
    private PythonIntegrationConfig pythonConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public AjaxResult analyzeTrend(Map<String, Object> data) {
        String url = pythonConfig.getUrl() + "/api/trend/analyze";
        try {
            // Forward request to Python
            Map<String, Object> response = restTemplate.postForObject(url, data, Map.class);

            if (response != null && response.containsKey("data")) {
                return AjaxResult.success(response.get("data"));
            }
            return AjaxResult.error("Failed to analyze trend: No data returned");
        } catch (Exception e) {
            return AjaxResult.error("Trend analysis service error: " + e.getMessage());
        }
    }
}
