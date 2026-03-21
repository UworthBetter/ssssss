package com.qkyd.health.controller.data;

import com.qkyd.common.annotation.Log;
import com.qkyd.common.core.controller.BaseController;
import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.common.core.page.TableDataInfo;
import com.qkyd.common.enums.BusinessType;
import com.qkyd.common.utils.poi.ExcelUtil;
import com.qkyd.health.domain.UeitDeviceInfo;
import com.qkyd.health.domain.dto.UserDevice;
import com.qkyd.health.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/data")
public class DataController extends BaseController {

    @Autowired
    private DataService dataService;

    /**
     * 健康数据看板 七种类型
     *
     * @param userId
     * @param type
     * @param beginReadTime
     * @param endReadTime
     * @return
     */
    @GetMapping(value = "/dataBoard")
    public AjaxResult getDataBoard(@RequestParam("userId") int userId,
                                   @RequestParam("type") String type,
                                   @RequestParam("beginReadTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginReadTime,
                                   @RequestParam("endReadTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endReadTime) {
        // 根据参数进行数据查询操作
        List dataList = dataService.getDataBoard(userId, type, beginReadTime, endReadTime);
        return success(dataList);
    }

    /**
     * 查询用户设备列表
     *
     * @param userDevice
     * @return
     */
    @GetMapping(value = "/userDevice")
    public TableDataInfo getUserDevice(UserDevice userDevice) {
        startPage();
        // 查询用户id、设备ID、设备名称、用户昵称、手机号码、IMEI信息、设备型号、电量、最后通讯时间、设备创建时间
        List dataList = dataService.getUserDevice(userDevice);
        return getDataTable(dataList);
    }

    /**
     * 通过UserId查询用户设备列表
     * @param userId
     * @return
     */
    @GetMapping(value = "/userDeviceByUserId/{userId}")
    public TableDataInfo getUserDeviceByUserId(@PathVariable("userId") int userId) {
        // 查询用户id、设备ID、设备名称、用户昵称、手机号码、IMEI信息、设备型号、电量、最后通讯时间、设备创建时间
        List dataList = dataService.getUserDeviceByUserId(userId);
        return getDataTable(dataList);
    }
    /**
     * 导出用户设备列表
     *
     * @param response
     * @param userDevice
     */
    @Log(title = "设备信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UserDevice userDevice) {
        List<UserDevice> dataList = dataService.getUserDevice(userDevice);
        ExcelUtil<UserDevice> util = new ExcelUtil<UserDevice>(UserDevice.class);
        util.exportExcel(response, dataList, "设备信息数据");
    }

    /**
     * 查询没有绑定用户的设备列表
     *  pageNum pageSize
     * @return
     */
    @GetMapping(value = "/deviceListWithoutUser")
    public TableDataInfo getDeviceListWithoutUser(UeitDeviceInfo ueitDeviceInfo) {
        startPage();
        List<UeitDeviceInfo> dataList = dataService.getDeviceListWithoutUser(ueitDeviceInfo);
        return getDataTable(dataList);
    }

    /**
     * 把设备绑定给某个用户
     */
    @GetMapping(value = "/addDeviceToUser")
    public AjaxResult addDeviceToUser(@RequestParam("deviceIds") Long[] deviceIds, @RequestParam("userId") int userId) {
        return toAjax(dataService.addDeviceToUser(deviceIds, userId));
    }

    /**
     * 实时跟踪
     */
    @GetMapping(value = "/location")
    public AjaxResult realTimeTracking(@RequestParam("coordinateType") String coordinateType, @RequestParam("userId") int userId) {
        AjaxResult success = success(dataService.realTimeTracking(coordinateType, userId));
        return success;
    }

    /**
     * 历史轨迹
     */
    @GetMapping(value = "/path")
    public AjaxResult pathList(@RequestParam("coordinateType") String coordinateType,
                               @RequestParam("beginReadTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginReadTime,
                               @RequestParam("endReadTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endReadTime,
                               @RequestParam("userId") int userId) {
        return success(dataService.pathList(coordinateType, beginReadTime, endReadTime, userId));
    }
}

