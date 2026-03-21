package com.qkyd.health.controller;

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
import com.qkyd.common.annotation.Log;
import com.qkyd.common.core.controller.BaseController;
import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.common.enums.BusinessType;
import com.qkyd.health.domain.UeitHeartRate;
import com.qkyd.health.service.IUeitHeartRateService;
import com.qkyd.common.utils.poi.ExcelUtil;
import com.qkyd.common.core.page.TableDataInfo;

/**
 * 心率数据Controller
 * 
 * @author z
 * @date 2024-01-05
 */
@RestController
@RequestMapping("/health/rate")
public class UeitHeartRateController extends BaseController
{
    @Autowired
    private IUeitHeartRateService ueitHeartRateService;

    /**
     * 查询心率数据列表
     */
    @PreAuthorize("@ss.hasPermi('health:rate:list')")
    @GetMapping("/list")
    public TableDataInfo list(UeitHeartRate ueitHeartRate)
    {
        startPage();
        List<UeitHeartRate> list = ueitHeartRateService.selectUeitHeartRateList(ueitHeartRate);
        return getDataTable(list);
    }

    /**
     * 导出心率数据列表
     */
    @PreAuthorize("@ss.hasPermi('health:rate:export')")
    @Log(title = "心率数据", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UeitHeartRate ueitHeartRate)
    {
        List<UeitHeartRate> list = ueitHeartRateService.selectUeitHeartRateList(ueitHeartRate);
        ExcelUtil<UeitHeartRate> util = new ExcelUtil<UeitHeartRate>(UeitHeartRate.class);
        util.exportExcel(response, list, "心率数据数据");
    }

    /**
     * 获取心率数据详细信息
     */
    @PreAuthorize("@ss.hasPermi('health:rate:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(ueitHeartRateService.selectUeitHeartRateById(id));
    }

    /**
     * 新增心率数据
     */
    @PreAuthorize("@ss.hasPermi('health:rate:add')")
    @Log(title = "心率数据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UeitHeartRate ueitHeartRate)
    {
        return toAjax(ueitHeartRateService.insertUeitHeartRate(ueitHeartRate));
    }

    /**
     * 修改心率数据
     */
    @PreAuthorize("@ss.hasPermi('health:rate:edit')")
    @Log(title = "心率数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UeitHeartRate ueitHeartRate)
    {
        return toAjax(ueitHeartRateService.updateUeitHeartRate(ueitHeartRate));
    }

    /**
     * 删除心率数据
     */
    @PreAuthorize("@ss.hasPermi('health:rate:remove')")
    @Log(title = "心率数据", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(ueitHeartRateService.deleteUeitHeartRateByIds(ids));
    }
}

