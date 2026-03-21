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
import com.qkyd.health.domain.HealthAlertHandle;
import com.qkyd.health.service.IHealthAlertHandleService;
import com.qkyd.common.utils.poi.ExcelUtil;
import com.qkyd.common.core.page.TableDataInfo;
import com.qkyd.common.utils.SecurityUtils;

/**
 * 告警处理记录Controller
 *
 * @author qkyd
 * @date 2026-02-02
 */
@RestController
@RequestMapping("/health/alert/handle")
public class HealthAlertHandleController extends BaseController
{
    @Autowired
    private IHealthAlertHandleService healthAlertHandleService;

    /**
     * 查询告警处理记录列表
     */
    @PreAuthorize("@ss.hasPermi('health:alert:list')")
    @GetMapping("/list")
    public TableDataInfo list(HealthAlertHandle healthAlertHandle)
    {
        startPage();
        List<HealthAlertHandle> list = healthAlertHandleService.selectHealthAlertHandleList(healthAlertHandle);
        return getDataTable(list);
    }

    /**
     * 根据告警ID查询处理记录
     */
    @GetMapping("/byAlert/{alertId}")
    public AjaxResult getByAlertId(@PathVariable("alertId") Long alertId)
    {
        List<HealthAlertHandle> list = healthAlertHandleService.selectByAlertId(alertId);
        return success(list);
    }

    /**
     * 导出告警处理记录列表
     */
    @PreAuthorize("@ss.hasPermi('health:alert:export')")
    @Log(title = "告警处理记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HealthAlertHandle healthAlertHandle)
    {
        List<HealthAlertHandle> list = healthAlertHandleService.selectHealthAlertHandleList(healthAlertHandle);
        ExcelUtil<HealthAlertHandle> util = new ExcelUtil<HealthAlertHandle>(HealthAlertHandle.class);
        util.exportExcel(response, list, "告警处理记录");
    }

    /**
     * 获取告警处理记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('health:alert:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(healthAlertHandleService.selectHealthAlertHandleById(id));
    }

    /**
     * 新增告警处理记录
     */
    @PreAuthorize("@ss.hasPermi('health:alert:handle')")
    @Log(title = "告警处理记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HealthAlertHandle healthAlertHandle)
    {
        healthAlertHandle.setHandlerId(SecurityUtils.getUserId());
        healthAlertHandle.setHandlerName(SecurityUtils.getUsername());
        return toAjax(healthAlertHandleService.insertHealthAlertHandle(healthAlertHandle));
    }

    /**
     * 修改告警处理记录
     */
    @PreAuthorize("@ss.hasPermi('health:alert:edit')")
    @Log(title = "告警处理记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HealthAlertHandle healthAlertHandle)
    {
        return toAjax(healthAlertHandleService.updateHealthAlertHandle(healthAlertHandle));
    }

    /**
     * 删除告警处理记录
     */
    @PreAuthorize("@ss.hasPermi('health:alert:remove')")
    @Log(title = "告警处理记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(healthAlertHandleService.deleteHealthAlertHandleByIds(ids));
    }
}
