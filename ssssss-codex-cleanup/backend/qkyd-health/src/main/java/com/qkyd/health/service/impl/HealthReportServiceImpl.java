package com.qkyd.health.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.qkyd.health.mapper.HealthReportMapper;
import com.qkyd.health.domain.HealthReport;
import com.qkyd.health.service.IHealthReportService;
import com.qkyd.health.mapper.UeitExceptionMapper;
import com.qkyd.health.domain.UeitException;
import com.qkyd.health.mapper.UeitHeartRateMapper;
import com.qkyd.health.domain.UeitHeartRate;
import com.qkyd.health.mapper.UeitBloodMapper;
import com.qkyd.health.domain.UeitBlood;
import com.qkyd.health.mapper.UeitStepsMapper;
import com.qkyd.health.domain.UeitSteps;
import com.qkyd.common.utils.SecurityUtils;
import com.qkyd.common.utils.StringUtils;

/**
 * 健康分析报告Service业务层处理
 *
 * @author qkyd
 * @date 2026-02-02
 */
@Service
public class HealthReportServiceImpl implements IHealthReportService
{
    @Autowired
    private HealthReportMapper healthReportMapper;

    @Autowired
    private UeitExceptionMapper ueitExceptionMapper;

    @Autowired
    private UeitHeartRateMapper ueitHeartRateMapper;

    @Autowired
    private UeitBloodMapper ueitBloodMapper;

    @Autowired
    private UeitStepsMapper ueitStepsMapper;

    @Value("${file.upload.path:/tmp/qkyd}")
    private String uploadPath;

    @Value("${server.port:8098}")
    private String serverPort;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 查询健康分析报告
     *
     * @param id 健庿分析报告主键
     * @return 健庿分析报告
     */
    @Override
    public HealthReport selectHealthReportById(Long id)
    {
        return healthReportMapper.selectHealthReportById(id);
    }

    /**
     * 查询健庿分析报告列表
     *
     * @param healthReport 健庿分析报告
     * @return 健庿分析报告
     */
    @Override
    public List<HealthReport> selectHealthReportList(HealthReport healthReport)
    {
        return healthReportMapper.selectHealthReportList(healthReport);
    }

    /**
     * 新增健庿分析报告
     *
     * @param healthReport 健庿分析报告
     * @return 结果
     */
    @Override
    public int insertHealthReport(HealthReport healthReport)
    {
        healthReport.setCreateTime(new Date());
        healthReport.setCreatedBy(SecurityUtils.getUserId());
        return healthReportMapper.insertHealthReport(healthReport);
    }

    /**
     * 修改健庿分析报告
     *
     * @param healthReport 健庿分析报告
     * @return 结果
     */
    @Override
    public int updateHealthReport(HealthReport healthReport)
    {
        healthReport.setUpdateTime(new Date());
        healthReport.setUpdateBy(SecurityUtils.getUsername());
        return healthReportMapper.updateHealthReport(healthReport);
    }

    /**
     * 批量删除健庿分析报告
     *
     * @param ids 需要删除的健庿分析报告主键
     * @return 结果
     */
    @Override
    public int deleteHealthReportByIds(Long[] ids)
    {
        return healthReportMapper.deleteHealthReportByIds(ids);
    }

    /**
     * 删除健庿分析报告信息
     *
     * @param id 健庿分析报告主键
     * @return 结果
     */
    @Override
    public int deleteHealthReportById(Long id)
    {
        return healthReportMapper.deleteHealthReportById(id);
    }

