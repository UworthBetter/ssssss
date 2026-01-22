package com.ueit.health.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ueit.common.annotation.Log;
import com.ueit.common.core.controller.BaseController;
import com.ueit.common.core.domain.AjaxResult;
import com.ueit.common.enums.BusinessType;
import com.ueit.health.domain.UeitSteps;
import com.ueit.health.service.IUeitStepsService;
import com.ueit.common.utils.poi.ExcelUtil;
import com.ueit.common.core.page.TableDataInfo;

/**
 * 步数Controller
 * 
 * @author z
 * @date 2024-01-05
 */
@RestController
@RequestMapping("/health/steps")
public class UeitStepsController extends BaseController
{
    @Autowired
    private IUeitStepsService ueitStepsService;

    /**
     * 查询步数列表
     */
    @PreAuthorize("@ss.hasPermi('health:steps:list')")
    @GetMapping("/list")
    public TableDataInfo list(UeitSteps ueitSteps)
    {
        startPage();
        List<UeitSteps> list = ueitStepsService.selectUeitStepsList(ueitSteps);
        return getDataTable(list);
    }

    /**
     * 导出步数列表
     */
    @PreAuthorize("@ss.hasPermi('health:steps:export')")
    @Log(title = "步数", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UeitSteps ueitSteps)
    {
        List<UeitSteps> list = ueitStepsService.selectUeitStepsList(ueitSteps);
        ExcelUtil<UeitSteps> util = new ExcelUtil<UeitSteps>(UeitSteps.class);
        util.exportExcel(response, list, "步数数据");
    }

    /**
     * 获取步数详细信息
     */
    @PreAuthorize("@ss.hasPermi('health:steps:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(ueitStepsService.selectUeitStepsById(id));
    }

    /**
     * 新增步数
     */
    @PreAuthorize("@ss.hasPermi('health:steps:add')")
    @Log(title = "步数", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UeitSteps ueitSteps)
    {
        return toAjax(ueitStepsService.insertUeitSteps(ueitSteps));
    }

    /**
     * 修改步数
     */
    @PreAuthorize("@ss.hasPermi('health:steps:edit')")
    @Log(title = "步数", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UeitSteps ueitSteps)
    {
        return toAjax(ueitStepsService.updateUeitSteps(ueitSteps));
    }

    /**
     * 删除步数
     */
    @PreAuthorize("@ss.hasPermi('health:steps:remove')")
    @Log(title = "步数", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(ueitStepsService.deleteUeitStepsByIds(ids));
    }
}

