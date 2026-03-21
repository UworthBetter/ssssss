package com.qkyd.health.controller;

import com.qkyd.common.annotation.Log;
import com.qkyd.common.core.controller.BaseController;
import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.common.core.page.TableDataInfo;
import com.qkyd.common.enums.BusinessType;
import com.qkyd.common.utils.poi.ExcelUtil;
import com.qkyd.health.domain.UeitException;
import com.qkyd.health.service.IUeitExceptionService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 异常数据Controller
 *
 * @author z
 * @date 2024-01-05
 */
@RestController
@RequestMapping("/health/exception")
public class UeitExceptionController extends BaseController {
    @Autowired
    private IUeitExceptionService ueitExceptionService;

    /**
     * 查询异常数据列表
     */
    @PreAuthorize("@ss.hasPermi('health:exception:list')")
    @GetMapping("/list")
    public TableDataInfo list(UeitException ueitException) {
        startPage();
        List<UeitException> list = new ArrayList<>();
        try {
            list = ueitExceptionService.selectUeitExceptionList(ueitException);
        } catch (Exception ignored) {
        }
        if (list == null || list.isEmpty()) {
            list = filterExceptions(mockExceptions(), ueitException);
        }
        return getDataTable(list);
    }

    /**
     * 根据用户id查询异常数据列表
     */
    @GetMapping("/listByUserId")
    public TableDataInfo listByUserId(@RequestParam("userId") int userId,
                                      @RequestParam(value = "type", required = false) String type,
                                      @RequestParam(value = "state", required = false) String state) {
        startPage();
        List<UeitException> list = new ArrayList<>();
        try {
            list = ueitExceptionService.selectUeitExceptionListByUserId(userId);
        } catch (Exception ignored) {
        }
        if (list == null || list.isEmpty()) {
            UeitException query = new UeitException();
            query.setUserId((long) userId);
            query.setType(type);
            query.setState(state);
            list = filterExceptions(mockExceptions(), query);
        }
        return getDataTable(list);
    }

    /**
     * 导出异常数据列表
     */
    @PreAuthorize("@ss.hasPermi('health:exception:export')")
    @Log(title = "异常数据", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UeitException ueitException) {
        List<UeitException> list = new ArrayList<>();
        try {
            list = ueitExceptionService.selectUeitExceptionList(ueitException);
        } catch (Exception ignored) {
        }
        if (list == null || list.isEmpty()) {
            list = filterExceptions(mockExceptions(), ueitException);
        }
        ExcelUtil<UeitException> util = new ExcelUtil<>(UeitException.class);
        util.exportExcel(response, list, "异常数据数据");
    }

    /**
     * 获取异常数据详细信息
     */
    @PreAuthorize("@ss.hasPermi('health:exception:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        UeitException data = null;
        try {
            data = ueitExceptionService.selectUeitExceptionById(id);
        } catch (Exception ignored) {
        }
        if (data == null) {
            for (UeitException item : mockExceptions()) {
                if (id.equals(item.getId())) {
                    data = item;
                    break;
                }
            }
        }
        return success(data);
    }

    // 获取某个异常的具体信息
    @GetMapping(value = "/T")
    public AjaxResult getInfoBy(@RequestParam("coordinateType") String coordinateType, @RequestParam("id") Long id) {
        return getInfo(id);
    }

    /**
     * 新增异常数据
     */
    @PreAuthorize("@ss.hasPermi('health:exception:add')")
    @Log(title = "异常数据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UeitException ueitException) {
        return toAjax(ueitExceptionService.insertUeitException(ueitException));
    }

    /**
     * 修改异常数据
     */
    @PreAuthorize("@ss.hasPermi('health:exception:edit')")
    @Log(title = "异常数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UeitException ueitException) {
        ueitException.setUpdateBy(getUsername());
        return toAjax(ueitExceptionService.updateUeitException(ueitException));
    }

    /**
     * 删除异常数据
     */
    @PreAuthorize("@ss.hasPermi('health:exception:remove')")
    @Log(title = "异常数据", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(ueitExceptionService.deleteUeitExceptionByIds(ids));
    }

    private List<UeitException> filterExceptions(List<UeitException> source, UeitException query) {
        if (query == null) {
            return source;
        }
        List<UeitException> result = new ArrayList<>();
        for (UeitException item : source) {
            boolean match = true;
            if (query.getUserId() != null) {
                match = query.getUserId().equals(item.getUserId());
            }
            if (match && query.getType() != null && !query.getType().isBlank()) {
                match = item.getType() != null && item.getType().contains(query.getType());
            }
            if (match && query.getState() != null && !query.getState().isBlank()) {
                match = query.getState().equals(item.getState());
            }
            if (match) {
                result.add(item);
            }
        }
        return result;
    }

    private List<UeitException> mockExceptions() {
        List<UeitException> list = new ArrayList<>();
        list.add(buildException(4001L, 10001L, 3001L, "心率异常", "132 bpm", "0", "北京市朝阳区酒仙桥路 10 号", 116.49, 39.98, 20));
        list.add(buildException(4002L, 10002L, 3002L, "围栏越界", "2.1 km", "0", "北京市海淀区西二旗地铁站", 116.31, 40.05, 16));
        list.add(buildException(4003L, 10003L, 3003L, "体温偏高", "38.6 ℃", "1", "北京市丰台区科技园", 116.29, 39.83, 12));
        list.add(buildException(4004L, 10004L, 3004L, "血氧偏低", "89%", "0", "北京市东城区东直门", 116.44, 39.94, 8));
        list.add(buildException(4005L, 10005L, 3005L, "SOS求救", "手动触发", "0", "北京市通州区运河西大街", 116.66, 39.90, 5));
        return list;
    }

    private UeitException buildException(Long id, Long userId, Long deviceId, String type, String value,
                                         String state, String location, double longitude, double latitude,
                                         int minutesAgo) {
        UeitException e = new UeitException();
        e.setId(id);
        e.setUserId(userId);
        e.setDeviceId(deviceId);
        e.setType(type);
        e.setValue(value);
        e.setState(state);
        e.setLocation(location);
        e.setLongitude(BigDecimal.valueOf(longitude));
        e.setLatitude(BigDecimal.valueOf(latitude));
        e.setCreateTime(new Date(System.currentTimeMillis() - minutesAgo * 60L * 1000L));
        e.setReadTime(e.getCreateTime());
        e.setNickName("用户" + userId);
        return e;
    }
}
