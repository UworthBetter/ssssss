package com.qkyd.health.service;

import java.util.List;
import com.qkyd.health.domain.HealthDeviceBinding;

/**
 * 设备绑定关系Service接口
 *
 * @author qkyd
 * @date 2026-02-02
 */
public interface IHealthDeviceBindingService
{
    /**
     * 查询设备绑定关系
     *
     * @param id 设备绑定关系主键
     * @return 设备绑定关系
     */
    public HealthDeviceBinding selectHealthDeviceBindingById(Long id);

    /**
     * 查询设备绑定关系列表
     *
     * @param healthDeviceBinding 设备绑定关系
     * @return 设备绑定关系集合
     */
    public List<HealthDeviceBinding> selectHealthDeviceBindingList(HealthDeviceBinding healthDeviceBinding);

    /**
     * 查询用户的绑定设备列表
     *
     * @param subjectId 服务对象ID
     * @return 设备绑定关系集合
     */
    public List<HealthDeviceBinding> selectBySubjectId(Long subjectId);

    /**
     * 查询设备绑定的用户列表
     *
     * @param deviceId 设备ID
     * @return 设备绑定关系集合
     */
    public List<HealthDeviceBinding> selectByDeviceId(Long deviceId);

    /**
     * 检查设备是否已被绑定
     *
     * @param deviceId 设备ID
     * @return 是否已绑定
     */
    public boolean isDeviceBound(Long deviceId);

    /**
     * 绑定设备
     *
     * @param subjectId 服务对象ID
     * @param deviceId 设备ID
     * @return 结果
     */
    public int bindDevice(Long subjectId, Long deviceId);

    /**
     * 解绑设备
     *
     * @param id 绑定记录ID
     * @return 结果
     */
    public int unbindDevice(Long id);

    /**
     * 批量删除设备绑定关系
     *
     * @param ids 需要删除的设备绑定关系主键
     * @return 结果
     */
    public int deleteHealthDeviceBindingByIds(Long[] ids);

    /**
     * 删除设备绑定关系信息
     *
     * @param id 设备绑定关系主键
     * @return 结果
     */
    public int deleteHealthDeviceBindingById(Long id);
}
