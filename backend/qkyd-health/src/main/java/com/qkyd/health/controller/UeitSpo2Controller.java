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
import com.qkyd.health.domain.UeitSpo2;
import com.qkyd.health.service.IUeitSpo2Service;
import com.qkyd.common.utils.poi.ExcelUtil;
import com.qkyd.common.core.page.TableDataInfo;

/**
 * 血氧数据Controller
 * 
 * @author z
 * @date 2024-01-05
 */
@RestController
@RequestMapping("/health/spo2")
public class UeitSpo2Controller extends BaseController
{
    @Autowired
    private IUeitSpo2Service ueitSpo2Service;

    /**
     * 查询血氧数据列表
     */
    @PreAuthorize("@ss.hasPermi('health:spo2:list')")
    @GetMapping("/list")
    public TableDataInfo list(UeitSpo2 ueitSpo2)
    {
        startPage();
        List<UeitSpo2> list = ueitSpo2Service.selectUeitSpo2List(ueitSpo2);
        return getDataTable(list);
    }

    /**
     * 导出血氧数据列表
     */
    @PreAuthorize("@ss.hasPermi('health:spo2:export')")
    @Log(title = "血氧数据", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UeitSpo2 ueitSpo2)
    {
        List<UeitSpo2> list = ueitSpo2Service.selectUeitSpo2List(ueitSpo2);
        ExcelUtil<UeitSpo2> util = new ExcelUtil<UeitSpo2>(UeitSpo2.class);
        util.exportExcel(response, list, "血氧数据数据");
    }

    /**
     * 获取血氧数据详细信息
     */
    @PreAuthorize("@ss.hasPermi('health:spo2:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(ueitSpo2Service.selectUeitSpo2ById(id));
    }

    /**
     * 新增血氧数据
     */
    @PreAuthorize("@ss.hasPermi('health:spo2:add')")
    @Log(title = "血氧数据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UeitSpo2 ueitSpo2)
    {
        return toAjax(ueitSpo2Service.insertUeitSpo2(ueitSpo2));
    }

    /**
     * 修改血氧数据
     */
    @PreAuthorize("@ss.hasPermi('health:spo2:edit')")
    @Log(title = "血氧数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UeitSpo2 ueitSpo2)
    {
        return toAjax(ueitSpo2Service.updateUeitSpo2(ueitSpo2));
    }

    /**
     * 删除血氧数据
     */
    @PreAuthorize("@ss.hasPermi('health:spo2:remove')")
    @Log(title = "血氧数据", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(ueitSpo2Service.deleteUeitSpo2ByIds(ids));
    }
}

