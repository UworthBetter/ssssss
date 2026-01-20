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
import com.ueit.health.domain.UeitLocation;
import com.ueit.health.service.IUeitLocationService;
import com.ueit.common.utils.poi.ExcelUtil;
import com.ueit.common.core.page.TableDataInfo;

/**
 * 瀹氫綅鏁版嵁Controller
 * 
 * @author z
 * @date 2024-01-05
 */
@RestController
@RequestMapping("/health/location")
public class UeitLocationController extends BaseController
{
    @Autowired
    private IUeitLocationService ueitLocationService;

    /**
     * 鏌ヨ瀹氫綅鏁版嵁鍒楄〃
     */
    @PreAuthorize("@ss.hasPermi('health:location:list')")
    @GetMapping("/list")
    public TableDataInfo list(UeitLocation ueitLocation)
    {
        startPage();
        List<UeitLocation> list = ueitLocationService.selectUeitLocationList(ueitLocation);
        return getDataTable(list);
    }

    /**
     * 瀵煎嚭瀹氫綅鏁版嵁鍒楄〃
     */
    @PreAuthorize("@ss.hasPermi('health:location:export')")
    @Log(title = "瀹氫綅鏁版嵁", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UeitLocation ueitLocation)
    {
        List<UeitLocation> list = ueitLocationService.selectUeitLocationList(ueitLocation);
        ExcelUtil<UeitLocation> util = new ExcelUtil<UeitLocation>(UeitLocation.class);
        util.exportExcel(response, list, "瀹氫綅鏁版嵁鏁版嵁");
    }

    /**
     * 鑾峰彇瀹氫綅鏁版嵁璇︾粏淇℃伅
     */
    @PreAuthorize("@ss.hasPermi('health:location:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(ueitLocationService.selectUeitLocationById(id));
    }

    /**
     * 鏂板瀹氫綅鏁版嵁
     */
    @PreAuthorize("@ss.hasPermi('health:location:add')")
    @Log(title = "瀹氫綅鏁版嵁", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UeitLocation ueitLocation)
    {
        return toAjax(ueitLocationService.insertUeitLocation(ueitLocation));
    }

    /**
     * 淇敼瀹氫綅鏁版嵁
     */
    @PreAuthorize("@ss.hasPermi('health:location:edit')")
    @Log(title = "瀹氫綅鏁版嵁", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UeitLocation ueitLocation)
    {
        return toAjax(ueitLocationService.updateUeitLocation(ueitLocation));
    }

    /**
     * 鍒犻櫎瀹氫綅鏁版嵁
     */
    @PreAuthorize("@ss.hasPermi('health:location:remove')")
    @Log(title = "瀹氫綅鏁版嵁", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(ueitLocationService.deleteUeitLocationByIds(ids));
    }
}
