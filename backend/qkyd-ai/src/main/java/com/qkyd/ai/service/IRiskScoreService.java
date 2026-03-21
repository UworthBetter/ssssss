package com.qkyd.ai.service;

import com.qkyd.common.core.domain.AjaxResult;
import java.util.Map;

public interface IRiskScoreService {
    AjaxResult assessRisk(Map<String, Object> data);
}
