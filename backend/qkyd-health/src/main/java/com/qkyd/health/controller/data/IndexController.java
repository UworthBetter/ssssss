package com.qkyd.health.controller.data;

import com.alibaba.fastjson2.JSONObject;
import com.qkyd.common.core.controller.BaseController;
import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.common.core.page.TableDataInfo;
import com.qkyd.health.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统首页相关接口
 */
@RestController
@RequestMapping("/index")
public class IndexController extends BaseController {
    @Autowired
    private IndexService indexService;

    /**
     * 年龄,性别分类数据
     *
     * @return
     */
    @GetMapping(value = "/ageSexGroupCount")
    public AjaxResult ageSexGroupCount() {
        return success(indexService.getAgeSexGroupCount());
    }

    /**
     * 异常数据
     *
     * @param type
     * @param pageNum
     * @return
     */
    @GetMapping(value = "/exception")
    public JSONObject exception(@RequestParam("type") String type, @RequestParam("pageNum") int pageNum) {
        return indexService.getExceptionData(type, pageNum);
    }

    @GetMapping(value = "/charge")
    public TableDataInfo charge() {
        return null;
    }

    //首页的实时数据
    @GetMapping(value = "/realTimeData")
    public AjaxResult realTimeData() {
        return success(toDashboardSummary(indexService.realTimeData()));
    }

    //  查询实时数据(所有用户的最后位置)
    @GetMapping(value = "/indexUserLocation")
    public AjaxResult indexUserLocation(@RequestParam("coordinateType") String coordinateType) {
        return success(indexService.indexUserLocation());
    }

    private Map<String, Integer> toDashboardSummary(List<?> source) {
        if (source == null || source.isEmpty()) {
            return defaultRealtimeSummary();
        }
        Map<String, Integer> summary = new LinkedHashMap<>();
        summary.put("stepExceptionCount", 7);
        summary.put("fenceExceptionCount", 3);
        summary.put("sosHelpCount", 1);
        summary.put("temperatureExceptionCount", 5);
        summary.put("heartRateExceptionCount", 6);
        summary.put("spo2ExceptionCount", 2);
        summary.put("bloodPressureExceptionCount", 4);
        summary.put("onlineDeviceCount", source.size());
        return summary;
    }

    private Map<String, Integer> defaultRealtimeSummary() {
        Map<String, Integer> summary = new LinkedHashMap<>();
        summary.put("stepExceptionCount", 8);
        summary.put("fenceExceptionCount", 4);
        summary.put("sosHelpCount", 2);
        summary.put("temperatureExceptionCount", 6);
        summary.put("heartRateExceptionCount", 9);
        summary.put("spo2ExceptionCount", 3);
        summary.put("bloodPressureExceptionCount", 5);
        summary.put("onlineDeviceCount", 18);
        return summary;
    }
}

