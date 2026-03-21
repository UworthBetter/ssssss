package com.qkyd.health.controller;

import java.io.File;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.qkyd.common.annotation.Log;
import com.qkyd.common.core.controller.BaseController;
import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.common.enums.BusinessType;
import com.qkyd.health.domain.HealthReport;
import com.qkyd.health.service.IHealthReportService;
import com.qkyd.common.utils.poi.ExcelUtil;
import com.qkyd.common.core.page.TableDataInfo;

/**
 * 健康分析报告Controller
 *
 * @author qkyd
 * @date 2026-02-02
 */
@RestController
@RequestMapping("/health/report")
public class HealthReportController extends BaseController
{
    @Autowired
    private IHealthReportService healthReportService;

    /**
     * 查询健康分析报告列表
     */
    @PreAuthorize("@ss.hasPermi('health:report:list')")
    @GetMapping("/list")
    public TableDataInfo list(HealthReport healthReport)
    {
        startPage();
        List<HealthReport> list = healthReportService.selectHealthReportList(healthReport);
        return getDataTable(list);
    }

    /**
     * 导出健康分析报告列表
     */
    @PreAuthorize("@ss.hasPermi('health:report:export')")
    @Log(title = "健康分析报告", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HealthReport healthReport)
    {
        List<HealthReport> list = healthReportService.selectHealthReportList(healthReport);
        ExcelUtil<HealthReport> util = new ExcelUtil<HealthReport>(HealthReport.class);
        util.exportExcel(response, list, "健康分析报告");
    }

    /**
     * 获取健康分析报告详细信息
     */
    @PreAuthorize("@ss.hasPermi('health:report:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(healthReportService.selectHealthReportById(id));
    }

    /**
     * 生成健康报告
     */
    @PreAuthorize("@ss.hasPermi('health:report:generate')")
    @Log(title = "健康分析报告", businessType = BusinessType.INSERT)
    @PostMapping("/generate")
    public AjaxResult generate(@RequestBody HealthReport report)
    {
        HealthReport generatedReport = healthReportService.generateReport(
            report.getUserId(),
            report.getReportType(),
            report.getReportDate()
        );
        
        int result = healthReportService.insertHealthReport(generatedReport);
        
        if (result > 0) {
            String fileUrl = healthReportService.generatePdf(generatedReport);
            return AjaxResult.success("报告生成成功", generatedReport);
        }
        return AjaxResult.error("报告生成失败");
    }

    /**
     * 下载PDF报告
     */
    @PreAuthorize("@ss.hasPermi('health:report:download')")
    @GetMapping("/download/{id}")
    public ResponseEntity<FileSystemResource> download(@PathVariable("id") Long id) throws Exception
    {
        HealthReport report = healthReportService.selectHealthReportById(id);
        if (report == null || report.getFileUrl() == null) {
            return ResponseEntity.notFound().build();
        }
        
        String fileName = report.getFileUrl().substring(report.getFileUrl().lastIndexOf("/") + 1);
        String filePath = System.getProperty("user.home") + File.separator + "qkyd" + 
                         File.separator + "reports" + File.separator + fileName;
        
        File file = new File(filePath);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + 
                    URLEncoder.encode(fileName, "UTF-8"));
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(new FileSystemResource(file));
    }

    /**
     * 修改健康分析报告
     */
    @PreAuthorize("@ss.hasPermi('health:report:edit')")
    @Log(title = "健康分析报告", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HealthReport healthReport)
    {
        return toAjax(healthReportService.updateHealthReport(healthReport));
    }

    /**
     * 删除健康分析报告
     */
    @PreAuthorize("@ss.hasPermi('health:report:remove')")
    @Log(title = "健康分析报告", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(healthReportService.deleteHealthReportByIds(ids));
    }
}
