package com.ueit.ai.controller;

import com.ueit.common.core.domain.AjaxResult;
import com.ueit.ai.service.IRiskScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/ai/risk")
public class RiskScoreController {
    @Autowired
    private IRiskScoreService riskScoreService;

    @PostMapping("/assess")
    public AjaxResult assess(@RequestBody Map<String, Object> data) {
        return riskScoreService.assessRisk(data);
    }
}
