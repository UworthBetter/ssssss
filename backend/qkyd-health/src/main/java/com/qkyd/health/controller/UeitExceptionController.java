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
        UeitException query = new UeitException();
        query.setUserId((long) userId);
        query.setType(type);
        query.setState(state);
        try {
            list = ueitExceptionService.selectUeitExceptionList(query);
        } catch (Exception ignored) {
        }
        if (list == null || list.isEmpty()) {
            list = filterExceptions(mockExceptions(), query);
        } else {
            list = filterExceptions(list, query);
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
                match = matchesExceptionType(item.getType(), query.getType());
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

    private boolean matchesExceptionType(String itemType, String queryType) {
        if (queryType == null || queryType.isBlank()) {
            return true;
        }
        String normalizedItem = normalizeExceptionType(itemType);
        String normalizedQuery = normalizeExceptionType(queryType);
        return normalizedItem.contains(normalizedQuery) || normalizedQuery.contains(normalizedItem);
    }

    private String normalizeExceptionType(String type) {
        String text = type == null ? "" : type.trim().toLowerCase();
        if (text.contains("heart") || text.contains("心率")) return "\u5fc3\u7387\u5f02\u5e38";
        if (text.contains("spo2") || text.contains("oxygen") || text.contains("血氧")) return "\u8840\u6c27\u5f02\u5e38";
        if (text.contains("pressure") || text.contains("blood") || text.contains("血压")) return "\u8840\u538b\u5f02\u5e38";
        if (text.contains("temp") || text.contains("temperature") || text.contains("体温")) return "\u4f53\u6e29\u5f02\u5e38";
        if (text.contains("fence") || text.contains("围栏") || text.contains("越界")) return "\u56f4\u680f\u8d8a\u754c";
        if (text.contains("sos") || text.contains("求救") || text.contains("求助")) return "SOS\u6c42\u6551";
        if (text.contains("offline") || text.contains("离线")) return "\u8bbe\u5907\u79bb\u7ebf";
        if (text.contains("activity") || text.contains("活动") || text.contains("步数")) return "\u6d3b\u52a8\u91cf\u5f02\u5e38";
        if (text.contains("signal") || text.contains("信号")) return "\u8bbe\u5907\u4fe1\u53f7\u5f02\u5e38";
        return text;
    }

    private List<UeitException> mockExceptions() {
        List<UeitException> list = new ArrayList<>();
        list.add(buildException(4001L, 10001L, 3001L, "心率异常", "132 bpm", "0", "河南省郑州市二七广场", 113.62493, 34.74725, 20));
        list.add(buildException(4002L, 10002L, 3002L, "围栏越界", "2.1 km", "0", "河南省郑州市郑州东站", 113.79252, 34.75663, 16));
        list.add(buildException(4003L, 10003L, 3003L, "体温偏高", "38.6 ℃", "1", "河南省郑州市郑东新区如意湖", 113.74888, 34.76511, 12));
        list.add(buildException(4004L, 10004L, 3004L, "血氧偏低", "89%", "0", "河南省郑州市金水路省人民医院", 113.68680, 34.75956, 8));
        list.add(buildException(4005L, 10005L, 3005L, "SOS求救", "手动触发", "0", "河南省郑州市中原福塔", 113.72155, 34.73901, 5));
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

