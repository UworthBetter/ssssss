package com.ueit.ai.controller;

import com.ueit.common.core.domain.AjaxResult;
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
