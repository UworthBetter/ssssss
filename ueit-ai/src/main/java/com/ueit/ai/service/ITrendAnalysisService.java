package com.ueit.ai.service;

import com.ueit.common.core.domain.AjaxResult;
import java.util.Map;

public interface ITrendAnalysisService {
    AjaxResult analyzeTrend(Map<String, Object> data);
}
