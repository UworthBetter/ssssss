package com.ueit.ai.controller;

import com.ueit.common.core.domain.AjaxResult;
import com.ueit.ai.service.ITrendAnalysisService;
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
