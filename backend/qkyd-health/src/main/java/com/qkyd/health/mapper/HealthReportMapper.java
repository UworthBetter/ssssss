package com.qkyd.health.mapper;

import java.util.List;
import com.qkyd.health.domain.HealthReport;

/**
 * 健康分析报告Mapper接口
 *
 * @author qkyd
 * @date 2026-02-02
 */
public interface HealthReportMapper
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
     * 删除健康分析报告
     *
     * @param id 健康分析报告主键
     * @return 结果
     */
    public int deleteHealthReportById(Long id);

    /**
     * 批量删除健康分析报告
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteHealthReportByIds(Long[] ids);

    /**
     * 统计用户健康数据
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计结果
     */
    public HealthReport statisticsByUserAndDate(Long userId, String startDate, String endDate);
}
