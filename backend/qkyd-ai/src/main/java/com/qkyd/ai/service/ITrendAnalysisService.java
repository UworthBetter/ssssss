package com.qkyd.ai.service;

import com.qkyd.common.core.domain.AjaxResult;
import java.util.Map;

public interface ITrendAnalysisService {
    AjaxResult analyzeTrend(Map<String, Object> data);
}
