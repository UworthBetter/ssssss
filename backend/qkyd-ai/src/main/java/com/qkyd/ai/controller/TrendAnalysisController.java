package com.qkyd.ai.controller;

import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.ai.service.ITrendAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/ai/trend")
public class TrendAnalysisController {
    @Autowired
    private ITrendAnalysisService trendAnalysisService;

    @PostMapping("/analyze")
    public AjaxResult analyze(@RequestBody Map<String, Object> data) {
        return trendAnalysisService.analyzeTrend(data);
    }
}
