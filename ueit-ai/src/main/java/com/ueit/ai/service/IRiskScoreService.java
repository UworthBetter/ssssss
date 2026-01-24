package com.ueit.ai.service;

import com.ueit.common.core.domain.AjaxResult;
import java.util.Map;

public interface IRiskScoreService {
    AjaxResult assessRisk(Map<String, Object> data);
}
