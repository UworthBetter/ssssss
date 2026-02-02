package com.qkyd.health.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.qkyd.common.annotation.Log;
import com.qkyd.common.core.controller.BaseController;
import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.common.core.page.TableDataInfo;
import com.qkyd.common.enums.BusinessType;
import com.qkyd.common.utils.poi.ExcelUtil;
import com.qkyd.health.domain.HealthSubject;
import com.qkyd.health.service.IHealthSubjectService;

/**
 * 服务对象Controller
 * 
 * @author qkyd
 * @date 2026-02-01
 */
@RestController
@RequestMapping("/health/subject")
public class HealthSubjectController extends BaseController
{
    @Autowired
    private IHealthSubjectService healthSubjectService;

    /**
     * 查询服务对象列表
     */
    @GetMapping("/list")
    public TableDataInfo list(HealthSubject healthSubject)
    {
        startPage();
        List<HealthSubject> list = healthSubjectService.selectHealthSubjectList(healthSubject);
        return getDataTable(list);
    }

    /**
     * 导出服务对象列表
     */
    @Log(title = "服务对象", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(HttpServletResponse response, HealthSubject healthSubject)
    {
        List<HealthSubject> list = healthSubjectService.selectHealthSubjectList(healthSubject);
        ExcelUtil<HealthSubject> util = new ExcelUtil<HealthSubject>(HealthSubject.class);
        util.exportExcel(response, list, "服务对象数据");
    }

    /**
     * 获取服务对象详细信息
     */
    @GetMapping(value = "/{subjectId}")
    public AjaxResult getInfo(@PathVariable("subjectId") Long subjectId)
    {
        return success(healthSubjectService.selectHealthSubjectBySubjectId(subjectId));
    }

    /**
     * 新增服务对象
     */
    @Log(title = "服务对象", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HealthSubject healthSubject)
    {
        return toAjax(healthSubjectService.insertHealthSubject(healthSubject));
    }

    /**
     * 修改服务对象
     */
    @Log(title = "服务对象", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HealthSubject healthSubject)
    {
        return toAjax(healthSubjectService.updateHealthSubject(healthSubject));
    }

    /**
     * 删除服务对象
     */
    @Log(title = "服务对象", businessType = BusinessType.DELETE)
    @DeleteMapping("/{subjectIds}")
    public AjaxResult remove(@PathVariable Long[] subjectIds)
    {
        return toAjax(healthSubjectService.deleteHealthSubjectBySubjectIds(subjectIds));
    }
}
