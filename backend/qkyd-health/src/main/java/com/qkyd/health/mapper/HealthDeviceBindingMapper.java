package com.qkyd.health.mapper;

import java.util.List;
import com.qkyd.health.domain.HealthDeviceBinding;

/**
 * 设备绑定关系Mapper接口
 *
 * @author qkyd
 * @date 2026-02-02
 */
public interface HealthDeviceBindingMapper
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
     * @return 绑定数量
     */
    public int checkDeviceBinding(Long deviceId);

    /**
     * 新增设备绑定关系
     *
     * @param healthDeviceBinding 设备绑定关系
     * @return 结果
     */
    public int insertHealthDeviceBinding(HealthDeviceBinding healthDeviceBinding);

    /**
     * 修改设备绑定关系
     *
     * @param healthDeviceBinding 设备绑定关系
     * @return 结果
     */
    public int updateHealthDeviceBinding(HealthDeviceBinding healthDeviceBinding);

    /**
     * 删除设备绑定关系
     *
     * @param id 设备绑定关系主键
     * @return 结果
     */
    public int deleteHealthDeviceBindingById(Long id);

    /**
     * 批量删除设备绑定关系
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteHealthDeviceBindingByIds(Long[] ids);

    /**
     * 解绑设备
     *
     * @param id 绑定记录ID
     * @return 结果
     */
    public int unbindDevice(Long id);
}
