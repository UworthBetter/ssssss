package com.qkyd.ai.controller;

import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.ai.service.IDataQualityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/ai/quality")
public class DataQualityController {
    @Autowired
    private IDataQualityService dataQualityService;

    @PostMapping("/check")
    public AjaxResult check(@RequestBody Map<String, Object> data) {
        return dataQualityService.checkQuality(data);
    }
}
