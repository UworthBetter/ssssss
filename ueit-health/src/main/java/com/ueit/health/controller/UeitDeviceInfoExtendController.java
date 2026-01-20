package com.ueit.health.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
import com.ueit.health.domain.UeitDeviceInfoExtend;
import com.ueit.health.service.IUeitDeviceInfoExtendService;
import com.ueit.common.utils.poi.ExcelUtil;
import com.ueit.common.core.page.TableDataInfo;

/**
 * 设备信息扩展Controller
 * 
 * @author douwq
 * @date 2024-01-03
 */
@RestController
@RequestMapping("/health/deviceInfoExt")
public class UeitDeviceInfoExtendController extends BaseController
{
    @Autowired
    private IUeitDeviceInfoExtendService ueitDeviceInfoExtendService;

    /**
     * 查询设备信息扩展列表
     */
    @PreAuthorize("@ss.hasPermi('health:deviceInfoExt:list')")
    @GetMapping("/list")
    public TableDataInfo list(UeitDeviceInfoExtend ueitDeviceInfoExtend)
    {
        startPage();
        List<UeitDeviceInfoExtend> list = ueitDeviceInfoExtendService.selectUeitDeviceInfoExtendList(ueitDeviceInfoExtend);
        return getDataTable(list);
    }

    /**
     * 导出设备信息扩展列表
     */
    @PreAuthorize("@ss.hasPermi('health:deviceInfoExt:export')")
    @Log(title = "设备信息扩展", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UeitDeviceInfoExtend ueitDeviceInfoExtend)
    {
        List<UeitDeviceInfoExtend> list = ueitDeviceInfoExtendService.selectUeitDeviceInfoExtendList(ueitDeviceInfoExtend);
        ExcelUtil<UeitDeviceInfoExtend> util = new ExcelUtil<UeitDeviceInfoExtend>(UeitDeviceInfoExtend.class);
        util.exportExcel(response, list, "设备信息扩展数据");
    }

    /**
     * 获取设备信息扩展详细信息
     */
    @PreAuthorize("@ss.hasPermi('health:deviceInfoExt:query')")
    @GetMapping(value = "/{deviceId}")
    public AjaxResult getInfo(@PathVariable("deviceId") Long deviceId)
    {
        return success(ueitDeviceInfoExtendService.selectUeitDeviceInfoExtendByDeviceId(deviceId));
    }

    /**
     * 新增设备信息扩展
     */
    @PreAuthorize("@ss.hasPermi('health:deviceInfoExt:add')")
    @Log(title = "设备信息扩展", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UeitDeviceInfoExtend ueitDeviceInfoExtend)
    {
        return toAjax(ueitDeviceInfoExtendService.insertUeitDeviceInfoExtend(ueitDeviceInfoExtend));
    }

    /**
     * 修改设备信息扩展
     */
    @PreAuthorize("@ss.hasPermi('health:deviceInfoExt:edit')")
    @Log(title = "设备信息扩展", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UeitDeviceInfoExtend ueitDeviceInfoExtend)
    {
        return toAjax(ueitDeviceInfoExtendService.updateUeitDeviceInfoExtend(ueitDeviceInfoExtend));
    }

    /**
     * 删除设备信息扩展
     */
    @PreAuthorize("@ss.hasPermi('health:deviceInfoExt:remove')")
    @Log(title = "设备信息扩展", businessType = BusinessType.DELETE)
	@DeleteMapping("/{deviceIds}")
    public AjaxResult remove(@PathVariable Long[] deviceIds)
    {
        return toAjax(ueitDeviceInfoExtendService.deleteUeitDeviceInfoExtendByDeviceIds(deviceIds));
    }
}
