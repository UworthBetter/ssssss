package com.qkyd.ai.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.common.event.RiskScoreUpdateEvent;
import com.qkyd.ai.service.IRiskScoreService;

@RestController
@RequestMapping("/ai/risk")
public class RiskScoreController {
    @Autowired
    private IRiskScoreService riskScoreService;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @PostMapping("/assess")
    public AjaxResult assess(@RequestBody Map<String, Object> data) {
        // 鎵ц椋庨櫓璇勫垎
        AjaxResult result = riskScoreService.assessRisk(data);
        
        // 濡傛灉璇勫垎鎴愬姛锛屽彂甯僕ebSocket鎺ㄩ€佷簨浠?
        if (result != null && result.get("data") != null) {
            try {
                Map<String, Object> riskData = (Map<String, Object>) result.get("data");
                Long patientId = data.get("patientId") != null ? Long.valueOf(data.get("patientId").toString()) : null;
                Integer riskScore = riskData.get("riskScore") != null ? Integer.valueOf(riskData.get("riskScore").toString()) : null;
                String riskLevel = riskData.get("riskLevel") != null ? riskData.get("riskLevel").toString() : "low";
                
                if (patientId != null && riskScore != null) {
                    RiskScoreUpdateEvent event = new RiskScoreUpdateEvent(
                        patientId, riskScore, riskLevel, riskData
                    );
                    
                    eventPublisher.publishEvent(event);
                }
            } catch (Exception e) {
                // 涓嶅奖鍝嶈瘎鍒嗙粨鏋滐紝鍙褰曟棩蹇?
                System.err.println("鍙戝竷椋庨櫓璇勫垎鏇存柊浜嬩欢澶辫触: " + e.getMessage());
            }
        }
        
        return result;
    }
}