    /**
     * 生成健康报告
     *
     * @param userId 用户ID
     * @param reportType 报告类型(daily/weekly/monthly)
     * @param reportDate 报告日期
     * @return 报告
     */
    @Override
    public HealthReport generateReport(Long userId, String reportType, Date reportDate)
    {
        HealthReport report = new HealthReport();
        report.setUserId(userId);
        report.setReportType(reportType);
        report.setReportDate(reportDate);

        String startDate = DATE_FORMAT.format(reportDate);
        String endDate = startDate;
        
        if ("weekly".equals(reportType)) {
            Date endDateDate = new Date(reportDate.getTime() + 6 * 24 * 60 * 60 * 1000);
            endDate = DATE_FORMAT.format(endDateDate);
            report.setReportTitle(startDate + " 至 " + endDate + " 周报");
        } else if ("monthly".equals(reportType)) {
            report.setReportTitle(DATE_FORMAT.format(reportDate).substring(0, 7) + " 月报");
        } else {
            report.setReportTitle(startDate + " 日报");
        }

        UeitException exceptionQuery = new UeitException();
        exceptionQuery.setUserId(userId);
        Date startDateTime = new Date(reportDate.getTime());
        Date endDateTime = new Date(reportDate.getTime() + 24 * 60 * 60 * 1000 - 1);
        if ("weekly".equals(reportType)) {
            endDateTime = new Date(reportDate.getTime() + 7 * 24 * 60 * 60 * 1000 - 1);
        } else if ("monthly".equals(reportType)) {
            int year = Integer.parseInt(startDate.substring(0, 4));
            int month = Integer.parseInt(startDate.substring(5, 7));
            int daysInMonth = getDaysInMonth(year, month);
            endDateTime = new Date(reportDate.getTime() + (daysInMonth - 1) * 24 * 60 * 60 * 1000);
        }
        exceptionQuery.setStartCreateTime(startDateTime);
        exceptionQuery.setEndCreateTime(endDateTime);
        
        List<UeitException> exceptions = ueitExceptionMapper.selectUeitExceptionList(exceptionQuery);
        report.setAbnormalCount(exceptions != null ? exceptions.size() : 0);

        int riskScore = 0;
        if (report.getAbnormalCount() > 5) {
            report.setRiskLevel("高");
            riskScore = 85;
        } else if (report.getAbnormalCount() > 2) {
            report.setRiskLevel("中");
            riskScore = 55;
        } else if (report.getAbnormalCount() > 0) {
            report.setRiskLevel("低");
            riskScore = 35;
        } else {
            report.setRiskLevel("正常");
            riskScore = 15;
        }

        HealthReport stats = healthReportMapper.statisticsByUserAndDate(userId, startDate, endDate);
        if (stats != null) {
            report.setAvgHeartRate(stats.getAvgHeartRate());
            report.setAvgBloodPressure(stats.getAvgBloodPressure());
            report.setTotalSteps(stats.getTotalSteps());
        }

        StringBuilder summary = new StringBuilder();
        summary.append("本").append("daily".equals(reportType) ? "日" : "weekly".equals(reportType) ? "周" : "月")
                .append("共检测到").append(report.getAbnormalCount()).append("次异常，");
        summary.append("平均心率").append(report.getAvgHeartRate() != null ? report.getAvgHeartRate() : 0).append("次/分，");
        summary.append("平均血压").append(StringUtils.isNotEmpty(report.getAvgBloodPressure()) ? report.getAvgBloodPressure() : "--").append("mmHg，");
        summary.append("总步数").append(report.getTotalSteps() != null ? report.getTotalSteps() : 0).append("步。");
        summary.append("综合风险等级为").append(report.getRiskLevel()).append("。");
        report.setSummary(summary.toString());

        return report;
    }

    /**
     * 生成PDF报告
     *
     * @param report 报告对象
     * @return PDF文件路径
     */
    @Override
    public String generatePdf(HealthReport report)
    {
        try {
            String fileName = "健康报告_" + report.getUserId() + "_" + 
                            DATE_FORMAT.format(report.getReportDate()) + "_" +
                            UUID.randomUUID().toString().substring(0, 8) + ".pdf";
            
            String dirPath = uploadPath + File.separator + "reports";
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            String filePath = dirPath + File.separator + fileName;
            
            StringBuilder content = new StringBuilder();
            content.append("耆康云盾健康管理平台\n");
            content.append("====================================\n\n");
            content.append("报告标题: ").append(report.getReportTitle()).append("\n\n");
            content.append("生成时间: ").append(DATETIME_FORMAT.format(new Date())).append("\n\n");
            content.append("====================================\n\n");
            content.append("健康摘要:\n");
            content.append(report.getSummary()).append("\n\n");
            content.append("====================================\n\n");
            content.append("统计指标:\n");
            content.append("- 风险等级: ").append(report.getRiskLevel()).append("\n");
            content.append("- 异常次数: ").append(report.getAbnormalCount()).append("\n");
            content.append("- 平均心率: ").append(report.getAvgHeartRate() != null ? report.getAvgHeartRate() : 0).append(" 次/分\n");
            content.append("- 平均血压: ").append(StringUtils.isNotEmpty(report.getAvgBloodPressure()) ? report.getAvgBloodPressure() : "--").append(" mmHg\n");
            content.append("- 总步数: ").append(report.getTotalSteps() != null ? report.getTotalSteps() : 0).append(" 步\n");
            content.append("\n====================================\n");
            content.append("本报告由耆康云盾系统自动生成\n");
            
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(content.toString().getBytes("UTF-8"));
            fos.close();

            String fileUrl = "http://localhost:" + serverPort + "/profile/reports/" + fileName;
            report.setFileUrl(fileUrl);
            healthReportMapper.updateHealthReport(report);

            return fileUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private int getDaysInMonth(int year, int month) {
        switch (month) {
            case 1: case 3: case 5: case 7: case 8: case 10: case 12:
                return 31;
            case 4: case 6: case 9: case 11:
                return 30;
            case 2:
                return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0) ? 29 : 28;
            default:
                return 30;
        }
    }
}
