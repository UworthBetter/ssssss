package com.qkyd.ai.service.impl;

import com.qkyd.ai.config.PythonIntegrationConfig;
import com.qkyd.ai.service.IDataQualityService;
import com.qkyd.common.core.domain.AjaxResult;
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
        // Pre-processing: Check for obvious gaps in timestamp or device status to
        // provide context to LLM
        // This simulates 'Qihuang' smart logic preparation on Java side
        if (!data.containsKey("missing_segment_context") && data.containsKey("device_status")) {
            // Mocking detection logic: if we see "charging", we add context
            // In real app, this would analyze the list of rows for time gaps
            String deviceStatus = (String) data.getOrDefault("device_status", "unknown");
            if ("charging".equalsIgnoreCase(deviceStatus)) {
                data.put("missing_segment_context", Map.of(
                        "time_range", data.getOrDefault("timestamp", "unknown"),
                        "device_status", "charging",
                        "location", data.getOrDefault("location", "Home"),
                        "prev_heart_rate", "0" // simplified
                ));
            }
        }

        String url = pythonConfig.getUrl() + "/api/data/quality";
        try {
            // Forward whole payload (including new context) to Python
            Map<String, Object> response = restTemplate.postForObject(url, data, Map.class);

            if (response != null && response.containsKey("data")) {
                Map<String, Object> resultData = (Map<String, Object>) response.get("data");

                // If Python returned a smart strategy, we can log it or process it here
                if (resultData.containsKey("smart_strategy")) {
                    // Logic to handle auto-filling or warning
                    // For now, just pass it through to frontend
                }

                return AjaxResult.success(resultData);
            }
            return AjaxResult.error("Failed to check data quality from Python AI");
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("Error connecting to AI service: " + e.getMessage());
        }
    }
}
