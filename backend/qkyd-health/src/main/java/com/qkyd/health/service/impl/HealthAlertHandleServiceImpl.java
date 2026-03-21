package com.qkyd.health.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.qkyd.health.mapper.HealthAlertHandleMapper;
import com.qkyd.health.domain.HealthAlertHandle;
import com.qkyd.health.service.IHealthAlertHandleService;
import com.qkyd.common.utils.SecurityUtils;

/**
 * 告警处理记录Service业务层处理
 *
 * @author qkyd
 * @date 2026-02-02
 */
@Service
public class HealthAlertHandleServiceImpl implements IHealthAlertHandleService
{
    @Autowired
    private HealthAlertHandleMapper healthAlertHandleMapper;

    /**
     * 查询告警处理记录
     *
     * @param id 告警处理记录主键
     * @return 告警处理记录
     */
    @Override
    public HealthAlertHandle selectHealthAlertHandleById(Long id)
    {
        return healthAlertHandleMapper.selectHealthAlertHandleById(id);
    }

    /**
     * 查询告警处理记录列表
     *
     * @param healthAlertHandle 告警处理记录
     * @return 告警处理记录
     */
    @Override
    public List<HealthAlertHandle> selectHealthAlertHandleList(HealthAlertHandle healthAlertHandle)
    {
        return healthAlertHandleMapper.selectHealthAlertHandleList(healthAlertHandle);
    }

    /**
     * 根据告警ID查询处理记录
     *
     * @param alertId 告警ID
     * @return 告警处理记录集合
     */
    @Override
    public List<HealthAlertHandle> selectByAlertId(Long alertId)
    {
        return healthAlertHandleMapper.selectByAlertId(alertId);
    }

    /**
     * 新增告警处理记录
     *
     * @param healthAlertHandle 告警处理记录
     * @return 结果
     */
    @Override
    public int insertHealthAlertHandle(HealthAlertHandle healthAlertHandle)
    {
        healthAlertHandle.setCreateTime(new Date());
        healthAlertHandle.setHandleTime(new Date());
        healthAlertHandle.setStatus("1");
        return healthAlertHandleMapper.insertHealthAlertHandle(healthAlertHandle);
    }

    /**
     * 修改告警处理记录
     *
     * @param healthAlertHandle 告警处理记录
     * @return 结果
     */
    @Override
    public int updateHealthAlertHandle(HealthAlertHandle healthAlertHandle)
    {
        healthAlertHandle.setUpdateTime(new Date());
        healthAlertHandle.setUpdateBy(SecurityUtils.getUsername());
        return healthAlertHandleMapper.updateHealthAlertHandle(healthAlertHandle);
    }

    /**
     * 批量删除告警处理记录
     *
     * @param ids 需要删除的告警处理记录主键
     * @return 结果
     */
    @Override
    public int deleteHealthAlertHandleByIds(Long[] ids)
    {
        return healthAlertHandleMapper.deleteHealthAlertHandleByIds(ids);
    }

    /**
     * 删除告警处理记录信息
     *
     * @param id 告警处理记录主键
     * @return 结果
     */
    @Override
    public int deleteHealthAlertHandleById(Long id)
    {
        return healthAlertHandleMapper.deleteHealthAlertHandleById(id);
    }
}
