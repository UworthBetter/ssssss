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
}
