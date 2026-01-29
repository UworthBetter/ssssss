package com.ueit.health.controller.ai;

import com.ueit.common.core.controller.BaseController;
import com.ueit.common.core.domain.AjaxResult;
import com.ueit.common.utils.StringUtils;
import com.ueit.health.domain.AiHealthRecord;
import com.ueit.health.domain.dto.ai.AiHealthCheckResponse;
import com.ueit.health.domain.dto.ai.MockUploadRequest;
import com.ueit.health.domain.dto.ai.VitalSignData;
import com.ueit.health.service.ai.HealthDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 健康数据模拟上传控制器
 * 提供设备模拟数据上传接口，用于测试和演示
 *
 * @author ueit
 * @date 2026-01-29
 */
@RestController
@RequestMapping("/health/mock")
public class HealthMockController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(HealthMockController.class);

    private final HealthDataService healthDataService;

    public HealthMockController(HealthDataService healthDataService) {
        this.healthDataService = healthDataService;
    }

    /**
     * 模拟设备上传体征数据
     * 
     * 接收模拟的硬件设备推送数据，调用Python AI服务进行分析
     *
     * @param request 模拟上传请求
     * @return 分析结果
     */
    @PostMapping("/upload")
    public AjaxResult upload(@RequestBody MockUploadRequest request) {
        log.info("[HealthMockController] 收到模拟上传请求, 设备: {}", request.getDeviceId());

        // 参数校验
        if (StringUtils.isEmpty(request.getDeviceId())) {
            return error("设备ID不能为空");
        }
        if (request.getDataList() == null || request.getDataList().isEmpty()) {
            return error("体征数据不能为空");
        }

        try {
            // 调用服务处理数据
            AiHealthCheckResponse response = healthDataService.processRawData(
                    request.getDeviceId(),
                    request.getUserId(),
                    request.getDataList());

            if (response != null && response.isSuccess()) {
                AjaxResult result = success();
                result.put("msg", "数据处理成功");
                result.put("data", response);
                return result;
            } else {
                return error("AI服务处理失败，请检查Python服务是否运行");
            }

        } catch (Exception e) {
            log.error("[HealthMockController] 处理失败: {}", e.getMessage(), e);
            return error("处理异常: " + e.getMessage());
        }
    }

    /**
     * 异步模拟设备上传体征数据
     * 
     * 适用于大批量数据处理场景
     *
     * @param request 模拟上传请求
     * @return 任务提交结果
     */
    @PostMapping("/upload/async")
    public AjaxResult uploadAsync(@RequestBody MockUploadRequest request) {
        log.info("[HealthMockController] 收到异步上传请求, 设备: {}", request.getDeviceId());

        // 参数校验
        if (StringUtils.isEmpty(request.getDeviceId())) {
            return error("设备ID不能为空");
        }
        if (request.getDataList() == null || request.getDataList().isEmpty()) {
            return error("体征数据不能为空");
        }

        // 异步提交任务
        healthDataService.processRawDataAsync(
                request.getDeviceId(),
                request.getUserId(),
                request.getDataList());

        return success("任务已提交，正在异步处理");
    }

    /**
     * 查询设备最新分析记录
     *
     * @param deviceId 设备ID
     * @return 最新分析记录
     */
    @GetMapping("/latest/{deviceId}")
    public AjaxResult getLatestRecord(@PathVariable String deviceId) {
        if (StringUtils.isEmpty(deviceId)) {
            return error("设备ID不能为空");
        }

        AiHealthRecord record = healthDataService.getLatestRecord(deviceId);
        if (record != null) {
            return success(record);
        } else {
            AjaxResult result = success();
            result.put("msg", "暂无分析记录");
            return result;
        }
    }

    /**
     * 检查AI服务状态
     *
     * @return 服务状态
     */
    @GetMapping("/ai-status")
    public AjaxResult checkAiServiceStatus() {
        boolean available = healthDataService.isAiServiceAvailable();
        if (available) {
            return success("AI服务正常运行");
        } else {
            return error("AI服务不可用，请检查Python服务是否启动");
        }
    }

    /**
     * 快速测试接口 - 生成模拟数据并分析
     *
     * @param deviceId 设备ID
     * @return 分析结果
     */
    @PostMapping("/quick-test")
    public AjaxResult quickTest(@RequestParam(value = "deviceId", defaultValue = "TEST_DEVICE_001") String deviceId) {
        log.info("[HealthMockController] 快速测试, 设备: {}", deviceId);

        // 生成模拟数据
        List<VitalSignData> mockData = generateMockData();

        MockUploadRequest request = new MockUploadRequest();
        request.setDeviceId(deviceId);
        request.setDataList(mockData);

        return upload(request);
    }

    /**
     * 生成模拟体征数据
     */
    private List<VitalSignData> generateMockData() {
        long baseTimestamp = System.currentTimeMillis();
        return Arrays.asList(
                new VitalSignData(72, "118/76", 500, baseTimestamp - 600000),
                new VitalSignData(75, "120/80", 800, baseTimestamp - 540000),
                new VitalSignData(73, "119/78", 1200, baseTimestamp - 480000),
                new VitalSignData(76, "121/79", 1500, baseTimestamp - 420000),
                new VitalSignData(74, "118/77", 2000, baseTimestamp - 360000),
                new VitalSignData(77, "122/81", 2300, baseTimestamp - 300000),
                new VitalSignData(75, "120/80", 2800, baseTimestamp - 240000),
                new VitalSignData(78, "123/82", 3200, baseTimestamp - 180000),
                new VitalSignData(76, "121/80", 3500, baseTimestamp - 120000),
                new VitalSignData(74, "119/78", 3800, baseTimestamp - 60000),
                new VitalSignData(130, "140/95", 100, baseTimestamp) // 异常数据点
        );
    }
}
