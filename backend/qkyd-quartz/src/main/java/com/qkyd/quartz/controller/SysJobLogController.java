package com.qkyd.quartz.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.qkyd.common.annotation.Log;
import com.qkyd.common.core.controller.BaseController;
import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.common.core.page.TableDataInfo;
import com.qkyd.common.enums.BusinessType;
import com.qkyd.common.utils.poi.ExcelUtil;
import com.qkyd.quartz.domain.SysJobLog;
import com.qkyd.quartz.service.ISysJobLogService;

/**
 * 璋冨害鏃ュ織鎿嶄綔澶勭悊
 * 
 * @author qkyd
 */
@RestController
@RequestMapping("/monitor/jobLog")
public class SysJobLogController extends BaseController
{
    @Autowired
    private ISysJobLogService jobLogService;

    /**
     * 鏌ヨ瀹氭椂浠诲姟璋冨害鏃ュ織鍒楄〃
     */
    @PreAuthorize("@ss.hasPermi('monitor:job:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysJobLog sysJobLog)
    {
        startPage();
        List<SysJobLog> list = jobLogService.selectJobLogList(sysJobLog);
        return getDataTable(list);
    }

    /**
     * 瀵煎嚭瀹氭椂浠诲姟璋冨害鏃ュ織鍒楄〃
     */
    @PreAuthorize("@ss.hasPermi('monitor:job:export')")
    @Log(title = "浠诲姟璋冨害鏃ュ織", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysJobLog sysJobLog)
    {
        List<SysJobLog> list = jobLogService.selectJobLogList(sysJobLog);
        ExcelUtil<SysJobLog> util = new ExcelUtil<SysJobLog>(SysJobLog.class);
        util.exportExcel(response, list, "璋冨害鏃ュ織");
    }
    
    /**
     * 鏍规嵁璋冨害缂栧彿鑾峰彇璇︾粏淇℃伅
     */
    @PreAuthorize("@ss.hasPermi('monitor:job:query')")
    @GetMapping(value = "/{jobLogId}")
    public AjaxResult getInfo(@PathVariable Long jobLogId)
    {
        return success(jobLogService.selectJobLogById(jobLogId));
    }


    /**
     * 鍒犻櫎瀹氭椂浠诲姟璋冨害鏃ュ織
     */
    @PreAuthorize("@ss.hasPermi('monitor:job:remove')")
    @Log(title = "瀹氭椂浠诲姟璋冨害鏃ュ織", businessType = BusinessType.DELETE)
    @DeleteMapping("/{jobLogIds}")
    public AjaxResult remove(@PathVariable Long[] jobLogIds)
    {
        return toAjax(jobLogService.deleteJobLogByIds(jobLogIds));
    }

    /**
     * 娓呯┖瀹氭椂浠诲姟璋冨害鏃ュ織
     */
    @PreAuthorize("@ss.hasPermi('monitor:job:remove')")
    @Log(title = "璋冨害鏃ュ織", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public AjaxResult clean()
    {
        jobLogService.cleanJobLog();
        return success();
    }
}


