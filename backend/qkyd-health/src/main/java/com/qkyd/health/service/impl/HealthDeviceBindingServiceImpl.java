package com.qkyd.health.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.qkyd.common.utils.SecurityUtils;
import com.qkyd.health.mapper.HealthDeviceBindingMapper;
import com.qkyd.health.domain.HealthDeviceBinding;
import com.qkyd.health.service.IHealthDeviceBindingService;

/**
 * 设备绑定关系Service业务层处理
 *
 * @author qkyd
 * @date 2026-02-02
 */
@Service
public class HealthDeviceBindingServiceImpl implements IHealthDeviceBindingService
{
    @Autowired
    private HealthDeviceBindingMapper healthDeviceBindingMapper;

    /**
     * 查询设备绑定关系
     *
     * @param id 设备绑定关系主键
     * @return 设备绑定关系
     */
    @Override
    public HealthDeviceBinding selectHealthDeviceBindingById(Long id)
    {
        return healthDeviceBindingMapper.selectHealthDeviceBindingById(id);
    }

    /**
     * 查询设备绑定关系列表
     *
     * @param healthDeviceBinding 设备绑定关系
     * @return 设备绑定关系
     */
    @Override
    public List<HealthDeviceBinding> selectHealthDeviceBindingList(HealthDeviceBinding healthDeviceBinding)
    {
        return healthDeviceBindingMapper.selectHealthDeviceBindingList(healthDeviceBinding);
    }

    /**
     * 查询用户的绑定设备列表
     *
     * @param subjectId 服务对象ID
     * @return 设备绑定关系集合
     */
    @Override
    public List<HealthDeviceBinding> selectBySubjectId(Long subjectId)
    {
        return healthDeviceBindingMapper.selectBySubjectId(subjectId);
    }

    /**
     * 查询设备绑定的用户列表
     *
     * @param deviceId 设备ID
     * @return 设备绑定关系集合
     */
    @Override
    public List<HealthDeviceBinding> selectByDeviceId(Long deviceId)
    {
        return healthDeviceBindingMapper.selectByDeviceId(deviceId);
    }

    /**
     * 检查设备是否已被绑定
     *
     * @param deviceId 设备ID
     * @return 是否已绑定
     */
    @Override
    public boolean isDeviceBound(Long deviceId)
    {
        int count = healthDeviceBindingMapper.checkDeviceBinding(deviceId);
        return count > 0;
    }

    /**
     * 绑定设备
     *
     * @param subjectId 服务对象ID
     * @param deviceId 设备ID
     * @return 结果
     */
    @Override
    public int bindDevice(Long subjectId, Long deviceId)
    {
        if (isDeviceBound(deviceId)) {
            throw new RuntimeException("设备已被绑定");
        }

        HealthDeviceBinding binding = new HealthDeviceBinding();
        binding.setSubjectId(subjectId);
        binding.setDeviceId(deviceId);
        binding.setBindTime(new Date());
        binding.setStatus("1");
        binding.setBindBy(SecurityUtils.getUsername());
        
        return healthDeviceBindingMapper.insertHealthDeviceBinding(binding);
    }

    /**
     * 解绑设备
     *
     * @param id 绑定记录ID
     * @return 结果
     */
    @Override
    public int unbindDevice(Long id)
    {
        return healthDeviceBindingMapper.unbindDevice(id);
    }

    /**
     * 批量删除设备绑定关系
     *
     * @param ids 需要删除的设备绑定关系主键
     * @return 结果
     */
    @Override
    public int deleteHealthDeviceBindingByIds(Long[] ids)
    {
        return healthDeviceBindingMapper.deleteHealthDeviceBindingByIds(ids);
    }

    /**
     * 删除设备绑定关系信息
     *
     * @param id 设备绑定关系主键
     * @return 结果
     */
    @Override
    public int deleteHealthDeviceBindingById(Long id)
    {
        return healthDeviceBindingMapper.deleteHealthDeviceBindingById(id);
    }
}
