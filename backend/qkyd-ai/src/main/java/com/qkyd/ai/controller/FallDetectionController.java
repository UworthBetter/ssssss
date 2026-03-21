package com.qkyd.ai.controller;

import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.ai.service.IFallDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai/fall")
public class FallDetectionController {

    @Autowired
    private IFallDetectionService fallDetectionService;

    @PostMapping("/detect")
    public AjaxResult detectFall(@RequestBody Map<String, Object> sensorData) {
        return AjaxResult.success(fallDetectionService.detectFall(sensorData));
    }
}
