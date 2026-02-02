package com.qkyd.ai.controller;

import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.ai.service.IAbnormalDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/ai/abnormal")
public class AbnormalDetectionController {

    @Autowired
    private IAbnormalDetectionService abnormalDetectionService;

    @PostMapping("/detect")
    public AjaxResult detect(@RequestBody Map<String, Object> data) {
        return AjaxResult.success(abnormalDetectionService.detect(data));
    }

    @Autowired
    private com.qkyd.ai.mapper.AbnormalRecordMapper abnormalRecordMapper;

    /**
     * Get recent abnormal records for dashboard
     */
    @GetMapping("/recent")
    public AjaxResult getRecentAbnormals(@RequestParam(defaultValue = "10") int limit) {
        return AjaxResult.success(abnormalRecordMapper.selectRecent(limit));
    }
}
