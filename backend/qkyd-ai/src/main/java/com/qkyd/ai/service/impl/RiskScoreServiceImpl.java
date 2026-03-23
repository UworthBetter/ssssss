package com.qkyd.ai.service.impl;

import com.qkyd.ai.config.PythonIntegrationConfig;
import com.qkyd.ai.model.dto.EventInsightResultDTO;
import com.qkyd.ai.service.IRiskScoreService;
import com.qkyd.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Risk Assessment Service
 */
@Service
public class RiskScoreServiceImpl implements IRiskScoreService {

    @Autowired
    private PythonIntegrationConfig pythonConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public AjaxResult assessRisk(Map<String, Object> data) {
        if (!data.containsKey("enable_llm")) {
            data.put("enable_llm", true);
        }

        String url = pythonConfig.getUrl() + "/api/risk/assess";
        try {
            Map<String, Object> response = restTemplate.postForObject(url, data, Map.class);
            if (response != null && response.containsKey("data")) {
                Map<String, Object> resultData = (Map<String, Object>) response.get("data");
                return AjaxResult.success(resultData);
            }
        } catch (Exception e) {
            // Fall through to the local rule-based scorer so the pipeline remains usable
        }

        return AjaxResult.success(buildLocalRiskResult(data));
    }

    private Map<String, Object> buildLocalRiskResult(Map<String, Object> data) {
        int score = 35;
        List<String> factors = new ArrayList<>();
        Map<String, Integer> subScores = new LinkedHashMap<>();

        String abnormalType = stringValue(data.get("abnormalType"));
        if (abnormalType.contains("SOS")) {
            score += addScore(subScores, "abnormalType", 35);
            factors.add("触发 SOS 求助，默认提升为高优先事件");
        } else if (abnormalType.contains("心率")) {
            score += addScore(subScores, "abnormalType", 20);
            factors.add("检测到心率相关异常");
        } else if (abnormalType.contains("血氧")) {
            score += addScore(subScores, "abnormalType", 24);
            factors.add("检测到血氧相关异常");
        } else if (abnormalType.contains("围栏")) {
            score += addScore(subScores, "abnormalType", 18);
            factors.add("检测到围栏/位置异常");
        } else if (!abnormalType.isBlank()) {
            score += addScore(subScores, "abnormalType", 12);
            factors.add("检测到健康异常事件");
        }

        Integer anomalyCount = integerValue(data.get("anomalyCount"));
        if (anomalyCount != null && anomalyCount > 0) {
            int anomalyScore = Math.min(20, anomalyCount * 6);
            score += addScore(subScores, "anomalyCount", anomalyScore);
            factors.add("近一次分析中发现 " + anomalyCount + " 个异常点");
        }

        Double riskScore = doubleValue(data.get("riskScore"));
        if (riskScore != null) {
            int normalized = (int) Math.round(Math.max(0D, Math.min(1D, riskScore)) * 30D);
            score += addScore(subScores, "deviceRiskScore", normalized);
            factors.add("设备侧健康评分已提示风险升高");
        }

        Integer age = integerValue(data.get("age"));
        if (age != null && age >= 80) {
            score += addScore(subScores, "age", 12);
            factors.add("高龄对象需要更保守处置");
        } else if (age != null && age >= 65) {
            score += addScore(subScores, "age", 6);
            factors.add("老年人群风险阈值下调");
        }

        Object insightObject = data.get("insight");
        if (insightObject instanceof EventInsightResultDTO insight) {
            EventInsightResultDTO.RiskAssessment insightRisk = insight.getRisk();
            if (insightRisk != null && insightRisk.getRiskScore() != null) {
                int insightScore = Math.min(25, Math.max(0, insightRisk.getRiskScore() / 4));
                score += addScore(subScores, "contextInsight", insightScore);
                factors.add("事件上下文补全后显示存在额外背景风险");
            }

            EventInsightResultDTO.ContextSnapshot context = insight.getContext();
            if (context != null) {
                if (context.getRecentSameTypeCount() != null && context.getRecentSameTypeCount() >= 2) {
                    score += addScore(subScores, "repeatedEvents", 10);
                    factors.add("同类事件近期重复出现");
                }
                if ("offline".equalsIgnoreCase(context.getDeviceStatus())) {
                    score += addScore(subScores, "deviceStatus", 12);
                    factors.add("设备处于离线状态，需人工核查");
                } else if ("unstable".equalsIgnoreCase(context.getDeviceStatus())) {
                    score += addScore(subScores, "deviceStatus", 6);
                    factors.add("设备状态不稳定");
                }
                if (context.getChronicDiseases() != null && !context.getChronicDiseases().isEmpty()
                        && !context.getChronicDiseases().contains("暂无基础疾病信息")) {
                    score += addScore(subScores, "chronicDiseases", 8);
                    factors.add("存在基础病背景，需要谨慎处置");
                }
            }
        }

        score = Math.max(0, Math.min(100, score));
        String level = score >= 80 ? "danger" : score >= 60 ? "warning" : score >= 40 ? "attention" : "normal";

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("riskScore", score);
        result.put("risk_score", score);
        result.put("riskLevel", level);
        result.put("risk_level", level);
        result.put("factors", factors);
        result.put("doctorReport", buildDoctorReport(level, factors));
        result.put("subScores", subScores);
        result.put("source", "local-rule-engine");
        return result;
    }

    private int addScore(Map<String, Integer> subScores, String key, int amount) {
        subScores.put(key, amount);
        return amount;
    }

    private String buildDoctorReport(String level, List<String> factors) {
        if (factors.isEmpty()) {
            return "当前未见明显风险因素，建议持续监测。";
        }
        return "综合上下文判定当前风险级别为 " + level + "，主要依据：" + String.join("；", factors) + "。";
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Integer integerValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Double doubleValue(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value != null) {
            try {
                return Double.parseDouble(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
