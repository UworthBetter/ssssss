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
import com.ueit.health.domain.UeitDeviceInfo;
import com.ueit.health.service.IUeitDeviceInfoService;
import com.ueit.common.utils.poi.ExcelUtil;
import com.ueit.common.core.page.TableDataInfo;

/**
 * 设备信息Controller
 *
 * @author douwq
 * @date 2023-12-22
 */
@RestController
@RequestMapping("/health/deviceInfo")
public class UeitDeviceInfoController extends BaseController
{
    @Autowired
    private IUeitDeviceInfoService ueitDeviceInfoService;

    /**
     * 查询设备信息列表
     */
    @PreAuthorize("@ss.hasPermi('health:deviceInfo:list')")
    @GetMapping("/list")
    public TableDataInfo list(UeitDeviceInfo ueitDeviceInfo)
    {
        startPage();
        List<UeitDeviceInfo> list = ueitDeviceInfoService.selectUeitDeviceInfoList(ueitDeviceInfo);
        return getDataTable(list);
    }

    /**
     * 导出设备信息列表
     */
    @PreAuthorize("@ss.hasPermi('health:deviceInfo:export')")
    @Log(title = "设备信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UeitDeviceInfo ueitDeviceInfo)
    {
        List<UeitDeviceInfo> list = ueitDeviceInfoService.selectUeitDeviceInfoList(ueitDeviceInfo);
        ExcelUtil<UeitDeviceInfo> util = new ExcelUtil<UeitDeviceInfo>(UeitDeviceInfo.class);
        util.exportExcel(response, list, "设备信息数据");
    }

    /**
     * 获取设备信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('health:deviceInfo:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(ueitDeviceInfoService.selectUeitDeviceInfoById(id));
    }

    /**
     * 新增设备信息
     */
    @PreAuthorize("@ss.hasPermi('health:deviceInfo:add')")
    @Log(title = "设备信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UeitDeviceInfo ueitDeviceInfo)
    {
        AjaxResult result = null;
        int i = ueitDeviceInfoService.insertUeitDeviceInfo(ueitDeviceInfo);
        if (i == 1) {
            result = AjaxResult.success();
        } else if (i == -1) {
            result = AjaxResult.error(500,"IMEI是空");
        } else if (i == -2) {
            result = AjaxResult.error(500,"IMEI已存在");
        }
        return result;
    }

    /**
     * 修改设备信息
     */
    @PreAuthorize("@ss.hasPermi('health:deviceInfo:edit')")
    @Log(title = "设备信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UeitDeviceInfo ueitDeviceInfo)
    {
        AjaxResult result = null;
        int i = ueitDeviceInfoService.updateUeitDeviceInfo(ueitDeviceInfo);
        if (i == 1) {
            result = AjaxResult.success();
        } else if (i == -1) {
            result = AjaxResult.error(500,"IMEI是空");
        } else if (i == -2) {
            result = AjaxResult.error(500,"IMEI已存在");
        }
        return result;
    }

    /**
     * 删除设备信息
     */
    @PreAuthorize("@ss.hasPermi('health:deviceInfo:remove')")
    @Log(title = "设备信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(ueitDeviceInfoService.deleteUeitDeviceInfoByIds(ids));
    }
}
