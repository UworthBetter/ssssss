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
import com.ueit.health.domain.UeitBlood;
import com.ueit.health.service.IUeitBloodService;
import com.ueit.common.utils.poi.ExcelUtil;
import com.ueit.common.core.page.TableDataInfo;

/**
 * 血压数据Controller
 * 
 * @author z
 * @date 2024-01-05
 */
@RestController
@RequestMapping("/health/blood")
public class UeitBloodController extends BaseController
{
    @Autowired
    private IUeitBloodService ueitBloodService;

    /**
     * 查询血压数据列表
     */
    @PreAuthorize("@ss.hasPermi('health:blood:list')")
    @GetMapping("/list")
    public TableDataInfo list(UeitBlood ueitBlood)
    {
        startPage();
        List<UeitBlood> list = ueitBloodService.selectUeitBloodList(ueitBlood);
        return getDataTable(list);
    }

    /**
     * 导出血压数据列表
     */
    @PreAuthorize("@ss.hasPermi('health:blood:export')")
    @Log(title = "血压数据", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UeitBlood ueitBlood)
    {
        List<UeitBlood> list = ueitBloodService.selectUeitBloodList(ueitBlood);
        ExcelUtil<UeitBlood> util = new ExcelUtil<UeitBlood>(UeitBlood.class);
        util.exportExcel(response, list, "血压数据数据");
    }

    /**
     * 获取血压数据详细信息
     */
    @PreAuthorize("@ss.hasPermi('health:blood:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(ueitBloodService.selectUeitBloodById(id));
    }

    /**
     * 新增血压数据
     */
    @PreAuthorize("@ss.hasPermi('health:blood:add')")
    @Log(title = "血压数据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UeitBlood ueitBlood)
    {
        return toAjax(ueitBloodService.insertUeitBlood(ueitBlood));
    }

    /**
     * 修改血压数据
     */
    @PreAuthorize("@ss.hasPermi('health:blood:edit')")
    @Log(title = "血压数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UeitBlood ueitBlood)
    {
        return toAjax(ueitBloodService.updateUeitBlood(ueitBlood));
    }

    /**
     * 删除血压数据
     */
    @PreAuthorize("@ss.hasPermi('health:blood:remove')")
    @Log(title = "血压数据", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(ueitBloodService.deleteUeitBloodByIds(ids));
    }
}
