package com.qkyd.health.service;

import java.util.Date;
import java.util.List;
import com.qkyd.health.domain.HealthReport;

/**
 * 健康分析报告Service接口
 *
 * @author qkyd
 * @date 2026-02-02
 */
public interface IHealthReportService
{
    /**
     * 查询健康分析报告
     *
     * @param id 健康分析报告主键
     * @return 健康分析报告
     */
    public HealthReport selectHealthReportById(Long id);

    /**
     * 查询健康分析报告列表
     *
     * @param healthReport 健康分析报告
     * @return 健康分析报告集合
     */
    public List<HealthReport> selectHealthReportList(HealthReport healthReport);

    /**
     * 新增健康分析报告
     *
     * @param healthReport 健康分析报告
     * @return 结果
     */
    public int insertHealthReport(HealthReport healthReport);

    /**
     * 修改健康分析报告
     *
     * @param healthReport 健康分析报告
     * @return 结果
     */
    public int updateHealthReport(HealthReport healthReport);

    /**
     * 批量删除健康分析报告
     *
     * @param ids 需要删除的健庿分析报告主键集合
     * @return 结果
     */
    public int deleteHealthReportByIds(Long[] ids);

    /**
     * 删除健庿分析报告信息
     *
     * @param id 健庿分析报告主键
     * @return 结果
     */
    public int deleteHealthReportById(Long id);

    /**
     * 生成健康报告
     *
     * @param userId 用户ID
     * @param reportType 报告类型(daily/weekly/monthly)
     * @param reportDate 报告日期
     * @return 报告
     */
    public HealthReport generateReport(Long userId, String reportType, Date reportDate);

    /**
     * 生成PDF报告
     *
     * @param report 报告对象
     * @return PDF文件路径
     */
    public String generatePdf(HealthReport report);
}
