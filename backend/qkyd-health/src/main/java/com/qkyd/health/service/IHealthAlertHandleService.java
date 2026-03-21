package com.qkyd.health.service;

import java.util.List;
import com.qkyd.health.domain.HealthAlertHandle;

/**
 * 告警处理记录Service接口
 *
 * @author qkyd
 * @date 2026-02-02
 */
public interface IHealthAlertHandleService
{
    /**
     * 查询告警处理记录
     *
     * @param id 告警处理记录主键
     * @return 告警处理记录
     */
    public HealthAlertHandle selectHealthAlertHandleById(Long id);

    /**
     * 查询告警处理记录列表
     *
     * @param healthAlertHandle 告警处理记录
     * @return 告警处理记录集合
     */
    public List<HealthAlertHandle> selectHealthAlertHandleList(HealthAlertHandle healthAlertHandle);

    /**
     * 根据告警ID查询处理记录
     *
     * @param alertId 告警ID
     * @return 告警处理记录集合
     */
    public List<HealthAlertHandle> selectByAlertId(Long alertId);

    /**
     * 新增告警处理记录
     *
     * @param healthAlertHandle 告警处理记录
     * @return 结果
     */
    public int insertHealthAlertHandle(HealthAlertHandle healthAlertHandle);

    /**
     * 修改告警处理记录
     *
     * @param healthAlertHandle 告警处理记录
     * @return 结果
     */
    public int updateHealthAlertHandle(HealthAlertHandle healthAlertHandle);

    /**
     * 批量删除告警处理记录
     *
     * @param ids 需要删除的告警处理记录主键集合
     * @return 结果
     */
    public int deleteHealthAlertHandleByIds(Long[] ids);

    /**
     * 删除告警处理记录信息
     *
     * @param id 告警处理记录主键
     * @return 结果
     */
    public int deleteHealthAlertHandleById(Long id);
}
