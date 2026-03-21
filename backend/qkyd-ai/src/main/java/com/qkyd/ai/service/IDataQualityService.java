package com.qkyd.ai.service;

import com.qkyd.common.core.domain.AjaxResult;
import java.util.Map;

public interface IDataQualityService {
    AjaxResult checkQuality(Map<String, Object> data);
}
