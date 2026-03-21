package com.qkyd.health.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.qkyd.common.annotation.Log;
import com.qkyd.common.core.controller.BaseController;
import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.common.enums.BusinessType;
import com.qkyd.health.domain.HealthDeviceBinding;
import com.qkyd.health.service.IHealthDeviceBindingService;
import com.qkyd.common.utils.poi.ExcelUtil;
import com.qkyd.common.core.page.TableDataInfo;

/**
 * 设备绑定关系Controller
 *
 * @author qkyd
 * @date 2026-02-02
 */
@RestController
@RequestMapping("/health/device/binding")
public class HealthDeviceBindingController extends BaseController
{
    @Autowired
    private IHealthDeviceBindingService healthDeviceBindingService;

    /**
     * 查询设备绑定关系列表
     */
    @PreAuthorize("@ss.hasPermi('health:device:list')")
    @GetMapping("/list")
    public TableDataInfo list(HealthDeviceBinding healthDeviceBinding)
    {
        startPage();
        List<HealthDeviceBinding> list = healthDeviceBindingService.selectHealthDeviceBindingList(healthDeviceBinding);
        return getDataTable(list);
    }

    /**
     * 查询用户的绑定设备列表
     */
    @GetMapping("/bySubject/{subjectId}")
    public AjaxResult listBySubjectId(@PathVariable("subjectId") Long subjectId)
    {
        List<HealthDeviceBinding> list = healthDeviceBindingService.selectBySubjectId(subjectId);
        return success(list);
    }

    /**
     * 导出设备绑定关系列表
     */
    @PreAuthorize("@ss.hasPermi('health:device:export')")
    @Log(title = "设备绑定关系", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HealthDeviceBinding healthDeviceBinding)
    {
        List<HealthDeviceBinding> list = healthDeviceBindingService.selectHealthDeviceBindingList(healthDeviceBinding);
        ExcelUtil<HealthDeviceBinding> util = new ExcelUtil<HealthDeviceBinding>(HealthDeviceBinding.class);
        util.exportExcel(response, list, "设备绑定关系");
    }

    /**
     * 获取设备绑定关系详细信息
     */
    @PreAuthorize("@ss.hasPermi('health:device:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(healthDeviceBindingService.selectHealthDeviceBindingById(id));
    }

    /**
     * 绑定设备
     */
    @PreAuthorize("@ss.hasPermi('health:subject:bind']")
    @Log(title = "设备绑定关系", businessType = BusinessType.INSERT)
    @PostMapping("/bind")
    public AjaxResult bind(@RequestBody HealthDeviceBinding binding)
    {
        try {
            return toAjax(healthDeviceBindingService.bindDevice(binding.getSubjectId(), binding.getDeviceId()));
        } catch (RuntimeException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 解绑设备
     */
    @PreAuthorize("@ss.hasPermi('health:subject:bind']")
    @Log(title = "设备绑定关系", businessType = BusinessType.UPDATE)
    @PutMapping("/unbind/{id}")
    public AjaxResult unbind(@PathVariable("id") Long id)
    {
        return toAjax(healthDeviceBindingService.unbindDevice(id));
    }

    /**
     * 删除设备绑定关系
     */
    @PreAuthorize("@ss.hasPermi('health:device:remove']")
    @Log(title = "设备绑定关系", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(healthDeviceBindingService.deleteHealthDeviceBindingByIds(ids));
    }
}
