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
import com.ueit.health.domain.UeitFence;
import com.ueit.health.service.IUeitFenceService;
import com.ueit.common.utils.poi.ExcelUtil;
import com.ueit.common.core.page.TableDataInfo;

/**
 * 围栏Controller
 *
 * @author z
 * @date 2024-01-19
 */
@RestController
@RequestMapping("/health/fence")
public class UeitFenceController extends BaseController
{
    @Autowired
    private IUeitFenceService ueitFenceService;

    /**
     * 查询围栏列表
     */
//    @PreAuthorize("@ss.hasPermi('health:fence:list')")
    @GetMapping("/list")
    public TableDataInfo list(UeitFence ueitFence)
    {
        startPage();
        List<UeitFence> list = ueitFenceService.selectUeitFenceList(ueitFence);
        return getDataTable(list);
    }

    /**
     * 导出围栏列表
     */
//    @PreAuthorize("@ss.hasPermi('health:fence:export')")
    @Log(title = "围栏", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UeitFence ueitFence)
    {
        List<UeitFence> list = ueitFenceService.selectUeitFenceList(ueitFence);
        ExcelUtil<UeitFence> util = new ExcelUtil<UeitFence>(UeitFence.class);
        util.exportExcel(response, list, "围栏数据");
    }

    /**
     * 获取围栏详细信息
     */
//    @PreAuthorize("@ss.hasPermi('health:fence:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(ueitFenceService.selectUeitFenceById(id));
    }

    /**
     * 新增围栏/修改围栏
     */
//    @PreAuthorize("@ss.hasPermi('health:fence:add')")
    @Log(title = "围栏", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UeitFence ueitFence)
    {
        ueitFence.setShape("1");
        return toAjax(ueitFenceService.insertUeitFence(ueitFence));
//        return toAjax(ueitFenceService.operateUeitFence(ueitFence));
    }

    /**
     * 修改围栏
     */
//    @PreAuthorize("@ss.hasPermi('health:fence:edit')")
    @Log(title = "围栏", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UeitFence ueitFence)
    {
        return toAjax(ueitFenceService.updateUeitFence(ueitFence));
    }
    /**
     * 修改围栏状态--->是否启用
     */
//    @PreAuthorize("@ss.hasPermi('health:fence:edit')")
    @Log(title = "围栏", businessType = BusinessType.UPDATE)
    @PutMapping("/updateFenceStatus")
    public AjaxResult updateFenceStatus(@RequestBody UeitFence ueitFence)
    {
        return toAjax(ueitFenceService.updateFenceStatus(ueitFence));
    }
    /**
     * 删除围栏
     */
//    @PreAuthorize("@ss.hasPermi('health:fence:remove')")
    @Log(title = "围栏", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(ueitFenceService.deleteUeitFenceByIds(ids));
    }
}
