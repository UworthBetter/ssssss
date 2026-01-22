package com.ueit.health.service;

import java.util.Date;
import java.util.List;

import com.ueit.health.domain.UeitHeartRate;
import com.ueit.health.domain.UeitLocation;
import com.ueit.health.domain.dto.RealTimeTracking;

/**
 * 定位数据Service接口
 *
 * @author z
 * @date 2024-01-05
 */
public interface IUeitLocationService
{
    /**
     * 查询定位数据
     *
     * @param id 定位数据主键
     * @return 定位数据
     */
    public UeitLocation selectUeitLocationById(Long id);
    //根据userId查询最新一条位置信息
    public UeitLocation selectUeitLocationByUserId(Long userId);
    /**
     * 查询定位数据列表
     *
     * @param ueitLocation 定位数据
     * @return 定位数据集合
     */
    public List<UeitLocation> selectUeitLocationList(UeitLocation ueitLocation);

    /**
     * 新增定位数据
     *
     * @param ueitLocation 定位数据
     * @return 结果
     */
    public int insertUeitLocation(UeitLocation ueitLocation);

    /**
     * 修改定位数据
     *
     * @param ueitLocation 定位数据
     * @return 结果
     */
    public int updateUeitLocation(UeitLocation ueitLocation);

    /**
     * 批量删除定位数据
     *
     * @param ids 需要删除的定位数据主键集合
     * @return 结果
     */
    public int deleteUeitLocationByIds(Long[] ids);

    /**
     * 删除定位数据信息
     *
     * @param id 定位数据主键
     * @return 结果
     */
    public int deleteUeitLocationById(Long id);
    /**
     * 查询某用户在创建时间beginReadTim, endReadTime之间的数据
     * @return
     */
    public List<UeitLocation> getDataBoard(int userId, Date beginReadTime, Date endReadTime);

    /**
     * 实时跟踪
     * @param coordinateType
     * @param userId
     * @return
     */
    RealTimeTracking realTimeTracking(String coordinateType, int userId);

    /**
     * 历史轨迹
     * @param coordinateType
     * @param beginReadTime
     * @param endReadTime
     * @param userId
     * @return
     */
    List<RealTimeTracking> pathList(String coordinateType, Date beginReadTime, Date endReadTime, int userId);
}

