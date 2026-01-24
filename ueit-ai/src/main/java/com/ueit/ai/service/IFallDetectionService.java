package com.ueit.ai.service;

import com.ueit.ai.model.vo.FallDetectionVO;
import java.util.Map;

public interface IFallDetectionService {
    /**
     * Detect Fall from Sensor Data
     * 
     * @param sensorData Raw sensor data map
     * @return Detection result VO
     */
    FallDetectionVO detectFall(Map<String, Object> sensorData);
}
