package com.ueit.health.mapper;

import java.util.List;

import com.ueit.common.core.page.TableDataInfo;
import com.ueit.health.domain.UeitDeviceInfoExtend;
import com.ueit.health.domain.dto.RealTimeData;

/**
 * 设备信息扩展Mapper接口
 *
 * @author douwq
 * @date 2024-01-03
 */
public interface UeitDeviceInfoExtendMapper
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
     * 删除设备信息扩展
     *
     * @param deviceId 设备信息扩展主键
     * @return 结果
     */
    public int deleteUeitDeviceInfoExtendByDeviceId(Long deviceId);

    /**
     * 批量删除设备信息扩展
     *
     * @param deviceIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUeitDeviceInfoExtendByDeviceIds(Long[] deviceIds);
    //实时数据
    List<RealTimeData> realTimeData();
    //查询实时数据
    List<UeitDeviceInfoExtend> indexUserLocation();
}
