package com.ueit.health.service.impl;

import java.util.List;
import com.ueit.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ueit.health.mapper.UeitDeviceInfoExtendMapper;
import com.ueit.health.domain.UeitDeviceInfoExtend;
import com.ueit.health.service.IUeitDeviceInfoExtendService;

/**
 * 设备信息扩展Service业务层处理
 * 
 * @author douwq
 * @date 2024-01-03
 */
@Service
public class UeitDeviceInfoExtendServiceImpl implements IUeitDeviceInfoExtendService 
{
    @Autowired
    private UeitDeviceInfoExtendMapper ueitDeviceInfoExtendMapper;

    /**
     * 查询设备信息扩展
     * 
     * @param deviceId 设备信息扩展主键
     * @return 设备信息扩展
     */
    @Override
    public UeitDeviceInfoExtend selectUeitDeviceInfoExtendByDeviceId(Long deviceId)
    {
        return ueitDeviceInfoExtendMapper.selectUeitDeviceInfoExtendByDeviceId(deviceId);
    }

    /**
     * 查询设备信息扩展列表
     * 
     * @param ueitDeviceInfoExtend 设备信息扩展
     * @return 设备信息扩展
     */
    @Override
    public List<UeitDeviceInfoExtend> selectUeitDeviceInfoExtendList(UeitDeviceInfoExtend ueitDeviceInfoExtend)
    {
        return ueitDeviceInfoExtendMapper.selectUeitDeviceInfoExtendList(ueitDeviceInfoExtend);
    }

    /**
     * 新增设备信息扩展
     * 
     * @param ueitDeviceInfoExtend 设备信息扩展
     * @return 结果
     */
    @Override
    public int insertUeitDeviceInfoExtend(UeitDeviceInfoExtend ueitDeviceInfoExtend)
    {
        return ueitDeviceInfoExtendMapper.insertUeitDeviceInfoExtend(ueitDeviceInfoExtend);
    }

    /**
     * 修改设备信息扩展
     * 
     * @param ueitDeviceInfoExtend 设备信息扩展
     * @return 结果
     */
    @Override
    public int updateUeitDeviceInfoExtend(UeitDeviceInfoExtend ueitDeviceInfoExtend)
    {
        ueitDeviceInfoExtend.setUpdateTime(DateUtils.getNowDate());
        return ueitDeviceInfoExtendMapper.updateUeitDeviceInfoExtend(ueitDeviceInfoExtend);
    }

    /**
     * 批量删除设备信息扩展
     * 
     * @param deviceIds 需要删除的设备信息扩展主键
     * @return 结果
     */
    @Override
    public int deleteUeitDeviceInfoExtendByDeviceIds(Long[] deviceIds)
    {
        return ueitDeviceInfoExtendMapper.deleteUeitDeviceInfoExtendByDeviceIds(deviceIds);
    }

    /**
     * 删除设备信息扩展信息
     * 
     * @param deviceId 设备信息扩展主键
     * @return 结果
     */
    @Override
    public int deleteUeitDeviceInfoExtendByDeviceId(Long deviceId)
    {
        return ueitDeviceInfoExtendMapper.deleteUeitDeviceInfoExtendByDeviceId(deviceId);
    }
}

