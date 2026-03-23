package com.qkyd.ai.service.impl;

import com.qkyd.ai.service.IDispositionRuleEngine;
import org.springframework.stereotype.Service;

/**
 * 处置规则引擎 - 根据异常类型和风险评分自动生成处置建议
 */
@Service
public class DispositionRuleEngineImpl implements IDispositionRuleEngine {

    @Override
    public String generateDisposition(String abnormalType, int riskScore) {
        if (riskScore >= 80) {
            return switch (abnormalType) {
                case "心率异常" -> "立即通知医生,建议患者就医";
                case "血氧偏低" -> "立即通知医生,建议患者吸氧";
                case "体温偏高" -> "立即通知医生,建议患者降温";
                case "SOS急救" -> "立即启动应急响应";
                default -> "立即通知医生";
            };
        } else if (riskScore >= 50) {
            return switch (abnormalType) {
                case "心率异常" -> "建议患者休息,监测心率变化";
                case "血氧偏低" -> "建议患者增加活动,监测血氧";
                case "体温偏高" -> "建议患者物理降温,监测体温";
                default -> "建议监测";
            };
        } else {
            return "继续监测";
        }
    }

    @Override
    public boolean shouldAutoExecute(String abnormalType, int riskScore) {
        // 只有高风险的SOS和心率异常才自动执行
        if (riskScore >= 85) {
            return abnormalType.contains("SOS") || abnormalType.contains("心率");
        }
        return false;
    }

    @Override
    public String getNotificationLevel(String abnormalType, int riskScore) {
        if (riskScore >= 80) return "URGENT";
        if (riskScore >= 50) return "HIGH";
        return "NORMAL";
    }
}