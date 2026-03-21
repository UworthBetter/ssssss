package com.qkyd.ai.controller;

import com.qkyd.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.*;

/**
 * Algorithm Overview Dashboard
 */
@RestController
@RequestMapping("/ai/dashboard")
public class AlgorithmDashboardController {

    @GetMapping("/stats")
    public AjaxResult getStats() {
        // Mock data for now
        return AjaxResult.success();
    }
}
