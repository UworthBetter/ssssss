package com.qkyd.web.controller.demo;

import com.qkyd.common.core.controller.BaseController;
import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.web.demo.DemoDataEngineService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Runtime controls for simulated device ingestion.
 */
@RestController
@PreAuthorize("@ss.hasRole('admin')")
@RequestMapping("/demo/device-simulator")
public class DemoDeviceSimulatorController extends BaseController {

    private final DemoDataEngineService demoDataEngineService;

    public DemoDeviceSimulatorController(DemoDataEngineService demoDataEngineService) {
        this.demoDataEngineService = demoDataEngineService;
    }

    @GetMapping("/status")
    public AjaxResult status() {
        return success(demoDataEngineService.getEngineStatus());
    }

    @PostMapping("/start")
    public AjaxResult start(@RequestParam(value = "subjectCount", required = false) Integer subjectCount) {
        AjaxResult result = success(demoDataEngineService.startRuntimeSimulation(subjectCount));
        result.put("msg", "simulator started");
        return result;
    }

    @PostMapping("/stop")
    public AjaxResult stop() {
        AjaxResult result = success(demoDataEngineService.stopRuntimeSimulation());
        result.put("msg", "simulator stopped");
        return result;
    }

    @PostMapping("/reset")
    public AjaxResult reset() {
        AjaxResult result = success(demoDataEngineService.resetRuntimeSimulation());
        result.put("msg", "simulator reset to config mode");
        return result;
    }

    @PutMapping("/subject-count")
    public AjaxResult updateSubjectCount(@RequestParam("subjectCount") Integer subjectCount) {
        AjaxResult result = success(demoDataEngineService.setRuntimeSubjectCount(subjectCount));
        result.put("msg", "simulator subject count updated");
        return result;
    }

    @PostMapping("/tick")
    public AjaxResult tick(@RequestParam(value = "subjectCount", required = false) Integer subjectCount) {
        AjaxResult result = success(demoDataEngineService.triggerManualTick(subjectCount));
        result.put("msg", "simulator tick generated");
        return result;
    }
}
