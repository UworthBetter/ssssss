package com.ueit.health.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ueit.common.annotation.Log;
import com.ueit.common.core.controller.BaseController;
import com.ueit.common.core.domain.AjaxResult;
import com.ueit.common.enums.BusinessType;
import com.ueit.health.domain.UeitException;
import com.ueit.health.service.IUeitExceptionService;
import com.ueit.common.utils.poi.ExcelUtil;
import com.ueit.common.core.page.TableDataInfo;

/**
 * 异常数据Controller
 *
 * @author z
 * @date 2024-01-05
 */
@RestController
@RequestMapping("/health/exception")
public class UeitExceptionController extends BaseController
{
    @Autowired
    private IUeitExceptionService ueitExceptionService;

    /**
     * 查询异常数据列表
     */
    @PreAuthorize("@ss.hasPermi('health:exception:list')")
    @GetMapping("/list")
    public TableDataInfo list(UeitException ueitException)
    {
        startPage();
        List<UeitException> list = ueitExceptionService.selectUeitExceptionList(ueitException);
        return getDataTable(list);
    }
    /**
     * 根据用户id查询异常数据列表
     */
    @GetMapping("/listByUserId")
    public TableDataInfo listByUserId(@RequestParam("userId") int userId)
    {
        startPage();
        List<UeitException> list = ueitExceptionService.selectUeitExceptionListByUserId(userId);
        return getDataTable(list);
    }
    /**
     * 导出异常数据列表
     */
    @PreAuthorize("@ss.hasPermi('health:exception:export')")
    @Log(title = "异常数据", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UeitException ueitException)
    {
        List<UeitException> list = ueitExceptionService.selectUeitExceptionList(ueitException);
        ExcelUtil<UeitException> util = new ExcelUtil<UeitException>(UeitException.class);
        util.exportExcel(response, list, "异常数据数据");
    }

    /**
     * 获取异常数据详细信息
     */
    @PreAuthorize("@ss.hasPermi('health:exception:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(ueitExceptionService.selectUeitExceptionById(id));
    }
    // 获取某个异常的具体信息
    @GetMapping(value = "/T")
    public AjaxResult getInfoBy(@RequestParam("coordinateType") String coordinateType, @RequestParam("id") Long id)
    {
        return success(ueitExceptionService.selectUeitExceptionById(id));
    }
    /**
     * 新增异常数据
     */
    @PreAuthorize("@ss.hasPermi('health:exception:add')")
    @Log(title = "异常数据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UeitException ueitException)
    {
        return toAjax(ueitExceptionService.insertUeitException(ueitException));
    }

    /**
     * 修改异常数据
     */
    @PreAuthorize("@ss.hasPermi('health:exception:edit')")
    @Log(title = "异常数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UeitException ueitException)
    {
        ueitException.setUpdateBy(getUsername());
        return toAjax(ueitExceptionService.updateUeitException(ueitException));
    }

    /**
     * 删除异常数据
     */
    @PreAuthorize("@ss.hasPermi('health:exception:remove')")
    @Log(title = "异常数据", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(ueitExceptionService.deleteUeitExceptionByIds(ids));
    }
}
