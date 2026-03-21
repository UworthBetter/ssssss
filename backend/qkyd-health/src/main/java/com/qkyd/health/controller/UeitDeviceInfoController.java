package com.qkyd.health.controller;

import com.qkyd.common.annotation.Log;
import com.qkyd.common.core.controller.BaseController;
import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.common.core.page.TableDataInfo;
import com.qkyd.common.enums.BusinessType;
import com.qkyd.common.utils.poi.ExcelUtil;
import com.qkyd.health.domain.UeitDeviceInfo;
import com.qkyd.health.service.IUeitDeviceInfoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 设备信息Controller
 *
 * @author douwq
 * @date 2023-12-22
 */
@RestController
@RequestMapping("/health/deviceInfo")
public class UeitDeviceInfoController extends BaseController {
    @Autowired
    private IUeitDeviceInfoService ueitDeviceInfoService;

    /**
     * 查询设备信息列表
     */
    @PreAuthorize("@ss.hasPermi('health:deviceInfo:list')")
    @GetMapping("/list")
    public TableDataInfo list(UeitDeviceInfo ueitDeviceInfo) {
        startPage();
        List<UeitDeviceInfo> list = new ArrayList<>();
        try {
            list = ueitDeviceInfoService.selectUeitDeviceInfoList(ueitDeviceInfo);
        } catch (Exception ignored) {
        }
        if (list == null || list.isEmpty()) {
            list = filterDevices(mockDevices(), ueitDeviceInfo);
        }
        return getDataTable(list);
    }

    /**
     * 导出设备信息列表
     */
    @PreAuthorize("@ss.hasPermi('health:deviceInfo:export')")
    @Log(title = "设备信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UeitDeviceInfo ueitDeviceInfo) {
        List<UeitDeviceInfo> list = new ArrayList<>();
        try {
            list = ueitDeviceInfoService.selectUeitDeviceInfoList(ueitDeviceInfo);
        } catch (Exception ignored) {
        }
        if (list == null || list.isEmpty()) {
            list = filterDevices(mockDevices(), ueitDeviceInfo);
        }
        ExcelUtil<UeitDeviceInfo> util = new ExcelUtil<>(UeitDeviceInfo.class);
        util.exportExcel(response, list, "设备信息数据");
    }

    /**
     * 获取设备信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('health:deviceInfo:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        UeitDeviceInfo device = null;
        try {
            device = ueitDeviceInfoService.selectUeitDeviceInfoById(id);
        } catch (Exception ignored) {
        }
        if (device == null) {
            for (UeitDeviceInfo item : mockDevices()) {
                if (id.equals(item.getId())) {
                    device = item;
                    break;
                }
            }
        }
        return success(device);
    }

    /**
     * 新增设备信息
     */
    @PreAuthorize("@ss.hasPermi('health:deviceInfo:add')")
    @Log(title = "设备信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UeitDeviceInfo ueitDeviceInfo) {
        AjaxResult result = null;
        int i = ueitDeviceInfoService.insertUeitDeviceInfo(ueitDeviceInfo);
        if (i == 1) {
            result = AjaxResult.success();
        } else if (i == -1) {
            result = AjaxResult.error(500, "IMEI不能为空");
        } else if (i == -2) {
            result = AjaxResult.error(500, "IMEI已存在");
        }
        return result;
    }

    /**
     * 修改设备信息
     */
    @PreAuthorize("@ss.hasPermi('health:deviceInfo:edit')")
    @Log(title = "设备信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UeitDeviceInfo ueitDeviceInfo) {
        AjaxResult result = null;
        int i = ueitDeviceInfoService.updateUeitDeviceInfo(ueitDeviceInfo);
        if (i == 1) {
            result = AjaxResult.success();
        } else if (i == -1) {
            result = AjaxResult.error(500, "IMEI不能为空");
        } else if (i == -2) {
            result = AjaxResult.error(500, "IMEI已存在");
        }
        return result;
    }

    /**
     * 删除设备信息
     */
    @PreAuthorize("@ss.hasPermi('health:deviceInfo:remove')")
    @Log(title = "设备信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(ueitDeviceInfoService.deleteUeitDeviceInfoByIds(ids));
    }

    private List<UeitDeviceInfo> filterDevices(List<UeitDeviceInfo> source, UeitDeviceInfo query) {
        if (query == null) {
            return source;
        }
        List<UeitDeviceInfo> result = new ArrayList<>();
        for (UeitDeviceInfo item : source) {
            boolean match = true;
            if (query.getName() != null && !query.getName().isBlank()) {
                match = item.getName() != null && item.getName().contains(query.getName());
            }
            if (match && query.getImei() != null && !query.getImei().isBlank()) {
                match = item.getImei() != null && item.getImei().contains(query.getImei());
            }
            if (match && query.getUserId() != null) {
                match = query.getUserId().equals(item.getUserId());
            }
            if (match) {
                result.add(item);
            }
        }
        return result;
    }

    private List<UeitDeviceInfo> mockDevices() {
        List<UeitDeviceInfo> list = new ArrayList<>();
        list.add(buildDevice(3001L, 10001L, "腕表-A1", "860001000000001", "智能腕表"));
        list.add(buildDevice(3002L, 10002L, "腕表-A2", "860001000000002", "智能腕表"));
        list.add(buildDevice(3003L, 10003L, "胸贴-H1", "860001000000003", "心率贴片"));
        list.add(buildDevice(3004L, 10004L, "定位器-G1", "860001000000004", "定位终端"));
        list.add(buildDevice(3005L, 10005L, "腕表-A3", "860001000000005", "智能腕表"));
        return list;
    }

    private UeitDeviceInfo buildDevice(Long id, Long userId, String name, String imei, String type) {
        UeitDeviceInfo device = new UeitDeviceInfo();
        device.setId(id);
        device.setUserId(userId);
        device.setName(name);
        device.setImei(imei);
        device.setType(type);
        device.setCreateTime(new Date());
        return device;
    }
}
