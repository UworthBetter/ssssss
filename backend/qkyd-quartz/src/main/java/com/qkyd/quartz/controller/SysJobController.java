package com.qkyd.quartz.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.qkyd.common.annotation.Log;
import com.qkyd.common.constant.Constants;
import com.qkyd.common.core.controller.BaseController;
import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.common.core.page.TableDataInfo;
import com.qkyd.common.enums.BusinessType;
import com.qkyd.common.exception.job.TaskException;
import com.qkyd.common.utils.StringUtils;
import com.qkyd.common.utils.poi.ExcelUtil;
import com.qkyd.quartz.domain.SysJob;
import com.qkyd.quartz.service.ISysJobService;
import com.qkyd.quartz.util.CronUtils;
import com.qkyd.quartz.util.ScheduleUtils;

/**
 * 璋冨害浠诲姟淇℃伅鎿嶄綔澶勭悊
 * 
 * @author qkyd
 */
@RestController
@RequestMapping("/monitor/job")
public class SysJobController extends BaseController
{
    @Autowired
    private ISysJobService jobService;

    /**
     * 鏌ヨ瀹氭椂浠诲姟鍒楄〃
     */
    @PreAuthorize("@ss.hasPermi('monitor:job:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysJob sysJob)
    {
        startPage();
        List<SysJob> list = jobService.selectJobList(sysJob);
        return getDataTable(list);
    }

    /**
     * 瀵煎嚭瀹氭椂浠诲姟鍒楄〃
     */
    @PreAuthorize("@ss.hasPermi('monitor:job:export')")
    @Log(title = "瀹氭椂浠诲姟", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysJob sysJob)
    {
        List<SysJob> list = jobService.selectJobList(sysJob);
        ExcelUtil<SysJob> util = new ExcelUtil<SysJob>(SysJob.class);
        util.exportExcel(response, list, "瀹氭椂浠诲姟");
    }

    /**
     * 鑾峰彇瀹氭椂浠诲姟璇︾粏淇℃伅
     */
    @PreAuthorize("@ss.hasPermi('monitor:job:query')")
    @GetMapping(value = "/{jobId}")
    public AjaxResult getInfo(@PathVariable("jobId") Long jobId)
    {
        return success(jobService.selectJobById(jobId));
    }

    /**
     * 鏂板瀹氭椂浠诲姟
     */
    @PreAuthorize("@ss.hasPermi('monitor:job:add')")
    @Log(title = "瀹氭椂浠诲姟", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysJob job) throws SchedulerException, TaskException
    {
        if (!CronUtils.isValid(job.getCronExpression()))
        {
            return error("鏂板浠诲姟'" + job.getJobName() + "'澶辫触锛孋ron琛ㄨ揪寮忎笉姝ｇ‘");
        }
        else if (StringUtils.containsIgnoreCase(job.getInvokeTarget(), Constants.LOOKUP_RMI))
        {
            return error("鏂板浠诲姟'" + job.getJobName() + "'澶辫触锛岀洰鏍囧瓧绗︿覆涓嶅厑璁?rmi'璋冪敤");
        }
        else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), new String[] { Constants.LOOKUP_LDAP, Constants.LOOKUP_LDAPS }))
        {
            return error("鏂板浠诲姟'" + job.getJobName() + "'澶辫触锛岀洰鏍囧瓧绗︿覆涓嶅厑璁?ldap(s)'璋冪敤");
        }
        else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), new String[] { Constants.HTTP, Constants.HTTPS }))
        {
            return error("鏂板浠诲姟'" + job.getJobName() + "'澶辫触锛岀洰鏍囧瓧绗︿覆涓嶅厑璁?http(s)'璋冪敤");
        }
        else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), Constants.JOB_ERROR_STR))
        {
            return error("鏂板浠诲姟'" + job.getJobName() + "'澶辫触锛岀洰鏍囧瓧绗︿覆瀛樺湪杩濊");
        }
        else if (!ScheduleUtils.whiteList(job.getInvokeTarget()))
        {
            return error("鏂板浠诲姟'" + job.getJobName() + "'澶辫触锛岀洰鏍囧瓧绗︿覆涓嶅湪鐧藉悕鍗曞唴");
        }
        job.setCreateBy(getUsername());
        return toAjax(jobService.insertJob(job));
    }

    /**
     * 淇敼瀹氭椂浠诲姟
     */
    @PreAuthorize("@ss.hasPermi('monitor:job:edit')")
    @Log(title = "瀹氭椂浠诲姟", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysJob job) throws SchedulerException, TaskException
    {
        if (!CronUtils.isValid(job.getCronExpression()))
        {
            return error("淇敼浠诲姟'" + job.getJobName() + "'澶辫触锛孋ron琛ㄨ揪寮忎笉姝ｇ‘");
        }
        else if (StringUtils.containsIgnoreCase(job.getInvokeTarget(), Constants.LOOKUP_RMI))
        {
            return error("淇敼浠诲姟'" + job.getJobName() + "'澶辫触锛岀洰鏍囧瓧绗︿覆涓嶅厑璁?rmi'璋冪敤");
        }
        else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), new String[] { Constants.LOOKUP_LDAP, Constants.LOOKUP_LDAPS }))
        {
            return error("淇敼浠诲姟'" + job.getJobName() + "'澶辫触锛岀洰鏍囧瓧绗︿覆涓嶅厑璁?ldap(s)'璋冪敤");
        }
        else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), new String[] { Constants.HTTP, Constants.HTTPS }))
        {
            return error("淇敼浠诲姟'" + job.getJobName() + "'澶辫触锛岀洰鏍囧瓧绗︿覆涓嶅厑璁?http(s)'璋冪敤");
        }
        else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), Constants.JOB_ERROR_STR))
        {
            return error("淇敼浠诲姟'" + job.getJobName() + "'澶辫触锛岀洰鏍囧瓧绗︿覆瀛樺湪杩濊");
        }
        else if (!ScheduleUtils.whiteList(job.getInvokeTarget()))
        {
            return error("淇敼浠诲姟'" + job.getJobName() + "'澶辫触锛岀洰鏍囧瓧绗︿覆涓嶅湪鐧藉悕鍗曞唴");
        }
        job.setUpdateBy(getUsername());
        return toAjax(jobService.updateJob(job));
    }

    /**
     * 瀹氭椂浠诲姟鐘舵€佷慨鏀?
     */
    @PreAuthorize("@ss.hasPermi('monitor:job:changeStatus')")
    @Log(title = "瀹氭椂浠诲姟", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysJob job) throws SchedulerException
    {
        SysJob newJob = jobService.selectJobById(job.getJobId());
        newJob.setStatus(job.getStatus());
        return toAjax(jobService.changeStatus(newJob));
    }

    /**
     * 瀹氭椂浠诲姟绔嬪嵆鎵ц涓€娆?
     */
    @PreAuthorize("@ss.hasPermi('monitor:job:changeStatus')")
    @Log(title = "瀹氭椂浠诲姟", businessType = BusinessType.UPDATE)
    @PutMapping("/run")
    public AjaxResult run(@RequestBody SysJob job) throws SchedulerException
    {
        boolean result = jobService.run(job);
        return result ? success() : error("浠诲姟涓嶅瓨鍦ㄦ垨宸茶繃鏈燂紒");
    }

    /**
     * 鍒犻櫎瀹氭椂浠诲姟
     */
    @PreAuthorize("@ss.hasPermi('monitor:job:remove')")
    @Log(title = "瀹氭椂浠诲姟", businessType = BusinessType.DELETE)
    @DeleteMapping("/{jobIds}")
    public AjaxResult remove(@PathVariable Long[] jobIds) throws SchedulerException, TaskException
    {
        jobService.deleteJobByIds(jobIds);
        return success();
    }
}


