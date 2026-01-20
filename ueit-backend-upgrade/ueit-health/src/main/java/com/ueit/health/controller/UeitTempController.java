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
import com.ueit.health.domain.UeitTemp;
import com.ueit.health.service.IUeitTempService;
import com.ueit.common.utils.poi.ExcelUtil;
import com.ueit.common.core.page.TableDataInfo;

/**
 * 浣撴俯鏁版嵁Controller
 *
 * @author z
 * @date 2024-01-05
 */
@RestController
@RequestMapping("/health/temp")
public class UeitTempController extends BaseController
{
    @Autowired
    private IUeitTempService ueitTempService;

    /**
     * 鏌ヨ浣撴俯鏁版嵁鍒楄〃
     */
    @PreAuthorize("@ss.hasPermi('health:temp:list')")
    @GetMapping("/list")
    public TableDataInfo list(UeitTemp ueitTemp)
    {
        startPage();
        List<UeitTemp> list = ueitTempService.selectUeitTempList(ueitTemp);
        return getDataTable(list);
    }

    /**
     * 瀵煎嚭浣撴俯鏁版嵁鍒楄〃
     */
    @PreAuthorize("@ss.hasPermi('health:temp:export')")
    @Log(title = "浣撴俯鏁版嵁", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UeitTemp ueitTemp)
    {
        List<UeitTemp> list = ueitTempService.selectUeitTempList(ueitTemp);
        ExcelUtil<UeitTemp> util = new ExcelUtil<UeitTemp>(UeitTemp.class);
        util.exportExcel(response, list, "浣撴俯鏁版嵁鏁版嵁");
    }

    /**
     * 鑾峰彇浣撴俯鏁版嵁璇︾粏淇℃伅
     */
    @PreAuthorize("@ss.hasPermi('health:temp:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(ueitTempService.selectUeitTempById(id));
    }

    /**
     * 鏂板浣撴俯鏁版嵁
     */
    @PreAuthorize("@ss.hasPermi('health:temp:add')")
    @Log(title = "浣撴俯鏁版嵁", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UeitTemp ueitTemp)
    {
        return toAjax(ueitTempService.insertUeitTemp(ueitTemp));
    }

    /**
     * 淇敼浣撴俯鏁版嵁
     */
    @PreAuthorize("@ss.hasPermi('health:temp:edit')")
    @Log(title = "浣撴俯鏁版嵁", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UeitTemp ueitTemp)
    {
        return toAjax(ueitTempService.updateUeitTemp(ueitTemp));
    }

    /**
     * 鍒犻櫎浣撴俯鏁版嵁
     */
    @PreAuthorize("@ss.hasPermi('health:temp:remove')")
    @Log(title = "浣撴俯鏁版嵁", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(ueitTempService.deleteUeitTempByIds(ids));
    }
}
