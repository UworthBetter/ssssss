package com.ueit.ai.service.impl;

import com.ueit.ai.config.PythonIntegrationConfig;
import com.ueit.ai.service.IDataQualityService;
import com.ueit.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Data Quality Service Implementation
 */
@Service
public class DataQualityServiceImpl implements IDataQualityService {

    @Autowired
    private PythonIntegrationConfig pythonConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public AjaxResult checkQuality(Map<String, Object> data) {
        String url = pythonConfig.getUrl() + "/api/data/quality";
        try {
            Map<String, Object> response = restTemplate.postForObject(url, data, Map.class);
            if (response != null && response.containsKey("data")) {
                return AjaxResult.success(response.get("data"));
            }
            return AjaxResult.error("Failed to check data quality");
        } catch (Exception e) {
            return AjaxResult.error("Error connecting to AI service: " + e.getMessage());
        }
    }
}
