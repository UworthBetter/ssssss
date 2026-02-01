package com.qkyd.health.service;

import java.util.List;
import com.qkyd.health.domain.UeitDeviceInfoExtend;

/**
 * 设备信息扩展Service接口
 * 
 * @author douwq
 * @date 2024-01-03
 */
public interface IUeitDeviceInfoExtendService 
{
    /**
     * 查询设备信息扩展
     * 
     * @param deviceId 设备信息扩展主键
     * @return 设备信息扩展
     */
    public UeitDeviceInfoExtend selectUeitDeviceInfoExtendByDeviceId(Long deviceId);

    /**
     * 查询设备信息扩展列表
     * 
     * @param ueitDeviceInfoExtend 设备信息扩展
     * @return 设备信息扩展集合
     */
    public List<UeitDeviceInfoExtend> selectUeitDeviceInfoExtendList(UeitDeviceInfoExtend ueitDeviceInfoExtend);

    /**
     * 新增设备信息扩展
     * 
     * @param ueitDeviceInfoExtend 设备信息扩展
     * @return 结果
     */
    public int insertUeitDeviceInfoExtend(UeitDeviceInfoExtend ueitDeviceInfoExtend);

    /**
     * 修改设备信息扩展
     * 
     * @param ueitDeviceInfoExtend 设备信息扩展
     * @return 结果
     */
    public int updateUeitDeviceInfoExtend(UeitDeviceInfoExtend ueitDeviceInfoExtend);

    /**
     * 批量删除设备信息扩展
     * 
     * @param deviceIds 需要删除的设备信息扩展主键集合
     * @return 结果
     */
    public int deleteUeitDeviceInfoExtendByDeviceIds(Long[] deviceIds);

    /**
     * 删除设备信息扩展信息
     * 
     * @param deviceId 设备信息扩展主键
     * @return 结果
     */
    public int deleteUeitDeviceInfoExtendByDeviceId(Long deviceId);
}

